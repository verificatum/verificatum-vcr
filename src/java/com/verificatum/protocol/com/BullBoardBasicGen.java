
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

package com.verificatum.protocol.com;

import com.verificatum.crypto.RandomSource;
import com.verificatum.ui.info.PartyInfo;
import com.verificatum.ui.info.PrivateInfo;
import com.verificatum.ui.info.ProtocolInfo;

/**
 * Defines which information is stored in the protocol and private
 * info files, and also defines default values of some fields. For
 * each subclass of {@link BullBoardBasic}, there should be a
 * corresponding subclass of this class that allows adding the needed
 * fields and default values.
 *
 * @author Douglas Wikstrom
 */
public class BullBoardBasicGen {

    /**
     * Add additional fields to the protocol info.
     *
     * @param pri Destination of new fields.
     */
    public void addProtocolInfo(final ProtocolInfo pri) {
    }

    /**
     * Add additional values to the protocol info.
     *
     * @param pri Destination of the values.
     */
    public void addDefault(final ProtocolInfo pri) {
    }

    /**
     * Add additional fields to the private info.
     *
     * @param pi Destination of new fields.
     */
    public void addPrivateInfo(final PrivateInfo pi) {
    }

    /**
     * Add additional values to the private info.
     *
     * @param pi Destination of values.
     * @param pri Associated protocol info.
     * @param rs Source of randomness.
     */
    public void addDefault(final PrivateInfo pi,
                           final ProtocolInfo pri,
                           final RandomSource rs) {
    }

    /**
     * Add additional values to the party info.
     *
     * @param pai Destination of values.
     * @param pri Associated protocol info.
     * @param pi Associated private info.
     * @param rs Source of randomness.
     */
    public void addDefault(final PartyInfo pai,
                           final ProtocolInfo pri,
                           final PrivateInfo pi,
                           final RandomSource rs) {
    }
}
