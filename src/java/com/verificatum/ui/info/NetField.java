
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Network data field that can be used in an XML configuration file.
 *
 * @author Douglas Wikstrom
 */
public abstract class NetField extends InfoField {

    /**
     * Type of net field.
     */
    private final String type;

    /**
     * Regular expression for validation.
     */
    private final String pattern;

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
     * @param type Type of net field.
     * @param pattern Pattern for a string representing a location on
     * the Internet.
     */
    public NetField(final String name,
                    final String description,
                    final int minOccurs,
                    final int maxOccurs,
                    final String type,
                    final String pattern) {
        super(name, description, minOccurs, maxOccurs);
        this.type = type;
        this.pattern = pattern;
    }

    // These methods are documented in the super class.

    @Override
    public String schemaElementString() {
        return "<xs:element name=\"" + name + "\" "
            + "type=\"" + type + "\"/>";
    }

    /**
     * Validate that the value satisfies the restrictions.
     *
     * @param value String to be validated.
     *
     * @throws InfoException If the input string does not satisfy the
     * restrictions.
     */
    public void validate(final String value) throws InfoException {
        final Pattern p = Pattern.compile(pattern);
        final Matcher m = p.matcher(value);
        if (!m.matches()) {
            throw new InfoException("Value does not match expression! "
                                    + "(" + value + " is not " + type + ")");
        }
    }

    @Override
    public Object parse(final String value) throws InfoException {
        validate(value);
        return value;
    }
}
