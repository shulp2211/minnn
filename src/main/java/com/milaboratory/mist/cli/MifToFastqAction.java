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
import com.milaboratory.mist.io.MifToFastqIO;

import java.util.*;

public final class MifToFastqAction implements Action {
    public static final String commandName = "mif2fastq";
    private final MifToFastqActionParameters params = new MifToFastqActionParameters();

    @Override
    public void go(ActionHelper helper) {
        MifToFastqIO mifToFastqIO = new MifToFastqIO(params.inputFileName, parseGroups(params.groupsQuery),
                params.copyOriginalHeaders, params.inputReadsLimit);
        mifToFastqIO.go();
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
            "Convert mif file to fastq format.")
    private static final class MifToFastqActionParameters extends ActionParameters {
        @Parameter(description = "group_options\n        Group Options:          Groups and their file names for " +
                "output reads. At least 1 group must be specified. Built-in groups R1, R2, R3... used for input " +
                "reads. Example: --group-R1 out_R1.fastq --group-R2 out_R2.fastq --group-UMI UMI.fastq",
                order = 0, required = true, variableArity = true)
        List<String> groupsQuery = new ArrayList<>();

        @Parameter(description = "Input file in \"mif\" format. If not specified, stdin will be used.",
                names = {"--input"}, order = 1)
        String inputFileName = null;

        @Parameter(description = "Copy original comments from initial fastq files to comments of output " +
                "fastq files.",
                names = {"--copy-original-headers"}, order = 2)
        boolean copyOriginalHeaders = false;

        @Parameter(description = "Number of reads to take; 0 value means to take the entire input file.",
                names = {"-n", "--number-of-reads"}, order = 3)
        long inputReadsLimit = 0;
    }

    private static LinkedHashMap<String, String> parseGroups(List<String> groupsQuery) throws ParameterException {
        if (groupsQuery.size() % 2 != 0)
            throw new ParameterException("Group parameters not parsed, expected pairs of groups and their file names: "
                    + groupsQuery);
        LinkedHashMap<String, String> groups = new LinkedHashMap<>();
        for (int i = 0; i < groupsQuery.size(); i += 2) {
            String currentGroup = groupsQuery.get(i);
            String currentFileName = groupsQuery.get(i + 1);
            if ((currentGroup.length() < 9) || !currentGroup.substring(0, 8).equals("--group-"))
                throw new ParameterException("Syntax error in group parameter: " + currentGroup);
            groups.put(currentGroup.substring(8), currentFileName);
        }
        return groups;
    }
}
