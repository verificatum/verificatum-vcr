
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

/**
 * A integer data field that can be used in an XML configuration file.
 *
 * @author Douglas Wikstrom
 */
public final class IntField extends InfoField {

    /**
     * Smallest integer that can be stored in this instance (inklusive
     * lower bound).
     */
    int minInclusive;

    /**
     * Upper bound on the integer that can be stored in this instance
     * (exklusive upper bound).
     */
    int maxExclusive;

    /**
     * Indicates if there are any bounds on this field.
     */
    private final boolean bounded;

    /**
     * Creates an instance with unbounded value.
     *
     * @param name Name of instance.
     * @param minOccurs Minimum number of times the field must occur
     * in the context it is used.
     * @param maxOccurs Strict upper bound on the number of times the
     * field may occur in the context it is used.
     */
    public IntField(final String name,
                    final int minOccurs,
                    final int maxOccurs) {
        super(name, minOccurs, maxOccurs);
        bounded = false;
    }

    /**
     * Creates an instance with unbounded value.
     *
     * @param name Name of instance.
     * @param description Description of field.
     * @param minOccurs Minimum number of times the field must occur
     * in the context it is used.
     * @param maxOccurs Strict upper bound on the number of times the
     * field may occur in the context it is used.
     */
    public IntField(final String name,
                    final String description,
                    final int minOccurs,
                    final int maxOccurs) {
        super(name, description, minOccurs, maxOccurs);
        bounded = false;
    }

    /**
     * Creates an instance with double sided bounds on value.
     *
     * @param name Name of instance.
     * @param minOccurs Minimum number of times the field must occur
     * in the context it is used (inklusive).
     * @param maxOccurs Strict upper bound on the number of times the
     * field may occur in the context it is used
     * (exklusive).
     * @param minInclusive Minimal value that this field represents
     * (inklusive).
     * @param maxExclusive Upper bound on the value that this field
     * represents (exklusive).
     */
    public IntField(final String name,
                    final int minOccurs,
                    final int maxOccurs,
                    final int minInclusive,
                    final int maxExclusive) {
        this(name, "", minOccurs, maxOccurs, minInclusive, maxExclusive);
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
     * @param minInclusive Minimal value that this field represents
     * (inklusive).
     * @param maxExclusive Upper bound on the value that this field
     * represents (exklusive).
     */
    public IntField(final String name,
                    final String description,
                    final int minOccurs,
                    final int maxOccurs,
                    final int minInclusive,
                    final int maxExclusive) {
        super(name, description, minOccurs, maxOccurs);
        this.minInclusive = minInclusive;
        this.maxExclusive = maxExclusive;
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
     * @param minInclusive Minimal value that this field represents
     * (inklusive).
     * @param maxExclusive Should be <code>"unbounded"</code>, but
     * this parameter is in fact ignored (exklusive).
     */
    public IntField(final String name,
                    final String description,
                    final int minOccurs,
                    final int maxOccurs,
                    final int minInclusive,
                    final String maxExclusive) { // NOPMD
        super(name, description, minOccurs, maxOccurs);
        this.minInclusive = minInclusive;
        this.maxExclusive = Integer.MAX_VALUE;
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
     * @param minInclusive Minimal value that this field represents
     * (inklusive).
     * @param maxExclusive Should be <code>"unbounded"</code>, but
     * this parameter is in fact ignored (exklusive).
     */
    public IntField(final String name,
                    final int minOccurs,
                    final int maxOccurs,
                    final int minInclusive,
                    final String maxExclusive) { // NOPMD
        this(name, "", minOccurs, maxOccurs, minInclusive, maxExclusive);
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
     * @param minInclusive Should be <code>"unbounded"</code>, but
     * this parameter is in fact ignored (inklusive).
     * @param maxExclusive Upper bound on the value that this field
     * represents (exklusive).
     */
    public IntField(final String name,
                    final int minOccurs,
                    final int maxOccurs,
                    final String minInclusive, // NOPMD
                    final int maxExclusive) {
        super(name, minOccurs, maxOccurs);
        this.minInclusive = Integer.MIN_VALUE;
        this.maxExclusive = maxExclusive;
        bounded = true;
    }

    // These methods are documented in the super class.

    @Override
    public String schemaElementString() {
        final StringBuffer sb = new StringBuffer();

        sb.append("<xs:element name=\"" + name + "\"\n");
        if (!bounded) {
            sb.append("            type=\"xs:integer\"\n");
        }
        sb.append("            minOccurs=\"" + minOccurs + "\"\n");
        sb.append("            maxOccurs=\"" + maxOccurs + "\"");

        if (!bounded) {
            sb.append("/>");
        }

        if (bounded) {
            sb.append(">\n");
            sb.append("<xs:simpleType>\n");
            sb.append("   <xs:restriction base=\"xs:integer\">\n");
            sb.append("      <xs:minInclusive value=\"" + minInclusive
                      + "\"/>\n");
            sb.append("      <xs:maxExclusive value=\"" + maxExclusive
                      + "\"/>\n");
            sb.append("   </xs:restriction>\n");
            sb.append("</xs:simpleType>\n");
            sb.append("</xs:element>\n");
        }

        return sb.toString();
    }

    /**
     * Validates that the given integer is bounded as expected.
     *
     * @param value Value to be validated.
     *
     * @throws InfoException If the given value is not bounded as
     * expected.
     */
    public void validate(final int value) throws InfoException {
        if (bounded) {
            if (value < minInclusive) {
                throw new InfoException("Value is too small! ("
                                        + value + " < " + minInclusive + ")");
            }
            if (maxExclusive <= value) {
                throw new InfoException("Value is too large! ("
                                        + value + " < " + maxExclusive + ")");
            }
        }
    }

    @Override
    public Object parse(final String value) throws InfoException {
        final Integer integerValue = Integer.valueOf(value);
        final int v = integerValue.intValue();
        validate(v);
        return integerValue;
    }
}
