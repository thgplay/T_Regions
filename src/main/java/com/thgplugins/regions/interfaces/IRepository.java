package com.thgplugins.regions.interfaces;

import com.thgplugins.regions.RegionsPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

public interface IRepository {

    void init();
    void createTableIfNotExists();

    default void close() {}

    @Nullable
    default <T> T handleExceptionally(@Nullable Throwable t) {
        RegionsPlugin.getInstance().getLogger().log(Level.WARNING, "Exception Catch in CompletableFuture", t);
        return null;
    }

    default void closeSafely(@NotNull AutoCloseable closeable) {
        try {
            closeable.close();
        } catch (Exception var2) {
            var2.printStackTrace();
        }

    }

}
