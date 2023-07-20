package com.thgplugins.regions.util;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Utility focused towards JVM control & manipulation
 */
public class UtilASM {

    @NotNull
    public static StackWalker.StackFrame getCallerClass(@NotNull Predicate<StackWalker.StackFrame> filter) {
        StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
        Optional<StackWalker.StackFrame> caller = walker.walk(s -> s.skip(1).filter(filter).findFirst());
        return caller.orElseThrow(() -> new IllegalStateException("Unable to find caller class"));
    }
}
