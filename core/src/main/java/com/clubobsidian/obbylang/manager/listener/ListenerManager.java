/*
 *     ObbyLang
 *     Copyright (C) 2021 virustotalop
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.clubobsidian.obbylang.manager.listener;

import com.clubobsidian.obbylang.ObbyLang;
import com.clubobsidian.obbylang.manager.RegisteredManager;
import com.clubobsidian.obbylang.manager.script.MappingsManager;
import com.clubobsidian.obbylang.manager.script.ScriptManager;
import com.clubobsidian.obbylang.manager.script.ScriptWrapper;
import com.clubobsidian.obbylang.manager.server.FakeServerManager;
import com.clubobsidian.obbylang.util.ListenerUtil;
import com.google.inject.Inject;
import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.MemberValue;
import org.apache.commons.lang3.ClassUtils;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ListenerManager<T> implements RegisteredManager {

    //T is event priority
    private Map<String, Map<T, ScriptWrapper[]>> scripts; //Will be wrapped in a separate object later for per script reloading
    private final Map<T, List<String>> registeredEvents = new ConcurrentHashMap<>();
    private final MappingsManager mappingsManager;
    private final ScriptManager scriptManager;

    @Inject
    protected ListenerManager(MappingsManager mappingsManager, ScriptManager scriptManager) {
        this.mappingsManager = mappingsManager;
        this.scriptManager = scriptManager;
    }

    protected void loadEvents(String[] events) {
        for(String event : events) {
            event = event.toLowerCase();
            Map<T, ScriptWrapper[]> scriptMap = new HashMap<>();

            for(T priority : this.getPriorities()) {
                scriptMap.put(priority, new ScriptWrapper[0]);
            }
            if(this.scripts.get(event) == null) {
                this.scripts.put(event, scriptMap);
            }
        }
    }


    protected Map<String, Map<T, ScriptWrapper[]>> initScripts() {
        Map<String, Map<T, ScriptWrapper[]>> scripts = new HashMap<>();
        Iterator<Entry<String, String>> it = this.mappingsManager.getEventMappings().entrySet().iterator();
        while(it.hasNext()) {
            Entry<String, String> next = it.next();
            Map<T, ScriptWrapper[]> scriptMap = new HashMap<>();
            for(T priority : this.getPriorities()) {
                scriptMap.put(priority, new ScriptWrapper[0]);
            }
            scripts.put(next.getValue(), scriptMap);
        }

        return scripts;
    }

    public ScriptWrapper[] getEventScripts(String event, T priority) {
        if(this.scripts == null) {
            this.scripts = this.initScripts();
        }
        return this.scripts.get(event).get(priority);
    }

    private void createListener(String event, T priority, String eventPriorityStr) {
        List<String> events = this.registeredEvents.get(priority);

        if(events == null) {
            events = new ArrayList<>();
            this.registeredEvents.put(priority, events);
        }

        if(!events.contains(event)) {
            events.add(event);
            //Create listener
            Map<String, String> mappings = this.mappingsManager.getEventMappings();
            Iterator<Entry<String, String>> it = mappings.entrySet().iterator();

            while(it.hasNext()) {
                Entry<String, String> next = it.next();
                if(next.getValue().equals(event)) {
                    try {
                        ClassPool.getDefault().insertClassPath(new ClassClassPath(Class.forName(next.getKey())));
                    } catch(ClassNotFoundException e1) {
                        e1.printStackTrace();
                    }
                    StringBuilder builder = new StringBuilder();

                    CtClass ctClass = ClassPool.getDefault().makeClass("com.clubobsidian.obbylang.manager.listener."
                            + eventPriorityStr + next.getValue() + "ObbyLangListener");
                    if(ctClass.isFrozen()) {
                        try {
                            Class<?> listenerClass = ctClass.toClass(ObbyLang.class.getClassLoader(), ObbyLang.class.getProtectionDomain());
                            FakeServerManager.get().registerListener(listenerClass.newInstance());
                        } catch(CannotCompileException | InstantiationException | IllegalAccessException e) {
                            e.printStackTrace();
                        }

                        return;
                    }
                    try {
                        if(this.getListenerClass() != null) {
                            ctClass.addInterface(ClassPool.getDefault().get(this.getListenerClass().getName()));
                        }
                        String generatedPriority = null;
                        if(this.getEventPriorityClass() != null) {
                            Class<?> priorityClass = priority.getClass();
                            generatedPriority = this.getEventPriorityClass().getName() + "." + eventPriorityStr;
                            if(ClassUtils.isPrimitiveOrWrapper(priorityClass)) {
                                generatedPriority = "(($w)" + generatedPriority + ")";
                                System.out.println("Generated priority: " + generatedPriority);
                            }
                        } else if(priority instanceof String) {
                            generatedPriority = "\"" + priority + "\"";
                        }

                        ctClass.setModifiers(Modifier.PUBLIC);
                        builder.append("public void " + next.getValue() + "(" + next.getKey() + " event)");
                        builder.append("{");

                        builder.append("com.clubobsidian.obbylang.manager.script.ScriptWrapper[] scripts = com.clubobsidian.obbylang.manager.listener.ListenerManager.get().getEventScripts(\"" + next.getValue() + "\", " + generatedPriority + ");");
                        builder.append("for(int i = 0; i < scripts.length; i++)");
                        builder.append("{");
                        builder.append("scripts[i].getScript().call(scripts[i].getOwner(), new Object[] {(Object) event});");
                        builder.append("}");
                        builder.append("}");

                        CtMethod ctMethod = CtNewMethod.make(builder.toString(), ctClass);
                        ctMethod.setModifiers(Modifier.PUBLIC);

                        ConstPool constPool = ctClass.getClassFile().getConstPool();
                        AnnotationsAttribute attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
                        Annotation annotation = new Annotation(this.getHandlerClass().getName(), constPool);

                        if(this.getEventPriorityClass() != null) //Annotation attribute
                        {
                            MemberValue memberValue = ListenerUtil.getMemberValue(priority, constPool);
                            annotation.addMemberValue(this.getPriorityName(), memberValue);
                        }

                        attr.addAnnotation(annotation);
                        ctMethod.getMethodInfo().addAttribute(attr);

                        ctClass.addMethod(ctMethod); //Thread.currentThread().getContextClassLoader(), ListenerManager.class.getProtectionDomain()
                        Class<?> listenerClass = ctClass.toClass(ListenerManager.class);
                        FakeServerManager.get().registerListener(listenerClass.getDeclaredConstructors()[0].newInstance());
                    } catch(CannotCompileException | NotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void register(String declaringClass, ScriptObjectMirror script, String event) {
        this.register(declaringClass, script, new String[]{event});
    }

    public void register(String declaringClass, ScriptObjectMirror script, String[] events) {
        this.register(declaringClass, script, events, this.getDefaultPriority());
    }

    public void register(String declaringClass, ScriptObjectMirror script, String event, String eventPriorityStr) {
        this.register(declaringClass, script, new String[]{event}, eventPriorityStr);
    }

    @SuppressWarnings("unchecked")
    public void register(String declaringClass, ScriptObjectMirror script, String[] events, String eventPriorityStr) {
        if(this.scripts == null) {
            this.scripts = initScripts();
        } else {
            this.loadEvents(events);
        }

        String eventPriorityUpper = eventPriorityStr.toUpperCase();
        Class<?> priorityClass = this.getEventPriorityClass();
        T eventPriority;
        if(priorityClass != null) {
            eventPriority = (T) ListenerUtil.getStaticDeclaredField(priorityClass, eventPriorityUpper);
        } else {
            eventPriority = (T) eventPriorityStr;
        }

        for(String event : events) {
            event = event.toLowerCase();
            this.createListener(event, eventPriority, eventPriorityUpper);

            Map<T, ScriptWrapper[]> priorityMap = this.scripts.get(event);

            ScriptWrapper[] oldArray = priorityMap.get(eventPriority);
            ScriptWrapper[] newArray = new ScriptWrapper[oldArray.length + 1];
            for(int i = 0; i < oldArray.length; i++) {
                newArray[i] = oldArray[i];
            }
            newArray[newArray.length - 1] = new ScriptWrapper(script, this.scriptManager.getScript(declaringClass));
            priorityMap.put(eventPriority, newArray);
        }
    }

    public void unregister(String declaringClass) {
        if(this.scripts == null) {
            this.scripts = initScripts();
        }

        Iterator<Entry<String, Map<T, ScriptWrapper[]>>> it = this.scripts.entrySet().iterator();
        while(it.hasNext()) {
            Entry<String, Map<T, ScriptWrapper[]>> next = it.next();

            Iterator<Entry<T, ScriptWrapper[]>> valueIterator = next.getValue().entrySet().iterator();

            while(valueIterator.hasNext()) {
                Entry<T, ScriptWrapper[]> valueIteratorNext = valueIterator.next();
                ScriptWrapper[] oldArray = valueIteratorNext.getValue();
                if(oldArray.length == 0)
                    continue;

                List<Integer> removalIndexes = new ArrayList<>();

                for(int i = 0; i < oldArray.length; i++) {
                    ScriptWrapper wrapper = oldArray[i];
                    if(wrapper.getOwner().equals(this.scriptManager.getScript(declaringClass))) {
                        removalIndexes.add(i);
                    }
                }

                if(removalIndexes.size() == 0)
                    continue;

                ScriptWrapper[] newArray = new ScriptWrapper[oldArray.length - removalIndexes.size()];

                int offset = 0;
                for(int i = 0; i < oldArray.length; i++) {
                    if(removalIndexes.contains(i)) {
                        offset += 1;
                        continue;
                    }
                    newArray[i - offset] = oldArray[i];
                }
                valueIteratorNext.setValue(newArray);
            }
        }
    }

    public abstract String getPriorityName();

    public abstract Class<?> getHandlerClass();

    public abstract Class<?> getListenerClass();

    public abstract Class<?> getEventPriorityClass();

    public abstract String getDefaultPriority();

    public abstract T[] getPriorities();
}