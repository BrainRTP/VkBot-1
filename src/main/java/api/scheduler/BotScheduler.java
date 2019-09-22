package api.scheduler;

import api.utils.TimingsHandler;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class BotScheduler {
    private final Executor executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().build());
    private final BlockingQueue<Task> taskQueue = new LinkedBlockingQueue<>();
    private final TimingsHandler timingsHandler = new TimingsHandler();

   public final void mainThreadHeartbeat() {
        timingsHandler.tick();
        Iterator<Task> iterator = taskQueue.iterator();
        while (iterator.hasNext()) {
            Task task = iterator.next();
            if (task.getCurrentTick() >= task.getPeriod()) {
                if (task.isSync()) {
                    task.run();
                } else {
                    executor.execute(task);
                }
                if (task.getPeriod() <= Task.NO_REPEATING) {
                    iterator.remove();
                    System.out.println("удалил задачу: " + taskQueue);
                    return;
                }
                task.setCurrentTickZero();
            }
            task.addCurrentTick();
        }
    }
    @Contract(pure = true)
    public final int getAverageTPS() {
        return timingsHandler.getAverageTPS();
    }

    @NotNull
    public final Task runTask(Runnable runnable) {
        return sync(runnable, Task.NO_REPEATING, Task.NO_REPEATING);
    }

    @NotNull
    public final Task runTask(Consumer<Task> consumer) {
        return sync(consumer, Task.NO_REPEATING, Task.NO_REPEATING);
    }

    @NotNull
    public final Task scheduleSyncRepeatingTask(Runnable runnable, int delay, int period) {
        return sync(runnable, delay, period);
    }

    @NotNull
    public final Task scheduleSyncRepeatingTask(Consumer<Task> consumer, int delay, int period) {
        return sync(consumer, delay, period);
    }

    @NotNull
    public final Task scheduleSyncDelayTask(Runnable runnable, int delay) {
        return sync(runnable, delay, Task.NO_REPEATING);
    }

    @NotNull
    public final Task scheduleSyncDelayTask(Consumer<Task> consumer, int delay) {
        return sync(consumer, delay, Task.NO_REPEATING);
    }

    @NotNull
    public final Task runTaskAsynchronously(Runnable runnable) {
        return async(runnable, Task.NO_REPEATING, Task.NO_REPEATING);
    }
    @NotNull
    public final Task runTaskAsynchronously(Consumer<Task> consumer) {
        return async(consumer, Task.NO_REPEATING, Task.NO_REPEATING);
    }
    @Deprecated
    @NotNull
    public final Task scheduleAsyncRepeatingTask(Runnable runnable, int delay, int period) {
        return async(runnable, delay, period);
    }
    @NotNull
    public final Task scheduleAsyncRepeatingTask(Consumer<Task> consumer, int delay, int period) {
        return async(consumer, delay, period);
    }
    @NotNull
    public final Task scheduleAsyncDelayTask(Runnable runnable, int delay) {
        return async(runnable, delay, Task.NO_REPEATING);
    }
    @NotNull
    public final Task scheduleAsyncDelayTask(Consumer<Task> consumer, int delay) {
        return async(consumer, delay, Task.NO_REPEATING);
    }

    public final void cancel(Task task) {
        taskQueue.remove(task);
    }

    public final void cancelTasks() {
        taskQueue.clear();
    }

    @NotNull
    private Task sync(Object o, int delay, int period) {
        Task task = new Task(o, delay, period);
        taskQueue.add(task);
        return task;
    }

    @NotNull
    private Task async(Object o, int delay, int period) {
        TaskAsync task = new TaskAsync(o, delay, period);
        taskQueue.add(task);
        return task;
    }
}