package com.milaboratory.mist.cli;

import static com.milaboratory.mist.Main.main;

class CommandLineTestUtils {
    static void exec(String cmdLine) throws Exception {
        main(cmdLine.split("[ ]+(?=([^\"]*\"[^\"]*\")*[^\"]*$)"));
    }
}
