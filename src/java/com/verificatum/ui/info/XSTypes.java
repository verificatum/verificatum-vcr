
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

/**
 * Defines a few XS types to be used in info schemas in terms of
 * regular expressions that can also be used programatically.
 *
 * @author Douglas Wikstrom
 */
public final class XSTypes {

    /**
     * Prevents accidental instantiation.
     */
    private XSTypes() {
    }

    /**
     * String pattern for validating port number in [1,65535].
     */
    public static final String PORT_PATTERN =
        "("
        +   "(6553[0-5])"
        +   "|(655[0-2][0-9])"
        +   "|(65[0-4][0-9]{2})"
        +   "|(6[0-4][0-9]{3})"
        +   "|([1-5][0-9]{4})"
        +   "|([1-9][0-9]{0,3})"
        + ")";

    /**
     * String pattern for validating IP number.
     */
    public static final String IPV4_PATTERN =
        "("
        +   "("
        +     "(25[0-5])"
        +     "|(2[0-4][0-9])"
        +     "|(1[0-9][0-9])"
        +     "|([1-9][0-9])"
        +     "|[0-9]"
        +   ")"
        + "\\.){3}"
        + "("
        +   "(25[0-5])"
        +   "|(2[0-4][0-9])"
        +   "|(1[0-9][0-9])"
        +   "|([1-9][0-9])"
        +   "|[0-9]"
        + ")";

    /**
     * String pattern for isolated hostname.
     */
    public static final String HOST_PATTERN =
        "([a-z0-9]([-a-z0-9]*[a-z0-9])*)"
        + "(\\.([a-z0-9]([-a-z0-9]*[a-z0-9])*))*";

    /**
     * Inet with port pattern.
     */
    public static final String INET_PORT_PATTERN =
        "((" + HOST_PATTERN + ")|(" + IPV4_PATTERN + "))" + ":" + PORT_PATTERN;

    /**
     * URL with port pattern.
     */
    public static final String URL_PORT_PATTERN = "http://" + INET_PORT_PATTERN;

    /**
     * XSD types for IP addresses, port numbers, and hostnames.
     */
    public static final String IP =
        "<xs:simpleType name=\"inetport\">\n"
        + "  <xs:restriction base=\"xs:string\">\n"
        + "    <xs:maxLength value=\"512\"/>\n"
        + "    <xs:pattern value=\"" + INET_PORT_PATTERN + "\"/>\n"
        + "  </xs:restriction>\n"
        + "</xs:simpleType>\n"
        + "\n"
        + "<xs:simpleType name=\"urlport\">\n"
        + "  <xs:restriction base=\"xs:string\">\n"
        + "    <xs:maxLength value=\"512\"/>\n"
        + "    <xs:pattern value=\"" + URL_PORT_PATTERN + "\"/>\n"
        + "  </xs:restriction>\n"
        + "</xs:simpleType>";
}
