
/* Copyright 2008-2019 Douglas Wikstrom
 *
 * This file is part of Verificatum Core Routines (VCR).
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
