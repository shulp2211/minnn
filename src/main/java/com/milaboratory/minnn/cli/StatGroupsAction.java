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
import com.milaboratory.minnn.io.StatGroupsIO;

import java.util.*;

import static com.milaboratory.minnn.cli.CliUtils.*;

public final class StatGroupsAction implements Action {
    public static final String commandName = "stat-groups";
    private final StatGroupsActionParameters params = new StatGroupsActionParameters();

    @Override
    public void go(ActionHelper helper) {
        StatGroupsIO statGroupsIO = new StatGroupsIO(params.groupList, params.inputFileName, params.outputFileName,
                params.inputReadsLimit, (byte)(params.readQualityFilter), (byte)(params.minQualityFilter),
                (byte)(params.avgQualityFilter), params.minCountFilter, params.minFracFilter);
        statGroupsIO.go();
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
            "Collect summary statistics: capture group sequence and quality table.")
    private static final class StatGroupsActionParameters extends ActionParameters {
        @Parameter(description = "--groups <group_names>", order = 0)
        private String description;

        @Parameter(description = "Space separated list of groups to output, determines the keys by which the output " +
                "table will be aggregated.",
                names = {"--groups"}, order = 1, required = true, variableArity = true)
        List<String> groupList = null;

        @Parameter(description = "Input file in \"mif\" format. If not specified, stdin will be used.",
                names = {"--input"}, order = 2)
        String inputFileName = null;

        @Parameter(description = "Output text file. If not specified, stdout will be used.",
                names = {"--output"}, order = 3)
        String outputFileName = null;

        @Parameter(description = "Filter group values with a min (non-aggregated) quality below a given threshold, " +
                "applied on by-read basis, should be applied prior to any aggregation. 0 value means no threshold.",
                names = {"--read-quality-filter"}, order = 4)
        int readQualityFilter = 0;

        @Parameter(description = "Filter group values based on min aggregated quality. 0 value means no filtering.",
                names = {"--min-quality-filter"}, order = 5)
        int minQualityFilter = 0;

        @Parameter(description = "Filter group values based on average aggregated quality. 0 value means no filtering.",
                names = {"--avg-quality-filter"}, order = 6)
        int avgQualityFilter = 0;

        @Parameter(description = "Filter unique group values represented by less than specified number of reads.",
                names = {"--min-count-filter"}, order = 7)
        int minCountFilter = 0;

        @Parameter(description = "Filter unique group values represented by less than specified fraction of reads.",
                names = {"--min-frac-filter"}, order = 8)
        float minFracFilter = 0;

        @Parameter(description = "Number of reads to take; 0 value means to take the entire input file.",
                names = {"-n", "--number-of-reads"}, order = 9)
        long inputReadsLimit = 0;

        @Override
        public void validate() {
            if (groupList.size() == 0)
                throw new ParameterException("List of output groups is not specified!");
            validateQuality(readQualityFilter);
            validateQuality(minQualityFilter);
            validateQuality(avgQualityFilter);
        }
    }
}
