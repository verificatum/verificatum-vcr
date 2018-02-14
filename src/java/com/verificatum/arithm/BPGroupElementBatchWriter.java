
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
            }
        }
        btw.close();
    }
}
