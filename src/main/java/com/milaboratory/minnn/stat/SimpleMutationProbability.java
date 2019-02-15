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
package com.milaboratory.minnn.stat;

import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.Wildcard;

import static com.milaboratory.core.mutations.Mutation.getFromSymbol;
import static com.milaboratory.core.mutations.Mutation.getToSymbol;
import static com.milaboratory.core.mutations.Mutation.isInDel;
import static com.milaboratory.minnn.util.SequencesCache.charToWildcard;
import static com.milaboratory.minnn.util.SequencesCache.wildcards;

public final class SimpleMutationProbability implements MutationProbability {
    private final float basicSubstitutionProbability;
    private final float indelProbability;

    public SimpleMutationProbability(float basicSubstitutionProbability, float indelProbability) {
        this.basicSubstitutionProbability = basicSubstitutionProbability;
        this.indelProbability = indelProbability;
    }

    @Override
    public float mutationProbability(NSequenceWithQuality seq, int position, NSequenceWithQuality newLetter) {
        return mutationProbability(seq.getSequence(), position, newLetter.getSequence());
    }

    @Override
    public float mutationProbability(NucleotideSequence seq, int position, NucleotideSequence newLetter) {
        if ((position < 0) && (newLetter == NucleotideSequence.EMPTY))
            throw new IllegalArgumentException("Mutation must not be insertion and deletion in the same time: "
                    + "seq=" + seq + ", position=" + position + ", newLetter is empty");
        else if ((position < 0) || (newLetter == NucleotideSequence.EMPTY))
            return indelProbability;
        else {
            Wildcard wildcardFrom = wildcards.get(seq.getRange(position, position + 1));
            Wildcard wildcardTo = wildcards.get(newLetter);
            if ((wildcardFrom.getBasicMask() & wildcardTo.getBasicMask()) == 0)
                return basicSubstitutionProbability;
            else
                return 1;
        }
    }

    @Override
    public float mutationProbability(int mutationCode) {
        if (isInDel(mutationCode))
            return indelProbability;
        else {
            Wildcard wildcardFrom = charToWildcard.get(getFromSymbol(mutationCode, NucleotideSequence.ALPHABET));
            Wildcard wildcardTo = charToWildcard.get(getToSymbol(mutationCode, NucleotideSequence.ALPHABET));
            if ((wildcardFrom.getBasicMask() & wildcardTo.getBasicMask()) == 0)
                return basicSubstitutionProbability;
            else
                return 1;
        }
    }
}
