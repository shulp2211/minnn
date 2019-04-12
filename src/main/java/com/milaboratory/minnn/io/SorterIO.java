/*
 * Copyright (c) 2016-2019, MiLaboratory LLC
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
package com.milaboratory.minnn.io;

import cc.redberry.pipe.CUtils;
import cc.redberry.pipe.OutputPortCloseable;
import com.google.common.io.NullOutputStream;
import com.milaboratory.cli.PipelineConfiguration;
import com.milaboratory.core.io.CompressionType;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.minnn.outputconverter.ParsedRead;
import com.milaboratory.primitivio.PrimitivI;
import com.milaboratory.primitivio.PrimitivIState;
import com.milaboratory.primitivio.PrimitivO;
import com.milaboratory.primitivio.PrimitivOState;
import com.milaboratory.util.*;
import org.apache.commons.io.input.NullInputStream;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.milaboratory.minnn.cli.Defaults.DEFAULT_SORT_CHUNK_SIZE;
import static com.milaboratory.minnn.util.SystemUtils.exitWithError;
import static com.milaboratory.util.TimeUtils.nanoTimeToString;

public final class SorterIO {
    private final PipelineConfiguration pipelineConfiguration;
    private final String inputFileName;
    private final String outputFileName;
    private final List<String> sortGroupNames;
    private final int chunkSize;
    private final boolean suppressWarnings;
    private final File tmpFile;

    public SorterIO(PipelineConfiguration pipelineConfiguration, String inputFileName, String outputFileName,
                    List<String> sortGroupNames, int chunkSize, boolean suppressWarnings, String tmpFile) {
        this.pipelineConfiguration = pipelineConfiguration;
        this.inputFileName = inputFileName;
        this.outputFileName = outputFileName;
        this.sortGroupNames = sortGroupNames;
        this.chunkSize = (chunkSize == -1) ? estimateChunkSize() : chunkSize;
        this.suppressWarnings = suppressWarnings;
        this.tmpFile = (tmpFile != null) ? new File(tmpFile) : TempFileManager.getTempFile((outputFileName == null)
                ? null : Paths.get(new File(outputFileName).getAbsolutePath()).getParent());
    }

    public void go() {
        long startTime = System.currentTimeMillis();
        long totalReads = 0;
        try (MifReader reader = createReader();
             MifWriter writer = createWriter(reader.getHeader())) {
            SmartProgressReporter.startProgressReport("Reading", reader, System.err);
            List<String> notCorrectedGroups = sortGroupNames.stream().filter(gn -> reader.getCorrectedGroups().stream()
                    .noneMatch(gn::equals)).collect(Collectors.toList());
            if (!suppressWarnings && (notCorrectedGroups.size() != 0))
                System.err.println("WARNING: group(s) " + notCorrectedGroups + " not corrected before sorting!");

            // Creating I/O states
            PrimitivOState oState;
            PrimitivIState iState;
            {
                PrimitivO o = new PrimitivO(new NullOutputStream());
                reader.getGroupEdges().forEach(o::putKnownObject);
                oState = o.getState();

                PrimitivI i = new PrimitivI(new NullInputStream(0));
                reader.getGroupEdges().forEach(i::putKnownObject);
                iState = i.getState();
            }

            Sorter2<NucleotideSequence[], ParsedRead> sorter = new Sorter2<>(reader,
                    pr -> sortGroupNames.stream().map(gr -> pr.getGroupValue(gr).getSequence()).toArray(NucleotideSequence[]::new),
                    (k1, k2) -> {
                        assert k1.length == k2.length;
                        int c;
                        for (int i = 0; i < k1.length; i++) {
                            NucleotideSequence
                                    s1 = k1[i],
                                    s2 = k2[i];
                            if (s1 == s2)
                                continue;
                            if (s1 == null)
                                return -1;
                            if (s2 == null)
                                return 1;
                            if ((c = (s1.compareTo(s2))) != 0)
                                return c;
                        }
                        return 0;
                    },
                    () -> {
                        VolatileDataInput vi = new VolatileDataInput();
                        PrimitivI i = iState.createPrimitivI(vi);
                        VolatileDataOutput vo = new VolatileDataOutput();
                        PrimitivO o = oState.createPrimitivO(vo);

                        return new Sorter2.Serializer<ParsedRead>() {
                            @Override
                            public void serialize(ParsedRead parsedRead, DataOutput dest) throws IOException {
                                vo.setInternal(dest);
                                o.writeObject(parsedRead);
                            }

                            @Override
                            public ParsedRead deserialize(DataInput source) throws IOException {
                                vi.setInternal(source);
                                return i.readObject(ParsedRead.class);
                            }
                        };
                    },
                    () -> {
                        VolatileDataInput vi = new VolatileDataInput();
                        PrimitivI i = new PrimitivI(vi);
                        VolatileDataOutput vo = new VolatileDataOutput();
                        PrimitivO o = new PrimitivO(vo);

                        return new Sorter2.Serializer<NucleotideSequence[]>() {
                            @Override
                            public void serialize(NucleotideSequence[] nucleotideSequences, DataOutput dest) throws IOException {
                                vo.setInternal(dest);
                                o.writeObject(nucleotideSequences);
                            }

                            @Override
                            public NucleotideSequence[] deserialize(DataInput source) throws IOException {
                                vi.setInternal(source);
                                return i.readObject(NucleotideSequence[].class);
                            }
                        };
                    },
                    Executors.newCachedThreadPool(),
                    Runtime.getRuntime().availableProcessors(),
                    6, 1L << 31, tmpFile.toPath()
            );

            OutputPortCloseable<ParsedRead> sorted = sorter.run();

            // OutputPortCloseable<ParsedRead> sorted = Sorter.sort(reader, new ParsedReadComparator(), chunkSize,
            //         new ParsedReadObjectSerializer(reader.getGroupEdges()), tmpFile);

            SmartProgressReporter.startProgressReport("Writing", writer, System.err);
            for (ParsedRead parsedRead : CUtils.it(sorted)) {
                totalReads++;
                if (totalReads == 1)
                    writer.setEstimatedNumberOfReads(reader.getEstimatedNumberOfReads());
                writer.write(parsedRead);
            }
            reader.close();
            writer.setOriginalNumberOfReads(reader.getOriginalNumberOfReads());
        } catch (IOException e) {
            throw exitWithError(e.getMessage());
        }

        long elapsedTime = System.currentTimeMillis() - startTime;
        System.err.println("\nProcessing time: " + nanoTimeToString(elapsedTime * 1000000));
        System.err.println("Sorted " + totalReads + " reads\n");
    }

    private MifReader createReader() throws IOException {
        return (inputFileName == null) ? new MifReader(System.in) : new MifReader(inputFileName);
    }

    private MifWriter createWriter(MifHeader inputHeader) throws IOException {
        MifHeader outputHeader = new MifHeader(pipelineConfiguration, inputHeader.getNumberOfTargets(),
                inputHeader.getCorrectedGroups(), new ArrayList<>(sortGroupNames), inputHeader.getGroupEdges());
        return (outputFileName == null) ? new MifWriter(new SystemOutStream(), outputHeader)
                : new MifWriter(outputFileName, outputHeader);
    }

    private int estimateChunkSize() {
        if (inputFileName == null)
            return DEFAULT_SORT_CHUNK_SIZE;
        else {
            // heuristic to auto-determine chunk size by input file size
            File inputFile = new File(inputFileName);
            CompressionType ct = CompressionType.detectCompressionType(inputFile);
            int averageBytesPerParsedRead = (ct == CompressionType.None) ? 50 : 15;
            return (int) Math.min(Math.max(16384, inputFile.length() / averageBytesPerParsedRead / 8),
                    DEFAULT_SORT_CHUNK_SIZE);
        }
    }

    private class ParsedReadComparator implements Comparator<ParsedRead> {
        @Override
        public int compare(ParsedRead parsedRead1, ParsedRead parsedRead2) {
            for (String groupName : sortGroupNames) {
                NSequenceWithQuality read1Value = parsedRead1.getBestMatch().getGroupValue(groupName);
                NSequenceWithQuality read2Value = parsedRead2.getBestMatch().getGroupValue(groupName);
                if ((read1Value == null) && (read2Value != null))
                    return -1;
                else if ((read1Value != null) && (read2Value == null))
                    return 1;
                else if (read1Value != null) {
                    int compareValue = read1Value.getSequence().compareTo(read2Value.getSequence());
                    if (compareValue != 0)
                        return compareValue;
                }
            }
            return 0;
        }
    }
}
