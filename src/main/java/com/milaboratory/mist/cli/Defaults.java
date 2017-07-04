package com.milaboratory.mist.cli;

import com.milaboratory.core.sequence.Alphabet;
import com.milaboratory.core.sequence.NucleotideSequence;

public final class Defaults {
    public final static long DEFAULT_PENALTY_THRESHOLD = -30;
    public final static long DEFAULT_SINGLE_OVERLAP_PENALTY = -1;
    public final static int DEFAULT_BITAP_MAX_ERRORS = 2;
    public final static Alphabet<NucleotideSequence> DEFAULT_ALPHABET = NucleotideSequence.ALPHABET;
    public final static int DEFAULT_MATCH_SCORE = 0;
    public final static int DEFAULT_MISMATCH_SCORE = -9;
    public final static int DEFAULT_GAP_SCORE = -10;
}
