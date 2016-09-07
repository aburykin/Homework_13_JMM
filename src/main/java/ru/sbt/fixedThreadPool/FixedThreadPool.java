package ru.sbt.fixedThreadPool;

import ru.sbt.model.ThreadPool;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Created by ABurykin on 07.09.2016.
 */
public class FixedThreadPool implements ThreadPool {

    private final Object lock = new Object();
    private final List<Worker> workers = new ArrayList<Worker>();
    private final Queue<Runnable> tasks = new ArrayDeque<Runnable>();
    private int countThreads;

    FixedThreadPool(int countThreads) {
        this.countThreads = countThreads;
    }

    public void start() {
        for (int i = 0; i < countThreads; i++) {
            Worker x = new Worker();
            x.setName("Worker_" + i);
            workers.add(x);
            x.start();
        }
    }

    public void execute(Runnable runnable) {
        tasks.add(runnable);
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    public void printQueue() {
        System.out.println("printQueue :");
        for (Runnable task : tasks) {
            System.out.println(task);
        }
        System.out.println("\n");
    }


    private class Worker extends Thread { // Исполнитель задач

        private boolean exit = false;

        public boolean isExit() {
            return exit;
        }

        public void setExit(boolean exit) {
            this.exit = exit;
        }

        @Override
        public void run() {
            System.out.println(this.getName() + " запущен");
            try {
                while (!this.isInterrupted()) {
                    synchronized (lock) {
                        if (tasks.isEmpty()) {
                            System.out.println(this.getName() + " засыпает");
                            lock.wait();
                            System.out.println(this.getName() + " просыпается");
                            if (isExit()) Thread.currentThread().interrupt();
                        }
                    }

                    if (!tasks.isEmpty()) {
                        Runnable task = tasks.poll();
                        try {
                            task.run();
                        } catch (Exception e) {
                            throw new RuntimeException("Задача" + task + " выбросила ошибку " + e);
                        }
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(this.getName() + " выбросил ошибку " + e);
            }
            System.out.println(this.getName() + " завершен");
        }
    }

    public void stop() {
        System.out.println("Stop workers");
        synchronized (lock) {
            for (Worker worker : workers) {
                worker.setExit(true);
                lock.notify();
            }

        }
    }
}
