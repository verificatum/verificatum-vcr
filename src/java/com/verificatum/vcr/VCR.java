
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
