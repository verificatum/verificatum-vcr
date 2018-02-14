
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

/**
 * Triple of objects.
 *
 * @param <F> Type of first member.
 * @param <S> Type of second member.
 * @param <T> Type of third member.
 *
 * @author Douglas Wikstrom
 */
public class Triple<F, S, T> {

    /**
     * First member.
     */
    public F first;

    /**
     * Second member.
     */
    public S second;

    /**
     * Third member.
     */
    public T third;

    /**
     * Creates a triple.
     *
     * @param first First member.
     * @param second Second member.
     * @param third Third member.
     */
    public Triple(final F first, final S second, final T third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }
}
