
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

import com.verificatum.eio.ByteTreeWriterF;

/**
 * Writes a batch of group elements in a separate thread. This allows
 * making better use of multiple cores.
 *
 * @author Douglas Wikstrom
 */
public class BPGroupElementBatchWriter {

    /**
     * Destination of group elements.
     */
    ByteTreeWriterF btw;

    /**
     * Indicates if this instance is still writing.
     */
    boolean active;

    /**
     * Creates a group element writer.
     *
     * @param btw Destination of group elements.
     */
    BPGroupElementBatchWriter(final ByteTreeWriterF btw) {
        this.btw = btw;
        this.active = false;
    }

    /**
     * Write the next batch of group element to this writer.
     *
     * @param batch Next batch of group elements.
     */
    void writeNext(final PGroupElement[] batch) {

        while (active) {
            try {
                Thread.sleep(100);
            } catch (final InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }

        active = true;

        final Thread thread = new Thread() {

                @Override
                public void run() {
                    btw.unsafeWrite(batch);
                    active = false;
                }
            };
        thread.start();
    }

    /**
     * Release allocated resources. This returns only after all
     * elements in the last write operation have been written.
     */
    void close() {
        while (active) {
            try {
                Thread.sleep(100);
            } catch (final InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
        btw.close();
    }
}
