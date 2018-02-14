
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
 * Basic interface for hash functions.
 *
 * <p>
 *
 * <b>If you implement this interface you MUST override
 * {@link Object#equals(Object)} as well. Unfortunately, it is not
 * possible to enforce this using interfaces in Java.</b>
 *
 * @author Douglas Wikstrom
 */
public interface HashfunctionBasic extends Marshalizable {

    /**
     * Returns the number of bits in the output.
     *
     * @return Number of bits in output.
     */
    int getOutputLength();
}
