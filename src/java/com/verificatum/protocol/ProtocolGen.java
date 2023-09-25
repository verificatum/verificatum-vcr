
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

package com.verificatum.protocol;

import com.verificatum.crypto.RandomSource;
import com.verificatum.ui.info.InfoException;
import com.verificatum.ui.info.InfoGenerator;
import com.verificatum.ui.info.IntField;
import com.verificatum.ui.info.PartyInfo;
import com.verificatum.ui.info.PrivateInfo;
import com.verificatum.ui.info.ProtocolInfo;
import com.verificatum.ui.info.RootInfo;
import com.verificatum.ui.info.StringField;
import com.verificatum.vcr.VCR;


/**
 * Defines which information is stored in the protocol and private
 * info files, and also defines default values of some fields.
 * Protocols that derive their parameters from info files should
 * subclass this class and provide the additional fields they need by
 * implementing {@link #addProtocolInfo}, {@link #addDefault}, {@link
 * #addPrivateInfo}, and {@link #addDefault}.
 *
 * @author Douglas Wikstrom
 */
public abstract class ProtocolGen implements InfoGenerator {

    /**
     * Pattern for validation of SIDs.
     */
    public static final String SID_PATTERN = "[A-Za-z][A-Za-z0-9]{1,1023}";

    /**
     * Pattern for validation of names.
     */
    public static final String NAME_PATTERN = "[A-Za-z][A-Za-z0-9_ \\-]{1,255}";

    /**
     * Pattern for validation of description.
     */
    public static final String DESCRIPTION_PATTERN =
        "|[A-Za-z][A-Za-z0-9:;?!.()\\[\\] ]{0,4000}";

    /**
     * Pattern for validation of sort by roles.
     */
    public static final String SORT_BY_ROLE_PATTERN =
        "[A-Za-z0-9][A-Za-z0-9]{1,63}";

    /**
     * Description of session id field.
     */
    public static final String SID_DESCRIPTION =
        "Session identifier of this protocol execution. This must be "
        + "globally unique and satisfy the regular expression "
        + SID_PATTERN + ".";

    /**
     * Description of name field for protocol info.
     */
    public static final String PROTOCOLNAME_DESCRIPTION =
        "Name of this protocol execution. This is a short descriptive name "
        + "that is NOT necessarily unique, but satisfies the regular "
        + "expression " + NAME_PATTERN + ".";

    /**
     * Description of description field.
     */
    public static final String PROTOCOL_DESCRIPTION =
        "Description of this protocol execution. This is merely a longer "
        + "description than the name of the protocol execution. It must "
        + "satisfy the regular expression "
        + DESCRIPTION_PATTERN + ".";

    /**
     * Maximal statistical distance bound.
     */
    public static final int MAX_STATDIST = 256;

    /**
     * Description of statistical distance field.
     */
    public static final String STATDIST_DESCRIPTION =
        "Statistical distance from uniform of objects sampled in protocols "
        + "or in proofs of security. This must be a non-negative integer at "
        + "most " + MAX_STATDIST + ".";
    /**
     * Maximal number of parties.
     */
    public static final int MAX_NOPARTIES = 25;

    /**
     * Description of number of parties field.
     */
    public static final String NOPARTIES_DESCRIPTION =
        "Number of parties taking part in the protocol execution. This must "
        + "be a positive integer that is at most " + MAX_NOPARTIES + ".";

    /**
     * Description of sort-by-role field.
     */
    public static final String SORT_BY_ROLE_DESCRIPTION =
        "Sorting attribute used to sort parties with respect to "
        + "their roles in the protocol. This is used to assign roles in "
        + "protocols where different parties play different roles.";

    /**
     * Description of name field for party info.
     */
    public static final String PARTYNAME_DESCRIPTION =
        "Name of party. This must satisfy the regular expression "
        + NAME_PATTERN + ".";

    /**
     * Description of party description field.
     */
    public static final String PARTYDESCRIPTION_DESCRIPTION =
        "Description of this party. This is merely a longer "
        + "description than the name of the party. It must "
        + "satisfy the regular expression "
        + DESCRIPTION_PATTERN + ".";

    /**
     * Description of directory field.
     */
    public static final String DIRECTORY_DESCRIPTION =
        "Working directory of this protocol instance. WARNING! This field "
        + "is not validated syntactically.";

    /**
     * Description of randomness field.
     */
    public static final String RANDOMNESS_DESCRIPTION =
        "Source of randomness "
        + "(instance of com.verificatum.crypto.RandomSource). "
        + "WARNING! This field is not validated syntactically and it is "
        + "impossible to verify that a random device points to a source of "
        + "randomness suitable for cryptographic use, or that a pseudo-random "
        + "generator has been initialized with such randomness";

    /**
     * Maximal certainty.
     */
    public static final int MAX_CERTAINTY = 256;

    /**
     * Description of certainty field.
     */
    public static final String CERTAINTY_DESCRIPTION =
        "Certainty with which probabilistically checked parameters are "
        + "verified, i.e., the probability of an error is bounded by "
        + "2^(-cert). This must be a positive integer at most equal to "
        + MAX_CERTAINTY + ".";

    /**
     * Add additional fields to the protocol info.
     *
     * @param pri Destination of new fields.
     */
    public void addProtocolInfo(final ProtocolInfo pri) {

        // RootInfo.VERSION is added in the constructor of Info.

        final StringField sidField =
            new StringField(Protocol.SID, SID_DESCRIPTION, 1, 1).
            setPattern(SID_PATTERN);
        final StringField protocolNameField =
            new StringField(Protocol.NAME,
                             PROTOCOLNAME_DESCRIPTION, 1, 1).
            setPattern(NAME_PATTERN);
        final StringField protocolDescriptionField =
            new StringField(Protocol.DESCRIPTION,
                             PROTOCOL_DESCRIPTION, 1, 1).
            setPattern(DESCRIPTION_PATTERN);

        pri.addInfoFields(sidField,
                          protocolNameField,
                          protocolDescriptionField,
                          new IntField(Protocol.NOPARTIES,
                                       NOPARTIES_DESCRIPTION, 1, 1, 1,
                                       MAX_NOPARTIES),
                          new IntField(Protocol.STATDIST,
                                       STATDIST_DESCRIPTION, 1, 1, 0,
                                       MAX_STATDIST));

        // RootInfo.VERSION is added to each PartyInfo through the
        // constructor of Info.

        final StringField nameField =
            new StringField(Protocol.NAME, PARTYNAME_DESCRIPTION, 1, 1).
            setPattern(NAME_PATTERN);

        final StringField roleField =
            new StringField(Protocol.SORT_BY_ROLE,
                             SORT_BY_ROLE_DESCRIPTION,
                             1, 1).setPattern(SORT_BY_ROLE_PATTERN);

        final StringField descriptionField =
            new StringField(Protocol.DESCRIPTION,
                             PARTYDESCRIPTION_DESCRIPTION,
                             1, 1).setPattern(DESCRIPTION_PATTERN);

        pri.getFactory().addInfoFields(nameField, roleField, descriptionField);
    }

    /**
     * Add additional values to the protocol info.
     *
     * @param pri Destination of the values.
     */
    public void addDefault(final ProtocolInfo pri) {

        try {
            pri.addValue(RootInfo.VERSION, VCR.version());

            // Protocol.SID must be given by the user.
            // Protocol.NAME must be given by the user.
            // Protocol.NOPARTIES must be given by the user.

            pri.addValue(Protocol.DESCRIPTION, "");
            pri.addValue(Protocol.STATDIST, ProtocolDefaults.STAT_DIST);
        } catch (final InfoException ie) {
            throw new ProtocolError("Failed to add default value!", ie);
        }
    }

    /**
     * Add additional fields to the private info.
     *
     * @param pi Destination of new fields.
     */
    public void addPrivateInfo(final PrivateInfo pi) {

        // RootInfo.VERSION is added in the constructor of Info.

        final StringField nameField =
            new StringField(Protocol.NAME,
                             PARTYNAME_DESCRIPTION, 1, 1).
            setPattern(NAME_PATTERN);
        pi.addInfoFields(nameField,
                         new StringField(Protocol.DIRECTORY,
                                         DIRECTORY_DESCRIPTION, 1, 1),
                         new StringField(Protocol.RANDOMNESS,
                                         RANDOMNESS_DESCRIPTION, 1, 1),
                         new IntField(Protocol.CERTAINTY,
                                      CERTAINTY_DESCRIPTION, 1, 1, 1,
                                      MAX_CERTAINTY));
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
        try {
            pi.addValue(RootInfo.VERSION, VCR.version());

            // Protocol.NAME must be given by the user.

            pi.addValue(Protocol.DIRECTORY, ProtocolDefaults.DIR());
            pi.addValue(Protocol.RANDOMNESS, ProtocolDefaults.RandomDevice());
            pi.addValue(Protocol.CERTAINTY, ProtocolDefaults.CERTAINTY);
        } catch (final InfoException ie) {
            throw new ProtocolError("Failed to add default value!", ie);
        }
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

        // Protocol.NAME must be given by the user.
        try {
            pai.addValue(Protocol.SORT_BY_ROLE, "anyrole");
            pai.addValue(Protocol.DESCRIPTION, "");
        } catch (final InfoException ie) {
            throw new ProtocolError("Failed to add default value!", ie);
        }
    }

    // Documented in InfoGenerator.java

    @Override
    public String protocolVersion() {
        return VCR.version();
    }

    @Override
    public String compatibleProtocolVersions() {
        return protocolVersion();
    }

    @Override
    public boolean compatible(final String packageVersion) {
        return VCR.version().equals(packageVersion);
    }

    @Override
    public ProtocolInfo newProtocolInfo() {
        final ProtocolInfo pri = new ProtocolInfo();
        addProtocolInfo(pri);
        return pri;
    }

    @Override
    public ProtocolInfo defaultProtocolInfo() {
        final ProtocolInfo pri = newProtocolInfo();
        addDefault(pri);
        return pri;
    }

    @Override
    public PrivateInfo newPrivateInfo() {
        final PrivateInfo pi = new PrivateInfo();
        addPrivateInfo(pi);
        return pi;
    }

    @Override
    public PrivateInfo defaultPrivateInfo(final ProtocolInfo pri,
                                          final RandomSource rs) {
        final PrivateInfo pi = newPrivateInfo();
        addDefault(pi, pri, rs);
        return pi;
    }

    @Override
    public PartyInfo defaultPartyInfo(final ProtocolInfo pri,
                                      final PrivateInfo pi,
                                      final RandomSource rs) {
        final PartyInfo pai = pri.getFactory().newInstance();
        addDefault(pai, pri, pi, rs);
        return pai;
    }

    @Override
    public void validateLocal(final ProtocolInfo pri) // NOPMD Default.
        throws InfoException {
    }

    @Override
    public void validate(final ProtocolInfo pri) throws InfoException {

        final int claimedNoParties = pri.getIntValue(Protocol.NOPARTIES);
        final int actualNoParties = pri.getNumberOfParties();

        if (claimedNoParties != actualNoParties) {
            throw new InfoException("Mismatching number of parties! ("
                                    + actualNoParties
                                    + " parties, but expected "
                                    + claimedNoParties + ")");
        }
    }
}
