
/* Copyright 2008-2019 Douglas Wikstrom
 *
 * This file is part of Verificatum Core Routines (VCR).
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
            Thread.currentThread().interrupt();
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
                    Thread.currentThread().interrupt();
                }
            }
        }

        // We use the zero length array as the end of stream indicator.
        bq.add(new LargeInteger[0]);
        btr.close();
    }
}
