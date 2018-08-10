/*
 * Copyright (c) 2016-2018, MiLaboratory LLC
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
package com.milaboratory.mist.io;

import com.milaboratory.mist.outputconverter.ParsedRead;
import com.milaboratory.mist.pattern.GroupEdge;
import com.milaboratory.primitivio.PrimitivO;
import com.milaboratory.util.CanReportProgress;

import java.io.*;

import static java.lang.Double.NaN;

public final class MifWriter implements AutoCloseable, CanReportProgress {
    private static final int DEFAULT_BUFFER_SIZE = 1 << 20;
    private final PrimitivO output;
    private boolean finished = false;
    private long estimatedNumberOfReads = -1;
    private long writtenReads = 0;

    public MifWriter(OutputStream outputStream, MifHeader mifHeader) {
        output = new PrimitivO(outputStream);
        writeHeader(mifHeader);
    }

    public MifWriter(String file, MifHeader mifHeader) throws IOException {
        output = new PrimitivO(new BufferedOutputStream(new FileOutputStream(file), DEFAULT_BUFFER_SIZE));
        writeHeader(mifHeader);
    }

    public MifWriter(String file, MifHeader mifHeader, int bufferSize) throws IOException {
        output = new PrimitivO(new BufferedOutputStream(new FileOutputStream(file), bufferSize));
        writeHeader(mifHeader);
    }

    private void writeHeader(MifHeader mifHeader) {
        output.writeInt(mifHeader.getNumberOfReads());
        output.writeInt(mifHeader.getCorrectedGroups().size());
        for (String correctedGroup : mifHeader.getCorrectedGroups())
            output.writeObject(correctedGroup);
        output.writeBoolean(mifHeader.isSorted());
        output.writeInt(mifHeader.getGroupEdges().size());
        for (GroupEdge groupEdge : mifHeader.getGroupEdges()) {
            output.writeObject(groupEdge);
            output.putKnownObject(groupEdge);
        }
    }

    public void write(ParsedRead parsedRead) {
        output.writeObject(parsedRead);
        writtenReads++;
    }

    @Override
    public void close() {
        output.writeObject(null);
        output.close();
        finished = true;
    }

    public void setEstimatedNumberOfReads(long estimatedNumberOfReads) {
        this.estimatedNumberOfReads = estimatedNumberOfReads;
    }

    @Override
    public double getProgress() {
        if (estimatedNumberOfReads == -1)
            return (writtenReads == 0) ? 0 : NaN;
        else
            return Math.min(1, (double)writtenReads / estimatedNumberOfReads);
    }

    @Override
    public boolean isFinished() {
        return finished;
    }
}
