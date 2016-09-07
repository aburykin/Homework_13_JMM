package ru.sbt.model;

/**
 * Created by ABurykin on 06.09.2016.
 */
public interface ThreadPool {
    void start();

    void execute(Runnable runnable);
}
