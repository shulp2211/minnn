package com.milaboratory.mist.pattern;

import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;

public final class BorderFilter implements Filter {
    private final PatternAligner patternAligner;
    private final boolean leftSide;
    private final NucleotideSequence seq;
    private final int minNucleotides;
    private final boolean useTarget;

    public BorderFilter(PatternAligner patternAligner, boolean leftSide, NucleotideSequence seq) {
        this(patternAligner, leftSide, seq, seq.size(), true);
    }

    public BorderFilter(PatternAligner patternAligner, boolean leftSide, NucleotideSequence seq, int minNucleotides) {
        this(patternAligner, leftSide, seq, minNucleotides, true);
    }

    public BorderFilter(PatternAligner patternAligner, boolean leftSide, NucleotideSequence seq, boolean useTarget) {
        this(patternAligner, leftSide, seq, seq.size(), useTarget);
    }

    /**
     * Filter that matches only if there is a fixed motif or its part on the left (or right) side of the target
     * (or of the matched value). Can be used only on single target matches.
     *
     * @param patternAligner pattern aligner: required to call FuzzyMatchPattern
     * @param leftSide true if motif must be on the left side of the target, false of on the right side
     * @param seq sequence to search
     * @param minNucleotides minimum number of nucleotides from sequence that must present;
     *                       must be 1<=minNucleotides<=seq.size(); default is seq.size()
     * @param useTarget true if we search motif on the border of the target (default), false if we search
     *                  on the border of matched value
     */
    public BorderFilter(PatternAligner patternAligner, boolean leftSide, NucleotideSequence seq, int minNucleotides, boolean useTarget) {
        this.patternAligner = patternAligner;
        this.leftSide = leftSide;
        this.seq = seq;
        this.minNucleotides = minNucleotides;
        if (minNucleotides < 1)
            throw new IllegalArgumentException("minNucleotides must be >= 1, actual value: " + minNucleotides);
        if (minNucleotides > seq.size())
            throw new IllegalArgumentException("minNucleotides for " + seq + " must be <= " + seq.size() + ", actual value: " + minNucleotides);
        this.useTarget = useTarget;
    }

    @Override
    public Match checkMatch(Match match) {
        if (match.getNumberOfPatterns() > 1)
            throw new IllegalArgumentException("BorderFilter must not be used for multi-target matches!");
        NSequenceWithQuality target = useTarget ? match.getMatchedRange().getTarget() : match.getValue();
        for (int motifSize = minNucleotides; motifSize <= seq.size(); motifSize++) {
            if (motifSize > target.size()) break;
            NSequenceWithQuality targetEdge = leftSide ? target.getRange(0, motifSize)
                    : target.getRange(target.size() - motifSize, target.size());
            NucleotideSequence motif = leftSide ? seq.getRange(seq.size() - motifSize, seq.size())
                    : seq.getRange(0, motifSize);
            if (new FuzzyMatchPattern(patternAligner, motif).match(targetEdge).isFound())
                return match;
        }
        return null;
    }
}
