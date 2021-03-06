/*
 * Copyright (c) 2016-2020, MiLaboratory LLC
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
package com.milaboratory.minnn.pattern;

import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.minnn.io.IO;
import com.milaboratory.minnn.outputconverter.MatchedGroup;
import com.milaboratory.primitivio.PrimitivI;
import com.milaboratory.primitivio.PrimitivO;
import com.milaboratory.primitivio.annotations.Serializable;

import java.util.*;
import java.util.stream.Collectors;

@Serializable(by = IO.MatchSerializer.class)
public class Match {
    protected final int numberOfTargets;
    protected final long score;
    protected final ArrayList<MatchedGroupEdge> matchedGroupEdges;
    private ArrayList<MatchedGroup> groups = null;
    private Map<String, NSequenceWithQuality> groupValues = null;
    private HashMap<MatchedGroupEdgeIndex, MatchedGroupEdge> matchedGroupEdgesCache = null;

    /**
     * Serializable final match for single- or multi-pattern.
     *
     * @param numberOfTargets       number of targets (R1, R2 etc)
     * @param score                 match score
     * @param matchedGroupEdges     list of matched group edges
     */
    public Match(int numberOfTargets, long score, ArrayList<MatchedGroupEdge> matchedGroupEdges) {
        this.numberOfTargets = numberOfTargets;
        this.score = score;
        this.matchedGroupEdges = matchedGroupEdges;
    }

    /**
     * Return MatchedGroupEdge by name and isStart flag.
     *
     * @param groupName group name
     * @param isStart   flag, true if it must be group start, false if must be group end
     * @return          MatchedRange for specified pattern
     */
    public MatchedGroupEdge getMatchedGroupEdge(String groupName, boolean isStart) {
        if (matchedGroupEdgesCache == null)
            matchedGroupEdgesCache = new HashMap<>();
        MatchedGroupEdgeIndex index = new MatchedGroupEdgeIndex(groupName, isStart);
        MatchedGroupEdge cachedMatchedGroupEdge = matchedGroupEdgesCache.get(index);
        if (cachedMatchedGroupEdge != null)
            return cachedMatchedGroupEdge;
        else {
            for (MatchedGroupEdge matchedGroupEdge : matchedGroupEdges)
                if (matchedGroupEdge.getGroupName().equals(groupName) && (matchedGroupEdge.isStart() == isStart)) {
                    matchedGroupEdgesCache.put(index, matchedGroupEdge);
                    return matchedGroupEdge;
                }
            throw new IllegalStateException("Trying to get group " + (isStart ? "start" : "end") + " with name "
                    + groupName + " and it doesn't exist");
        }
    }

    /**
     * Get all matched group edges.
     *
     * @return ArrayList with all matched group edges.
     */
    public ArrayList<MatchedGroupEdge> getMatchedGroupEdges() {
        return matchedGroupEdges;
    }

    public int getNumberOfTargets() {
        return numberOfTargets;
    }

    public long getScore() {
        return score;
    }

    public void assembleGroups() {
        if (groups == null) {
            groups = new ArrayList<>();
            ArrayList<MatchedGroupEdge> matchedGroupEdges = getMatchedGroupEdges();
            /* in matches made with ParsedRead.retarget() we can have duplicate groups; in this case use first instance
               of each group */
            LinkedHashSet<String> groupNames = matchedGroupEdges.stream()
                    .map(MatchedGroupEdge::getGroupName).collect(Collectors.toCollection(LinkedHashSet::new));
            MatchedGroupEdge start;
            MatchedGroupEdge end;
            Range range;
            for (String groupName : groupNames) {
                start = getMatchedGroupEdge(groupName, true);
                end = getMatchedGroupEdge(groupName, false);
                if ((start.getPosition() == -1) ^ (end.getPosition() == -1))
                    throw new IllegalStateException("Group start and group end can be -1 only simultaneously. Start: "
                            + start.getPosition() + ", end: " + end.getPosition());
                else if ((start.getPosition() != -1) && (start.getPosition() > end.getPosition()))
                    throw new IllegalStateException("Group start must not be bigger than the end. Start: "
                            + start.getPosition() + ", end: " + end.getPosition());
                if ((start.getValueOverride() != null) ^ (start.getPosition() == -1))
                    throw new IllegalStateException("Value override must be set if position is -1 and not set if "
                            + "position is not -1. Value override: " + start.getValueOverride() + ", position: "
                            + start.getPosition());
                if (start.getTargetId() != end.getTargetId())
                    throw new IllegalStateException("Group start has targetId " + start.getTargetId()
                            + ", end has targetId " + end.getTargetId());
                if (start.getValueOverride() != null)
                    groups.add(new MatchedGroup(groupName, start.getTarget(), start.getTargetId(),
                            start.getValueOverride()));
                else {
                    range = new Range(start.getPosition(), end.getPosition());
                    groups.add(new MatchedGroup(groupName, start.getTarget(), start.getTargetId(), range));
                }
            }
        }
    }

    public ArrayList<MatchedGroup> getGroups() {
        assembleGroups();
        return new ArrayList<>(groups);
    }

    public NSequenceWithQuality getGroupValue(String groupName) {
        if (groupValues == null)
            groupValues = getGroups().stream()
                    .collect(Collectors.toMap(MatchedGroup::getGroupName, MatchedGroup::getValue));
        return groupValues.get(groupName);
    }

    public static Match read(PrimitivI input) {
        int numberOfPatterns = input.readVarIntZigZag();
        long score = input.readVarLongZigZag();
        ArrayList<MatchedGroupEdge> matchedGroupEdges = new ArrayList<>();
        int matchedGroupEdgesNum = input.readVarIntZigZag();
        for (int i = 0; i < matchedGroupEdgesNum; i++)
            matchedGroupEdges.add(input.readObject(MatchedGroupEdge.class));
        return new Match(numberOfPatterns, score, matchedGroupEdges);
    }

    public static void write(PrimitivO output, Match object) {
        output.writeVarIntZigZag(object.getNumberOfTargets());
        output.writeVarLongZigZag(object.getScore());
        output.writeVarIntZigZag(object.getMatchedGroupEdges().size());
        for (MatchedGroupEdge matchedGroupEdge : object.getMatchedGroupEdges())
            output.writeObject(matchedGroupEdge);
    }

    private static class MatchedGroupEdgeIndex {
        private final String groupName;
        private final boolean isStart;

        MatchedGroupEdgeIndex(String groupName, boolean isStart) {
            this.groupName = groupName;
            this.isStart = isStart;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MatchedGroupEdgeIndex that = (MatchedGroupEdgeIndex)o;

            return isStart == that.isStart && groupName.equals(that.groupName);
        }

        @Override
        public int hashCode() {
            int result = groupName.hashCode();
            result = 31 * result + (isStart ? 1 : 0);
            return result;
        }
    }
}
