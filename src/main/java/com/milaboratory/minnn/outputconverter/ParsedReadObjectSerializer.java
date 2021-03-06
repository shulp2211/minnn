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
package com.milaboratory.minnn.outputconverter;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.minnn.pattern.GroupEdge;
import com.milaboratory.primitivio.PrimitivI;
import com.milaboratory.primitivio.PrimitivO;
import com.milaboratory.primitivio.SerializersManager;
import com.milaboratory.util.ObjectSerializer;

import java.io.*;
import java.util.*;

public final class ParsedReadObjectSerializer implements ObjectSerializer<ParsedRead> {
    private final List<GroupEdge> groupEdges;
    private final SerializersManager serializersManager = new SerializersManager();

    public ParsedReadObjectSerializer(List<GroupEdge> groupEdges) {
        this.groupEdges = groupEdges;
    }

    @Override
    public void write(Collection<ParsedRead> data, OutputStream stream) {
        final PrimitivO out = new PrimitivO(new DataOutputStream(stream), serializersManager);
        groupEdges.forEach(out::putKnownObject);
        for (ParsedRead parsedRead : data)
            out.writeObject(parsedRead);
        out.writeObject(null);
    }

    @Override
    public OutputPort<ParsedRead> read(InputStream stream) {
        final PrimitivI in = new PrimitivI(new DataInputStream(stream), serializersManager);
        groupEdges.forEach(in::putKnownObject);
        return () -> in.readObject(ParsedRead.class);
    }
}
