package ru.sbt.threadPoolInClassWork;


import ru.sbt.model.ThreadPool;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.ArrayDeque;

/**
 * Created by ABurykin on 06.09.2016.
 */

// Реализация ThreadPoolImplЮ в котором есть 3 потока исполнителя, они лазают по очереди и выполняют задачи пока они там есть, иначе засыпают и жду задач.
public class ThreadPoolImpl implements ThreadPool {

    private final Object lock = new Object();
    private final List<Worker> workers = new ArrayList<Worker>();
    private final Queue<Runnable> tasks = new ArrayDeque<Runnable>();

    public void start() {
        for (int i = 0; i < 3; i++) {
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


    public void stop() {
        System.out.println("Stop workers");
        synchronized (lock) {
            for (Worker worker : workers) {
                worker.setExit(true);
                lock.notify();
            }

        }
    }


    public class Worker extends Thread { // Исполнитель задач

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
                        }
                        if (isExit()) Thread.currentThread().interrupt();
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
}


