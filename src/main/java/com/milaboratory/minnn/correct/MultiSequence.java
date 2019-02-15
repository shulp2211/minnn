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

import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.Wildcard;

import java.util.*;

import static com.milaboratory.minnn.util.SequencesCache.*;

final class MultiSequence {
    final HashMap<NucleotideSequence, Long> sequences = new HashMap<>();

    MultiSequence(NucleotideSequence seq) {
        long variability = 1;
        for (int i = 0; i < seq.size(); i++)
            variability *= charToWildcard.get(seq.symbolAt(i)).basicSize();
        sequences.put(seq, variability);
    }

    /**
     * @return sequence with smallest variability by wildcards
     */
    NucleotideSequence getBestSequence() {
        Map.Entry<NucleotideSequence, Long> bestEntry = null;
        for (Map.Entry<NucleotideSequence, Long> entry : sequences.entrySet())
            if ((bestEntry == null) || (bestEntry.getValue() > entry.getValue()))
                bestEntry = entry;
        return Objects.requireNonNull(bestEntry).getKey();
    }

    private boolean equalByWildcards(NucleotideSequence seq1, NucleotideSequence seq2) {
        if (seq1.size() != seq2.size())
            return false;
        for (int i = 0; i < seq1.size(); i++) {
            Wildcard wildcard1 = charToWildcard.get(seq1.symbolAt(i));
            Wildcard wildcard2 = charToWildcard.get(seq2.symbolAt(i));
            if ((wildcard1.getBasicMask() & wildcard2.getBasicMask()) == 0)
                return false;
        }
        return true;
    }

    /**
     * Important! Call to equals() will perform mutual merge if MultiSequences are equal by wildcards!
     *
     * @param o     other MultiSequence
     * @return      true if MultiSequences are equal by wildcards
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MultiSequence that = (MultiSequence)o;
        if (sequences.keySet().parallelStream().allMatch(seq1 -> that.sequences.keySet().stream()
                .allMatch(seq2 -> equalByWildcards(seq1, seq2)))) {
            sequences.putAll(that.sequences);
            that.sequences.putAll(sequences);
            return true;
        } else
            return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
