
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
