package com.clubobsidian.obbylang.compat;

import com.caoccao.qjs4j.core.JSObject;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;

public final class JavaObjectRegistry {

    private final Cache<Integer, Object> wrapperToJava = Caffeine.newBuilder().maximumSize(10_000)
            .expireAfterWrite(Duration.ofMinutes(1))
            .refreshAfterWrite(Duration.ofMinutes(1))
            .build();
    private final Cache<Integer, JSObject> javaToWrapper = Caffeine.newBuilder().maximumSize(10_000)
            .expireAfterWrite(Duration.ofMinutes(1))
            .refreshAfterWrite(Duration.ofMinutes(1))
            .build();

    public void register(JSObject wrapper, Object javaObj) {
        this.wrapperToJava.put(System.identityHashCode(wrapper), javaObj);
        this.javaToWrapper.put(System.identityHashCode(javaObj), wrapper);
    }

    public JSObject existingWrapper(Object javaObj) {
        return this.javaToWrapper.getIfPresent(System.identityHashCode(javaObj));
    }

    public Object unwrap(JSObject wrapper) {
        return this.wrapperToJava.getIfPresent(System.identityHashCode(wrapper));
    }

    public void clear() {
        this.wrapperToJava.invalidateAll();
        this.javaToWrapper.invalidateAll();
    }
}