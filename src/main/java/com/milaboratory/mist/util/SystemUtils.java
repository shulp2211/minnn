package com.milaboratory.mist.util;

public class SystemUtils {
    public static void exitWithError(String message) {
        System.err.println(message);
        //System.exit(1);
        throw new IllegalStateException();
    }
}
