
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
