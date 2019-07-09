
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

package com.verificatum.ui.info;

import java.net.InetSocketAddress;

/**
 * Inet with port data field that can be used in an XML configuration
 * file.
 *
 * @author Douglas Wikstrom
 */
public class InetPortField extends NetField {

    /**
     * Creates an instance.
     *
     * @param name Name of instance.
     * @param description Description of this field.
     * @param minOccurs Minimum number of times the field must occur
     * in the context it is used (inklusive lower bound).
     * @param maxOccurs Strict upper bound on the number of times the
     * field may occur in the context it is used (exlusive
     * upper bound).
     */
    public InetPortField(final String name,
                         final String description,
                         final int minOccurs,
                         final int maxOccurs) {
        super(name, description, minOccurs, maxOccurs, "inetport",
              XSTypes.INET_PORT_PATTERN);
    }

    /**
     * Validate that input represents an inet address.
     *
     * @param value String to verify.
     *
     * @throws InfoException If the input does not represent an inet
     * address.
     */
    public void validate(final String value) throws InfoException {
        super.validate(value);
        final String[] s = value.split(":");
        if (s.length != 2) {
            throw new InfoException("Invalid InetPortField! (" + value + ")");
        }
        try {
            final int port = Integer.parseInt(s[1]);
            new InetSocketAddress(s[0], port);
        } catch (final NumberFormatException nfe) {
            throw new InfoException("Invalid port number! (" + s[1] + ")", nfe);
        }
    }
}
