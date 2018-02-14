
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

package com.verificatum.arithm;

/**
 * Iterator over a {@link ModPGroupElementArray}.
 *
 * @author Douglas Wikstrom
 */
public final class ModPGroupElementIterator implements PGroupElementIterator {

    /**
     * Underlying group.
     */
    ModPGroup modPGroup;

    /**
     * Underlying iterator over integers.
     */
    LargeIntegerIterator integerIterator;

    /**
     * Creates an instance over a {@link ModPGroupElementArray}.
     *
     * @param modPGroup Underlying group.
     * @param integerIterator Underlying integer iterator.
     */
    public
        ModPGroupElementIterator(final ModPGroup modPGroup,
                                 final LargeIntegerIterator integerIterator) {
        this.modPGroup = modPGroup;
        this.integerIterator = integerIterator;
    }

    // Documented in PGroupElementIterator.java

    @Override
    public PGroupElement next() {
        final LargeInteger integer = integerIterator.next();
        if (integer == null) {
            return null;
        } else {
            return modPGroup.toElement(integer);
        }
    }

    @Override
    public boolean hasNext() {
        return integerIterator.hasNext();
    }

    @Override
    public void close() {
        integerIterator.close();
    }
}
