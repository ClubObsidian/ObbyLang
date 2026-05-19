package com.clubobsidian.obbylang.compat;

final class IdentityKey<T> {

    static <T> IdentityKey<T> of(T ref) {
        return new IdentityKey<>(ref);
    }

    private final T ref;
    private final int hash;
    private IdentityKey(T ref) {
        this.ref = ref;
        this.hash = System.identityHashCode(ref);
    }
    @Override
    public int hashCode() {
        return this.hash;
    }
    @Override
    public boolean equals(Object o) {
        return o instanceof IdentityKey<?> other && this.ref == other.ref;
    }
}