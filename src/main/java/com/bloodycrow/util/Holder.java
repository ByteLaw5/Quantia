package com.bloodycrow.util;

import java.util.function.Function;

public final class Holder<T> {
    private static final Holder<?> EMPTY = new Holder<>();

    private final T value;
    private final boolean immutable;

    private Holder() {
        this.value = null;
        this.immutable = true;
    }

    private Holder(T t, boolean immutable) {
        value = t;
        this.immutable = immutable;
    }

    public static <T> Holder<T> ofMutable(T value) {
        return new Holder<>(value, false);
    }

    public static <T> Holder<T> ofImmutable(T value) {
        return new Holder<>(value, true);
    }

    public static <T> Holder<T> empty() {
        return (Holder<T>)EMPTY;
    }

    public T getValue() {
        return value;
    }

    public <R> Holder<R> map(Function<? super T, ? extends R> mappingFunction) {
        if(immutable)
            throw new IllegalStateException("Tried to map value on immutable holder");
        else
            return ofMutable(mappingFunction.apply(value));
    }

    public Holder<T> setValue(T value) {
        if(immutable)
            throw new IllegalStateException("Tried to set new value for immutable holder");
        else
            return ofMutable(value);
    }
}
