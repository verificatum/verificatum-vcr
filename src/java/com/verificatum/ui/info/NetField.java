
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
