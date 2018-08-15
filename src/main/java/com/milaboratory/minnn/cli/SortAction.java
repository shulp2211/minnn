/*
 * Copyright (c) 2016-2018, MiLaboratory LLC
 * All Rights Reserved
 *
 * Permission to use, copy, modify and distribute any part of this program for
 * educational, research and non-profit purposes, by non-profit institutions
 * only, without fee, and without a written agreement is hereby granted,
 * provided that the above copyright notice, this paragraph and the following
 * three paragraphs appear in all copies.
 *
 * Those desiring to incorporate this work into commercial products or use for
 * commercial purposes should contact MiLaboratory LLC, which owns exclusive
 * rights for distribution of this program for commercial purposes, using the
 * following email address: licensing@milaboratory.com.
 *
 * IN NO EVENT SHALL THE INVENTORS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 * ARISING OUT OF THE USE OF THIS SOFTWARE, EVEN IF THE INVENTORS HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE SOFTWARE PROVIDED HEREIN IS ON AN "AS IS" BASIS, AND THE INVENTORS HAS
 * NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
 * MODIFICATIONS. THE INVENTORS MAKES NO REPRESENTATIONS AND EXTENDS NO
 * WARRANTIES OF ANY KIND, EITHER IMPLIED OR EXPRESS, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A
 * PARTICULAR PURPOSE, OR THAT THE USE OF THE SOFTWARE WILL NOT INFRINGE ANY
 * PATENT, TRADEMARK OR OTHER RIGHTS.
 */
package com.milaboratory.minnn.cli;

import com.beust.jcommander.*;
import com.milaboratory.cli.Action;
import com.milaboratory.cli.ActionHelper;
import com.milaboratory.cli.ActionParameters;
import com.milaboratory.minnn.io.SorterIO;

import java.util.*;

public final class SortAction implements Action {
    public static final String commandName = "sort";
    private final SortActionParameters params = new SortActionParameters();

    @Override
    public void go(ActionHelper helper) {
        SorterIO sorterIO = new SorterIO(params.inputFileName, params.outputFileName, params.sortGroupNames,
                params.chunkSize, params.suppressWarnings, params.tmpFile);
        sorterIO.go();
    }

    @Override
    public String command() {
        return commandName;
    }

    @Override
    public ActionParameters params() {
        return params;
    }

    @Parameters(commandDescription =
            "Sort reads by contents (nucleotide sequences) of specified groups.")
    private static final class SortActionParameters extends ActionParameters {
        @Parameter(description = "--groups <group_names>", order = 0)
        private String description;

        @Parameter(description = "Group names to use for sorting. Priority is in descending order.",
                names = {"--groups"}, order = 1, required = true, variableArity = true)
        List<String> sortGroupNames = null;

        @Parameter(description = "Input file in \"mif\" format. If not specified, stdin will be used.",
                names = {"--input"}, order = 2)
        String inputFileName = null;

        @Parameter(description = "Output file in \"mif\" format. If not specified, stdout will be used.",
                names = {"--output"}, order = 3)
        String outputFileName = null;

        @Parameter(description = "Chunk size for sorter.",
                names = {"--chunk-size"}, order = 4)
        int chunkSize = -1;

        @Parameter(description = "Don't display any warnings.",
                names = {"--suppress-warnings"}, order = 5)
        boolean suppressWarnings = false;

        @Parameter(description = "Custom temp file, used for debugging purposes.",
                names = {"--temp-file"}, hidden = true)
        String tmpFile = null;

        @Override
        public void validate() {
            if (sortGroupNames.size() == 0)
                throw new ParameterException("Sorting groups are not specified!");
        }
    }
}
