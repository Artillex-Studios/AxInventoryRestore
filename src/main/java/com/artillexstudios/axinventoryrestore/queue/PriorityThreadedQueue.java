package com.artillexstudios.axinventoryrestore.queue;

import com.google.common.collect.Queues;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public final class PriorityThreadedQueue<T extends Runnable> implements Runnable, Executor {
    private static final Logger log = LoggerFactory.getLogger(PriorityThreadedQueue.class);
    private final ArrayDeque<T> jobs = Queues.newArrayDeque();
    private final Thread thread;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private volatile boolean killed = false;

    public PriorityThreadedQueue(String threadName) {
        this.thread = new Thread(this, threadName);
        thread.start();
    }

    public void stop() {
        killed = true;

        lock.lock();
        try {
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void submit(T task) {
        submit(task, Priority.LOW);
    }

    public void submit(T task, Priority priority) {
        lock.lock();
        try {
            if (priority == Priority.LOW) {
                jobs.offer(task);
            } else if (priority == Priority.HIGH) {
                jobs.offerFirst(task);
            }
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void run() {
        while (!killed) {
            try {
                T next = next();

                if (next != null) {
                    next.run();
                }
            } catch (Exception exception) {
                log.error("An unexpected error occurred while running ThreadedQueue {}!", thread.getName(), exception);
            }
        }
    }

    public T next() throws InterruptedException {
        lock.lock();
        try {
            while (jobs.isEmpty() && !killed) {
                condition.await();
            }

            if (jobs.isEmpty()) {
                return null;
            }

            return jobs.remove();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void execute(@NotNull Runnable command) {
        submit((T) command);
    }
}
