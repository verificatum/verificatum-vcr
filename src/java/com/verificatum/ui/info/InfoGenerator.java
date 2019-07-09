
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
