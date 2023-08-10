package com.artillexstudios.axinventoryrestore.database;

import com.google.common.collect.Queues;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DatabaseQueue implements Runnable {
    private final Queue<Runnable> handlingQueue = Queues.newArrayDeque();
    private final Thread thread;
    private final Lock lock = new ReentrantLock();
    private final Condition cond = lock.newCondition();
    private volatile boolean kill = false;

    public DatabaseQueue(@NotNull String name) {
        this.thread = new Thread(this, name);
        this.start();
    }

    public void kill() {
        this.kill = true;

        synchronized (lock) {
            cond.signalAll();
        }
    }

    public void submit(@NotNull Runnable runnable) {
        lock.lock();
        try {
            handlingQueue.offer(runnable);
            cond.signalAll();
        } finally {
            lock.unlock();
        }
    }

    private void start() {
        this.thread.start();
    }

    @Override
    public void run() {
        while (!kill) {
            try {
                Runnable next = getNext();
                if (next != null) {
                    next.run();
                }
            } catch (InterruptedException exception) {
                exception.printStackTrace();
            }
        }
    }

    public Runnable getNext() throws InterruptedException {
        lock.lock();
        try {
            while (handlingQueue.isEmpty() && !kill) {
                cond.await();
            }

            if (handlingQueue.isEmpty()) return null;

            return handlingQueue.remove();
        } finally {
            lock.unlock();
        }
    }
}
