
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

package com.verificatum.util;

import com.verificatum.crypto.Hashdigest;
import com.verificatum.crypto.HashfunctionHeuristic;
import com.verificatum.eio.ByteTreeConvertible;
import com.verificatum.eio.ExtIO;

/**
 * Various utility functions.
 *
 * @author Douglas Wikstrom
 */
public final class Functions {

    /**
     * Avoid accidental instantiation.
     */
    private Functions() {
    }

    /**
     * Default implementation of hash code for objects that can be
     * converted to a byte tree. This is slow, but very conservative.
     *
     * @param convertible Convertible object.
     * @return Hash code.
     */
    public static int hashCode(final ByteTreeConvertible convertible) {

        // This is a correct, but very slow implementation.

        final HashfunctionHeuristic hh = new HashfunctionHeuristic("SHA-256");
        final Hashdigest hd = hh.getDigest();
        convertible.toByteTree().update(hd);
        final byte[] d = hd.digest();
        return ExtIO.readInt(d, 0);
    }
}
