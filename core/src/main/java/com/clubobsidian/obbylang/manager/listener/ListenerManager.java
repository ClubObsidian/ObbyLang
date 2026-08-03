/*
 *     ObbyLang
 *     Copyright (C) 2021 virustotalop
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.clubobsidian.obbylang.manager.listener;

import com.caoccao.qjs4j.core.JSFunction;
import com.caoccao.qjs4j.core.JSValue;
import com.clubobsidian.obbylang.compat.NashornJavaCompat;
import com.clubobsidian.obbylang.manager.RegisteredManager;
import com.clubobsidian.obbylang.manager.script.MappingsManager;
import com.clubobsidian.obbylang.manager.script.ScriptManager;
import com.clubobsidian.obbylang.manager.script.ScriptWrapper;
import com.clubobsidian.obbylang.manager.server.FakeServerManager;
import com.clubobsidian.obbylang.plugin.ObbyLangPlugin;
import com.clubobsidian.obbylang.util.JSUtil;
import com.clubobsidian.obbylang.util.ListenerUtil;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.FieldValue;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public abstract class ListenerManager<T> implements RegisteredManager {

    // T is event priority
    private Map<String, Map<T, ScriptWrapper[]>> scripts;
    private final Map<T, List<String>> registeredEvents = new ConcurrentHashMap<>();
    private final MappingsManager mappingsManager;
    private final ScriptManager scriptManager;
    private final FakeServerManager fakeServer;
    private final ObbyLangPlugin plugin;

    @Inject
    protected ListenerManager(MappingsManager mappingsManager, ScriptManager scriptManager,
                              FakeServerManager fakeServer, ObbyLangPlugin plugin) {
        this.mappingsManager = mappingsManager;
        this.scriptManager = scriptManager;
        this.fakeServer = fakeServer;
        this.plugin = plugin;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Interceptor — called by ByteBuddy-generated listener methods
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Static interceptor class used by ByteBuddy's MethodDelegation.
     *
     * <p>ByteBuddy injects the {@code listenerManager} field value and the event
     * argument at the call site. The interceptor pulls the relevant scripts and
     * dispatches each one via {@link JSUtil#call}, wrapping the event object via
     * {@link NashornJavaCompat#wrapJavaObject} so JS can call methods on it.
     */
    public static class EventInterceptor {

        @RuntimeType
        public static void intercept(
                @FieldValue("listenerManager") ListenerManager<?> listenerManager,
                @FieldValue("eventName") String eventName,
                @FieldValue("eventPriority") Object eventPriority,
                @Argument(0) Object event) {

            @SuppressWarnings("unchecked")
            ScriptWrapper[] scripts = ((ListenerManager<Object>) listenerManager)
                    .getEventScripts(eventName, eventPriority);

            for (ScriptWrapper wrapper : scripts) {
                try {
                    JSFunction script = wrapper.getScript();
                    JSValue wrappedEvent = NashornJavaCompat.wrapJavaObject(wrapper.getOwnerName(), event);
                    JSUtil.call(script, new JSValue[]{wrappedEvent});
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Initialization
    // ─────────────────────────────────────────────────────────────────────────

    protected void loadEvents(String[] events) {
        for (String event : events) {
            event = event.toLowerCase();
            if (this.scripts.get(event) != null) continue;
            Map<T, ScriptWrapper[]> scriptMap = new HashMap<>();
            for (T priority : this.getPriorities()) {
                scriptMap.put(priority, new ScriptWrapper[0]);
            }
            this.scripts.put(event, scriptMap);
        }
    }

    protected Map<String, Map<T, ScriptWrapper[]>> initScripts() {
        Map<String, Map<T, ScriptWrapper[]>> scripts = new HashMap<>();
        for (Entry<String, String> entry : this.mappingsManager.getEventMappings().entrySet()) {
            String className = entry.getKey();
            if (!eventClassExists(className)) {
                this.plugin.getLogger().log(Level.INFO, String.format("The class %s does not exist", className));
                continue;
            }
            Map<T, ScriptWrapper[]> scriptMap = new HashMap<>();
            for (T priority : this.getPriorities()) {
                scriptMap.put(priority, new ScriptWrapper[0]);
            }
            scripts.put(entry.getValue(), scriptMap);
        }
        return scripts;
    }

    private boolean eventClassExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Script lookup
    // ─────────────────────────────────────────────────────────────────────────

    public ScriptWrapper[] getEventScripts(String event, T priority) {
        if (this.scripts == null) {
            this.scripts = this.initScripts();
        }
        return this.scripts.get(event).get(priority);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Listener generation via ByteBuddy
    // ─────────────────────────────────────────────────────────────────────────

    private void createListener(String declaringClass, String event,
                                T priority,
                                String eventPriorityStr) {
        List<String> events = this.registeredEvents.computeIfAbsent(priority, k -> new ArrayList<>());
        if (events.contains(event)) {
            return;
        }
        events.add(event);

        Map<String, String> mappings = this.mappingsManager.getEventMappings();
        for (Entry<String, String> entry : mappings.entrySet()) {
            if (!entry.getValue().equals(event)) continue;

            String eventClassName = entry.getKey();
            Class<?> eventClass;
            try {
                eventClass = Class.forName(eventClassName);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return;
            }

            String generatedName = "com.clubobsidian.obbylang.manager.listener."
                    + eventPriorityStr
                    + event
                    + "ObbyLangListener";

            try {
                // Build the annotation for the handler method
                AnnotationDescription handlerAnnotation = buildHandlerAnnotation(
                        priority, eventPriorityStr);

                // Build the ByteBuddy subclass/implementor
                DynamicType.Builder<?> builder = new ByteBuddy()
                        .subclass(Object.class)
                        .name(generatedName);

                // Implement the listener interface if present
                if (this.getListenerClass() != null) {
                    builder = builder.implement(this.getListenerClass());
                }

                // Fields to carry context into the generated method
                builder = builder
                        .defineField("listenerManager", ListenerManager.class,
                                net.bytebuddy.description.modifier.Visibility.PRIVATE)
                        .defineField("eventName", String.class,
                                net.bytebuddy.description.modifier.Visibility.PRIVATE)
                        .defineField("eventPriority", Object.class,
                                net.bytebuddy.description.modifier.Visibility.PRIVATE)
                        .defineField("declaringClass", String.class,
                                net.bytebuddy.description.modifier.Visibility.PRIVATE);

                // Constructor: (ListenerManager, String eventName, Object priority, String declaringClass)
                builder = builder
                        .defineConstructor(net.bytebuddy.description.modifier.Visibility.PUBLIC)
                        .withParameters(ListenerManager.class, String.class, Object.class, String.class)
                        .intercept(net.bytebuddy.implementation.MethodCall
                                .invoke(Object.class.getDeclaredConstructor())
                                .andThen(net.bytebuddy.implementation.FieldAccessor
                                        .ofField("listenerManager").setsArgumentAt(0))
                                .andThen(net.bytebuddy.implementation.FieldAccessor
                                        .ofField("eventName").setsArgumentAt(1))
                                .andThen(net.bytebuddy.implementation.FieldAccessor
                                        .ofField("eventPriority").setsArgumentAt(2))
                                .andThen(net.bytebuddy.implementation.FieldAccessor
                                        .ofField("declaringClass").setsArgumentAt(3)));

                // The handler method — named after the event, takes the event class as arg
                builder = builder
                        .defineMethod(event, void.class,
                                net.bytebuddy.description.modifier.Visibility.PUBLIC)
                        .withParameters(eventClass)
                        .intercept(MethodDelegation.to(EventInterceptor.class))
                        .annotateMethod(handlerAnnotation);

                Class<?> listenerClass = builder
                        .make()
                        .load(ListenerManager.class.getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                        .getLoaded();

                Object listenerInstance = listenerClass
                        .getDeclaredConstructors()[0]
                        .newInstance(this, event, priority, declaringClass);

                this.fakeServer.registerListener(listenerInstance);
            } catch (NoSuchMethodException | InvocationTargetException
                     | InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Builds the handler annotation (e.g. {@code @EventHandler(priority = EventPriority.HIGH)})
     * using the subclass-provided annotation and priority classes.
     */
    private AnnotationDescription buildHandlerAnnotation(T priority, String eventPriorityStr) {
        AnnotationDescription.Builder annotBuilder =
                AnnotationDescription.Builder.ofType(
                        this.getHandlerClass().asSubclass(java.lang.annotation.Annotation.class));

        if (this.getEventPriorityClass() != null) {
            // Enum priority — look up the constant by name
            Object priorityValue = ListenerUtil.getStaticDeclaredField(
                    this.getEventPriorityClass(), eventPriorityStr.toUpperCase()
            );
            if (priorityValue instanceof Enum<?> enumValue) {
                annotBuilder = annotBuilder.define(this.getPriorityName(), enumValue);
            }
        }
        // If no priority class (String-based priority), no annotation member needed

        return annotBuilder.build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Public register / unregister API
    // ─────────────────────────────────────────────────────────────────────────

    public void register(String declaringClass, JSFunction script, String event) {
        this.register(declaringClass, script, new String[]{ event });
    }

    public void register(String declaringClass, JSFunction script, String[] events) {
        this.register(declaringClass, script, events, this.getDefaultPriority());
    }

    public void register(String declaringClass, JSFunction script, String event,
                         String eventPriorityStr) {
        this.register(declaringClass, script, new String[]{ event }, eventPriorityStr);
    }

    @SuppressWarnings("unchecked")
    public void register(String declaringClass,
                         JSFunction script,
                         String[] events,
                         String eventPriorityStr) {
        if (this.scripts == null) {
            this.scripts = initScripts();
        } else {
            this.loadEvents(events);
        }

        String eventPriorityUpper = eventPriorityStr.toUpperCase();
        Class<?> priorityClass = this.getEventPriorityClass();
        T eventPriority;
        if (priorityClass != null) {
            eventPriority = (T) ListenerUtil.getStaticDeclaredField(priorityClass, eventPriorityUpper);
        } else {
            eventPriority = (T) eventPriorityStr;
        }

        for (String event : events) {
            event = event.toLowerCase();
            this.createListener(declaringClass, event, eventPriority, eventPriorityUpper);
            Map<T, ScriptWrapper[]> priorityMap = this.scripts.get(event);
            ScriptWrapper[] oldArray = priorityMap.get(eventPriority);
            ScriptWrapper[] newArray = new ScriptWrapper[oldArray.length + 1];
            System.arraycopy(oldArray, 0, newArray, 0, oldArray.length);
            newArray[newArray.length - 1] = new ScriptWrapper(
                    script,
                    this.scriptManager.getScript(declaringClass),
                    declaringClass
            );
            priorityMap.put(eventPriority, newArray);
        }
    }

    public void unregister(String declaringClass) {
        if (this.scripts == null) {
            this.scripts = initScripts();
        }

        for (Entry<String, Map<T, ScriptWrapper[]>> entry : this.scripts.entrySet()) {
            for (Entry<T, ScriptWrapper[]> valueEntry : entry.getValue().entrySet()) {
                ScriptWrapper[] oldArray = valueEntry.getValue();
                if (oldArray.length == 0) {
                    continue;
                }

                List<Integer> removalIndexes = new ArrayList<>();
                for (int i = 0; i < oldArray.length; i++) {
                    if (oldArray[i].getOwner().equals(this.scriptManager.getScript(declaringClass))) {
                        removalIndexes.add(i);
                    }
                }
                if (removalIndexes.isEmpty()) {
                    continue;
                }

                ScriptWrapper[] newArray = new ScriptWrapper[oldArray.length - removalIndexes.size()];
                int offset = 0;
                for (int i = 0; i < oldArray.length; i++) {
                    if (removalIndexes.contains(i)) {
                        offset++;
                        continue;
                    }
                    newArray[i - offset] = oldArray[i];
                }
                valueEntry.setValue(newArray);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Abstract API for subclasses
    // ─────────────────────────────────────────────────────────────────────────

    public abstract String getPriorityName();

    public abstract Class<?> getHandlerClass();

    public abstract Class<?> getListenerClass();

    public abstract Class<?> getEventPriorityClass();

    public abstract String getDefaultPriority();

    public abstract T[] getPriorities();
}