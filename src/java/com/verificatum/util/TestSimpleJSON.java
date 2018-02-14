
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

package com.verificatum.util;

import java.util.Map;
import java.util.TreeMap;

import com.verificatum.test.TestParameters;


/**
 * Tests the minimal JSON implementation.
 *
 * @author Douglas Wikstrom
 */
// PMD_ANNOTATION @SuppressWarnings("PMD.SignatureDeclareThrowsException")
public final class TestSimpleJSON {

    /**
     * Constructor needed to avoid that this class is instantiated.
     */
    private TestSimpleJSON() {
    }

    /**
     * Verify conversion to and from JSON.
     *
     * @param tp Test parameters configuration of the servers.
     * @throws Exception when failing test.
     */
    public static void toAndFromJSON(final TestParameters tp) throws Exception {

        final TreeMap<String, String> map1 = new TreeMap<String, String>();

        map1.put("hej1", "hopp1");
        map1.put("hej2", "hopp2");
        map1.put("hej3", "hopp3");

        final String mapString1 = SimpleJSON.toJSON(map1);

        final Map<String, String> map2 = SimpleJSON.readMap(mapString1);

        final String mapString2 = SimpleJSON.toJSON(map2);

        assert mapString1.equals(mapString2)
            : "Failed to convert to and from JSON map!";
    }
}
