package ru.sbt.scalableThreadPool;

import ru.sbt.fixedThreadPool.FixedThreadPool;
import ru.sbt.model.Task;

/**
 * Created by ABurykin on 07.09.2016.
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        ScalableThreadPool pool = new ScalableThreadPool(2,7);
        pool.start();


        //  Thread.currentThread().sleep(2000);

        for (int i = 0; i < 25; i++) {
            Task t = new Task("Задача "+i, 3000L);
            pool.execute(t);
        }

        for (int i = 0; i < 5; i++) {
            Thread.currentThread().sleep(5000);
            pool.printQueue();
        }



       // Thread.currentThread().sleep(20000);
        pool.stop();
    }
}
