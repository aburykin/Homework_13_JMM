package ru.sbt.model;

/**
 * Created by ABurykin on 07.09.2016.
 */
public class Task implements Runnable{
    private String name;
    private long time;

    public Task(String name, long time) {
        this.name = name;
        this.time = time;
    }

    public void run() {
        try {
            System.out.println("- " + name + " выполняется " + time%1000 +" секунд");
            Thread.currentThread().sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
