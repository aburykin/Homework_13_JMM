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
    private AtomicInteger minThreads;
    private AtomicInteger maxThreads;

    ScalableThreadPool(int min, int max) {
        this.minThreads = new AtomicInteger(min);
        this.maxThreads = new AtomicInteger(max);
    }

    public void start() {
        for (int i = 0; i < minThreads.get(); i++) {
            addWorker();
        }
    }

    public void execute(Runnable runnable) {
        synchronized (tasks) {
            tasks.add(runnable);
            synchronized (workers) {
                if (workers.size() < tasks.size()) addWorker();
            }
        }
        synchronized (lock) {
            lock.notify();
        }
    }


    public void addWorker() {
        synchronized (workers) {
            if (workers.size() < maxThreads.get()) {
                Worker newWorker = new Worker();
                workers.add(newWorker);
                System.out.println("Добавляем исполнителя " + newWorker.getName());
                newWorker.start();
            }
        }
    }

    public void removeWorker(Worker worker) {
        synchronized (workers) {
            if (workers.size() > minThreads.get()) {
                System.out.println("Удаляем исполнителя " + worker.getName());
                workers.remove(worker);
                worker.interrupt();
            }
        }
    }


    public void printQueue() {
        System.out.println("\n Печатаем очередь заданий:");
        synchronized (tasks) {
            for (Runnable task : tasks) System.out.println(task);
        }
        System.out.println("\n");
    }


    public void stop() {
        synchronized (workers) {
            System.out.println("Stop workers, на текущий момент потоков не завершено: " + workers.size());
            for (Worker worker : workers) {
                worker.interrupt();
            }
        }

        synchronized (lock) {
            System.out.println("В итоге в конце смогли проснуться потоки:");
            lock.notifyAll();
        }
    }


    public class Worker extends Thread { // Исполнитель задач
        @Override
        public void run() {
            System.out.println(this.getName() + " запущен");
            try {
                while (!this.isInterrupted()) {
                    Runnable task = null;
                    synchronized (tasks) {
                        if (!tasks.isEmpty()) task = tasks.poll();
                        else {
                            synchronized (workers) {
                                if (workers.size() > tasks.size()) {
                                    removeWorker(this);
                                    continue;
                                }
                            }
                        }
                    }

                    if (task != null) {
                        try {
                            System.out.print(this.getName() + " начал выполнять задачу: ");
                            task.run();
                        } catch (Exception e) {
                            throw new RuntimeException("Задача " + task + " завершилась с ошибкой: " + e);
                        }
                    } else {
                        synchronized (lock) {
                            System.out.println(this.getName() + " засыпает");
                            lock.wait();
                            System.out.println(this.getName() + " просыпается");
                        }
                    }
                }
            } catch (InterruptedException e) {
                System.out.println(this.getName() + " завершен через InterruptedException" + e);
            }
            System.out.println(this.getName() + " завершен.");
        }
    }

}
