package com.clubobsidian.obbylang.compat;

import com.caoccao.qjs4j.core.JSObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-context registry that keeps live Java objects reachable while a JS wrapper
 * exists for them, provides reverse lookups in both directions, and caches wrappers
 * so the same Java object is never wrapped more than once per context.
 *
 * <p>Two maps are maintained:
 * <ul>
 *   <li>{@code wrapperToJava} — JSObject identity → Java object, used by
 *       {@link NashornJavaCompat#coerceOne} to unwrap wrapped values at call sites.</li>
 *   <li>{@code javaToWrapper} — Java object identity → JSObject, used by
 *       {@link NashornJavaCompat#wrapJavaObject} to return the existing wrapper
 *       when the same Java object is encountered again (avoids re-wrapping).</li>
 * </ul>
 *
 * <p>Call {@link #clear()} when the owning {@link com.caoccao.qjs4j.core.JSContext}
 * is closed to release all references.
 */
public final class JavaObjectRegistry {

    /** identity(wrapper) → javaObj — used by coerceOne to unwrap. */
    private final Map<Integer, Object> wrapperToJava = new ConcurrentHashMap<>();

    /** identity(javaObj) → wrapper — used by wrapJavaObject to avoid re-wrapping. */
    private final Map<Integer, JSObject> javaToWrapper = new ConcurrentHashMap<>();

    /**
     * Pins {@code javaObj} and records the wrapper↔java mapping in both directions.
     */
    public void register(JSObject wrapper, Object javaObj) {
        wrapperToJava.put(System.identityHashCode(wrapper), javaObj);
        javaToWrapper.put(System.identityHashCode(javaObj), wrapper);
    }

    /**
     * Returns the existing {@link JSObject} wrapper for {@code javaObj}, or
     * {@code null} if this object has not been wrapped in this context yet.
     */
    public JSObject existingWrapper(Object javaObj) {
        return javaToWrapper.get(System.identityHashCode(javaObj));
    }

    /**
     * Returns the original Java object for {@code wrapper}, or {@code null} if
     * {@code wrapper} was not created by {@link NashornJavaCompat#wrapJavaObject}.
     */
    public Object unwrap(JSObject wrapper) {
        return wrapperToJava.get(System.identityHashCode(wrapper));
    }

    /** Releases a single object and all its mappings. */
    public void unregister(JSObject wrapper, Object javaObj) {
        wrapperToJava.remove(System.identityHashCode(wrapper));
        javaToWrapper.remove(System.identityHashCode(javaObj));
    }

    /** Releases all objects pinned by this registry. Call on context close. */
    public void clear() {
        wrapperToJava.clear();
        javaToWrapper.clear();
    }

    /** Returns the number of Java objects currently pinned by this registry. */
    public int size() {
        return javaToWrapper.size();
    }
}