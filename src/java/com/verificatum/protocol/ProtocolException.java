
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

package com.verificatum.protocol;

/**
 * Thrown when something goes wrong in a protocol. This should
 * normally be caught by the calling party and dealt with
 * appropriately.
 *
 * @author Douglas Wikstrom
 */
public class ProtocolException extends Exception {

    /**
     * Serialization version.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message Detailed message of the problem.
     */
    public ProtocolException(final String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message
     * and cause.
     *
     * @param message Detailed message of the problem.
     * @param cause What caused this exception to be thrown.
     */
    public ProtocolException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
