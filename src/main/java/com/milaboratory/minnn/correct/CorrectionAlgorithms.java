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
package com.milaboratory.minnn.correct;

import cc.redberry.pipe.CUtils;
import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.clustering.Clustering;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.minnn.io.MifReader;
import com.milaboratory.minnn.io.MifWriter;
import com.milaboratory.minnn.outputconverter.MatchedGroup;
import com.milaboratory.minnn.outputconverter.ParsedRead;
import com.milaboratory.minnn.pattern.Match;
import com.milaboratory.minnn.pattern.MatchedGroupEdge;
import com.milaboratory.minnn.pattern.MatchedItem;
import com.milaboratory.util.SmartProgressReporter;
import gnu.trove.map.hash.TByteObjectHashMap;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static com.milaboratory.minnn.util.SystemUtils.*;

public final class CorrectionAlgorithms {
    private CorrectionAlgorithms() {}

    public static CorrectionStats fullFileCorrect(
            MifReader pass1Reader, MifReader pass2Reader, MifWriter writer, MifWriter excludedBarcodesWriter,
            long inputReadsLimit, BarcodeClusteringStrategy barcodeClusteringStrategy, Set<String> defaultGroups,
            LinkedHashSet<String> keyGroups, int maxUniqueBarcodes) {
        Map<String, HashMap<NucleotideSequence, SequenceCounter>> sequenceMaps = keyGroups.stream()
                .collect(Collectors.toMap(groupName -> groupName, groupName -> new HashMap<>()));
        Map<String, Map<NucleotideSequence, RawSequenceCounter>> notCorrectedBarcodeCounters = (maxUniqueBarcodes > 0)
                ? new HashMap<>() : null;
        long totalReads = 0;
        long correctedReads = 0;
        long excludedReads = 0;

        // 1st pass: counting barcodes
        SmartProgressReporter.startProgressReport("Counting sequences", pass1Reader, System.err);
        for (ParsedRead parsedRead : CUtils.it(pass1Reader)) {
            // get sequences from parsed read and fill sequence maps
            pass1ProcessRead(parsedRead, sequenceMaps, notCorrectedBarcodeCounters);
            if (++totalReads == inputReadsLimit)
                break;
        }

        // clustering and filling barcode correction maps
        Map<String, HashMap<NucleotideSequence, NucleotideSequence>> sequenceCorrectionMaps =
                performClustering(sequenceMaps, barcodeClusteringStrategy, true);

        // calculating which barcodes must be included or excluded; only if filtering by count is enabled
        if (notCorrectedBarcodeCounters != null)
            System.err.println("Filtering corrected barcodes by count...");
        Map<String, Set<NucleotideSequence>> includedBarcodes = (notCorrectedBarcodeCounters == null) ? null
                : filterByCount(notCorrectedBarcodeCounters, sequenceCorrectionMaps, maxUniqueBarcodes);

        // 2nd pass: correcting barcodes
        totalReads = 0;
        SmartProgressReporter.startProgressReport("Correcting barcodes", pass2Reader, System.err);
        for (ParsedRead parsedRead : CUtils.it(pass2Reader)) {
            CorrectBarcodesResult correctBarcodesResult = correctBarcodes(parsedRead, sequenceCorrectionMaps,
                    includedBarcodes, defaultGroups, keyGroups);
            correctedReads += correctBarcodesResult.numCorrectedBarcodes;
            if (correctBarcodesResult.excluded) {
                if (excludedBarcodesWriter != null)
                    excludedBarcodesWriter.write(correctBarcodesResult.parsedRead);
                excludedReads++;
            } else
                writer.write(correctBarcodesResult.parsedRead);
            if (++totalReads == inputReadsLimit)
                break;
        }
        return new CorrectionStats(totalReads, correctedReads, excludedReads);
    }

    public static CorrectionStats sortedClustersCorrect(
            MifReader reader, MifWriter writer, MifWriter excludedBarcodesWriter, long inputReadsLimit,
            BarcodeClusteringStrategy barcodeClusteringStrategy, Set<String> defaultGroups,
            LinkedHashSet<String> primaryGroups, LinkedHashSet<String> keyGroups, int maxUniqueBarcodes) {
        AtomicLong totalReads = new AtomicLong(0);
        long correctedReads = 0;
        long excludedReads = 0;

        SmartProgressReporter.startProgressReport("Counting sequences", reader, System.err);
        OutputPort<List<ParsedRead>> clustersOutputPort = new OutputPort<List<ParsedRead>>() {
            LinkedHashMap<String, NucleotideSequence> previousGroups = null;
            List<ParsedRead> currentCluster = new ArrayList<>();
            boolean finished = false;

            @Override
            public List<ParsedRead> take() {
                if (finished)
                    return null;
                List<ParsedRead> preparedCluster = null;
                while (preparedCluster == null) {
                    ParsedRead parsedRead = ((inputReadsLimit == 0) || (totalReads.get() < inputReadsLimit))
                            ? reader.take() : null;
                    if (parsedRead != null) {
                        LinkedHashMap<String, NucleotideSequence> currentGroups = extractPrimaryBarcodes(parsedRead,
                                defaultGroups, primaryGroups);
                        if (!currentGroups.equals(previousGroups)) {
                            if (previousGroups != null) {
                                preparedCluster = currentCluster;
                                currentCluster = new ArrayList<>();
                            }
                            previousGroups = currentGroups;
                        }
                        currentCluster.add(parsedRead);
                        totalReads.getAndIncrement();
                    } else {
                        finished = true;
                        if (previousGroups != null)
                            return currentCluster;
                        else
                            return null;
                    }
                }
                return preparedCluster;
            }
        };

        boolean correctionStarted = false;
        for (List<ParsedRead> cluster : CUtils.it(clustersOutputPort)) {
            if (!correctionStarted) {
                SmartProgressReporter.startProgressReport("Correcting barcodes", writer, System.err);
                correctionStarted = true;
            }
            ClusterStats stats = processCluster(cluster, writer, excludedBarcodesWriter, barcodeClusteringStrategy,
                    defaultGroups, keyGroups, maxUniqueBarcodes);
            correctedReads += stats.correctedReads;
            excludedReads += stats.excludedReads;
        }

        return new CorrectionStats(totalReads.get(), correctedReads, excludedReads);
    }

    public static CorrectionStats unsortedClustersCorrect(
            MifReader reader, MifWriter writer, MifWriter excludedBarcodesWriter, long inputReadsLimit,
            BarcodeClusteringStrategy barcodeClusteringStrategy, Set<String> defaultGroups,
            LinkedHashSet<String> primaryGroups, LinkedHashSet<String> keyGroups, int maxUniqueBarcodes) {
        // keys: primary barcodes values; values: all reads that have this combination of barcodes values
        HashMap<LinkedHashMap<String, NucleotideSequence>, List<ParsedRead>> allClusters = new HashMap<>();
        long totalReads = 0;
        long correctedReads = 0;
        long excludedReads = 0;

        // reading the entire input file into memory and clustering by primary barcodes values
        SmartProgressReporter.startProgressReport("Reading input file into memory", reader, System.err);
        for (ParsedRead parsedRead : CUtils.it(reader)) {
            LinkedHashMap<String, NucleotideSequence> primaryBarcodes = extractPrimaryBarcodes(parsedRead,
                    defaultGroups, primaryGroups);
            allClusters.computeIfAbsent(primaryBarcodes, pb -> new ArrayList<>());
            allClusters.get(primaryBarcodes).add(parsedRead);
            if (++totalReads == inputReadsLimit)
                break;
        }

        boolean correctionStarted = false;
        for (List<ParsedRead> cluster : allClusters.values()) {
            if (!correctionStarted) {
                SmartProgressReporter.startProgressReport("Correcting barcodes", writer, System.err);
                correctionStarted = true;
            }
            ClusterStats stats = processCluster(cluster, writer, excludedBarcodesWriter, barcodeClusteringStrategy,
                    defaultGroups, keyGroups, maxUniqueBarcodes);
            correctedReads += stats.correctedReads;
            excludedReads += stats.excludedReads;
        }

        return new CorrectionStats(totalReads, correctedReads, excludedReads);
    }

    /**
     * Extract values of primary barcodes from parsed read.
     *
     * @param parsedRead        parsed read
     * @param defaultGroups     default group names: R1, R2 etc
     * @param primaryGroups     names of primary groups
     * @return                  keys: names of primary groups; values: values (sequences) of primary groups
     */
    private static LinkedHashMap<String, NucleotideSequence> extractPrimaryBarcodes(
            ParsedRead parsedRead, Set<String> defaultGroups, LinkedHashSet<String> primaryGroups) {
        Set<String> allGroups = parsedRead.getGroups().stream().map(MatchedGroup::getGroupName)
                .filter(groupName -> !defaultGroups.contains(groupName)).collect(Collectors.toSet());
        for (String groupName : primaryGroups)
            if (!allGroups.contains(groupName))
                throw exitWithError("Group " + groupName + " not found in the input!");
        return parsedRead.getGroups().stream().filter(g -> primaryGroups.contains(g.getGroupName()))
                .collect(LinkedHashMap::new, (m, g) -> m.put(g.getGroupName(), g.getValue().getSequence()),
                        Map::putAll);
    }

    /**
     * Fill sequence maps with barcodes from parsed read. Also update initial (not corrected) unique barcode counters
     * if filtering by count is enabled.
     *
     * @param parsedRead                    parsed read
     * @param sequenceMaps                  keys: group names, values: counters for multi-sequences for this group
     * @param notCorrectedBarcodeCounters   keys: group names, values: counters for raw sequences (without merging
     *                                      by wildcards) for this group
     */
    private static void pass1ProcessRead(
            ParsedRead parsedRead, Map<String, HashMap<NucleotideSequence, SequenceCounter>> sequenceMaps,
            Map<String, Map<NucleotideSequence, RawSequenceCounter>> notCorrectedBarcodeCounters) {
        for (Map.Entry<String, HashMap<NucleotideSequence, SequenceCounter>> entry : sequenceMaps.entrySet()) {
            // creating multi-sequence counters, without merging multi-sequences on this stage
            NucleotideSequence groupValue = parsedRead.getGroupValue(entry.getKey()).getSequence();
            SequenceCounter counter = entry.getValue().get(groupValue);
            if (counter == null)
                entry.getValue().put(groupValue, new SequenceCounter(groupValue));
            else
                counter.count++;

            // counting raw barcode sequences if filtering by count is enabled
            if (notCorrectedBarcodeCounters != null) {
                notCorrectedBarcodeCounters.computeIfAbsent(entry.getKey(), groupName -> new HashMap<>());
                Map<NucleotideSequence, RawSequenceCounter> currentGroupCounters = notCorrectedBarcodeCounters
                        .get(entry.getKey());
                RawSequenceCounter rawSequenceCounter = currentGroupCounters.get(groupValue);
                if (rawSequenceCounter == null)
                    currentGroupCounters.put(groupValue, new RawSequenceCounter(groupValue));
                else
                    rawSequenceCounter.count++;
            }
        }
    }

    /**
     * Sort nucleotide sequences by count in each group and perform clustering.
     *
     * @param sequenceMaps              keys: group names, values: counters for multi-sequences for this group
     * @param barcodeClusteringStrategy clustering strategy parameters, from CLI arguments values
     * @param reportProgress            report clustering progress; must be used only with full file correction
     * @return                          barcode correction maps; keys: group names, values: maps with not corrected
     *                                  barcodes as keys and corrected barcodes as values
     */
    private static Map<String, HashMap<NucleotideSequence, NucleotideSequence>> performClustering(
            Map<String, HashMap<NucleotideSequence, SequenceCounter>> sequenceMaps,
            BarcodeClusteringStrategy barcodeClusteringStrategy, boolean reportProgress) {
        SequenceCounterExtractor sequenceCounterExtractor = new SequenceCounterExtractor();
        Map<String, HashMap<NucleotideSequence, NucleotideSequence>> sequenceCorrectionMaps = new HashMap<>();
        for (Map.Entry<String, HashMap<NucleotideSequence, SequenceCounter>> entry : sequenceMaps.entrySet()) {
            TreeSet<SequenceCounter> sortedSequences = new TreeSet<>(entry.getValue().values());
            Clustering<SequenceCounter, NucleotideSequence> clustering = new Clustering<>(sortedSequences,
                    sequenceCounterExtractor, barcodeClusteringStrategy);
            HashMap<NucleotideSequence, NucleotideSequence> currentCorrectionMap = new HashMap<>();
            if (reportProgress)
                SmartProgressReporter.startProgressReport("Clustering barcodes in group " + entry.getKey(),
                        clustering, System.err);
            clustering.performClustering().forEach(cluster -> {
                NucleotideSequence headSequence = cluster.getHead().multiSequence.getBestSequence();
                cluster.processAllChildren(child -> {
                    child.getHead().multiSequence.sequences.keySet().forEach(seq -> currentCorrectionMap.put(seq,
                            headSequence));
                    return true;
                });
            });
            sequenceCorrectionMaps.put(entry.getKey(), currentCorrectionMap);
        }
        return sequenceCorrectionMaps;
    }

    /**
     * Filter barcodes by count and fill sets of included barcodes (that were not filtered out) for each group.
     *
     * @param notCorrectedBarcodeCounters   keys: group names, values: counters for raw sequences (without merging
     *                                      by wildcards) for this group
     * @param sequenceCorrectionMaps        barcode correction maps; keys: group names, values: maps with not
     *                                      corrected barcodes as keys and corrected barcodes as values
     * @param maxUniqueBarcodes             maximum number of included unique barcodes for each group
     * @return                              keys: group names, values: included barcodes for this group
     */
    private static Map<String, Set<NucleotideSequence>> filterByCount(
            Map<String, Map<NucleotideSequence, RawSequenceCounter>> notCorrectedBarcodeCounters,
            Map<String, HashMap<NucleotideSequence, NucleotideSequence>> sequenceCorrectionMaps,
            int maxUniqueBarcodes) {
        Map<String, Map<NucleotideSequence, RawSequenceCounter>> correctedBarcodeCounters = new HashMap<>();
        // counting corrected barcodes by not corrected barcodes counts
        for (Map.Entry<String, Map<NucleotideSequence, RawSequenceCounter>> groupEntry
                : notCorrectedBarcodeCounters.entrySet()) {
            String groupName = groupEntry.getKey();
            correctedBarcodeCounters.computeIfAbsent(groupName, gn -> new HashMap<>());
            Map<NucleotideSequence, RawSequenceCounter> currentGroupCorrectedCounters =
                    correctedBarcodeCounters.get(groupName);
            Map<NucleotideSequence, NucleotideSequence> currentGroupCorrectionMap =
                    sequenceCorrectionMaps.get(groupName);
            for (Map.Entry<NucleotideSequence, RawSequenceCounter> barcodeValueEntry
                    : groupEntry.getValue().entrySet()) {
                NucleotideSequence oldValue = barcodeValueEntry.getKey();
                long oldCount = barcodeValueEntry.getValue().count;
                NucleotideSequence newValue = currentGroupCorrectionMap.get(oldValue);
                if (newValue == null)
                    newValue = oldValue;
                RawSequenceCounter correctedSequenceCounter = currentGroupCorrectedCounters.get(newValue);
                if (correctedSequenceCounter == null) {
                    RawSequenceCounter newCounter = new RawSequenceCounter(newValue);
                    newCounter.count = oldCount;
                    currentGroupCorrectedCounters.put(newValue, newCounter);
                } else
                    correctedSequenceCounter.count += oldCount;
            }
        }
        // filtering by count
        return correctedBarcodeCounters.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                entry -> new TreeSet<>(entry.getValue().values()).stream().limit(maxUniqueBarcodes)
                        .map(counter -> counter.seq).collect(Collectors.toSet())));
    }

    /**
     * Correct barcodes in the parsed read.
     *
     * @param parsedRead                original parsed read
     * @param sequenceCorrectionMaps    barcode correction maps; keys: group names, values: maps with not
     *                                  corrected barcodes as keys and corrected barcodes as values
     * @param includedBarcodes          keys: group names, values: included barcodes for this group;
     *                                  or null if filtering barcodes by count is disabled
     * @param defaultGroups             default group names: R1, R2 etc
     * @param keyGroups                 group names in which we will correct barcodes
     * @return                          parsed read with corrected barcodes, number of corrected barcodes and
     *                                  excluded flag (which is true if any of barcodes in this parsed read was
     *                                  filtered out by count)
     */
    private static CorrectBarcodesResult correctBarcodes(
            ParsedRead parsedRead, Map<String, HashMap<NucleotideSequence, NucleotideSequence>> sequenceCorrectionMaps,
            Map<String, Set<NucleotideSequence>> includedBarcodes, Set<String> defaultGroups,
            LinkedHashSet<String> keyGroups) {
        TByteObjectHashMap<ArrayList<CorrectedGroup>> correctedGroups = new TByteObjectHashMap<>();
        boolean isCorrection = false;
        int numCorrectedBarcodes = 0;
        boolean excluded = false;
        for (Map.Entry<String, MatchedGroup> entry : parsedRead.getGroups().stream()
                .filter(group -> keyGroups.contains(group.getGroupName()))
                .collect(Collectors.toMap(MatchedGroup::getGroupName, group -> group)).entrySet()) {
            String groupName = entry.getKey();
            MatchedGroup matchedGroup = entry.getValue();
            byte targetId = matchedGroup.getTargetId();
            NucleotideSequence oldValue = matchedGroup.getValue().getSequence();
            NucleotideSequence correctValue = sequenceCorrectionMaps.get(groupName).get(oldValue);
            if (correctValue == null)
                correctValue = oldValue;
            isCorrection |= !correctValue.equals(oldValue);
            correctedGroups.putIfAbsent(targetId, new ArrayList<>());
            correctedGroups.get(targetId).add(new CorrectedGroup(groupName, correctValue));
            // includedBarcodes is null when filtering barcodes by count is disabled
            if (includedBarcodes != null)
                excluded |= !includedBarcodes.get(groupName).contains(correctValue);
        }

        ArrayList<MatchedGroupEdge> newGroupEdges;
        if (!isCorrection)
            newGroupEdges = parsedRead.getMatchedGroupEdges();
        else {
            newGroupEdges = new ArrayList<>();
            for (byte targetId : parsedRead.getGroups().stream().map(MatchedItem::getTargetId)
                    .collect(Collectors.toCollection(LinkedHashSet::new))) {
                ArrayList<CorrectedGroup> currentCorrectedGroups = correctedGroups.get(targetId);
                if (currentCorrectedGroups == null)
                    parsedRead.getMatchedGroupEdges().stream()
                            .filter(mge -> mge.getTargetId() == targetId).forEach(newGroupEdges::add);
                else {
                    Map<String, CorrectedGroup> currentCorrectedGroupsMap = currentCorrectedGroups.stream()
                            .collect(Collectors.toMap(cg -> cg.groupName, cg -> cg));
                    for (MatchedGroupEdge matchedGroupEdge : parsedRead.getMatchedGroupEdges().stream()
                            .filter(mge -> mge.getTargetId() == targetId).collect(Collectors.toList())) {
                        String currentGroupName = matchedGroupEdge.getGroupEdge().getGroupName();
                        if (!keyGroups.contains(currentGroupName))
                            newGroupEdges.add(matchedGroupEdge);
                        else {
                            CorrectedGroup currentCorrectedGroup = currentCorrectedGroupsMap.get(currentGroupName);
                            newGroupEdges.add(new MatchedGroupEdge(matchedGroupEdge.getTarget(),
                                    matchedGroupEdge.getTargetId(), matchedGroupEdge.getGroupEdge(),
                                    new NSequenceWithQuality(currentCorrectedGroup.correctedValue)));
                        }
                    }
                }
            }
            numCorrectedBarcodes++;
        }

        int numberOfTargets = defaultGroups.size();
        Match newMatch = new Match(numberOfTargets, parsedRead.getBestMatchScore(), newGroupEdges);
        if (newMatch.getGroups().stream().map(MatchedGroup::getGroupName)
                .filter(defaultGroups::contains).count() != numberOfTargets)
            throw new IllegalStateException("Missing default groups in new Match: expected " + defaultGroups
                    + ", got " + newMatch.getGroups().stream().map(MatchedGroup::getGroupName)
                    .filter(defaultGroups::contains).collect(Collectors.toList()));
        return new CorrectBarcodesResult(new ParsedRead(parsedRead.getOriginalRead(), parsedRead.isReverseMatch(),
                newMatch, 0), numCorrectedBarcodes, excluded);
    }

    /**
     * Correct barcodes in cluster (for correction with primary barcodes) and write corrected cluster to output file.
     *
     * @param cluster                       cluster: list of parsed reads with the same primary barcodes
     * @param writer                        MifWriter for output file
     * @param excludedBarcodesWriter        MifWriter for excluded barcodes output file
     * @param barcodeClusteringStrategy     clustering strategy parameters, from CLI arguments values
     * @param defaultGroups                 default group names: R1, R2 etc
     * @param keyGroups                     group names in which we will correct barcodes
     * @param maxUniqueBarcodes             maximum number of included unique barcodes for each group, for filtering
     *                                      corrected barcodes by count
     * @return                              stats for cluster: number of corrected reads and number of excluded reads
     */
    private static ClusterStats processCluster(
            List<ParsedRead> cluster, MifWriter writer, MifWriter excludedBarcodesWriter,
            BarcodeClusteringStrategy barcodeClusteringStrategy, Set<String> defaultGroups,
            LinkedHashSet<String> keyGroups, int maxUniqueBarcodes) {
        Map<String, HashMap<NucleotideSequence, SequenceCounter>> sequenceMaps = keyGroups.stream()
                .collect(Collectors.toMap(groupName -> groupName, groupName -> new HashMap<>()));
        Map<String, Map<NucleotideSequence, RawSequenceCounter>> notCorrectedBarcodeCounters =
                (maxUniqueBarcodes > 0) ? new HashMap<>() : null;
        long correctedReads = 0;
        long excludedReads = 0;

        // counting barcodes
        cluster.forEach(parsedRead -> pass1ProcessRead(parsedRead, sequenceMaps, notCorrectedBarcodeCounters));

        // clustering and filling barcode correction maps
        Map<String, HashMap<NucleotideSequence, NucleotideSequence>> sequenceCorrectionMaps =
                performClustering(sequenceMaps, barcodeClusteringStrategy, false);

        // calculating which barcodes must be included or excluded; only if filtering by count is enabled
        Map<String, Set<NucleotideSequence>> includedBarcodes = (notCorrectedBarcodeCounters == null) ? null
                : filterByCount(notCorrectedBarcodeCounters, sequenceCorrectionMaps, maxUniqueBarcodes);

        for (ParsedRead parsedRead : cluster) {
            CorrectBarcodesResult correctBarcodesResult = correctBarcodes(parsedRead, sequenceCorrectionMaps,
                    includedBarcodes, defaultGroups, keyGroups);
            correctedReads += correctBarcodesResult.numCorrectedBarcodes;
            if (correctBarcodesResult.excluded) {
                if (excludedBarcodesWriter != null)
                    excludedBarcodesWriter.write(correctBarcodesResult.parsedRead);
                excludedReads++;
            } else
                writer.write(correctBarcodesResult.parsedRead);
        }

        return new ClusterStats(correctedReads, excludedReads);
    }

    private static class ClusterStats {
        final long correctedReads;
        final long excludedReads;

        ClusterStats(long correctedReads, long excludedReads) {
            this.correctedReads = correctedReads;
            this.excludedReads = excludedReads;
        }
    }

    private static class CorrectBarcodesResult {
        final ParsedRead parsedRead;
        final int numCorrectedBarcodes;
        final boolean excluded;

        CorrectBarcodesResult(ParsedRead parsedRead, int numCorrectedBarcodes, boolean excluded) {
            this.parsedRead = parsedRead;
            this.numCorrectedBarcodes = numCorrectedBarcodes;
            this.excluded = excluded;
        }
    }

    private static class CorrectedGroup {
        final String groupName;
        final NucleotideSequence correctedValue;

        CorrectedGroup(String groupName, NucleotideSequence correctedValue) {
            this.groupName = groupName;
            this.correctedValue = correctedValue;
        }
    }
}