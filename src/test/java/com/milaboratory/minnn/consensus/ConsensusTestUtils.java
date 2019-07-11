package com.milaboratory.minnn.consensus;

import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.SequenceQuality;
import com.milaboratory.minnn.consensus.doublemultialign.ConsensusAlgorithmDoubleMultiAlign;
import com.milaboratory.minnn.consensus.singlecell.ConsensusAlgorithmSingleCell;
import com.milaboratory.minnn.util.DebugUtils.*;
import gnu.trove.map.hash.TByteObjectHashMap;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.milaboratory.minnn.cli.Defaults.*;
import static org.junit.Assert.*;

public class ConsensusTestUtils {
    public static synchronized void displayTestWarning(String text) {
        System.err.println(text);
    }

    public static ConsensusAlgorithm createConsensusAlgorithm(
            ConsensusAlgorithms algorithmType, int numberOfTargets, Map<String, Object> overrideDefaults) {
        int alignerWidth = DEFAULT_CONSENSUS_ALIGNER_WIDTH;
        int scoreThreshold = DEFAULT_CONSENSUS_SCORE_THRESHOLD;
        float skippedFractionToRepeat = DEFAULT_CONSENSUS_SKIPPED_FRACTION_TO_REPEAT;
        int maxPerCluster = DEFAULT_CONSENSUS_MAX_PER_CLUSTER;
        byte readsMinGoodSeqLength = DEFAULT_CONSENSUS_READS_MIN_GOOD_SEQ_LENGTH;
        float readsAvgQualityThreshold = DEFAULT_CONSENSUS_READS_AVG_QUALITY_THRESHOLD;
        int readsTrimWindowSize = DEFAULT_CONSENSUS_READS_TRIM_WINDOW_SIZE;
        byte minGoodSeqLength = DEFAULT_CONSENSUS_MIN_GOOD_SEQ_LENGTH;
        float avgQualityThreshold = DEFAULT_CONSENSUS_AVG_QUALITY_THRESHOLD;
        int trimWindowSize = DEFAULT_CONSENSUS_TRIM_WINDOW_SIZE;
        long goodQualityMismatchPenalty = DEFAULT_CONSENSUS_GOOD_QUALITY_MISMATCH_PENALTY;
        byte goodQualityMismatchThreshold = DEFAULT_CONSENSUS_GOOD_QUALITY_MISMATCH_THRESHOLD;
        int matchScore = DEFAULT_MATCH_SCORE;
        int mismatchScore = DEFAULT_MISMATCH_SCORE;
        int gapScore = DEFAULT_GAP_SCORE;
        int kmerLength = DEFAULT_CONSENSUS_KMER_LENGTH;
        int kmerOffset = DEFAULT_CONSENSUS_KMER_OFFSET;
        int kmerMaxErrors = DEFAULT_CONSENSUS_KMER_MAX_ERRORS;

        if (overrideDefaults != null)
            for (Map.Entry<String, Object> entry : overrideDefaults.entrySet())
                switch (entry.getKey()) {
                    case "ALIGNER_WIDTH":
                        alignerWidth = (Integer)(entry.getValue());
                        break;
                    case "SCORE_THRESHOLD":
                        scoreThreshold = (Integer)(entry.getValue());
                        break;
                    case "SKIPPED_FRACTION_TO_REPEAT":
                        skippedFractionToRepeat = (Float)(entry.getValue());
                        break;
                    case "MAX_PER_CLUSTER":
                        maxPerCluster = (Integer)(entry.getValue());
                        break;
                    case "READS_MIN_GOOD_SEQ_LENGTH":
                        readsMinGoodSeqLength = (Byte)(entry.getValue());
                        break;
                    case "READS_AVG_QUALITY_THRESHOLD":
                        readsAvgQualityThreshold = (Float)(entry.getValue());
                        break;
                    case "READS_TRIM_WINDOW_SIZE":
                        readsTrimWindowSize = (Integer)(entry.getValue());
                        break;
                    case "MIN_GOOD_SEQ_LENGTH":
                        minGoodSeqLength = (Byte)(entry.getValue());
                        break;
                    case "AVG_QUALITY_THRESHOLD":
                        avgQualityThreshold = (Float)(entry.getValue());
                        break;
                    case "TRIM_WINDOW_SIZE":
                        trimWindowSize = (Integer)(entry.getValue());
                        break;
                    case "GOOD_QUALITY_MISMATCH_PENALTY":
                        goodQualityMismatchPenalty = (Long)(entry.getValue());
                        break;
                    case "GOOD_QUALITY_MISMATCH_THRESHOLD":
                        goodQualityMismatchThreshold = (Byte)(entry.getValue());
                        break;
                    case "MATCH_SCORE":
                        matchScore = (Integer)(entry.getValue());
                        break;
                    case "MISMATCH_SCORE":
                        mismatchScore = (Integer)(entry.getValue());
                        break;
                    case "GAP_SCORE":
                        gapScore = (Integer)(entry.getValue());
                        break;
                    case "KMER_LENGTH":
                        kmerLength = (Integer)(entry.getValue());
                        break;
                    case "KMER_OFFSET":
                        kmerOffset = (Integer)(entry.getValue());
                        break;
                    case "KMER_MAX_ERRORS":
                        kmerMaxErrors = (Integer)(entry.getValue());
                        break;
                    default:
                        throw new IllegalArgumentException();
                }

        switch (algorithmType) {
            case DOUBLE_MULTI_ALIGN:
                return new ConsensusAlgorithmDoubleMultiAlign(ConsensusTestUtils::displayTestWarning, numberOfTargets,
                        alignerWidth, matchScore, mismatchScore, gapScore, goodQualityMismatchPenalty,
                        goodQualityMismatchThreshold, scoreThreshold, skippedFractionToRepeat, maxPerCluster,
                        readsMinGoodSeqLength, readsAvgQualityThreshold, readsTrimWindowSize, minGoodSeqLength,
                        avgQualityThreshold, trimWindowSize, false, null,
                        (byte)0, null);
            case SINGLE_CELL:
                return new ConsensusAlgorithmSingleCell(ConsensusTestUtils::displayTestWarning, numberOfTargets,
                        maxPerCluster, skippedFractionToRepeat, readsMinGoodSeqLength, readsAvgQualityThreshold,
                        readsTrimWindowSize, minGoodSeqLength, avgQualityThreshold, trimWindowSize,
                        false, null, (byte)0, null,
                        kmerLength, kmerOffset, kmerMaxErrors);
            case RNA_SEQ:
                throw new NotImplementedException();
        }
        throw new IllegalStateException();
    }

    public static Cluster rawSequencesToCluster(List<List<String>> testSequences,
                                                List<LinkedHashMap<String, String>> barcodeValues) {
        int numberOfReads = testSequences.size();
        int numberOfTargets = testSequences.get(0).size();
        testSequences.forEach(readSeqs -> assertEquals(numberOfTargets, readSeqs.size()));
        assertEquals(numberOfTargets, barcodeValues.size());

        List<Barcode> barcodes = new ArrayList<>();
        for (byte targetId = 1; targetId <= numberOfTargets; targetId++) {
            for (HashMap.Entry<String, String> currentRawBarcode : barcodeValues.get(targetId - 1).entrySet())
                barcodes.add(new Barcode(currentRawBarcode.getKey(),
                        toSeqWithAttributes(currentRawBarcode.getValue(), 0), targetId));
        }

        Cluster cluster = new Cluster(0);
        for (int readIndex = 0; readIndex < numberOfReads; readIndex++) {
            TByteObjectHashMap<SequenceWithAttributes> sequences = new TByteObjectHashMap<>();
            for (byte targetId = 1; targetId <= numberOfTargets; targetId++)
                sequences.put(targetId,
                        toSeqWithAttributes(testSequences.get(readIndex).get(targetId - 1), readIndex));
            cluster.data.add(new BasicDataFromParsedRead(sequences, barcodes, readIndex, false));
        }

        return cluster;
    }

    public static List<List<String>> consensusesToRawSequences(CalculatedConsensuses calculatedConsensuses,
                                                               boolean withQuality) {
        List<List<String>> outputSequences = new ArrayList<>();
        for (Consensus consensus : calculatedConsensuses.consensuses)
            if (withQuality)
                outputSequences.add(IntStream.rangeClosed(1, consensus.sequences.size())
                        .mapToObj(targetId -> consensus.sequences.get((byte)targetId).toString()
                        .replace(" ", "\n")).collect(Collectors.toList()));
            else
                outputSequences.add(IntStream.rangeClosed(1, consensus.sequences.size())
                        .mapToObj(targetId -> consensus.sequences.get((byte)targetId).getSeq().toString())
                        .collect(Collectors.toList()));
        return outputSequences;
    }

    public static SequenceWithAttributes toSeqWithAttributes(String str, long readId) {
        if (str.contains(" ")) {
            NucleotideSequence sequence = new NucleotideSequence(str.split(" ")[0]);
            SequenceQuality quality = new SequenceQuality(str.split(" ")[1]);
            return new SequenceWithAttributes(sequence, quality, readId);
        } else {
            NSequenceWithQuality seqWithQuality = new NSequenceWithQuality(str);
            return new SequenceWithAttributes(seqWithQuality.getSequence(), seqWithQuality.getQuality(), readId);
        }
    }
}
