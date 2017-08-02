package com.milaboratory.mist.util;

import java.util.HashMap;
import java.util.concurrent.Callable;

public final class DebugUtils {
    public static HashMap<String, Long> timeCounter = new HashMap<>();
    public static HashMap<String, Long> callCounter = new HashMap<>();

    public static void printExecutionTime(String label, Callable<Void> f) {
        long startTime = System.currentTimeMillis();
        try {
            f.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        long elapsedTime = System.currentTimeMillis() - startTime;
        System.out.println(label + ": " + elapsedTime + " ms");
    }

    public static void countExecutionTime(String label, Callable<Void> f) {
        long startTime = System.currentTimeMillis();
        try {
            f.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        long elapsedTime = System.currentTimeMillis() - startTime;
        Long totalElapsedTime = timeCounter.get(label);
        timeCounter.put(label, (totalElapsedTime == null) ? elapsedTime : totalElapsedTime + elapsedTime);
    }

    public static void countCall(String label) {
        Long count = callCounter.get(label);
        callCounter.put(label, (count == null) ? 1 : count + 1);
    }

    public static void resetTimeCounter() {
        timeCounter = new HashMap<>();
    }

    public static void resetCallCounter() {
        callCounter = new HashMap<>();
    }
}
