package com.asemenkov.utils;

/**
 * @author asemenkov
 * @since Feb 12, 2018
 */
public class Factories {

    @FunctionalInterface
    public interface MonoFactory<R, T1> {
        R get(T1 t1);
    }

    @FunctionalInterface
    public interface DuoFactory<R, T1, T2> {
        R get(T1 t1, T2 t2);
    }

    @FunctionalInterface
    public interface TriFactory<R, T1, T2, T3> {
        R get(T1 t1, T2 t2, T3 t3);
    }

    @FunctionalInterface
    public interface TetraFactory<R, T1, T2, T3, T4> {
        R get(T1 t1, T2 t2, T3 t3, T4 t4);
    }

    @FunctionalInterface
    public interface PentaFactory<R, T1, T2, T3, T4, T5> {
        R get(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5);
    }

}
