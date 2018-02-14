
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
 * Interface representing a public key of a cryptosystem with labels.
 *
 * <p>
 *
 * <b>If you implement this interface you MUST override
 * {@link Object#equals(Object)} as well. Unfortunately, it is not
 * possible to enforce this using interfaces in Java.</b>
 *
 * @author Douglas Wikstrom
 */
public interface CryptoPKey extends Marshalizable {

    /**
     * Encrypts the given message using randomness from the given
     * source.
     *
     * @param label Label used when encrypting.
     * @param message Message to be encrypted.
     * @param randomSource Source of randomness.
     * @param statDist Allowed statistical error from ideal
     * distribution.
     * @return Resulting ciphertext.
     */
    byte[] encrypt(byte[] label, byte[] message,
                   RandomSource randomSource, int statDist);
}
