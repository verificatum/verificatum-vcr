
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
