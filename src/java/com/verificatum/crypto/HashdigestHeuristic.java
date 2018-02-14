
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

package com.verificatum.crypto;

import java.security.MessageDigest;

/**
 * Interface for a digest of a collision-free hash function.
 *
 * @author Douglas Wikstrom
 */
public final class HashdigestHeuristic implements Hashdigest {

    /**
     * Message digest wrapped by this instance.
     */
    MessageDigest md;

    /**
     * Constructs a wrapper for the given message digest.
     *
     * @param md Message digest wrapped by this instance.
     */
    public HashdigestHeuristic(final MessageDigest md) {
        this.md = md;
    }

    // Documented in Hashdigest.java

    @Override
    public void update(final byte[]... data) {
        for (int i = 0; i < data.length; i++) {
            md.update(data[i]);
        }
    }

    @Override
    public void update(final byte[] data, final int offset, final int length) {
        md.update(data, offset, length);
    }

    @Override
    public byte[] digest() {
        return md.digest();
    }
}
