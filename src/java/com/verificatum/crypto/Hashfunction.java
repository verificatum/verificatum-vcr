
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

/**
 * Interface for a collision-free hash function with any length input.
 *
 * @author Douglas Wikstrom
 */
public interface Hashfunction extends HashfunctionBasic {

    /**
     * Evaluates the function on the given input.
     *
     * @param datas Input data.
     * @return Output of hash function.
     */
    byte[] hash(byte[]... datas);

    /**
     * Returns an updateable digest.
     *
     * @return Updateable digest.
     */
    Hashdigest getDigest();
}
