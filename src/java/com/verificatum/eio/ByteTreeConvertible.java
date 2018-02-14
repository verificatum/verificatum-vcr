
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
 * This interface captures the ability of an instance to convert
 * itself to a {@link ByteTreeBasic}.
 *
 * @author Douglas Wikstrom
 */
public interface ByteTreeConvertible {

    /**
     * Returns a representation of this instance. The output byte tree
     * is only valid as long as this instance is alive. This is
     * important when using classes which requires active deletion.
     *
     * @return Representation of this instance.
     */
    ByteTreeBasic toByteTree();
}
