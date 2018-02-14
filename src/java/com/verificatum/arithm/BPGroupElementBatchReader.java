
/*
 * Copyright 2008-2018 Douglas Wikstrom
 *
 * This file is part of Verificatum Core Routines (VCR).
 *
 * VCR is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * VCR is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with VCR. If not, see <http://www.gnu.org/licenses/>.
 */

package com.verificatum.arithm;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.verificatum.eio.ByteTreeReader;

/**
 * Reads batch of group elements in a separate thread. This allows
 * making better use of multiple cores.
 *
 * @author Douglas Wikstrom
 */
public final class BPGroupElementBatchReader {

    /**
     * Delay between reading attempts in milliseconds.
     */
    public static final int BATCH_SLEEP_TIME = 100;

    /**
     * Queue used for the producer-consumer pattern.
     */
    BlockingQueue<PGroupElement[]> bq;

    /**
     * Producer thread of batches of element arrays.
     */
    BPGroupElementBatchReaderThread reader;

    /**
     * Creates a group element reader.
     *
     * @param pGroup Underlying group.
     * @param btr Source of group elements.
     */
    BPGroupElementBatchReader(final PGroup pGroup, final ByteTreeReader btr) {
        this.bq = new LinkedBlockingQueue<PGroupElement[]>(2);
        this.reader = new BPGroupElementBatchReaderThread(pGroup, btr, bq);
        reader.start();
    }

    /**
     * Return the next batch of elements in this reader, or null if
     * there are no more elements.
     *
     * @return Next batch of elements.
     */
    PGroupElement[] readNext() {
        try {
            final PGroupElement[] elements = bq.take();
            if (elements.length == 0) {
                return null;
            } else {
                return elements;
            }
        } catch (final InterruptedException ie) {
            throw new ArithmError("Failed to read next batch!", ie);
        }
    }

    /**
     * Release allocated resources.
     */
    void close() {
        reader.close();
    }
}

/**
 * Thread running in the background that reads batches of group
 * elements.
 */
final class BPGroupElementBatchReaderThread extends Thread {

    /**
     * Underlying group.
     */
    PGroup pGroup;

    /**
     * Source of elements.
     */
    ByteTreeReader btr;

    /**
     * Blocking queue.
     */
    BlockingQueue<PGroupElement[]> bq;

    /**
     * Used to abort execution.
     */
    boolean running;

    /**
     * Creates thread running in the background that reads batches of
     * group elements.
     *
     * @param pGroup Group containing the elements read.
     * @param btr Source of group elements.
     * @param bq Destination of batches of group elements.
     */
    BPGroupElementBatchReaderThread(final PGroup pGroup,
                                    final ByteTreeReader btr,
                                    final BlockingQueue<PGroupElement[]> bq) {
        this.pGroup = pGroup;
        this.btr = btr;
        this.bq = bq;
        this.running = true;
    }

    /**
     * Halt this producer thread.
     */
    public void close() {
        running = false;
    }

    @Override
    public void run() {
        while (running && btr.getRemaining() > 0) {
            if (bq.size() == 0) {
                bq.add(BPGroupElementArrayF.readBatch(pGroup, btr));
            } else {
                try {
                    Thread.sleep(BPGroupElementBatchReader.BATCH_SLEEP_TIME);
                } catch (final InterruptedException ie) {
                }
            }
        }

        // We use the zero length array as the end of stream indicator.
        bq.add(new BPGroupElement[0]);
        btr.close();
    }
}
