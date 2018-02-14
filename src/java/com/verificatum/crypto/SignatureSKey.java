
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
 * Interface representing a secret signature key.
 *
 * @author Douglas Wikstrom
 */
public interface SignatureSKey extends Marshalizable {

    /**
     * Sign the input and output the resulting signature.
     *
     * @param message Data to be signed.
     * @param randomSource Source of randomness used to create the
     * signature.
     * @return Signature of the message.
     */
    byte[] sign(RandomSource randomSource, byte[]... message);

    /**
     * Sign the input hashdigest and output the resulting signature.
     * Unless a hashdigest {@link #getDigest()} is used to compute the
     * digest the result is undefined.
     *
     * @param d Hash digest to be signed.
     * @param randomSource Source of randomness used to create the
     * signature.
     * @return Signature of the digest.
     */
    byte[] signDigest(RandomSource randomSource, byte[] d);

    /**
     * Returns an updateable digest.
     *
     * @return Updateable digest.
     */
    Hashdigest getDigest();
}
