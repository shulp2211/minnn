package com.milaboratory.mist.util;

public final class SystemUtils {
    public static boolean exitOnError = true;

    public static void exitWithError(String message) {
        if (exitOnError) {
            System.err.println(message);
            System.exit(1);
            throw new IllegalStateException();
        } else
            throw new IllegalStateException(message);
    }
}
