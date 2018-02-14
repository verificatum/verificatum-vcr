
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
 * Reads batch of integers in a separate thread. This allows making
 * better use of multiple cores.
 *
 * @author Douglas Wikstrom
 */
public final class LargeIntegerBatchReader {

    /**
     * Delay between reading attempts in milliseconds.
     */
    public static final int BATCH_SLEEP_TIME = 100;

    /**
     * Queue used for the producer-consumer pattern.
     */
    BlockingQueue<LargeInteger[]> bq;

    /**
     * Producer thread of batches of integer arrays.
     */
    LargeIntegerBatchReaderThread reader;

    /**
     * Creates a reader using the given source.
     *
     * @param btr Source of integers.
     */
    LargeIntegerBatchReader(final ByteTreeReader btr) {
        this.bq = new LinkedBlockingQueue<LargeInteger[]>(2);
        this.reader = new LargeIntegerBatchReaderThread(btr, bq);
        reader.start();
    }

    /**
     * Return the next batch of integers in this reader, or null if
     * there are no more elements.
     *
     * @return Next batch of integers.
     */
    LargeInteger[] readNext() {
        try {
            final LargeInteger[] integers = bq.take();
            if (integers.length == 0) {
                return null;
            } else {
                return integers;
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
 * Thread reading batches in the backgrounds.
 */
final class LargeIntegerBatchReaderThread extends Thread {

    /**
     * Source of integers.
     */
    ByteTreeReader btr;

    /**
     * Blocking queue.
     */
    BlockingQueue<LargeInteger[]> bq;

    /**
     * Used to abort execution.
     */
    boolean running;

    /**
     * Creates a thread reading from the byte tree reader and putting
     * batches in the queue.
     *
     * @param btr Source of integers.
     * @param bq Destination for batches of integers.
     */
    LargeIntegerBatchReaderThread(final ByteTreeReader btr,
                                  final BlockingQueue<LargeInteger[]> bq) {
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
                bq.add(LargeIntegerArrayF.readBatch(btr));
            } else {
                try {
                    Thread.sleep(LargeIntegerBatchReader.BATCH_SLEEP_TIME);
                } catch (final InterruptedException ie) {
                }
            }
        }

        // We use the zero length array as the end of stream indicator.
        bq.add(new LargeInteger[0]);
        btr.close();
    }
}
