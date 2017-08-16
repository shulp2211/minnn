package com.milaboratory.mist.util;

public final class SystemUtils {
    public static boolean exitOnError = true;

    public static RuntimeException exitWithError(String message) {
        if (exitOnError) {
            System.err.println(message);
            System.exit(1);
            throw new RuntimeException();
        } else
            throw new RuntimeException(message);
    }
}
