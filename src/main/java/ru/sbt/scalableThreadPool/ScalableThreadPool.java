package ru.sbt.scalableThreadPool;

import ru.sbt.model.ThreadPool;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ABurykin on 07.09.2016.
 */
public class ScalableThreadPool implements ThreadPool {

    private final Object lock = new Object();
    private final List<Worker> workers = new ArrayList<Worker>();
    private final Queue<Runnable> tasks = new ArrayDeque<Runnable>();
    private AtomicInteger countThreads = new AtomicInteger(0);
    private AtomicInteger minThreads;
    private AtomicInteger maxThreads;

    ScalableThreadPool(int min, int max) {
        this.minThreads = new AtomicInteger(min);
        this.maxThreads = new AtomicInteger(max);
    }

    public void start() {
        for (int i = 0; i < minThreads.get(); i++) addWorker("Добавление до минимума");
    }

    public void addWorker(String message) {
        System.out.println(message);
        if (countThreads.get() <= maxThreads.get()) {
            Worker x = new Worker();
            x.setName("Worker_" + countThreads.get());
            workers.add(x);
            x.start();
            countThreads.incrementAndGet();
        }
    }

    public void removeWorker(String message) {
        System.out.println(message);
        if (countThreads.get() > minThreads.get()) {
            synchronized (lock) {
                for (Worker worker : workers) {
                    worker.setExit(true);
                    countThreads.decrementAndGet();
                    worker.interrupt();
                    lock.notify();
                    break;
                }
            }
        }
    }


    public void execute(Runnable runnable) {
        tasks.add(runnable);
        synchronized (lock) {
            lock.notifyAll();
        }
        if (tasks.size() > minThreads.get() && tasks.size() < maxThreads.get())
            addWorker("Добавление из-за переполнения, т.к. задача больше чем минимальное число потоков исполнителей.");
    }

    public void printQueue() {
        System.out.println("printQueue :");
        int count = 1;
        for (Runnable task : tasks) {
            System.out.println(count + " " + task);
            count++;
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

                            if (tasks.size() > minThreads.get() && tasks.size() <= maxThreads.get())
                                removeWorker("Поток удаляется из-за ненадобности");
                            else lock.wait();

                            System.out.println(this.getName() + " просыпается");
                            if (isExit()) Thread.currentThread().interrupt();
                        }
                    }


                    if (!tasks.isEmpty()) {
                        Runnable task = tasks.poll();
                        try {
                            task.run();
                        } catch (Exception e) {
                            throw new RuntimeException("Задача " + task + " выбросила ошибку " + e);
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
        for (Worker worker : workers) {
            removeWorker("Удаление перед завершением программы");
        }
    }

}
