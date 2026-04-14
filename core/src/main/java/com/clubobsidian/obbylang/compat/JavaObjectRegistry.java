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

package com.clubobsidian.obbylang.compat;

import com.caoccao.qjs4j.core.JSObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-context registry that keeps live Java objects reachable while a JS wrapper
 * exists for them, and provides the reverse lookup needed to unwrap them.
 *
 * <p>One instance is created per {@link com.caoccao.qjs4j.core.JSContext} by
 * {@link NashornJavaCompat#install} and passed through the interop layer. When
 * the context is closed, call {@link #clear()} to release all pinned objects.
 * Since the registry is not static, closing one context never affects objects
 * belonging to another.
 *
 * <p>Two maps are maintained:
 * <ul>
 *   <li>{@code live} — identity-hash-keyed, prevents GC of pinned Java objects.</li>
 *   <li>{@code wrapperToJava} — JSObject identity → original Java object, used
 *       by {@link NashornJavaCompat} to unwrap arguments when passing a JS-wrapped
 *       Java object into a Java constructor or method that expects the real type.</li>
 * </ul>
 */
public final class JavaObjectRegistry {

    private final Map<Integer, Object> live = new ConcurrentHashMap<>();
    private final Map<Integer, Object> wrapperToJava = new ConcurrentHashMap<>();

    /** Pins {@code javaObj} and records that {@code wrapper} is its JS representation. */
    public void register(JSObject wrapper, Object javaObj) {
        live.put(System.identityHashCode(javaObj), javaObj);
        wrapperToJava.put(System.identityHashCode(wrapper), javaObj);
    }

    /**
     * Returns the original Java object for {@code wrapper}, or {@code null} if
     * {@code wrapper} was not created by {@link NashornJavaCompat#wrapJavaObject}.
     */
    public Object unwrap(JSObject wrapper) {
        return wrapperToJava.get(System.identityHashCode(wrapper));
    }

    /** Releases a single object and its wrapper mapping. */
    public void unregister(JSObject wrapper, Object javaObj) {
        live.remove(System.identityHashCode(javaObj));
        wrapperToJava.remove(System.identityHashCode(wrapper));
    }

    /**
     * Releases all objects pinned by this registry.
     * Call this when the owning {@link com.caoccao.qjs4j.core.JSContext} is closed.
     */
    public void clear() {
        live.clear();
        wrapperToJava.clear();
    }

    /** Returns the number of Java objects currently pinned by this registry. */
    public int size() {
        return live.size();
    }
}