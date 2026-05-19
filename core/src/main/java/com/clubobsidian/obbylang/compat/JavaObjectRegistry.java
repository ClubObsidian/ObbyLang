package com.clubobsidian.obbylang.compat;

import com.caoccao.qjs4j.core.JSObject;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;

public final class JavaObjectRegistry {

    private final Cache<IdentityKey<JSObject>, Object> wrapperToJava = Caffeine.newBuilder().maximumSize(10_000)
            .expireAfterAccess(Duration.ofMinutes(1))
            .build();
    private final Cache<IdentityKey<Object>, JSObject> javaToWrapper = Caffeine.newBuilder().maximumSize(10_000)
            .expireAfterAccess(Duration.ofMinutes(1))
            .build();

    public void register(JSObject wrapper, Object javaObj) {

        this.wrapperToJava.put(IdentityKey.of(wrapper), javaObj);
        this.javaToWrapper.put(IdentityKey.of(javaObj), wrapper);
    }

    public JSObject existingWrapper(Object javaObj) {
        return this.javaToWrapper.getIfPresent(IdentityKey.of(javaObj));
    }

    public Object unwrap(JSObject wrapper) {
        return this.wrapperToJava.getIfPresent(IdentityKey.of(wrapper));
    }

    public void clear() {
        this.wrapperToJava.invalidateAll();
        this.javaToWrapper.invalidateAll();
    }
}