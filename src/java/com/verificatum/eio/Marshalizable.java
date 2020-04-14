
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

package com.verificatum.eio;

/**
 * Interface capturing the ability of the instance of a class to be
 * marshalled into a byte tree with type information that can later be
 * restored to the instance. This is useful to, e.g., be able to
 * obliviously recover an instance of a subclass of {@link
 * com.verificatum.arithm.PGroup} of the precise subclass.
 *
 * <p>
 *
 * <b>Convention.</b> For this to be possible we require by convention
 * that every class that implements this interface also implements a
 * static method with one of the following signatures:
 *
 * <p>
 *
 * <code>
 * public static {@link Object}
 *     newInstance({@link ByteTreeReader} btr,
 *                 {@link com.verificatum.crypto.RandomSource} rs,
 *                 int certainty)
 * </code>
 * <br>
 * <code>
 * public static {@link Object} newInstance({@link ByteTreeReader} btr)
 * </code>
 *
 * <p>
 *
 * The first method allows probabilistically checking the input using
 * the given source of randomness. The error probability should be
 * bounded by <i>2<sup>- <code>certainty</code></sup></i>.
 *
 * @author Douglas Wikstrom
 */
public interface Marshalizable extends ByteTreeConvertible {

    /**
     * Returns a brief human friendly description of this instance.
     * This is merely a comment which can not be used to recover the
     * instance.
     *
     * @param verbose Decides if the description should be verbose or
     * not.
     * @return Human friendly description of this instance.
     */
    String humanDescription(boolean verbose);
}
