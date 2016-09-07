package ru.sbt.fixedThreadPool;

import ru.sbt.model.Task;
import ru.sbt.threadPoolInClassWork.ThreadPoolImpl;

/**
 * Created by ABurykin on 07.09.2016.
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        FixedThreadPool pool = new FixedThreadPool(3);
        pool.start();


        //  Thread.currentThread().sleep(2000);

        Task t1 = new Task("Задача 1", 2000L);
        Task t2 = new Task("Задача 2", 2000L);
        Task t3 = new Task("Задача 3", 2000L);
        Task t4 = new Task("Задача 4", 2000L);
        Task t5 = new Task("Задача 5", 2000L);
        Task t6 = new Task("Задача 6", 2000L);
        Task t7 = new Task("Задача 7", 2000L);

        pool.execute(t1);
        pool.execute(t2);
        pool.execute(t3);
        pool.execute(t4);
        pool.execute(t3);
        pool.execute(t5);
        pool.execute(t6);
        pool.execute(t7);

        // Thread.currentThread().sleep(3000);
        // pool.printQueue();

        Thread.currentThread().sleep(20000);
        pool.stop();
    }
}
