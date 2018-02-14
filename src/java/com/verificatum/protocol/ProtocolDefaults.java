
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

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.verificatum.arithm.ArithmFormatException;
import com.verificatum.arithm.ECqPGroupParams;
import com.verificatum.arithm.PGroup;
import com.verificatum.crypto.CryptoKeyGen;
import com.verificatum.crypto.CryptoKeyGenNaorYung;
import com.verificatum.crypto.Hashfunction;
import com.verificatum.crypto.HashfunctionHeuristic;
import com.verificatum.crypto.RandomDevice;
import com.verificatum.crypto.RandomSource;
import com.verificatum.crypto.SignatureKeyGen;
import com.verificatum.crypto.SignatureKeyGenHeuristic;
import com.verificatum.crypto.SignatureKeyPair;
import com.verificatum.eio.Marshalizer;
import com.verificatum.util.Lazy;

/**
 * Provides default values of common protocol parameters.
 *
 * @author Douglas Wikstrom
 */
// PMD_ANNOTATION @SuppressWarnings("PMD.MethodNamingConventions")
public final class ProtocolDefaults {

    /**
     * Avoid accidental instantiation.
     */
    private ProtocolDefaults() { }

    /**
     * Main security parameter.
     */
    public static final int SEC_PARAM = 2048;

    /**
     * Security parameter deciding the bit length of challenges in
     * <b>interactive</b> protocols.
     */
    public static final int SEC_PARAM_CHALLENGE = 128;

    /**
     * Security parameter deciding the bit length of challenges in
     * <b>non-interactive</b> proofs based on the Fiat-Shamir
     * heuristic.
     */
    public static final int SEC_PARAM_CHALLENGE_RO = 256;

    /**
     * Security parameter deciding the bit length of each component of
     * random vectors used for batching in <b>interactive</b> proofs.
     */
    public static final int SEC_PARAM_BATCH = 128;

    /**
     * Security parameter deciding the bit length of each component of
     * random vectors used for batching in <b>non-interactive</b>
     * proofs based on the Fiat-Shamir heuristic.
     */
    public static final int SEC_PARAM_BATCH_RO = 256;

    /**
     * Security parameter deciding the statistical error in a protocol
     * or hypothetical simulator, on top of any computational error.
     */
    public static final int STAT_DIST = 100;

    /**
     * Certainty with which probabilistically checked parameters are
     * verified, i.e., the probability of an error is bounded by
     * 2<sup>-{@link #CERTAINTY} </sup>.
     */
    public static final int CERTAINTY = 50;

    /**
     * Default model for storing huge arrays group elements, field
     * elements, and integers.
     */
    public static final String ARRAYS = "file";

    /**
     * Default type of http server.
     */
    public static final String HTTP_TYPE = "internal";

    /**
     * Default http port.
     */
    public static final int HTTP_PORT = 8040;

    /**
     * Default hint port.
     */
    public static final int HINT_PORT = 4040;

    /**
     * Default name of subdirectory for non-interative proofs.
     */
    public static final String NIZKP = "nizkp";

    /**
     * Returns a path to the user directory.
     *
     * @return Path to user directory.
     */
    public static String DIR() {
        final File cwd = new File(System.getProperty("user.dir"));
        return new File(cwd, "dir").toString();
    }

    /**
     * Directory published by a HTTP server.
     *
     * @return String representation of directory.
     */
    public static String HTTPDIR() {
        final File cwd = new File(System.getProperty("user.dir"));
        return new File(cwd, "httproot").toString();
    }

    /**
     * Returns a string representation of a randomly generated
     * signature key pair.
     *
     * @param rs Source of random bits.
     * @return String representation of a key pair.
     */
    public static String SignatureSKey(final RandomSource rs) {
        final SignatureKeyGen keygen = new SignatureKeyGenHeuristic(SEC_PARAM);
        final SignatureKeyPair keyPair = keygen.gen(rs);
        return Marshalizer.marshalToHexHuman(keyPair, true);
    }

    /**
     * Returns the name of the default bulletin board implementation.
     *
     * @return Default bulletin board implementation.
     */
    public static String BullBoard() {

        // This forces a local error if we fail to find the given
        // class.
        try {
            return Class
                .forName("com.verificatum.protocol.com.BullBoardBasicHTTPW")
                .getName();
        } catch (final ClassNotFoundException cnfe) {
            final String e = "Unable to find default basic bulletin board!";
            throw new ProtocolError(e, cnfe);
        }
    }

    /**
     * A lazy-evaluation version of {@link #SignatureSKey}.
     *
     * @param rs Source of random bits.
     * @return Object that can generate a string representation of a
     * key pair.
     */
    public static Object LazySignatureSKey(final RandomSource rs) {
        return new Lazy() {
            String skey;

            @Override
            public String gen() {
                if (skey == null) {
                    skey = SignatureSKey(rs);
                    return skey;
                } else {
                    return skey;
                }
            }
        };
    }

    /**
     * Returns a string representation of the default {@link
     * com.verificatum.arithm.PGroup}, which is a {@link
     * com.verificatum.arithm.ECqPGroup} instance implementing P-256.
     *
     * @return String representation of the default {@link
     * com.verificatum.arithm.PGroup}.
     */
    public static String PGroup() {
        try {
            // PGroup pGroup = new ModPGroup(SEC_PARAM);
            final PGroup pGroup = ECqPGroupParams.getECqPGroup("P-256");
            return Marshalizer.marshalToHexHuman(pGroup, true);

        } catch (final ArithmFormatException afe) {
            throw new ProtocolError("Bad constant!", afe);
        }
    }

    /**
     * Returns a string representation of the default hashfunction,
     * which is SHA-256.
     *
     * @return String representation of the default hashfunction.
     */
    public static String Hashfunction() {
        return "SHA-256";
    }

    /**
     * Returns a string representation of a key generator for the
     * Naor-Yung cryptosystem defined over the default group and using
     * SHA-256.
     *
     * @return String representation of the default key generator of a
     * cryptosystem.
     */
    public static String CryptoKeyGen() {
        try {

            // Let us be very conservative.
            final PGroup pGroup = ECqPGroupParams.getECqPGroup("P-521");

            // Note that digests must be smaller than the order of the
            // group to preserve collision-resistance modulo the order
            // of the group. Thus, if we want to use SHA-512 (or even
            // SHA-256), then, e.g., P-256 is not acceptable.
            final Hashfunction hashfunction =
                new HashfunctionHeuristic("SHA-512");

            final CryptoKeyGen keyGen =
                new CryptoKeyGenNaorYung(pGroup, hashfunction,
                                         SEC_PARAM_CHALLENGE_RO);

            return Marshalizer.marshalToHexHuman(keyGen, true);

        } catch (final ArithmFormatException afe) {
            throw new ProtocolError("Bad constant!", afe);
        }
    }

    /**
     * Returns a string representation of the default random device,
     * which is the Un*x standard pseudo-random device
     * <code>/dev/urandom</code>.
     *
     * @return String representation of the default random device.
     */
    public static String RandomDevice() {
        final RandomDevice dev = new RandomDevice();
        return Marshalizer.marshalToHexHuman(dev, true);
    }

    /**
     * Returns a string representation of the default pseudo-random
     * generator (PRG).
     *
     * @return String representation of the default PRG.
     */
    public static String PRG() {
        return "SHA-256";
    }

    /**
     * Returns the default hostname, i.e., the hostname of the machine
     * executing this code. Depending on the configuration of the
     * server and the network, this may, or may not give a useful
     * result.
     *
     * @return Hostname of the machine executing this code.
     */
    public static String HOST() {
        try {
            final InetAddress ia = InetAddress.getLocalHost();
            return ia.getHostName();
        } catch (final UnknownHostException uhe) {
            throw new ProtocolError("Can not find out our own hostname!", uhe);
        }
    }

    /**
     * Returns a string representation of the default port address of
     * the hint server.
     *
     * @return String representation of the default port address of
     * the hint server.
     */
    public static String HINT() {
        return HOST() + ":" + HINT_PORT;
    }

    /**
     * Returns a string representation of the default local port
     * address of the hint server, i.e., the address to which the hint
     * server is bound.
     *
     * @return String representation of the default local port address
     * of the hint server.
     */
    public static String HINTL() {
        return HINT();
    }

    /**
     * Returns a string representation of the default local port
     * address of the http server, i.e., the address to which the http
     * server is bound.
     *
     * @return String representation of the default local port address
     * of the http server.
     */
    public static String HTTP() {
        return "http://" + HOST() + ":" + HTTP_PORT;
    }

    /**
     * Returns a string representation of the default local port
     * address of the http server, i.e., the address to which the http
     * server is bound.
     *
     * @return String representation of the default local port address
     * of the http server.
     */
    public static String HTTPL() {
        return HTTP();
    }
}
