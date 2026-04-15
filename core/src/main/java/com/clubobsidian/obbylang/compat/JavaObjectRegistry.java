package com.clubobsidian.obbylang.compat;

import com.caoccao.qjs4j.core.JSObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-context registry that keeps live Java objects reachable while a JS wrapper
 * exists for them, provides reverse lookups in both directions, and caches wrappers
 * so the same Java object is never wrapped more than once per context.
 *
 * <p>Three maps are maintained:
 * <ul>
 *   <li>{@code live} — identity-hash → Java object, prevents GC.</li>
 *   <li>{@code wrapperToJava} — JSObject identity → Java object, for unwrapping
 *       JS-wrapped values back to Java at call sites.</li>
 *   <li>{@code javaToWrapper} — Java object identity → JSObject, so that the same
 *       Java object is never wrapped twice. This is the primary performance
 *       optimisation: fluent builder chains that return {@code this} hit this cache
 *       on every call and skip the full {@link NashornJavaCompat#wrapJavaObject}
 *       enumeration.</li>
 * </ul>
 *
 * <p>Call {@link #clear()} when the owning {@link com.caoccao.qjs4j.core.JSContext}
 * is closed to release all references.
 */
public final class JavaObjectRegistry {

    /** identity(javaObj) → javaObj — keeps objects strongly reachable. */
    private final Map<Integer, Object> live = new ConcurrentHashMap<>();

    /** identity(wrapper) → javaObj — used by coerceOne to unwrap. */
    private final Map<Integer, Object> wrapperToJava = new ConcurrentHashMap<>();

    /** identity(javaObj) → wrapper — used by wrapJavaObject to avoid re-wrapping. */
    private final Map<Integer, JSObject> javaToWrapper = new ConcurrentHashMap<>();

    /**
     * Pins {@code javaObj}, records the wrapper↔java mapping in both directions.
     */
    public void register(JSObject wrapper, Object javaObj) {
        int javaId = System.identityHashCode(javaObj);
        int wrapperId = System.identityHashCode(wrapper);
        live.put(javaId, javaObj);
        wrapperToJava.put(wrapperId, javaObj);
        javaToWrapper.put(javaId, wrapper);
    }

    /**
     * Returns the existing {@link JSObject} wrapper for {@code javaObj}, or
     * {@code null} if this object has not been wrapped in this context yet.
     * Used by {@link NashornJavaCompat#wrapJavaObject} to skip re-wrapping.
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
        int javaId = System.identityHashCode(javaObj);
        int wrapperId = System.identityHashCode(wrapper);
        live.remove(javaId);
        wrapperToJava.remove(wrapperId);
        javaToWrapper.remove(javaId);
    }

    /** Releases all objects pinned by this registry. Call on context close. */
    public void clear() {
        live.clear();
        wrapperToJava.clear();
        javaToWrapper.clear();
    }

    /** Returns the number of Java objects currently pinned by this registry. */
    public int size() {
        return live.size();
    }
}