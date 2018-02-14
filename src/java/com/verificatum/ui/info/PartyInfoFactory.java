
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

import java.util.ArrayList;
import java.util.List;

/**
 * Factory for creating <code>PartyInfo</code> instances.
 *
 * @author Douglas Wikstrom
 */
public final class PartyInfoFactory {

    /**
     * Additional fields.
     */
    List<InfoField> additionalFields;

    /**
     * Creates a factory that creates <code>PartyInfo</code> instances
     * with the given additional fields.
     *
     * @param infoFields Additional info fields.
     */
    public PartyInfoFactory(final InfoField... infoFields) {
        additionalFields = new ArrayList<InfoField>();
        addInfoFields(infoFields);
    }

    /**
     * Adds the given info fields to this instance.
     *
     * @param infoFields Additional info fields.
     */
    public void addInfoFields(final InfoField... infoFields) {
        for (int i = 0; i < infoFields.length; i++) {
            additionalFields.add(infoFields[i]);
        }
    }

    /**
     * Creates an instance of <code>PartyInfo</code>.
     *
     * @return A new instance.
     */
    public PartyInfo newInstance() {

        final int size = additionalFields.size();

        return new PartyInfo(additionalFields.toArray(new InfoField[size]));
    }
}
