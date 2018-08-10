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
package com.milaboratory.mist.cli;

import com.beust.jcommander.*;
import com.milaboratory.cli.Action;
import com.milaboratory.cli.ActionHelper;
import com.milaboratory.cli.ActionParameters;
import com.milaboratory.mist.io.StatPositionsIO;

import java.util.*;

public final class StatPositionsAction implements Action {
    private final StatPositionsActionParameters params = new StatPositionsActionParameters();

    @Override
    public void go(ActionHelper helper) {
        StatPositionsIO statPositionsIO = new StatPositionsIO(params.groupList, params.readIdList, params.outputWithSeq,
                params.inputFileName, params.outputFileName, params.inputReadsLimit, params.minCountFilter,
                params.minFracFilter);
        statPositionsIO.go();
    }

    @Override
    public String command() {
        return "stat-positions";
    }

    @Override
    public ActionParameters params() {
        return params;
    }

    @Parameters(commandDescription =
            "Collect summary statistics: positions of group matches.")
    private static final class StatPositionsActionParameters extends ActionParameters {
        @Parameter(description = "--groups <group_names>", order = 0)
        private String description;

        @Parameter(description = "Space separated list of groups to output, determines IDs allowed in group.id column.",
                names = {"--groups"}, order = 1, required = true, variableArity = true)
        List<String> groupList = null;

        @Parameter(description = "Space separated list of original read IDs to output (R1, R2 etc), determines IDs " +
                "allowed in read column. If not specified, all reads will be used.",
                names = {"--reads"}, order = 2, variableArity = true)
        List<String> readIdList = null;

        @Parameter(description = "Also output matched sequences. If specified, key columns are group.id + read " +
                "+ seq + pos; if not specified, key columns are group.id + read + pos.",
                names = {"--output-with-seq"}, order = 3)
        boolean outputWithSeq = false;

        @Parameter(description = "Input file in \"mif\" format. If not specified, stdin will be used.",
                names = {"--input"}, order = 4)
        String inputFileName = null;

        @Parameter(description = "Output text file. If not specified, stdout will be used.",
                names = {"--output"}, order = 5)
        String outputFileName = null;

        @Parameter(description = "Filter unique group values represented by less than specified number of reads.",
                names = {"--min-count-filter"}, order = 6)
        int minCountFilter = 0;

        @Parameter(description = "Filter unique group values represented by less than specified fraction of reads.",
                names = {"--min-frac-filter"}, order = 7)
        float minFracFilter = 0;

        @Parameter(description = "Number of reads to take; 0 value means to take the entire input file.",
                names = {"-n", "--number-of-reads"}, order = 8)
        long inputReadsLimit = 0;

        @Override
        public void validate() {
            if (groupList.size() == 0)
                throw new ParameterException("List of output groups is not specified!");
        }
    }
}
