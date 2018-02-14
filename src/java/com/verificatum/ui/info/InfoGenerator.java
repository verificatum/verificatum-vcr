
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

import com.verificatum.crypto.RandomSource;

/**
 * Defines the methods needed by {@link InfoTool} to allow
 * manipulation of info files for a given protocol, i.e., for every
 * protocol that is supposed to be used directly by an end user, this
 * interface should be implemented by a generator class.
 *
 * @author Douglas Wikstrom
 */
public interface InfoGenerator {

    /**
     * Returns the current protocol version.
     *
     * @return Current protocol version.
     */
    String protocolVersion();

    /**
     * Returns a comma-separated list of protocol versions compatible
     * with this implementation.
     *
     * @return Comma-separated list of protocol versions compatible
     *         with this implementation.
     */
    String compatibleProtocolVersions();

    /**
     * Returns true if and only if the given protocol version is
     * compatible with this implementation.
     *
     * @param protocolVersion Protocol version to be tested.
     * @return True if and only if the given protocol version is
     *         compatible with this implementation.
     */
    boolean compatible(String protocolVersion);

    /**
     * Creates an instance containing all the fields needed by the
     * protocol.
     *
     * @return Instance containing all the needed fields.
     */
    ProtocolInfo newProtocolInfo();

    /**
     * Creates an instance containing all the fields needed by the
     * protocol, and a number of default values.
     *
     * @return Instance containing all the needed fields and some
     *         default values.
     */
    ProtocolInfo defaultProtocolInfo();

    /**
     * Creates an instance containing all the fields needed by the
     * protocol.
     *
     * @return Instance containing all the needed fields.
     */
    PrivateInfo newPrivateInfo();

    /**
     * Creates an instance containing all the fields needed by the
     * protocol, and a number of default values. This assumes that the
     * protocol info has been filled with default values.
     *
     * @param pri Protocol info on which this instance is based.
     * @param rs Source of randomness.
     * @return Instance containing all the needed fields and some
     *         default values.
     */
    PrivateInfo defaultPrivateInfo(ProtocolInfo pri, RandomSource rs);

    /**
     * Creates an instance containing all the fields needed by the
     * protocol, and a number of default values. This assumes that the
     * protocol info and the private info has been filled with default
     * values.
     *
     * @param pri Protocol info on which this instance is based.
     * @param pi Private info on which this instance is based.
     * @param rs Source of randomness.
     * @return Instance containing all the needed fields and some
     *         default values.
     */
    PartyInfo defaultPartyInfo(ProtocolInfo pri, PrivateInfo pi,
                               RandomSource rs);

    /**
     * Verify the consistency of a local protocol info and throw
     * exception otherwise.
     *
     * @param pri Protocol info.
     *
     * @throws InfoException If the input info does not validate.
     */
    void validateLocal(ProtocolInfo pri) throws InfoException;

    /**
     * Verify the consistency of the protocol info and throw exception
     * otherwise.
     *
     * @param pri Protocol info.
     *
     * @throws InfoException If the input info does not validate.
     */
    void validate(ProtocolInfo pri) throws InfoException;
}
