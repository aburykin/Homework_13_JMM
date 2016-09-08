package ru.sbt.scalableThreadPool;

import ru.sbt.fixedThreadPool.FixedThreadPool;
import ru.sbt.model.Task;

/**
 * Created by ABurykin on 07.09.2016.
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        ScalableThreadPool pool = new ScalableThreadPool(2,5);
        pool.start();

        for (int i = 0; i < 13; i++) {
            Task t = new Task("Задача "+i, 1000L);
            pool.execute(t);
        }

        for (int i = 0; i < 3; i++) {
            Thread.currentThread().sleep(2000);
            pool.printQueue();
        }



        Thread.currentThread().sleep(1000);
        pool.stop();
    }
}
