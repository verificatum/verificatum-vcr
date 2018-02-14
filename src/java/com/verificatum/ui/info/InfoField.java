
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
 * Abstract base class for an info field.
 *
 * @author Douglas Wikstrom
 */
public abstract class InfoField implements Comparable<InfoField> {

    /**
     * Name of this instance.
     */
    protected String name;

    /**
     * Description of value stored in this instance.
     */
    protected String description;

    /**
     * Minimal number of times this field must occur (inclusive lower
     * bound).
     */
    protected int minOccurs;

    /**
     * Upper bound on the number of times this field may occur
     * (exclusive upper bound).
     */
    protected int maxOccurs;

    /**
     * Creates a field.
     *
     * @param name Name of the field.
     * @param minOccurs Minimum number of times the field must occur
     * in the context it is used (inclusive).
     * @param maxOccurs Strict upper bound on the number of times the
     * field may occur in the context it is used
     * (exclusive).
     */
    public InfoField(final String name,
                     final int minOccurs,
                     final int maxOccurs) {
        this(name, "", minOccurs, maxOccurs);
    }

    /**
     * Creates a field.
     *
     * @param name Name of the field.
     * @param description Description of this field.
     * @param minOccurs Minimum number of times the field must occur
     * in the context it is used (inclusive).
     * @param maxOccurs Strict upper bound on the number of times the
     * field may occur in the context it is used
     * (exclusive).
     */
    public InfoField(final String name,
                     final String description,
                     final int minOccurs,
                     final int maxOccurs) {
        this.name = name;
        this.description = description;
        this.minOccurs = minOccurs;
        this.maxOccurs = maxOccurs;
    }

    /**
     * Returns the name of this instance.
     *
     * @return Name of this instance.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the description of this instance.
     *
     * @return Description of this instance
     */
    public String getDescription() {
        return description;
    }

    /**
     * Outputs an XML schema element tag for this instance.
     *
     * @return XML schema element tag.
     */
    public abstract String schemaElementString();

    /**
     * Parses the input.
     *
     * @param value Value to be parsed.
     * @return Parsed value.
     *
     * @throws InfoException If the field can not be parsed.
     */
    public abstract Object parse(String value) throws InfoException;


    @Override
    public int hashCode() {
        return name.hashCode()
            + description.hashCode()
            + minOccurs * minOccurs
            + maxOccurs * maxOccurs * maxOccurs;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof InfoField)) {
            return false;
        }
        final InfoField iff = (InfoField) obj;

        return name.equals(iff.name)
            && description.equals(iff.description)
            && minOccurs == iff.minOccurs
            && maxOccurs == iff.maxOccurs;
    }

    @Override
    public int compareTo(final InfoField iff) {
        return name.compareTo(iff.name);
    }
}
