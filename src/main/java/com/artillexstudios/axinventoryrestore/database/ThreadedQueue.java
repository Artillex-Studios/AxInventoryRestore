package com.artillexstudios.axinventoryrestore.database;

import com.artillexstudios.axapi.reflection.FastFieldAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadedQueue<T extends Runnable> extends com.artillexstudios.axapi.data.ThreadedQueue<T> {
    private static final FastFieldAccessor thread = FastFieldAccessor.forClassField(com.artillexstudios.axapi.data.ThreadedQueue.class, "thread");
    private static final Logger log = LoggerFactory.getLogger(ThreadedQueue.class);

    public ThreadedQueue(String threadName) {
        super(threadName);
    }

    @Override
    public void submit(T task) {
        Thread thread = ThreadedQueue.thread.get(this);
        if (Thread.currentThread() == thread) {
            log.error("Running threadedqueue operation from the same thread {}!", thread.getName(), new Throwable());
        }

        super.submit(task);
    }
}
