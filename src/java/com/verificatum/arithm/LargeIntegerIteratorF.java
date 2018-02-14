
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

import java.io.File;

import com.verificatum.eio.ByteTreeF;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.eio.EIOException;


/**
 * Interface for an iterator over a {@link LargeIntegerArray}.
 *
 * @author Douglas Wikstrom
 */
public final class LargeIntegerIteratorF implements LargeIntegerIterator {

    /**
     * Underlying source of elements.
     */
    private final ByteTreeReader btr;

    /**
     * Creates an iterator reading from the given array.
     *
     * @param file Underlying file
     */
    public LargeIntegerIteratorF(final File file) {
        this.btr = new ByteTreeF(file).getByteTreeReader();
    }

    // Documented in LargeIntegerIterator.java

    @Override
    public LargeInteger next() {
        if (btr.getRemaining() > 0) {
            try {
                return new LargeInteger(btr.getNextChild());
            } catch (final ArithmFormatException afe) {
                throw new ArithmError("Unable to read integer!", afe);

            // UNCOVERABLE (We are guaranteed there are children.)
            } catch (final EIOException eioe) {
                throw new ArithmError("Unable to read integer!", eioe);
            }
        } else {
            return null;
        }
    }

    @Override
    public boolean hasNext() {
        return btr.getRemaining() > 0;
    }

    @Override
    public void close() {
        btr.close();
    }
}
