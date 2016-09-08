package ru.sbt.threadPoolInClassWork;


import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import ru.sbt.model.ThreadPool;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.ArrayDeque;

/**
 * Created by ABurykin on 06.09.2016.
 */

// Реализация ThreadPoolImplЮ в котором есть 3 потока исполнителя, они лазают по очереди и выполняют задачи пока они там есть, иначе засыпают и ждут задач.
public class ThreadPoolImpl implements ThreadPool {

    private final Object lock = new Object();
    private final List<Worker> workers = new ArrayList<Worker>();
    private final Queue<Runnable> tasks = new ArrayDeque<Runnable>();

    public void start() {
        for (int i = 0; i < 3; i++) {
            Worker newWorker = new Worker();
            newWorker.setName("Worker_" + i);
            addWorker(newWorker);
            newWorker.start();
        }
    }

    public void execute(Runnable runnable) {
        synchronized (tasks) {
            tasks.add(runnable);
        }
        synchronized (lock) {
            lock.notifyAll();
        }
    }


    public void addWorker(Worker worker) {
        synchronized (workers) {
            workers.add(worker);
        }
    }

    public void removeWorker(Worker worker) {
        synchronized (workers) {
            workers.remove(worker);
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
        System.out.println("Stop workers");
        synchronized (workers) {
            for (Worker worker : workers) {
                worker.setExit(true);
                synchronized (lock) {
                    lock.notifyAll();
                }
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

                    Runnable task = null;
                    boolean tasksEmpty;
                    synchronized (tasks) {
                        tasksEmpty = tasks.isEmpty();
                        if (!tasks.isEmpty())
                            task = tasks.poll();
                    }

                    if (task != null) {
                        try {

                            System.out.print(this.getName() + " начал выполнять задачу: ");
                            task.run();
                        } catch (Exception e) {
                            throw new RuntimeException("Задача " + task + " выбросила ошибку " + e);
                        }
                    }
                    synchronized (lock) {
                        if (tasksEmpty) {
                            System.out.println(this.getName() + " засыпает");
                            lock.wait();
                            System.out.println(this.getName() + " просыпается");
                        }
                        if (isExit()) Thread.currentThread().interrupt(); // останавливает поток без выброса ошибок
                    }

                }
            } catch (InterruptedException e) {
                throw new RuntimeException(this.getName() + " выбросил ошибку " + e);
            }
            System.out.println(this.getName() + " завершен");
        }
    }
}


