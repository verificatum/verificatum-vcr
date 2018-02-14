
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

import com.verificatum.eio.Marshalizable;

/**
 * Interface representing a public signature key.
 *
 * @author Douglas Wikstrom
 */
public interface SignaturePKey extends Marshalizable {

    /**
     * Verify the given signature and message using this public key.
     *
     * @param signature Candidate signature.
     * @param message Data that supposedly is signed.
     * @return Verdict for the signature and message pair.
     */
    boolean verify(byte[] signature, byte[]... message);

    /**
     * Verify the given signature and digest using this public key.
     * The result is undefined unless {@link #getDigest()} was used to
     * compute the digest.
     *
     * @param signature Candidate signature.
     * @param d Digest that supposedly is signed.
     * @return Verdict for the signature and message pair.
     */
    boolean verifyDigest(byte[] signature, byte[] d);

    /**
     * Returns an updateable digest that can later be given as input
     * to {@link #verify(byte[],byte[][])}.
     *
     * @return Updateable digest.
     */
    Hashdigest getDigest();
}
