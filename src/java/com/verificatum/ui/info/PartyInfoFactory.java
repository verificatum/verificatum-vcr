
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
