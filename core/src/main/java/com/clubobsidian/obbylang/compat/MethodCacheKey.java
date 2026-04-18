package com.clubobsidian.obbylang.compat;

import java.util.Objects;

public class MethodCacheKey {

    private final Class<?> clazz;
    private final String name;
    private final Object[] rawArgs;
    private final boolean isStatic;

    public MethodCacheKey(Class<?> clazz, String name, Object[] rawArgs, boolean isStatic) {
        this.clazz = clazz;
        this.name = name;
        this.rawArgs = rawArgs;
        this.isStatic = isStatic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MethodCacheKey that = (MethodCacheKey) o;
        return this.isStatic == that.isStatic
                && this.clazz.getName().equals(that.clazz.getName())
                && this.name.equals(that.name)
                && arraysEquals(this.rawArgs, that.rawArgs);
    }

    private boolean arraysEquals(Object[] thisArgs, Object[] thatArgs) {
        if (thisArgs.length != thatArgs.length) {
            return false;
        }
        for (int i = 0; i < thisArgs.length; i++) {
            if (thisArgs[i] == null && thatArgs[i] != null) {
                return false;
            }
            if (thisArgs[i] == null && thatArgs[i] == null) {
                continue;
            }
            if (!thisArgs[i].getClass().getName().equals(thatArgs[i].getClass().getName())) {
                return false;
            }
        }
        return true;
    }

    private int arrayHashCode(Object[] args) {
        int hash = 0;
        for (Object obj : args) {
            hash += obj.getClass().getName().hashCode();
        }
        return hash;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.clazz.getName(), this.name, arrayHashCode(this.rawArgs), this.isStatic);
    }
}
