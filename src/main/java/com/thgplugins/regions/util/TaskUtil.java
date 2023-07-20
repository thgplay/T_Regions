package com.thgplugins.regions.util;

import com.thgplugins.regions.RegionsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public final class TaskUtil {

    private TaskUtil() {
    }

    public static void runTask(@NotNull Runnable runnable) {
        Bukkit.getScheduler().runTask(RegionsPlugin.getInstance(), runnable);
    }

    public static void runTask(long delay, @NotNull Runnable runnable) {
        Bukkit.getScheduler().runTaskLater(RegionsPlugin.getInstance(), runnable, delay);
    }

    @NotNull
    public static BukkitTask runTaskTimer(long delay, long interval, @NotNull Runnable runnable) {
        return Bukkit.getScheduler().runTaskTimer(RegionsPlugin.getInstance(), runnable, delay, interval);
    }

    public static void runAsync(@NotNull Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(RegionsPlugin.getInstance(), runnable);
    }

    public static void runAsync(long delay, @NotNull Runnable runnable) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(RegionsPlugin.getInstance(), runnable, delay);
    }

    @NotNull
    public static BukkitTask runAsyncTimer(long delay, long interval, @NotNull Runnable runnable) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(RegionsPlugin.getInstance(), runnable, delay, interval);
    }

    public static void runSyncSafe(@NotNull Runnable runnable) {
        if (Bukkit.isPrimaryThread()) {
            runnable.run();
        } else {
            TaskUtil.runTask(runnable);
        }
    }

    @NotNull
    public static <T> CompletableFuture<T> runSyncSafe(@NotNull Supplier<T> supplier) {
        if (Bukkit.isPrimaryThread()) {
            try {
                var value = supplier.get();
                return CompletableFuture.completedFuture(value);
            } catch (Exception e) {
                return CompletableFuture.failedFuture(e);
            }
        } else {
            var future = new CompletableFuture<T>();
            TaskUtil.runTask(() -> {
                try {
                    var value = supplier.get();
                    future.complete(value);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            });

            return future;
        }
    }

}
