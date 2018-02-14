
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

package com.verificatum.ui.gen;

import com.verificatum.crypto.RandomSource;

/**
 * Interface representing the ability of a class to be used by
 * {@link GeneratorTool} to generate an instance based on some command
 * line arguments. This is used to generate basic cryptographic
 * objects, e.g., keys for signature schemes, groups in which the
 * discrete logarithm problem is hard, or provably secure
 * collision-free hashfunctions.
 *
 * @author Douglas Wikstrom
 */
public interface Generator {

    /**
     * Outputs a string representation of an instance generated
     * according to the input instructions.
     *
     * @param randomSource Source of randomness that can be used in
     * generation.
     * @param args Instructions for how to generate the instance.
     * @return Description of an instance.
     * @throws GenException If the parameters are malformed or if the
     *  given random source is <code>null</code> and a
     *  functioning random source is needed to generate the
     *  required instance. In this case the description of
     *  the exception should be suitable to present to the
     *  user as an error message.
     */
    String gen(RandomSource randomSource, String[] args)
        throws GenException;

    /**
     * Returns a brief description of the implementing class.
     *
     * @return Brief description of class.
     */
    String briefDescription();
}
