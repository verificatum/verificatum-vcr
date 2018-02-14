
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
 * URL with port data field that can be used in an XML configuration
 * file.
 *
 * @author Douglas Wikstrom
 */
public class URLPortField extends NetField {

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
    public URLPortField(final String name,
                         final String description,
                         final int minOccurs,
                         final int maxOccurs) {
        super(name, description, minOccurs, maxOccurs, "urlport",
              XSTypes.URL_PORT_PATTERN);
    }
}
