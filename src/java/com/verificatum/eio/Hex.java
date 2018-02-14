
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Utility class for converting hexadecimal strings to/from
 * <code>byte[]</code>.
 *
 * @author Douglas Wikstrom
 */
public final class Hex {

    /**
     * Number of bytes in buffer used for copying.
     */
    static final int COPY_BUFFER_SIZE = 4096;

    /**
     * Avoid accidental instantiation.
     */
    private Hex() { }

    /**
     * Used to translate to hex code.
     */
    static final char[] HEXTABLE =
    {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
     'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * Returns the hex code of the input. It is assumes that the input
     * is an integer between 0 and 15. Otherwise the output is
     * undefined.
     *
     * @param i Integer to be translated.
     * @return Hexadecimal code for the input integer.
     */
    public static char toHex(final int i) {
        return HEXTABLE[i % 16];
    }

    /**
     * Returns the hex code representation of a <code>byte[]</code>.
     *
     * @param array Array to be translated.
     * @return Representation of the input in hexadecimal.
     */
    public static String toHexString(final byte[] array) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < array.length; i++) {
            final byte b1 = (byte) (array[i] & 0x0F);
            final byte b2 = (byte) ((array[i] & 0xF0) >>> 4);

            sb = sb.append(HEXTABLE[b2]).append(HEXTABLE[b1]);
        }
        return sb.toString();
    }

    /**
     * Converts a hexadecimal <code>String</code> of even length into
     * a <code>byte[]</code>. If the input does not have even length a
     * leading zero is prepended to the input before processing.
     * Characters that do not represent hexadecimal digits are
     * replaced by "0" before conversion.
     *
     * @param hexString Hexadecimal <code>String</code> of even
     * length.
     * @return Representation of the input as a <code>byte[]</code>.
     */
    public static byte[] toByteArray(final String hexString) {

        String hs = hexString;

        if (hs.length() % 2 != 0) {
            hs = hexString + "0";
        }

        final byte[] result = new byte[hs.length() / 2];
        for (int i = 0; i < result.length; i++) {
            final String subStr = hs.substring(2 * i, 2 * i + 2);
            try {
                result[i] = (byte) Integer.parseInt(subStr, 16);
            } catch (final NumberFormatException nfe) {
                result[i] = 0;
            }
        }
        return result;
    }

    /**
     * Reads a byte tree from the input file and prints nested JSON
     * arrays representation of it to the output file.
     *
     * @param inputFile File containing byte tree.
     * @param outputFile Destination of nested JSON arrays.
     * @throws IOException If the input is not a byte tree.
     */
    public static void toByteArray(final File inputFile,
                                   final File outputFile)
        throws IOException {

        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {

            final Charset charset = Charset.forName("US-ASCII");

            fis = new FileInputStream(inputFile);
            fos = new FileOutputStream(outputFile);

            final byte[] buf = new byte[COPY_BUFFER_SIZE];
            int len = fis.read(buf);
            while (len >= 0) {

                final String s = new String(buf, 0, len, charset);

                fos.write(Hex.toByteArray(s));
                len = fis.read(buf);
            }
        } finally {
            ExtIO.strictClose(fos);
            ExtIO.strictClose(fis);
        }
    }
}
