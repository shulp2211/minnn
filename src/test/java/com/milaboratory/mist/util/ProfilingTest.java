package com.milaboratory.mist.util;

import com.milaboratory.core.sequence.*;
import com.milaboratory.test.TestUtil;
import org.junit.*;

import static com.milaboratory.core.sequence.NucleotideSequenceCaseSensitive.fromNucleotideSequence;
import static com.milaboratory.mist.util.DebugUtils.*;
import static org.junit.Assert.*;

public class ProfilingTest {
    @Before
    public void setUp() throws Exception {
        resetTimeCounter();
    }

    @After
    public void tearDown() throws Exception {
        resetTimeCounter();
    }

    @Test
    public void nucleotideSequenceProfilingTest() throws Exception {
        NucleotideAlphabet alphabetOld = NucleotideSequence.ALPHABET;
        NucleotideAlphabetCaseSensitive alphabetNew = NucleotideSequenceCaseSensitive.ALPHABET;
        for (int i = 0; i < 100000; i++) {
            final NucleotideSequence sequenceOld = TestUtil.randomSequence(alphabetOld,
                    500, 500);
            final NucleotideSequenceCaseSensitive sequenceNew = TestUtil.randomSequence(alphabetNew,
                    500, 500);
            final String stringOld = (String)countExecutionTimeR("ToStringOld", sequenceOld::toString);
            final String stringNew = (String)countExecutionTimeR("ToStringNew", sequenceNew::toString);
            countExecutionTime("CreateOld", () -> {
                new NucleotideSequence(stringOld);
                return null;
            });
            countExecutionTime("CreateNew", () -> {
                new NucleotideSequenceCaseSensitive(stringNew);
                return null;
            });
            countExecutionTime("OldToNew", () -> {
                fromNucleotideSequence(sequenceOld, false);
                return null;
            });
            countExecutionTime("NewToOld", () -> {
                sequenceNew.toNucleotideSequence();
                return null;
            });
        }
        assertTrue(timeCounter.get("ToStringOld") * 1.5 > timeCounter.get("ToStringNew"));
        assertTrue(timeCounter.get("CreateOld") * 1.5 > timeCounter.get("CreateNew"));
        assertTrue(timeCounter.get("ToStringOld") * 10 > timeCounter.get("OldToNew"));
        assertTrue(timeCounter.get("ToStringOld") * 10 > timeCounter.get("NewToOld"));
    }
}
