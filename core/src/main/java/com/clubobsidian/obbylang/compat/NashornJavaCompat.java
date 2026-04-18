package com.clubobsidian.obbylang.compat;

import com.caoccao.qjs4j.core.*;
import com.clubobsidian.obbylang.manager.script.ScriptManager;
import com.clubobsidian.obbylang.ObbyLang;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class NashornJavaCompat {

    private static final Map<Class<?>, Class<?>> SUBCLASS_CACHE = new ConcurrentHashMap<>();
    private static final Map<Constructor<?>, MethodHandle> CTOR_CACHE = new ConcurrentHashMap<>();
    private static final Map<Method, MethodHandle> METHOD_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, MethodHandle> SUPER_CACHE = new ConcurrentHashMap<>();
    private static final Map<Field, MethodHandle> FIELD_GET_CACHE = new ConcurrentHashMap<>();
    private static final Map<Field, MethodHandle> FIELD_SET_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Method[]> INSTANCE_METHOD_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Method> METHOD_LOOKUP_CACHE = new ConcurrentHashMap<>();
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private NashornJavaCompat() {}

    public static JavaObjectRegistry install(JSContext context) {
        JavaObjectRegistry registry = new JavaObjectRegistry();
        JSObject javaNamespace = context.createJSObject();
        javaNamespace.set("type", fn(context, "type", 1,
                (ctx, $this, args) -> javaType(ctx, registry, args)));
        javaNamespace.set("extend", fn(context, "extend", 2,
                (ctx, $this, args) -> javaExtend(ctx, registry, args)));
        javaNamespace.set("from", fn(context, "from", 1,
                (ctx, $this, args) -> javaFrom(ctx, registry, args)));
        javaNamespace.set("to", fn(context, "to", 2,
                (ctx, $this, args) -> javaTo(ctx, registry, args)));
        context.getGlobalObject().set("Java", javaNamespace);
        context.getGlobalObject().set("print", fn(context, "print", 1,
                (ctx, $this, args) -> {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < args.length; i++) {
                        if (i > 0) sb.append(' ');
                        sb.append(args[i].toString());
                    }
                    System.out.println(sb);
                    return JSUndefined.INSTANCE;
                }));
        context.getGlobalObject().set("load", fn(context, "load", 1,
                (ctx, $this, args) -> {
                    if (args.length < 1 || !(args[0] instanceof JSString pathArg)) {
                        return ctx.throwTypeError("load() requires a string path");
                    }
                    try {
                        String code = java.nio.file.Files.readString(
                                java.nio.file.Path.of(pathArg.value()));
                        return ctx.eval(code);
                    } catch (java.io.IOException e) {
                        return ctx.throwError("load(): could not read file '"
                                + pathArg.value() + "': " + e.getMessage());
                    }
                }));
        return registry;
    }

    private static JSValue javaType(JSContext context, JavaObjectRegistry registry,
                                    JSValue[] args) {
        if (args.length < 1 || !(args[0] instanceof JSString className)) {
            return context.throwTypeError("Java.type() requires a string class name");
        }
        try {
            return buildClassProxy(context, registry, Class.forName(className.value()));
        } catch (ClassNotFoundException e) {
            return context.throwError("Java.type: class not found: " + className.value());
        }
    }

    static JSObject buildClassProxy(JSContext context, JavaObjectRegistry registry,
                                    Class<?> clazz) {
        JSNativeFunction proxy = new JSNativeFunction(context, clazz.getSimpleName(), 0,
                (ctx, $this, args) -> construct(ctx, registry, clazz, args),
                /* isConstructor = */ true);

        // Static methods — first declared overload wins; handle looked up once and cached
        for (Method m : clazz.getMethods()) {
            if (!Modifier.isStatic(m.getModifiers())) continue;
            if (proxy.has(m.getName())) continue;
            final String name = m.getName();
            proxy.set(name, fn(context, name, m.getParameterCount(),
                    (ctx, $this, args) -> invokeStatic(ctx, registry, clazz, name, args)));
        }

        // Static fields — read once via MethodHandle getter at proxy-build time
        for (Field f : clazz.getFields()) {
            if (!Modifier.isStatic(f.getModifiers())) continue;
            try {
                MethodHandle getter = fieldGetHandle(f);
                proxy.set(f.getName(), toJSValue(context, registry, getter.invoke()));
            } catch (Throwable ignored) {}
        }

        // Nested classes — getClasses() returns all public nested/member classes and
        // interfaces, including nested enums, from this class and its superclasses.
        // Each is exposed as a nested class proxy under its simple name, so
        // Java.type("Outer").Inner works without a separate Java.type("Outer$Inner").
        for (Class<?> nested : clazz.getClasses()) {
            // Use simple name relative to the immediate enclosing class so that
            // deeply nested types are still reachable one level at a time.
            // e.g. Outer.Inner.Deepest — not the full "Outer.Inner$Deepest" form.
            String simpleName = nested.getSimpleName();
            if (simpleName.isEmpty()) continue; // skip anonymous classes
            if (proxy.has(simpleName)) continue; // static field/method takes priority
            proxy.set(simpleName, buildClassProxy(context, registry, nested));
        }

        proxy.set("__javaClass__", new JSString(clazz.getName()));
        return proxy;
    }

    private static JSValue javaExtend(JSContext context, JavaObjectRegistry registry,
                                      JSValue[] args) {
        if (args.length < 1 || !(args[0] instanceof JSObject classProxy)) {
            return context.throwTypeError("Java.extend() requires a Java.type() proxy as first argument");
        }
        JSValue classNameVal = classProxy.get("__javaClass__");
        if (!(classNameVal instanceof JSString classNameStr)) {
            return context.throwTypeError("Java.extend(): first argument is not a Java.type() proxy");
        }
        Class<?> base;
        try {
            base = Class.forName(classNameStr.value());
        } catch (ClassNotFoundException e) {
            return context.throwError("Java.extend: class not found: " + classNameStr.value());
        }

        Map<String, JSFunction> overrides = new HashMap<>();
        if (args.length >= 2 && args[1] instanceof JSObject implObj) {
            for (PropertyKey key : implObj.getOwnPropertyKeys()) {
                String keyStr = key.asString();
                if (keyStr == null) continue;
                JSValue val = implObj.get(keyStr);
                if (val instanceof JSFunction fn) {
                    overrides.put(keyStr, fn);
                }
            }
        }

        Object instance = createExtendedInstance(context, registry, base, overrides);
        return wrapJavaObject(context, registry, instance);
    }

    /**
     * Creates an instance of a class that dispatches overridden methods to JS functions.
     *
     * <ul>
     *   <li><b>Interface</b>: {@code java.lang.reflect.Proxy} — zero overhead.</li>
     *   <li><b>Abstract / concrete class</b>: ByteBuddy subclass whose {@code __handler__}
     *       field is injected via a cached MethodHandle setter after construction.</li>
     * </ul>
     */
    private static Object createExtendedInstance(JSContext context,
                                                 JavaObjectRegistry registry,
                                                 Class<?> base,
                                                 Map<String, JSFunction> overrides) {
        InvocationHandler handler = buildHandler(context, registry, overrides, base);

        if (base.isInterface()) {
            return Proxy.newProxyInstance(classLoader(base), new Class<?>[]{ base }, handler);
        }

        Class<?> subclass = SUBCLASS_CACHE.computeIfAbsent(base,
                NashornJavaCompat::generateSubclass);

        try {
            // Use the cheapest available constructor, passing dummy args
            Constructor<?> ctor = findCheapestConstructor(subclass);
            MethodHandle ctorHandle = LOOKUP.unreflectConstructor(ctor);
            Object[] dummyArgs = dummyArgs(ctor.getParameterTypes());
            Object instance = invoke(ctorHandle, dummyArgs);

            // Inject handler via cached MethodHandle setter
            Field handlerField = subclass.getDeclaredField("__handler__");
            MethodHandle setter = fieldSetHandle(handlerField);
            setter.invoke(instance, handler);

            return instance;
        } catch (Throwable e) {
            throw new RuntimeException("Java.extend: could not instantiate subclass of "
                    + base.getName() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Uses ByteBuddy to generate a concrete subclass of {@code base}.
     * The generated class has a public {@code __handler__} field of type
     * {@link InvocationHandler} that routes all overridable methods.
     */
    @SuppressWarnings("unchecked")
    private static Class<?> generateSubclass(Class<?> base) {
        try {
            return (Class<?>) new ByteBuddy()
                    .subclass(base)
                    .defineField("__handler__", InvocationHandler.class, Modifier.PUBLIC)
                    .method(
                            ElementMatchers.not(ElementMatchers.isFinal())
                                    .and(ElementMatchers.not(ElementMatchers.isStatic()))
                                    .and(ElementMatchers.not(ElementMatchers.isPrivate()))
                                    .and(
                                            ElementMatchers.not(ElementMatchers.isDeclaredBy(Object.class))
                                                    .or(ElementMatchers.named("toString"))
                                                    .or(ElementMatchers.named("equals"))
                                                    .or(ElementMatchers.named("hashCode"))
                                    )
                    )
                    .intercept(InvocationHandlerAdapter.toField("__handler__"))
                    .make()
                    .load(classLoader(base), ClassLoadingStrategy.Default.INJECTION)
                    .getLoaded();
        } catch (Exception e) {
            throw new RuntimeException("ByteBuddy failed to subclass " + base.getName()
                    + ": " + e.getMessage(), e);
        }
    }

    /**
     * Builds the {@link InvocationHandler} that dispatches to JS overrides and falls
     * back to the super implementation for non-overridden concrete methods.
     *
     * <p>Each JS override receives a {@code this} object that has a {@code $super}
     * property. {@code $super} is a JS object whose properties are functions mirroring
     * every overridable method on the base class, each invoking the real superclass
     * implementation via {@code unreflectSpecial}. The script author calls
     * {@code this.$super.methodName(args)} to reach the Java super implementation:
     *
     * <pre>
     *   Java.extend(Java.type('java.util.ArrayList'), {
     *       add: function(obj) {
     *           console.log('adding: ' + obj);
     *           return this.$super.add(obj);
     *       }
     *   });
     * </pre>
     *
     * <p>The {@code $super} object is built lazily on the first override dispatch for
     * a given proxy instance and cached in the handler closure after that.
     */
    private static InvocationHandler buildHandler(JSContext context,
                                                  JavaObjectRegistry registry,
                                                  Map<String, JSFunction> overrides,
                                                  Class<?> base) {
        // Lazy cache: built once the first time any override fires for this instance.
        // Array wrapper lets the lambda mutate the reference.
        JSObject[] superObjHolder = new JSObject[1];

        return (proxy, method, methodArgs) -> {
            String name = method.getName();

            // JS override takes priority
            if (overrides.containsKey(name)) {
                JSFunction fn = overrides.get(name);
                JSValue[] jsArgs = methodArgs == null
                        ? JSValue.NO_ARGS
                        : toJSArgs(context, registry, methodArgs);

                // Build $super lazily on first override invocation
                if (superObjHolder[0] == null) {
                    superObjHolder[0] = buildSuperObject(context, registry, base, proxy);
                }

                // Wrap $super in a thisArg object so JS can call this.$super.foo()
                JSObject thisArg = context.createJSObject();
                thisArg.set("$super", superObjHolder[0]);

                JSValue result = fn.call(context, thisArg, jsArgs);
                if (context.hasPendingException()) {
                    JSValue ex = context.getPendingException();
                    context.clearPendingException();
                    throw new RuntimeException(
                            "JS exception in Java.extend override '" + name + "': " + ex);
                }
                return fromJSValue(result, method.getReturnType());
            }

            // Default interface method — use JDK invokeDefault
            if (method.isDefault()) {
                return InvocationHandler.invokeDefault(proxy, method, methodArgs);
            }

            // Non-abstract super method — invoke via unreflectSpecial to bypass the
            // generated override without any recursion risk.
            if (!Modifier.isAbstract(method.getModifiers())) {
                MethodHandle superHandle = superMethodHandle(proxy.getClass(), method);
                if (superHandle != null) {
                    Object[] callArgs = methodArgs != null ? methodArgs : new Object[0];
                    return invokeWithReceiver(superHandle, proxy, callArgs);
                }
            }

            // Abstract method with no JS override — safe zero/null
            return primitiveDefault(method.getReturnType());
        };
    }

    /**
     * Builds the {@code $super} JS object for a given proxy instance.
     *
     * <p>Every public, non-static, non-final method reachable from {@code base}
     * (walking the full superclass chain) is exposed as a property. Each property
     * is a native JS function that invokes the corresponding superclass method on
     * {@code proxy} via a cached {@code unreflectSpecial} handle, bypassing the
     * ByteBuddy-generated override entirely.
     *
     * <p>Only the first matching method per name is exposed (same first-overload-wins
     * policy used elsewhere). Overloads are uncommon in practice and resolving them
     * by argument count at call time would add complexity without much benefit.
     */
    private static JSObject buildSuperObject(JSContext context, JavaObjectRegistry registry,
                                             Class<?> base, Object proxy) {
        JSObject superObj = context.createJSObject();

        // Collect all accessible non-static, non-private, non-abstract methods from the
        // superclass chain, grouped by name. We need all overloads so we can resolve by
        // argument count at call time — registering only the first-declared overload would
        // pick the wrong signature when a class has multiple same-named methods (e.g.
        // ArrayList.add(E) vs ArrayList.add(int, E)).
        Map<String, List<Method>> byName = new HashMap<>();
        Class<?> cursor = base.isInterface() ? null : base;
        while (cursor != null && cursor != Object.class) {
            for (Method m : cursor.getDeclaredMethods()) {
                int mods = m.getModifiers();
                if (Modifier.isStatic(mods) || Modifier.isPrivate(mods)
                        || Modifier.isAbstract(mods)) continue;
                byName.computeIfAbsent(m.getName(), k -> new ArrayList<>()).add(m);
            }
            cursor = cursor.getSuperclass();
        }

        for (Map.Entry<String, List<Method>> entry : byName.entrySet()) {
            final String mName = entry.getKey();
            final List<Method> overloads = entry.getValue();
            // Use the minimum parameter count as the declared JS arity hint
            int minArity = overloads.stream().mapToInt(Method::getParameterCount).min().orElse(0);
            superObj.set(mName, fn(context, mName, minArity,
                    (ctx, $this, callArgs) -> {
                        try {
                            // Resolve overload by argument count and type compatibility
                            Object[] rawArgs = fromJSArgs(callArgs);
                            Method resolved = null;
                            Method anyExact = null;
                            Method varargExactM = null;
                            Method varargFallbackM = null;
                            for (Method m : overloads) {
                                if (m.getParameterCount() == callArgs.length && !m.isVarArgs()) {
                                    if (isCompatible(m.getParameterTypes(), rawArgs, registry)) {
                                        resolved = m;
                                        break;
                                    }
                                    if (anyExact == null) anyExact = m;
                                } else if (m.getParameterCount() == callArgs.length && m.isVarArgs()) {
                                    if (varargExactM == null) varargExactM = m;
                                } else if (m.isVarArgs() && callArgs.length >= m.getParameterCount() - 1) {
                                    if (varargFallbackM == null) varargFallbackM = m;
                                }
                            }
                            if (resolved == null) resolved = anyExact;
                            if (resolved == null) resolved = varargExactM;
                            if (resolved == null) resolved = varargFallbackM;
                            if (resolved == null) resolved = overloads.get(0);

                            MethodHandle handle = superMethodHandle(proxy.getClass(), resolved);
                            if (handle == null) {
                                return context.throwError(
                                        "$super." + mName + ": no accessible super method");
                            }
                            Object[] javaArgs = coerce(
                                    rawArgs, resolved.getParameterTypes(), registry);
                            Object result = invokeWithReceiver(handle, proxy, javaArgs);
                            return toJSValue(ctx, registry, result);
                        } catch (Throwable e) {
                            return context.throwError(
                                    "$super." + mName + "() threw: " + e.getMessage());
                        }
                    }));
        }

        // Interface default methods
        for (Class<?> iface : base.isInterface() ? new Class<?>[]{ base } : base.getInterfaces()) {
            for (Method m : iface.getMethods()) {
                if (!m.isDefault()) continue;
                if (superObj.has(m.getName())) continue;
                final Method captured = m;
                final String mName = m.getName();
                superObj.set(mName, fn(context, mName, m.getParameterCount(),
                        (ctx, $this, callArgs) -> {
                            try {
                                Object result = InvocationHandler.invokeDefault(
                                        proxy, captured,
                                        callArgs.length == 0 ? null : fromJSArgs(callArgs));
                                return toJSValue(ctx, registry, result);
                            } catch (Throwable e) {
                                return context.throwError(
                                        "$super." + mName + "() threw: " + e.getMessage());
                            }
                        }));
            }
        }

        return superObj;
    }

    /**
     * Returns a cached {@code invokespecial}-equivalent handle for {@code method}
     * bound to the superclass implementation, bypassing any ByteBuddy override.
     *
     * <p>Three strategies are tried in order:
     * <ol>
     *   <li>{@code privateLookupIn(declaringClass)} + {@code unreflectSpecial} —
     *       works for classes in modules open to us.</li>
     *   <li>{@code privateLookupIn(generatedClass)} + {@code findSpecial} with
     *       {@code generatedClass} as the special caller — works for non-open JDK
     *       modules (e.g. {@code java.util.ArrayList}) because the generated
     *       ByteBuddy subclass IS a subclass of the declaring class, satisfying the
     *       {@code invokespecial} access rule.</li>
     *   <li>Regular virtual {@code unreflect} as last resort — avoids the recursion
     *       that would normally occur because the handle is always bound to {@code proxy}
     *       directly; the JVM still dispatches virtually but the ByteBuddy override
     *       is not active for this direct invocation path in practice.</li>
     * </ol>
     */
    private static MethodHandle superMethodHandle(Class<?> generatedClass, Method method) {
        String cacheKey = generatedClass.getName() + ">"
                + method.getDeclaringClass().getName() + "." + method.getName();
        return SUPER_CACHE.computeIfAbsent(cacheKey, k -> {
            Class<?> cursor = generatedClass.getSuperclass();
            while (cursor != null) {
                try {
                    Method declared = cursor.getDeclaredMethod(
                            method.getName(), method.getParameterTypes());

                    // Strategy 1: privateLookupIn the declaring class (open modules)
                    try {
                        return MethodHandles.privateLookupIn(cursor, LOOKUP)
                                .unreflectSpecial(declared, cursor);
                    } catch (IllegalAccessException ignored1) {}

                    // Strategy 2: privateLookupIn the generated subclass, then findSpecial.
                    // The generated class is a true subclass of cursor, so the JVM accepts
                    // it as the special caller for invokespecial on cursor's methods.
                    try {
                        MethodType mt = MethodType.methodType(
                                declared.getReturnType(), declared.getParameterTypes());
                        return MethodHandles.privateLookupIn(generatedClass, LOOKUP)
                                .findSpecial(cursor, declared.getName(), mt, generatedClass);
                    } catch (IllegalAccessException | NoSuchMethodException ignored2) {}

                    // Strategy 3: last resort — regular virtual unreflect with setAccessible.
                    // This can only be reached for non-open modules where privateLookupIn
                    // fails on both the declaring class and the generated subclass.
                    // Virtual dispatch applies, but in practice this path is only hit for
                    // methods that are NOT in the JS overrides map (otherwise strategies
                    // 1 or 2 would have succeeded for an overridden method on a user class).
                    declared.setAccessible(true);
                    return MethodHandles.lookup().unreflect(declared);

                } catch (NoSuchMethodException ignored) {
                    cursor = cursor.getSuperclass();
                } catch (IllegalAccessException e) {
                    return null;
                }
            }
            return null;
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Java.from  /  Java.to
    // ─────────────────────────────────────────────────────────────────────────

    private static JSValue javaFrom(JSContext context, JavaObjectRegistry registry,
                                    JSValue[] args) {
        if (args.length < 1) {
            return context.throwTypeError("Java.from() requires an argument");
        }
        Object raw = args[0] instanceof JSObject o ? o.toJavaObject() : null;
        if (!(raw instanceof Iterable<?> iterable)) {
            return context.throwTypeError("Java.from() requires a Java Iterable");
        }
        JSArray arr = context.createJSArray();
        int idx = 0;
        for (Object item : iterable) arr.set(idx++, toJSValue(context, registry, item));
        return arr;
    }

    private static JSValue javaTo(JSContext context, JavaObjectRegistry registry,
                                  JSValue[] args) {
        if (args.length < 2
                || !(args[0] instanceof JSArray jsArr)
                || !(args[1] instanceof JSString typeName)) {
            return context.throwTypeError("Java.to() requires (JSArray, typeName)");
        }
        try {
            Class<?> componentType = Class.forName(typeName.value());
            int len = (int) ((JSNumber) jsArr.get("length")).value();
            Object javaArr = Array.newInstance(componentType, len);
            for (int i = 0; i < len; i++) {
                Array.set(javaArr, i, fromJSValue(jsArr.get(i), componentType));
            }
            return wrapJavaObject(context, registry, javaArr);
        } catch (ClassNotFoundException e) {
            return context.throwError("Java.to: class not found: " + args[1]);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // wrapJavaObject
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Wraps a live Java object in a lazy {@link JSProxy}, so that method
     * {@link JSNativeFunction} objects and field values are only created when JS
     * actually accesses a property — not upfront on every wrap.
     *
     * <p>This eliminates the recursive {@code wrapJavaObject → toJSValue → wrapJavaObject}
     * chain that appeared in profiler output: return values from method calls are now
     * wrapped in O(1) with zero method enumeration, and the per-method function objects
     * are only created on first property access, then cached on the target object so
     * subsequent accesses are a plain property read.
     *
     * <p>If this exact object (by identity) has already been wrapped in this context,
     * the existing wrapper is returned immediately.
     */
    /**
     * Wraps a live Java object in a {@link JSObject}, exposing all public instance
     * methods (via cached MethodHandles) and readable fields as own properties.
     *
     * <p>The method list for each class is computed once and cached statically so
     * {@link Class#getMethods()} is called at most once per class across all contexts.
     * The same object (by identity) is never wrapped twice in the same context —
     * the existing wrapper is returned immediately from the registry.
     *
     * <p>We use a plain JSObject rather than JSProxy so that the wrapper identity
     * is stable when passed as a function argument through JS code. JSProxy values
     * can be surfaced as their inner target by the qjs4j VM, breaking registry
     * lookups by identity hash.
     */
    /**
     * Wraps a live Java object in a {@link JSObject} with lazy method population.
     *
     * <p>Methods are installed as configurable accessor properties (getter-only).
     * On first access the getter fires, creates the real {@link JSNativeFunction},
     * replaces itself with a plain data property, and returns the function — so
     * subsequent accesses hit the data property directly with no getter overhead.
     *
     * <p>This gives lazy allocation (only methods actually called get a function
     * object) while keeping a stable plain {@link JSObject} identity so the
     * registry lookup by {@link System#identityHashCode} always works correctly.
     *
     * <p>Fields are also lazy: a getter-only accessor is defined that reads the
     * field on access and replaces itself with the value.
     */
    public static JSObject wrapJavaObject(JSContext context, JavaObjectRegistry registry,
                                          Object javaObj) {
        // Fast path: same object wrapped before in this context
        JSObject existing = registry.existingWrapper(javaObj);
        if (existing != null) return existing;

        JSObject wrapper = context.createJSObject();
        Class<?> clazz = javaObj.getClass();

        Method[] methods = INSTANCE_METHOD_CACHE.computeIfAbsent(clazz, c -> {
            List<Method> list = new ArrayList<>();
            java.util.Set<String> seen = new java.util.HashSet<>();
            for (Method m : c.getMethods()) {
                if (Modifier.isStatic(m.getModifiers())) {
                    continue;
                }
                if (isNoisyObjectMethod(m)) {
                    continue;
                }
                if (seen.add(m.getName())) {
                    list.add(m);
                }
            }
            return list.toArray(new Method[0]);
        });

        for (Method m : methods) {
            final String name = m.getName();
            final int arity = m.getParameterCount();
            // Configurable getter: fires once, installs a data property, replaces itself
            JSNativeFunction getter = fn(context, name, 0, (ctx, $this, ignored) -> {
                JSNativeFunction methodFn = fn(ctx, name, arity,
                        (mCtx, mThis, mArgs) -> invokeInstance(mCtx, registry, javaObj, name, mArgs));
                // Replace the accessor with a plain data property so future accesses
                // bypass this getter entirely — O(1) property read from here on
                wrapper.defineProperty(
                        PropertyKey.fromString(name),
                        PropertyDescriptor.dataDescriptor(methodFn,
                                PropertyDescriptor.DataState.ConfigurableWritable));
                return methodFn;
            });
            wrapper.defineProperty(
                    PropertyKey.fromString(name),
                    PropertyDescriptor.accessorDescriptor(getter, null,
                            PropertyDescriptor.AccessorState.Configurable));
        }

        for (Field f : clazz.getFields()) {
            if (Modifier.isStatic(f.getModifiers())) continue;
            final Field capturedField = f;
            JSNativeFunction getter = fn(context, f.getName(), 0, (ctx, $this, ignored) -> {
                try {
                    JSValue val = toJSValue(ctx, registry, fieldGetHandle(capturedField).invoke(javaObj));
                    wrapper.defineProperty(
                            PropertyKey.fromString(capturedField.getName()),
                            PropertyDescriptor.dataDescriptor(val,
                                    PropertyDescriptor.DataState.ConfigurableWritable));
                    return val;
                } catch (Throwable e) {
                    return ctx.throwError(capturedField.getName() + " field read failed: " + e.getMessage());
                }
            });
            wrapper.defineProperty(
                    PropertyKey.fromString(f.getName()),
                    PropertyDescriptor.accessorDescriptor(getter, null,
                            PropertyDescriptor.AccessorState.Configurable));
        }

        registry.register(wrapper, javaObj);
        return wrapper;
    }

    /**
     * Convenience overload that resolves the {@link JSContext} and
     * {@link JavaObjectRegistry} from the {@link ScriptManager} by script name.
     * Intended for use from Java code that holds a reference to a Java object
     * and needs to hand it back to a specific script context.
     */
    public static JSObject wrapJavaObject(String declaringClass, Object javaObj) {
        ScriptManager scriptManager = ObbyLang.get().getScriptManager();
        JSContext context = scriptManager.getScript(declaringClass);
        JavaObjectRegistry registry = scriptManager.getRegistry(declaringClass);
        return wrapJavaObject(context, registry, javaObj);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Invocation via MethodHandles
    // ─────────────────────────────────────────────────────────────────────────

    private static JSValue construct(JSContext context, JavaObjectRegistry registry,
                                     Class<?> clazz, JSValue[] args) {
        try {
            Constructor<?> ctor = findConstructor(clazz, args.length);
            MethodHandle mh = ctorHandle(ctor);
            Object[] javaArgs = coerce(fromJSArgs(args), ctor.getParameterTypes(), registry);
            Object instance = invoke(mh, javaArgs);
            return wrapJavaObject(context, registry, instance);
        } catch (InvocationTargetException e) {
            return context.throwError("Java constructor threw: " + unwrap(e).getMessage());
        } catch (Throwable e) {
            return context.throwError("Java constructor failed: " + e.getMessage());
        }
    }

    private static JSValue invokeStatic(JSContext context, JavaObjectRegistry registry,
                                        Class<?> clazz, String name, JSValue[] args) {
        try {
            Object[] rawArgs = fromJSArgs(args);
            Method m = findMethod(clazz, name, rawArgs, true, registry);
            MethodHandle mh = methodHandle(m);
            Object[] javaArgs = coerce(rawArgs, m.getParameterTypes(), registry);
            Object result = invoke(mh, javaArgs);
            return toJSValue(context, registry, result);
        } catch (InvocationTargetException e) {
            return context.throwError(clazz.getSimpleName() + "." + name + "() threw: "
                    + unwrap(e).getMessage());
        } catch (Throwable e) {
            return context.throwError("Java." + name + "() failed: " + e.getMessage());
        }
    }

    private static JSValue invokeInstance(JSContext context, JavaObjectRegistry registry,
                                          Object target, String name, JSValue[] args) {
        try {
            Class<?> concreteClass = target.getClass();
            Object[] rawArgs = fromJSArgs(args);
            Method m = findMethod(concreteClass, name, rawArgs, false, registry);
            MethodHandle mh = methodHandle(m, concreteClass);
            Object[] javaArgs = coerce(rawArgs, m.getParameterTypes(), registry);
            // Pass receiver inline — avoids bindTo() allocating a new handle each call
            Object result = invokeWithReceiver(mh, target, javaArgs);
            return toJSValue(context, registry, result);
        } catch (InvocationTargetException e) {
            return context.throwError(name + "() threw: " + unwrap(e).getMessage());
        } catch (Throwable e) {
            return context.throwError(name + "() failed: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MethodHandle lookup helpers (with caching)
    // ─────────────────────────────────────────────────────────────────────────

    private static MethodHandle ctorHandle(Constructor<?> ctor) {
        return CTOR_CACHE.computeIfAbsent(ctor, k -> lookupCtor(ctor));
    }

    private static MethodHandle methodHandle(Method m) {
        return METHOD_CACHE.computeIfAbsent(m, k -> lookupMethod(m, m.getDeclaringClass()));
    }

    /**
     * Variant used by {@link #invokeInstance} that passes the concrete runtime class
     * so that {@link #lookupMethod} can search from the actual object type rather than
     * the (possibly package-private) declaring class.
     * <p>
     * Keyed by the method itself — the concrete class only affects the lookup path, not
     * the resulting handle, since {@code findVirtual} dispatches on receiver type at
     * call time regardless of which interface the handle was obtained through.
     */
    private static MethodHandle methodHandle(Method m, Class<?> concreteClass) {
        return METHOD_CACHE.computeIfAbsent(m, k -> lookupMethod(m, concreteClass));
    }

    private static MethodHandle fieldGetHandle(Field f) {
        return FIELD_GET_CACHE.computeIfAbsent(f, k -> lookupFieldGet(f));
    }

    private static MethodHandle fieldSetHandle(Field f) {
        return FIELD_SET_CACHE.computeIfAbsent(f, k -> lookupFieldSet(f));
    }

    /**
     * Tiered lookup strategy used by all handle helpers:
     *
     * <ol>
     *   <li>Try {@link MethodHandles#privateLookupIn} — works for classes in modules
     *       that are open to us (e.g. application classes, qjs4j itself).</li>
     *   <li>Fall back to {@link MethodHandles#publicLookup()} — works for any genuinely
     *       public member of any public class in any module, including non-open JDK modules
     *       like {@code java.util}. Does not require {@code setAccessible} and will not
     *       throw an {@code InaccessibleObjectException}.</li>
     *   <li>Last resort: {@code setAccessible(true)} + {@link MethodHandles#lookup()} —
     *       for non-public members that the caller legitimately needs to reach.</li>
     * </ol>
     *
     * Strategy 2 is the fix for errors like "Unable to make iterator() accessible:
     * module java.base does not open java.util" — {@code publicLookup()} needs no
     * open declaration, only that the member itself is public.
     */
    private static MethodHandle lookupCtor(Constructor<?> ctor) {
        // Strategy 1: private lookup (open modules / app classes)
        try {
            return MethodHandles.privateLookupIn(ctor.getDeclaringClass(), LOOKUP)
                    .unreflectConstructor(ctor);
        } catch (IllegalAccessException ignored) {}
        // Strategy 2: public lookup — works for any genuinely public constructor in
        // any module without requiring the module to be open. setAccessible is not
        // needed and will fail on non-open JDK modules (e.g. java.util).
        try {
            return MethodHandles.publicLookup().unreflectConstructor(ctor);
        } catch (IllegalAccessException ignored) {}
        // Strategy 3: force-accessible last resort (non-public members)
        try {
            ctor.setAccessible(true);
            return MethodHandles.lookup().unreflectConstructor(ctor);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot obtain MethodHandle for constructor of "
                    + ctor.getDeclaringClass().getName(), e);
        }
    }

    private static MethodHandle lookupMethod(Method m, Class<?> concreteClass) {
        try { //Public
            return LOOKUP.unreflect(m);
        } catch (IllegalAccessException ignored) {}
        try { //Private
            return MethodHandles.privateLookupIn(m.getDeclaringClass(), LOOKUP).unreflect(m);
        } catch (IllegalAccessException ignored) {}
        Method publicMethod = findViaPublicSupertype(m);
        if (publicMethod != null && publicMethod != m) { //Non public class
            try {
                return MethodHandles.publicLookup().unreflect(publicMethod);
            } catch (IllegalAccessException ignored) {}
        }
        // Strategy 4: search from the concrete runtime class upward. This covers the
        // case where the declaring class (e.g. HashMap$HashIterator) is in the middle
        // of a package-private hierarchy and doesn't directly implement the public
        // interface — but the concrete class (e.g. HashMap$EntryIterator) does via
        // Iterator. findVirtual dispatches on receiver type at call time so it always
        // calls the right implementation.
        MethodHandle virtualHandle = findVirtualViaInterfaces(m, concreteClass);
        if (virtualHandle != null) return virtualHandle;
        // Strategy 5: force-accessible last resort
        try {
            m.setAccessible(true);
            return MethodHandles.lookup().unreflect(m);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot obtain MethodHandle for "
                    + m.getDeclaringClass().getName() + "." + m.getName(), e);
        }
    }

    /**
     * For methods declared on package-private classes that sit in the middle of a
     * hierarchy (e.g. {@code HashMap$HashIterator} which is the abstract base for
     * concrete iterators but doesn't itself implement {@code Iterator}), we walk the
     * full superclass chain collecting all public interfaces and try
     * {@code publicLookup().findVirtual(iface, name, type)} on each one.
     *
     * <p>{@code findVirtual} on an interface produces a handle that dispatches
     * virtually on the concrete receiver at call time, so the right implementation
     * is always invoked even though the handle was obtained through the interface.
     */
    private static MethodHandle findVirtualViaInterfaces(Method m, Class<?> concreteClass) {
        String name = m.getName();
        MethodType type = MethodType.methodType(m.getReturnType(), m.getParameterTypes());
        // Start from concrete class — it may implement interfaces the declaring class doesn't
        Class<?> cursor = concreteClass;
        while (cursor != null) {
            for (Class<?> iface : cursor.getInterfaces()) {
                if (!Modifier.isPublic(iface.getModifiers())) continue;
                try {
                    return MethodHandles.publicLookup().findVirtual(iface, name, type);
                } catch (NoSuchMethodException | IllegalAccessException ignored) {}
                // Also check superinterfaces recursively
                MethodHandle fromSuper = findVirtualInSuperInterfaces(iface, name, type);
                if (fromSuper != null) return fromSuper;
            }
            cursor = cursor.getSuperclass();
        }
        return null;
    }

    private static MethodHandle findVirtualInSuperInterfaces(Class<?> iface, String name,
                                                             MethodType type) {
        for (Class<?> superIface : iface.getInterfaces()) {
            if (!Modifier.isPublic(superIface.getModifiers())) continue;
            try {
                return MethodHandles.publicLookup().findVirtual(superIface, name, type);
            } catch (NoSuchMethodException | IllegalAccessException ignored) {}
            MethodHandle h = findVirtualInSuperInterfaces(superIface, name, type);
            if (h != null) return h;
        }
        return null;
    }

    /**
     * When a method is declared on a non-public class (e.g. {@code HashMap$EntrySet},
     * {@code HashMap$HashIterator}), {@link MethodHandles#publicLookup()} cannot
     * unreflect it even if the method itself is public. This walks the entire type
     * hierarchy — from the declaring class upward — looking for the same method
     * signature declared on a public type (interface or class) so the handle can be
     * obtained through that type instead.
     *
     * <p>Note: we walk the full hierarchy including superclasses' interfaces, because
     * some JDK classes implement interfaces via a non-public intermediary
     * (e.g. {@code HashMap$EntryIterator} extends {@code HashMap$HashIterator}
     * and {@code HashMap$HashIterator} doesn't directly implement {@code Iterator} —
     * the subclass does). {@link Class#getMethods()} returns inherited interface
     * methods with their declaring class set to the non-public abstract class, so
     * we must search the full hierarchy from the concrete class downward.
     */
    private static Method findViaPublicSupertype(Method m) {
        return findViaPublicSupertypeFrom(m.getDeclaringClass(), m.getName(), m.getParameterTypes());
    }

    /**
     * Searches {@code startClass} and all its supertypes for a public declaration of
     * {@code name(params)}. Used both by {@link #findViaPublicSupertype(Method)} and
     * directly from {@link #lookupMethod} when we need to search from the concrete
     * runtime class rather than the (possibly non-public) declaring class.
     */
    private static Method findViaPublicSupertypeFrom(Class<?> startClass, String name,
                                                     Class<?>[] params) {
        Class<?> cursor = startClass;
        while (cursor != null) {
            // Check this class itself if it's public
            if (Modifier.isPublic(cursor.getModifiers())) {
                try {
                    Method found = cursor.getMethod(name, params);
                    if (Modifier.isPublic(found.getDeclaringClass().getModifiers())) {
                        return found;
                    }
                } catch (NoSuchMethodException ignored) {}
            }
            // Check all interfaces of this class
            for (Class<?> iface : cursor.getInterfaces()) {
                if (!Modifier.isPublic(iface.getModifiers())) continue;
                try {
                    return iface.getMethod(name, params);
                } catch (NoSuchMethodException ignored) {}
                // Also check superinterfaces
                Method fromSuper = findViaPublicSupertypeFrom(iface, name, params);
                if (fromSuper != null) return fromSuper;
            }
            cursor = cursor.getSuperclass();
        }
        return null;
    }

    private static MethodHandle lookupFieldGet(Field f) {
        // Strategy 1: private lookup
        try {
            return MethodHandles.privateLookupIn(f.getDeclaringClass(), LOOKUP)
                    .unreflectGetter(f);
        } catch (IllegalAccessException ignored) {}
        // Strategy 2: public lookup (public fields in non-open modules)
        try {
            return MethodHandles.publicLookup().unreflectGetter(f);
        } catch (IllegalAccessException ignored) {}
        // Strategy 3: force-accessible
        try {
            f.setAccessible(true);
            return MethodHandles.lookup().unreflectGetter(f);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot obtain getter MethodHandle for "
                    + f.getDeclaringClass().getName() + "." + f.getName(), e);
        }
    }

    private static MethodHandle lookupFieldSet(Field f) {
        // Strategy 1: private lookup
        try {
            return MethodHandles.privateLookupIn(f.getDeclaringClass(), LOOKUP)
                    .unreflectSetter(f);
        } catch (IllegalAccessException ignored) {}
        // Strategy 2: public lookup (public fields in non-open modules)
        try {
            return MethodHandles.publicLookup().unreflectGetter(f);
        } catch (IllegalAccessException ignored) {}
        // Strategy 3: force-accessible
        try {
            f.setAccessible(true);
            return MethodHandles.lookup().unreflectSetter(f);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot obtain setter MethodHandle for "
                    + f.getDeclaringClass().getName() + "." + f.getName(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Discovery helpers (reflection for searching, not for invocation)
    // ─────────────────────────────────────────────────────────────────────────

    private static Constructor<?> findConstructor(Class<?> clazz, int argCount)
            throws NoSuchMethodException {
        Constructor<?> varargExact = null;
        Constructor<?> varargFallback = null;
        for (Constructor<?> c : clazz.getConstructors()) {
            if (c.getParameterCount() == argCount) {
                if (!c.isVarArgs()) return c;
                if (varargExact == null) varargExact = c;
            } else if (c.isVarArgs() && argCount >= c.getParameterCount() - 1) {
                if (varargFallback == null) varargFallback = c;
            }
        }
        if (varargExact != null) return varargExact;
        if (varargFallback != null) return varargFallback;
        throw new NoSuchMethodException(
                "No constructor on " + clazz.getName() + " accepting " + argCount + " argument(s)");
    }

    private static Constructor<?> findCheapestConstructor(Class<?> clazz) {
        Constructor<?> best = null;
        for (Constructor<?> c : clazz.getConstructors()) {
            if (best == null || c.getParameterCount() < best.getParameterCount()) best = c;
        }
        if (best != null) return best;
        Constructor<?>[] declared = clazz.getDeclaredConstructors();
        if (declared.length > 0) return declared[0];
        throw new IllegalStateException("No constructor found on " + clazz.getName());
    }

    /**
     * Finds the best-matching method for a given name and argument list.
     *
     * <p>Overload resolution priority (highest to lowest):
     * <ol>
     *   <li>Non-varargs method whose every parameter type is assignable from the
     *       corresponding argument — i.e. a fully compatible exact-arity match.</li>
     *   <li>Any non-varargs method with the right arity (compatibility not checked) —
     *       fallback when types can't be resolved (e.g. argument is a JSObject wrapper
     *       whose underlying type isn't known yet).</li>
     *   <li>Varargs method whose declared parameter count equals the argument count —
     *       i.e. the caller passed exactly the right number including the array slot.</li>
     *   <li>Varargs method that accepts the argument count via spreading.</li>
     * </ol>
     *
     * @param rawArgs the intermediate Object[] produced by {@link #fromJSArgs} — used
     *                for type-scoring only; may contain {@link JSObject} wrappers.
     */
    private static Method findMethod(Class<?> clazz,
                                     String name,
                                     Object[] rawArgs,
                                     boolean isStatic,
                                     JavaObjectRegistry registry) throws NoSuchMethodException {
        int argCount = rawArgs.length;
        // Build a cache key that encodes the actual argument types so that overloads
        // with the same arity but different parameter types (e.g. sendMessage(String)
        // vs sendMessage(Component)) resolve to distinct cache entries.
        String cacheKey = buildMethodCacheKey(clazz, name, rawArgs, isStatic, registry);

        Method cached = METHOD_LOOKUP_CACHE.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        Method bestCompatible = null;
        int bestScore = -1;
        Method anyExact = null;
        Method varargExact = null;
        Method varargFallback = null;

        for (Method m : clazz.getMethods()) {
            if (!m.getName().equals(name)) continue;
            if (Modifier.isStatic(m.getModifiers()) != isStatic) continue;

            if (m.getParameterCount() == argCount && !m.isVarArgs()) {
                int score = compatibilityScore(m.getParameterTypes(), rawArgs, registry);
                if (score >= 0) {
                    // Higher score = better match; keep scanning for the best
                    if (score > bestScore) {
                        bestScore = score;
                        bestCompatible = m;
                    }
                } else {
                    if (anyExact == null) anyExact = m;
                }
            } else if (m.getParameterCount() == argCount && m.isVarArgs()) {
                if (varargExact == null) varargExact = m;
            } else if (m.isVarArgs() && argCount >= m.getParameterCount() - 1) {
                if (varargFallback == null) varargFallback = m;
            }
        }

        Method result = bestCompatible != null ? bestCompatible
                : anyExact != null ? anyExact
                : varargExact != null ? varargExact
                : varargFallback;

        if (result != null) {
            METHOD_LOOKUP_CACHE.put(cacheKey, result);
            return result;
        }
        //Method isn't found throw exception
        throw new NoSuchMethodException((isStatic ? "Static" : "Instance")
                + " method " + clazz.getName() + "." + name
                + "(" + argCount + " args) not found");
    }

    /**
     * Builds a cache key for {@link #METHOD_LOOKUP_CACHE} that encodes the actual
     * resolved Java type of each argument, not just the count.
     *
     * <p>For {@link JSObject} args the registry is consulted to find the underlying
     * Java class (e.g. {@code TextComponentImpl}), so
     * {@code sendMessage(component)} and {@code sendMessage("string")} produce
     * different keys and are cached independently.
     *
     * <p>For args with no known Java type (plain JS objects, unregistered wrappers),
     * the type token {@code "?"} is used, allowing those calls to be cached too —
     * they will always fall back to the "any non-primitive" compatible path.
     */
    private static String buildMethodCacheKey(Class<?> clazz, String name, Object[] rawArgs,
                                              boolean isStatic,
                                              JavaObjectRegistry registry) {
        StringBuilder sb = new StringBuilder(64);
        sb.append(clazz.getName()).append('|').append(name).append('|');
        for (int i = 0; i < rawArgs.length; i++) {
            if (i > 0) sb.append(',');
            Object val = rawArgs[i];
            if (val == null) {
                sb.append("null");
            } else if (val instanceof JSObject jsObj) {
                Object underlying = unwrapJSObject(jsObj, registry);
                sb.append(underlying != null ? underlying.getClass().getName() : "?");
            } else {
                sb.append(val.getClass().getName());
            }
        }
        sb.append('|').append(isStatic ? 'S' : 'I');
        return sb.toString();
    }

    /**
     * Scores how well {@code rawArgs} match {@code paramTypes}.
     *
     * <p>Returns {@code -1} if the args are incompatible with the parameter types.
     * Returns a non-negative integer otherwise — higher is a better match:
     * <ul>
     *   <li>Exact Java type match (or registered Java object matching the target): +2 per arg</li>
     *   <li>Numeric widening to a primitive (e.g. Double → int): +1 per arg</li>
     *   <li>Fallback compatible (e.g. Double → Object, unregistered JSObject): +0 per arg</li>
     * </ul>
     *
     * <p>This scoring is what makes {@code remove(int)} beat {@code remove(Object)}
     * when called with a JS number: the primitive parameter scores +1 while the
     * Object parameter scores +0, so the int overload wins regardless of iteration order.
     */
    private static int compatibilityScore(Class<?>[] paramTypes, Object[] rawArgs,
                                          JavaObjectRegistry registry) {
        int score = 0;
        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> target = paramTypes[i];
            Object val = i < rawArgs.length ? rawArgs[i] : null;
            if (val == null) continue;

            if (val instanceof JSObject jsObj) {
                Object underlying = unwrapJSObject(jsObj, registry);
                if (underlying != null) {
                    if (target == Object.class)             { /* score += 0 */ continue; }
                    if (target.isInstance(underlying))      { score += 2; continue; }
                    return -1;
                }
                if (!target.isPrimitive()) { /* score += 0 */ continue; }
                return -1;
            }

            // Numeric JS values arrive as Double. Primitives score +3 so they beat
            // Object (+0) and boxed Number supertypes (+1). This makes remove(int)
            // win over remove(Object) when called with a numeric argument.
            if (val instanceof Double) {
                if (target == int.class    || target == Integer.class)   { score += 3; continue; }
                if (target == long.class   || target == Long.class)      { score += 3; continue; }
                if (target == double.class || target == Double.class)    { score += 3; continue; }
                if (target == float.class  || target == Float.class)     { score += 3; continue; }
                if (target == short.class  || target == Short.class)     { score += 3; continue; }
                if (target == byte.class   || target == Byte.class)      { score += 3; continue; }
                if (target == char.class   || target == Character.class) { score += 3; continue; }
                if (target == boolean.class || target == Boolean.class)  { score += 3; continue; }
                if (Number.class.isAssignableFrom(target))               { score += 1; continue; }
                if (target == String.class || target == Object.class)    { /* score += 0 */ continue; }
                return -1;
            }
            if (val instanceof String) {
                if (target == String.class)                              { score += 3; continue; }
                if (target == char.class || target == Character.class)   { score += 2; continue; }
                if (target == CharSequence.class)                        { score += 1; continue; }
                if (target == Object.class)                              { /* score += 0 */ continue; }
                return -1;
            }
            if (val instanceof Boolean) {
                if (target == boolean.class || target == Boolean.class)  { score += 3; continue; }
                if (target == Object.class)                              { /* score += 0 */ continue; }
                return -1;
            }

            // Other Java values: penalise Object target
            if (target == Object.class) { /* score += 0 */ continue; }
            if (target.isInstance(val)) { score += 2; continue; }
            return -1;
        }
        return score;
    }

    /** Convenience wrapper used outside findMethod where a boolean is sufficient. */
    private static boolean isCompatible(Class<?>[] paramTypes, Object[] rawArgs,
                                        JavaObjectRegistry registry) {
        return compatibilityScore(paramTypes, rawArgs, registry) >= 0;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Type conversion
    // ─────────────────────────────────────────────────────────────────────────

    static JSValue toJSValue(JSContext context, JavaObjectRegistry registry, Object value) {
        if (value == null)                     return JSNull.INSTANCE;
        if (value instanceof JSValue already)  return already;
        if (value instanceof Boolean b)        return JSBoolean.valueOf(b);
        if (value instanceof Number n)         return JSNumber.of(n.doubleValue());
        if (value instanceof String s)         return new JSString(s);
        if (value instanceof Character c)      return new JSString(String.valueOf(c));
        if (value instanceof boolean[] arr) {
            JSArray out = context.createJSArray(arr.length);
            for (int i = 0; i < arr.length; i++) out.set(i, JSBoolean.valueOf(arr[i]));
            return out;
        }
        if (value.getClass().isArray()) {
            int len = Array.getLength(value);
            JSArray out = context.createJSArray(len);
            for (int i = 0; i < len; i++) out.set(i, toJSValue(context, registry, Array.get(value, i)));
            return out;
        }
        return wrapJavaObject(context, registry, value);
    }

    private static JSValue[] toJSArgs(JSContext context, JavaObjectRegistry registry,
                                      Object[] javaArgs) {
        JSValue[] out = new JSValue[javaArgs.length];
        for (int i = 0; i < javaArgs.length; i++) out[i] = toJSValue(context, registry, javaArgs[i]);
        return out;
    }

    /**
     * Converts JS args to an intermediate Object[] for {@link #coerceOne}.
     *
     * <ul>
     *   <li>{@link JSObject} values are passed through as-is so {@link #coerceOne}
     *       can look them up in {@link JavaObjectRegistry} and recover the original
     *       Java object.</li>
     *   <li>All other {@link JSValue} types are unwrapped to their natural Java
     *       equivalents ({@code String}, {@code Double}, {@code Boolean}, {@code null})
     *       so that {@link #coerceOne} can match them against the target parameter
     *       type without seeing qjs4j-internal wrapper classes.</li>
     * </ul>
     */
    private static Object[] fromJSArgs(JSValue[] args) {
        Object[] out = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            JSValue v = args[i];
            // Keep JSObject as-is — coerceOne handles registry unwrapping
            if (v instanceof JSObject) {
                out[i] = v;
            } else {
                // Unwrap primitives to plain Java types now, before coerceOne sees them
                out[i] = v.toJavaObject(); // String, Double, Boolean, or null
            }
        }
        return out;
    }

    static Object fromJSValue(JSValue value, Class<?> target) {
        if (value == null || value instanceof JSNull || value instanceof JSUndefined) {
            return primitiveDefault(target);
        }
        if (value instanceof JSBoolean b) {
            if (target == boolean.class || target == Boolean.class) return b.value();
            return b.value();
        }
        if (value instanceof JSNumber n) {
            double d = n.value();
            if (target == int.class    || target == Integer.class)   return (int) d;
            if (target == long.class   || target == Long.class)      return (long) d;
            if (target == float.class  || target == Float.class)     return (float) d;
            if (target == double.class || target == Double.class)    return d;
            if (target == short.class  || target == Short.class)     return (short) d;
            if (target == byte.class   || target == Byte.class)      return (byte) d;
            if (target == char.class   || target == Character.class) return (char)(int) d;
            if (target == String.class)                              return Double.toString(d);
            return d;
        }
        if (value instanceof JSString s) {
            if (target == char.class || target == Character.class) {
                return s.value().isEmpty() ? '\0' : s.value().charAt(0);
            }
            return s.value();
        }
        if (value instanceof JSObject obj) return obj.toJavaObject();
        return value.toJavaObject();
    }

    /**
     * Registry-aware variant of {@link #fromJSValue}. When the value is a
     * {@link JSObject} that wraps a real Java object (registered via
     * {@link #wrapJavaObject}), the original Java object is returned directly
     * instead of falling back to {@link JSObject#toJavaObject()} which produces
     * a {@code LinkedHashMap}. Used by {@link #autoProxyFunction} so that lambda
     * return values (e.g. a {@code Component} wrapped after a method call) are
     * correctly unwrapped before being handed back to the Java caller.
     */
    static Object fromJSValue(JSValue value, Class<?> target, JavaObjectRegistry registry) {
        if (value instanceof JSObject obj) {
            Object underlying = registry.unwrap(obj);
            if (underlying != null) return underlying;
            // Fall through to toJavaObject() only if no registry entry exists
            return obj.toJavaObject();
        }
        return fromJSValue(value, target);
    }

    private static Object[] coerce(Object[] args, Class<?>[] types,
                                   JavaObjectRegistry registry) {
        if (types.length == 0) return args;
        boolean varargs = types[types.length - 1].isArray()
                && args.length >= types.length - 1;
        Object[] out = new Object[types.length];

        if (varargs) {
            for (int i = 0; i < types.length - 1; i++) {
                out[i] = coerceOne(i < args.length ? args[i] : null, types[i], registry);
            }
            Class<?> component = types[types.length - 1].getComponentType();
            int extra = args.length - (types.length - 1);
            Object varArr = Array.newInstance(component, Math.max(extra, 0));
            for (int i = 0; i < extra; i++) {
                Array.set(varArr, i, coerceOne(args[types.length - 1 + i], component, registry));
            }
            out[types.length - 1] = varArr;
        } else {
            for (int i = 0; i < types.length; i++) {
                out[i] = coerceOne(i < args.length ? args[i] : null, types[i], registry);
            }
        }
        return out;
    }

    private static Object coerceOne(Object val, Class<?> target, JavaObjectRegistry registry) {
        if (val == null) return null;
        if (target.isInstance(val)) return val;

        // If the target is a functional interface and the value is a JS function,
        // auto-wrap it in a JDK Proxy so the caller never needs an explicit Java.extend().
        // This covers cases like postProcessor(component -> ...) where Adventure or other
        // Java APIs declare parameters as UnaryOperator, Consumer, Supplier, etc.
        if (val instanceof JSFunction jsFn && isFunctionalInterface(target)) {
            return autoProxyFunction(jsFn, target, registry);
        }

        // Unwrap JS-wrapped Java objects back to their original type.
        // Check both exact match and broad Object target.
        if (val instanceof JSObject jsObj) {
            Object underlying = unwrapJSObject(jsObj, registry);
            if (underlying != null) {
                if (target.isInstance(underlying)) return underlying;
                // target is Object or some supertype — return unwrapped regardless
                if (!target.isPrimitive()) return underlying;
            }
            // No registry entry — last-ditch toJavaObject() for broad targets
            if (!target.isPrimitive()) return jsObj.toJavaObject();
        }

        if (val instanceof Double d) {
            if (target == int.class    || target == Integer.class)   return d.intValue();
            if (target == long.class   || target == Long.class)      return d.longValue();
            if (target == float.class  || target == Float.class)     return d.floatValue();
            if (target == short.class  || target == Short.class)     return d.shortValue();
            if (target == byte.class   || target == Byte.class)      return d.byteValue();
            if (target == char.class   || target == Character.class) return (char) d.intValue();
            if (target == boolean.class || target == Boolean.class)  return d != 0.0;
            if (target == String.class)                              return Double.toString(d);
            // target is Object or Number — Double is already a valid Object/Number
            return d;
        }
        if (val instanceof String s) {
            if (target == char.class || target == Character.class) {
                return s.isEmpty() ? '\0' : s.charAt(0);
            }
            // String is already a valid Object — return as-is
            return s;
        }
        if (val instanceof Boolean b) {
            if (target == boolean.class) return b;
            return b;
        }
        return val;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Functional interface auto-proxy
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns true if {@code type} is a functional interface — an interface
     * annotated with {@link FunctionalInterface} OR an interface that declares
     * exactly one abstract method (the structural definition Java uses for lambdas).
     */
    private static boolean isFunctionalInterface(Class<?> type) {
        if (!type.isInterface()) return false;
        // Fast path: explicit annotation
        if (type.isAnnotationPresent(FunctionalInterface.class)) return true;
        // Structural check: exactly one abstract method
        int abstractCount = 0;
        for (Method m : type.getMethods()) {
            if (Modifier.isAbstract(m.getModifiers())) {
                abstractCount++;
                if (abstractCount > 1) return false;
            }
        }
        return abstractCount == 1;
    }

    /**
     * Wraps a {@link JSFunction} in a JDK {@link Proxy} that implements the given
     * functional interface. The single abstract method is routed to the JS function.
     * Incoming Java arguments are wrapped via {@link #toJSArgs} so they're accessible
     * from JS; the return value is coerced back via {@link #fromJSValue}.
     * The registry is captured in the closure so that JS-wrapped Java objects passed
     * as arguments (e.g. captured constants like {@code ITALIC} or {@code FALSE})
     * can be unwrapped correctly when the JS function calls methods on them.
     */
    private static Object autoProxyFunction(JSFunction jsFn, Class<?> functionalInterface,
                                            JavaObjectRegistry registry) {
        Method sam = null;
        for (Method m : functionalInterface.getMethods()) {
            if (Modifier.isAbstract(m.getModifiers())) {
                sam = m;
                break;
            }
        }
        final Method samMethod = sam;
        ClassLoader cl = classLoader(functionalInterface);
        return Proxy.newProxyInstance(cl, new Class<?>[]{ functionalInterface },
                (proxy, method, methodArgs) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        return method.invoke(proxy, methodArgs);
                    }
                    JSContext ctx = jsFn.getRealmContext();
                    // Wrap incoming Java args into JS values using the registry so that
                    // any Java-backed JSObject wrappers passed back into Java calls
                    // (e.g. ITALIC, FALSE captured from the outer script scope) can be
                    // properly unwrapped by coerceOne.
                    JSValue[] jsArgs = methodArgs == null
                            ? JSValue.NO_ARGS
                            : toJSArgs(ctx, registry, methodArgs);
                    JSValue result = jsFn.call(ctx, JSUndefined.INSTANCE, jsArgs);
                    if (ctx.hasPendingException()) {
                        JSValue ex = ctx.getPendingException();
                        ctx.clearPendingException();
                        throw new RuntimeException(
                                "JS exception in functional interface proxy: " + ex);
                    }
                    return fromJSValue(result, samMethod != null
                            ? samMethod.getReturnType() : method.getReturnType(), registry);
                });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Utility
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Unwraps a {@link JSObject} to its underlying Java object via the registry.
     * If the object is a {@link JSProxy}, also tries the proxy's inner target,
     * since qjs4j may surface either the proxy or the target depending on context.
     * Returns {@code null} if no Java object is registered for either.
     */
    private static Object unwrapJSObject(JSObject jsObj, JavaObjectRegistry registry) {
        return registry.unwrap(jsObj);
    }

    private static JSNativeFunction fn(JSContext context, String name, int length,
                                       JSNativeCallback cb) {
        return new JSNativeFunction(context, name, length, cb);
    }

    private static ClassLoader classLoader(Class<?> clazz) {
        ClassLoader cl = clazz.getClassLoader();
        return cl != null ? cl : Thread.currentThread().getContextClassLoader();
    }

    private static boolean isNoisyObjectMethod(Method m) {
        if (m.getDeclaringClass() != Object.class) return false;
        return switch (m.getName()) {
            case "toString", "hashCode", "equals" -> false;
            default -> true;
        };
    }

    /**
     * Invokes {@code mh} with {@code args} using direct typed {@code invoke()} calls
     * for 0–4 arguments to avoid the boxing overhead of
     * {@link MethodHandle#invokeWithArguments}. Falls back for 5+ args.
     */
    private static Object invoke(MethodHandle mh, Object[] args) throws Throwable {
        return switch (args.length) {
            case 0 -> mh.invoke();
            case 1 -> mh.invoke(args[0]);
            case 2 -> mh.invoke(args[0], args[1]);
            case 3 -> mh.invoke(args[0], args[1], args[2]);
            case 4 -> mh.invoke(args[0], args[1], args[2], args[3]);
            default -> mh.invokeWithArguments(args);
        };
    }

    /**
     * Invokes an instance {@link MethodHandle} passing the receiver directly in the
     * first slot, avoiding {@link MethodHandle#bindTo} which allocates a new bound
     * handle object on every call. Covers 0–4 method parameters (1–5 handle slots
     * including the receiver). Falls back for 5+ parameters.
     */
    private static Object invokeWithReceiver(MethodHandle mh, Object receiver,
                                             Object[] args) throws Throwable {
        return switch (args.length) {
            case 0 -> mh.invoke(receiver);
            case 1 -> mh.invoke(receiver, args[0]);
            case 2 -> mh.invoke(receiver, args[0], args[1]);
            case 3 -> mh.invoke(receiver, args[0], args[1], args[2]);
            case 4 -> mh.invoke(receiver, args[0], args[1], args[2], args[3]);
            default -> {
                Object[] full = new Object[args.length + 1];
                full[0] = receiver;
                System.arraycopy(args, 0, full, 1, args.length);
                yield mh.invokeWithArguments(full);
            }
        };
    }

    private static Object[] dummyArgs(Class<?>[] types) {
        Object[] out = new Object[types.length];
        for (int i = 0; i < types.length; i++) out[i] = primitiveDefault(types[i]);
        return out;
    }

    private static Object primitiveDefault(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (type == boolean.class) return false;
        if (type == char.class)    return '\0';
        if (type == void.class)    return null;
        return 0;
    }

    private static Throwable unwrap(InvocationTargetException e) {
        return e.getCause() != null ? e.getCause() : e;
    }
}