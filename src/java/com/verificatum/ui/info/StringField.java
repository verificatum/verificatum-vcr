
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
 * A <code>String</code> data field that can be used in an XML
 * configuration file.
 *
 * @author Douglas Wikstrom
 */
public final class StringField extends InfoField {

    /**
     * Minimal length of strings that can be stored in this instance
     * (inklusive lower bound).
     */
    int minLengthInclusive;

    /**
     * Upper bound on the length of strings that can be stored in this
     * instance (exklusive upper bound).
     */
    int maxLengthExclusive;

    /**
     * Indicates if there are any bounds on this field.
     */
    private final boolean bounded;

    /**
     * XSD pattern for string content.
     */
    private String pattern = null;

    /**
     * Creates an instance.
     *
     * @param name Name of instance.
     * @param minOccurs Minimum number of times the field must occur
     * in the context it is used (inklusive lower bound).
     * @param maxOccurs Strict upper bound on the number of times the
     * field may occur in the context it is used (exlusive
     * upper bound).
     */
    public StringField(final String name,
                       final int minOccurs,
                       final int maxOccurs) {
        super(name, minOccurs, maxOccurs);
        bounded = false;
    }

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
    public StringField(final String name,
                       final String description,
                       final int minOccurs,
                       final int maxOccurs) {
        super(name, description, minOccurs, maxOccurs);
        bounded = false;
    }

    /**
     * Creates an instance with double sided bounds on length.
     *
     * @param name Name of instance.
     * @param minOccurs Minimum number of times the field must occur
     * in the context it is used (inklusive).
     * @param maxOccurs Strict upper bound on the number of times the
     * field may occur in the context it is used
     * (exklusive).
     * @param minLengthInclusive Minimal length of strings that this field
     * represents (inklusive).
     * @param maxLengthExclusive Upper bound on the length of strings that
     * this field represents (exklusive).
     */
    public StringField(final String name,
                       final int minOccurs,
                       final int maxOccurs,
                       final int minLengthInclusive,
                       final int maxLengthExclusive) {
        this(name, "", minOccurs, maxOccurs, minLengthInclusive,
             maxLengthExclusive);
    }

    /**
     * Creates an instance with double sided bounds on value.
     *
     * @param name Name of instance.
     * @param description Description of field.
     * @param minOccurs Minimum number of times the field must occur
     * in the context it is used (inklusive).
     * @param maxOccurs Strict upper bound on the number of times the
     * field may occur in the context it is used
     * (exklusive).
     * @param minLengthInclusive Minimal length of strings that this field
     * represents (inklusive).
     * @param maxLengthExclusive Upper bound on the length of strings that
     * this field represents (exklusive).
     */
    public StringField(final String name,
                       final String description,
                       final int minOccurs,
                       final int maxOccurs,
                       final int minLengthInclusive,
                       final int maxLengthExclusive) {
        super(name, description, minOccurs, maxOccurs);
        this.minLengthInclusive = minLengthInclusive;
        this.maxLengthExclusive = maxLengthExclusive;
        bounded = true;
    }

    /**
     * Creates an instance with upper bound only.
     *
     * @param name Name of instance.
     * @param description Description of field.
     * @param minOccurs Minimum number of times the field must occur
     * in the context it is used (inklusive).
     * @param maxOccurs Strict upper bound on the number of times the
     * field may occur in the context it is used
     * (exklusive).
     * @param minLengthInclusive Minimal length of strings that this field
     * represents (inklusive).
     * @param maxLengthExclusive Should be <code>"unbounded"</code>, but
     * this parameter is in fact ignored (exklusive).
     */
    public StringField(final String name,
                       final String description,
                       final int minOccurs,
                       final int maxOccurs,
                       final int minLengthInclusive,
                       final String maxLengthExclusive) { // NOPMD
        super(name, description, minOccurs, maxOccurs);
        this.minLengthInclusive = minLengthInclusive;
        this.maxLengthExclusive = Integer.MAX_VALUE;
        bounded = true;
    }

    /**
     * Creates an instance with upper bound only.
     *
     * @param name Name of instance.
     * @param minOccurs Minimum number of times the field must occur
     * in the context it is used (inklusive).
     * @param maxOccurs Strict upper bound on the number of times the
     * field may occur in the context it is used
     * (exklusive).
     * @param minLengthInclusive Minimal length of strings that this field
     * represents (inklusive).
     * @param maxLengthExclusive Should be <code>"unbounded"</code>, but
     * this parameter is in fact ignored (exklusive).
     */
    public StringField(final String name,
                       final int minOccurs,
                       final int maxOccurs,
                       final int minLengthInclusive,
                       final String maxLengthExclusive) { // NOPMD
        this(name, "", minOccurs, maxOccurs, minLengthInclusive,
             maxLengthExclusive);
    }

    /**
     * Creates an instance with lower bound only.
     *
     * @param name Name of instance.
     * @param minOccurs Minimum number of times the field must occur
     * in the context it is used (inklusive).
     * @param maxOccurs Strict upper bound on the number of times the
     * field may occur in the context it is used
     * (exklusive).
     * @param minLengthInclusive Should be <code>"unbounded"</code>, but
     * this parameter is in fact ignored (inklusive).
     * @param maxLengthExclusive Upper bound on the length of strings that
     * this field represents (exklusive).
     */
    public StringField(final String name,
                       final int minOccurs,
                       final int maxOccurs,
                       final String minLengthInclusive, // NOPMD
                       final int maxLengthExclusive) {
        super(name, minOccurs, maxOccurs);
        this.minLengthInclusive = Integer.MIN_VALUE;
        this.maxLengthExclusive = maxLengthExclusive;
        bounded = true;
    }

    /**
     * Set XSD regexp pattern.
     *
     * @param pattern XSD regexp pattern.
     * @return This field after setting the pattern.
     */
    public StringField setPattern(final String pattern) {
        this.pattern = pattern;
        return this;
    }

    // These methods are documented in the super class.

    @Override
    public String schemaElementString() {
        final StringBuffer sb = new StringBuffer();

        sb.append("<xs:element name=\"" + name + "\"\n");
        if (!bounded && pattern == null) {
            sb.append("            type=\"xs:string\"\n");
        }
        sb.append("            minOccurs=\"" + minOccurs + "\"\n");
        sb.append("            maxOccurs=\"" + maxOccurs + "\"");

        if (!bounded && pattern == null) {
            sb.append("/>");
        }

        if (bounded || pattern != null) {
            sb.append(">\n");
            sb.append("<xs:simpleType>\n");
            sb.append("   <xs:restriction base=\"xs:string\">\n");
            if (bounded) {
                sb.append("      <xs:minLength value=\"" + minLengthInclusive
                          + "\"/>\n");
                sb.append("      <xs:maxLength value=\"" + maxLengthExclusive
                          + "\"/>\n");
            }
            if (pattern != null) {
                sb.append("      <xs:pattern value=\"" + pattern + "\"/>\n");
            }
            sb.append("   </xs:restriction>\n");
            sb.append("</xs:simpleType>\n");
            sb.append("</xs:element>\n");
        }

        return sb.toString();
    }

    /**
     * Validate that the value satisfies the restrictions.
     *
     * @param value String to be validated.
     *
     * @throws InfoException In validates that an integer falls within
     * a given interval.
     */
    public void validate(final String value) throws InfoException {
        if (bounded) {
            if (value.length() < minLengthInclusive) {
                throw new InfoException("Value is too short! ("
                                        + value + " < "
                                        + minLengthInclusive + ")");
            }
            if (maxLengthExclusive <= value.length()) {
                throw new InfoException("Value is too long! ("
                                        + value + " < "
                                        + maxLengthExclusive + ")");
            }
        }
        if (pattern != null) {
            final Pattern p = Pattern.compile(pattern);
            final Matcher m = p.matcher(value);
            if (!m.matches()) {
                throw new InfoException("Value does not match expression! "
                                        + "(\"" + value + "\" not contained in "
                                        + pattern + ")");
            }
        }
    }

    @Override
    public Object parse(final String value) throws InfoException {
        validate(value);
        return value;
    }
}
