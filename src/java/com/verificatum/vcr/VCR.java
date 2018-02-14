
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

package com.verificatum.vcr;

/**
 * Global utility functions that has to do with any Verificatum
 * software.
 *
 * @author Douglas Wikstrom
 */
public final class VCR {

    /**
     * Avoid accidental instantiation.
     */
    private VCR() { }

    /**
     * Avoid accidental instantiation.
     *
     * @return Version of this library.
     */
    public static String version() {
        return VCR.class.getPackage().getSpecificationVersion();
    }

    /**
     * Returns the major version number as an integer.
     *
     * @return Major version number
     */
    public static int major() {
        return Integer.parseInt(version().split("\\.")[0]);
    }

    /**
     * Returns the minor version number as an integer.
     *
     * @return Minor version number
     */
    public static int minor() {
        return Integer.parseInt(version().split("\\.")[1]);
    }

    /**
     * Returns the revision version number as an integer.
     *
     * @return Revision version number
     */
    public static int revision() {
        return Integer.parseInt(version().split("\\.")[2]);
    }
}
