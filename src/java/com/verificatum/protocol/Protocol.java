
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

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.util.Arrays;

import com.verificatum.crypto.CryptoException;
import com.verificatum.crypto.PRG;
import com.verificatum.crypto.RandomSource;
import com.verificatum.eio.ByteTree;
import com.verificatum.eio.EIOException;
import com.verificatum.eio.ExtIO;
import com.verificatum.eio.Marshalizer;
import com.verificatum.eio.TempFile;
import com.verificatum.ui.Log;
import com.verificatum.ui.UI;
import com.verificatum.ui.info.InfoException;
import com.verificatum.ui.info.InfoGenerator;
import com.verificatum.ui.info.PrivateInfo;
import com.verificatum.ui.info.ProtocolInfo;


/**
 * Implements the basic functionality required by virtually every
 * cryptographic protocol. Any concrete protocol should subclass this
 * class or one of its subclasses.
 *
 * <p>
 *
 * More precisely, an instance of this class keeps the following data:
 *
 * <ul>
 *
 * <li> {@link #k} -- Number of parties.
 *
 * <li> {@link #j} -- Index of this party (indices start at one).
 *
 * <li> {@link #randomSource} -- Default source of (pseudo) random
 * bits.
 *
 * <li>A working directory dedicated to this instance. Use
 * {@link #getFile(String)} to get a file in this directory.
 *
 * <li> {@link #ui} -- User interface. This allows all the usual ways
 * of interacting with the user.
 *
 * </ul>
 *
 * <p>
 *
 * There are two constructors in this class corresponding to if the
 * constructed instance is the main protocol or not.
 *
 * <p>
 *
 * The constructor of the main protocol should call
 * {@link #Protocol(PrivateInfo, ProtocolInfo, UI)}. This should
 * normally be done only a few times in an application. It initializes
 * the above variables based on the information in the public and
 * private info instances. It also sets the user interface.
 *
 * <p>
 *
 * For all subsequent subprotocols, the constructor
 * {@link #Protocol(String, Protocol)} should be used instead. The
 * latter copies some of the above fields from the parent protocol
 * given as input, and creates sub-versions of others, e.g., it
 * creates a subdirectory of the directory of the parent protocol. The
 * idea is to make it easy to build a tree of instantiations of
 * protocols invoking other protocols as subprotocols, each with its
 * own scope.
 *
 * @author Douglas Wikstrom
 */
public class Protocol {

    /**
     * Maximal length of session identifier.
     */
    public static final int SID_MAX_LEN = 256;

    /**
     * Name of session identifier tag.
     */
    public static final String SID = "sid";

    /**
     * Name of name tag.
     */
    public static final String NAME = "name";

    /**
     * Name of the sort-by-role tag used to assign parties different
     * roles within a protocol based on their index derived by
     * sorting.
     */
    public static final String SORT_BY_ROLE = "srtbyrole";

    /**
     * Name of description tag.
     */
    public static final String DESCRIPTION = "descr";

    /**
     * Name of statistical distance tag.
     */
    public static final String STATDIST = "statdist";

    /**
     * Name of number of parties tag.
     */
    public static final String NOPARTIES = "nopart";

    /**
     * Name of directory tag.
     */
    public static final String DIRECTORY = "dir";

    /**
     * Name of randomness tag.
     */
    public static final String RANDOMNESS = "rand";

    /**
     * Name of certainty tag.
     */
    public static final String CERTAINTY = "cert";

    /**
     * Name of file where the seed to the global PRG is stored if a
     * PRG is used to provide a source of randomness.
     */
    public static final String SEED_FILENAME = ".prgseed_DO_NOT_TOUCH";

    /**
     * Name of temporary file where the seed to the global PRG is
     * stored before updating.
     */
    public static final String TMP_SEED_FILENAME = ".prgseed_DO_NOT_TOUCH_TMP";

    /**
     * Parent protocol instance that spawned this one. This is null if
     * this the root protocol.
     */
    protected Protocol parent;

    /**
     * Number of parties executing the protocol.
     */
    public final int k;

    /**
     * Index of this instance/party.
     */
    public final int j;

    /**
     * Directory where this instance stores its files.
     */
    protected File directory;

    /**
     * Default source of randomness used in the protocol.
     */
    public final RandomSource randomSource;

    /**
     * Decides the statistical distance from the uniform distribution
     * when sampling objects in protocols or in proofs.
     */
    public final int rbitlen;

    /**
     * Determines the probability that probabilistic tests fail to
     * identify an inconsistency. The probability is at most 2<sup>-
     * {@link #certainty}</sup>
     */
    public final int certainty;

    /**
     * Session identifier that singles out this instance from other
     * instances of this protocol invoked by the same protocol.
     */
    protected final String sid;

    /**
     * A unique name among all subprotocols.
     */
    private final String fullName;

    /**
     * User interface.
     */
    protected final UI ui;

    /**
     * Array indicating the set of active parties.
     */
    protected boolean[] active;

    /**
     * Returns a file representing a random seed as defined in the
     * given private info.
     *
     * @param privateInfo Private info of this party.
     * @return File representing random seed.
     */
    public static File seedFile(final PrivateInfo privateInfo) {
        final File dir = new File(privateInfo.getStringValue(DIRECTORY));
        return new File(dir, SEED_FILENAME);
    }

    /**
     * Returns the standard source of random bits as defined in the
     * given private info.
     *
     * @param privateInfo Private info of this party.
     * @return Source of random bits.
     *
     * @throws ProtocolError If a random source can not be
     * instantiated based on the input representation.
     */
    public static RandomSource randomSource(final PrivateInfo privateInfo)
        throws ProtocolError {

        // Set up default source of randomness.
        final String randomness = privateInfo.getStringValue(RANDOMNESS);
        try {
            return Marshalizer.unmarshalHex_RandomSource(randomness);
        } catch (final EIOException eioe) {
            throw new ProtocolError("Unable to use random source! ("
                                    + randomness + ")", eioe);
        }
    }

    /**
     * Returns a parsed protocol info.
     *
     * @param generator Generator used to parse the protocol info file.
     * @param protocolInfoFile File representation of protocol info.
     * @return Protocol info initialized with the data in the protocol
     * info file.
     * @throws ProtocolFormatException If the protocol info file can
     * not be parsed.
     */
    public static ProtocolInfo getProtocolInfo(final InfoGenerator generator,
                                               final File protocolInfoFile)
        throws ProtocolFormatException {

        // Generate a protocol info and parse the protocol info file.
        try {
            final ProtocolInfo protocolInfo = generator.newProtocolInfo();
            protocolInfo.parse(protocolInfoFile);
            generator.validate(protocolInfo);
            return protocolInfo;
        } catch (final InfoException ie) {
            throw new ProtocolFormatException("Failed to parse info files!",
                                              ie);
        }
    }

    /**
     * Returns a parsed private info.
     *
     * @param generator Generator used to parse the private info file.
     * @param privateInfoFile File representation of private info.
     * @return Private info initialized with the data in the private
     * info file.
     * @throws ProtocolFormatException If the private info file can
     * not be parsed.
     */
    public static PrivateInfo getPrivateInfo(final InfoGenerator generator,
                                             final File privateInfoFile)
        throws ProtocolFormatException {

        // Generate a private info and parse the private info file.
        try {
            final PrivateInfo privateInfo = generator.newPrivateInfo();
            privateInfo.parse(privateInfoFile);
            return privateInfo;
        } catch (final InfoException ie) {
            throw new ProtocolFormatException("Failed to parse info files!",
                                              ie);
        }
    }

    /**
     * Returns the logging context used by this protocol.
     *
     * @return Logging context.
     */
    public final Log getLog() {
        return ui.getLog();
    }

    /**
     * Returns the user interface used by this protocol.
     *
     * @return User interface.
     */
    public final UI getUI() {
        return ui;
    }

    /**
     * Prints license information in the log.
     */
    public final void licenseLogEntry() {
        final String info =
            "\n-----------------------------------------------------------"
            + "\n Copyright Douglas Wikstrom 2008-2019"
            + "\n Licensed under AGPL as defined in:"
            + "\n https://www.gnu.org/licenses/agpl-3.0.en.html"
            + "\n-----------------------------------------------------------";

        ui.getLog().plainInfo(info);
    }

    /**
     * Verifies that the session identifier has the correct form and
     * throws an exception otherwise.
     *
     * @param sid Session identifier.
     *
     * @throws ProtocolError If the session identifier does not
     *  consist of at most {@link #SID_MAX_LEN} digits 0-9, letters
     *  a-z or A-Z, or underscore characters.
     */
    public static void validateSid(final String sid)
        throws ProtocolError {

        if (sid.length() > SID_MAX_LEN) {
            final String e =
                "The session identifier must be at most " + SID_MAX_LEN
                + " characters long. It is " + sid.length() + ".";
            throw new ProtocolError(e);
        }
        for (int i = 0; i < sid.length(); i++) {
            final char c = sid.charAt(i);
            if (!('0' <= c && c <= '9'
                  || 'a' <= c && c <= 'z'
                  || 'A' <= c && c <= 'Z'
                  || c == '_')) {
                final String e =
                    "The session identifier must consist of only "
                    + "of digits 0-9, letters a-z or A-Z, or underscore.";
                throw new ProtocolError(e);
            }
        }
    }

    /**
     * Creates a root protocol. This constructor should normally only
     * be called once in each application. All other protocols should
     * be constructed by calling a constructor of a subclass of this
     * class that makes a super call to
     * {@link #Protocol(String,Protocol)}.
     *
     * @param privateInfo Information about this party.
     * @param protocolInfo Information about the protocol executed,
     * including information about the other parties.
     * @param ui User interface.
     *
     * @throws ProtocolError If the protocol can not be instantiated
     * because the parameters are illegal or resources can not be
     * allocated. This error can safely be caught, i.e., any resources
     * allocated while attempting to instantiate this class are
     * released before throwing the error.
     */
    public Protocol(final PrivateInfo privateInfo,
                    final ProtocolInfo protocolInfo,
                    final UI ui)
        throws ProtocolError {

        // No protocol is invoking this one.
        this.parent = null;

        // Session ID of root protocol. This is read from file and
        // separates this execution from all previous, provided
        // that the user provides a fresh session ID.
        sid = protocolInfo.getStringValue(SID);
        validateSid(sid);

        // User interface.
        this.ui = ui;

        // Set up the directory of this instance.
        this.directory = new File(privateInfo.getStringValue(DIRECTORY));
        try {
            ExtIO.mkdirs(directory);
        } catch (final EIOException eioe) {
            throw new ProtocolError("Unable to create directory!", eioe);
        }

        // Initialize directory for temporary files.
        final File tmpFileDir = new File(this.directory, "tmp");
        try {
            ExtIO.mkdirs(tmpFileDir);
        } catch (final EIOException eioe) {
            throw new ProtocolError("Unable to create directory!", eioe);
        }
        TempFile.init(tmpFileDir);

        // Number of parties involved in the protocol.
        this.k = protocolInfo.getNumberOfParties();

        // Set the index of this party.
        final String name = privateInfo.getStringValue(NAME);
        try {
            this.j = protocolInfo.getIndex(name);
        } catch (final InfoException ie) {
            throw new ProtocolError("The protocol info does not contain a "
                                    + "segment for the party with the name "
                                    + name + "!", ie);
        }

        // Set default source of randomness used by all subprotocols.
        this.randomSource = randomSource(privateInfo);

        // If we get randomness from a PRG, then we need to seed it.
        if (randomSource instanceof PRG) {
            final File seedFile = getFile(SEED_FILENAME);
            final File tmpSeedFile = getFile(TMP_SEED_FILENAME);

            // We replace the seed before continuing to avoid reuse.
            try {
                ((PRG) randomSource).setSeedReplaceStored(seedFile,
                                                               tmpSeedFile);
            } catch (final CryptoException ce) {
                throw new ProtocolError("Unable to read or to "
                                        + "write PRG seed from seed file! "
                                        + ce.getMessage(),
                                        ce);
            }
        }

        // Set statistical distance used when sampling random objects.
        this.rbitlen = protocolInfo.getIntValue(STATDIST);

        // Set maximal accepted probability to fail identifying
        // errors during probabilistic tests.
        this.certainty = privateInfo.getIntValue(CERTAINTY);

        // Set a globally unique "fullname".
        this.fullName = getNameAndSid();

        // Set array indicating the array of currently active parties.
        final File activeFile = getFile("active");
        if (activeFile.exists()) {

            try {
                final ByteTree bt = new ByteTree(activeFile);
                active = ByteTree.byteTreeToBooleanArray(bt);
            } catch (final IOException eio) {
                throw new ProtocolError("Unable to read active file! ("
                                        + activeFile.toString() + ")", eio);
            } catch (final EIOException eioe) {
                throw new ProtocolError("Unable to read active file! ("
                                        + activeFile.toString() + ")", eioe);
            }

        } else {

            active = new boolean[k + 1];
            Arrays.fill(active, true);
            writeActive(active);
        }
    }

    /**
     * Creates a child instance of <code>protocol</code> with session
     * identifier <code>sid</code>. It copies most of the fields of
     * the input protocol, but some fields are modified to give
     * subprotocols local scope, e.g., a distinct subdirectory is
     * created for each subprotocol.
     *
     * @param sid Session identifier for this instance, unique among
     * the instances created by the parent protocol given as input.
     * @param prot Protocol that invokes this protocol as a
     * subprotocol.
     *
     * @throws ProtocolError If the resources needed for instantiation
     * can not be allocated. This error can safely be caught, i.e.,
     * any resources allocated while attempting to instantiate are
     * released before throwing the error.
     */
    public Protocol(final String sid, final Protocol prot)
        throws ProtocolError {

        this.parent = prot;
        this.sid = sid;
        this.k = prot.k;
        this.j = prot.j;
        this.randomSource = prot.randomSource;
        this.ui = prot.ui;

        // Create our own sub directory if it does not exist.
        this.directory = new File(prot.directory, getNameAndSid());
        try {
            ExtIO.mkdirs(directory);
        } catch (final EIOException eioe) {
            throw new ProtocolError("Unable to create directory!", eioe);
        }

        this.rbitlen = prot.rbitlen;
        this.certainty = prot.certainty;

        // Make sure we have a globally unique full name.
        this.fullName = prot.fullName + "/" + getNameAndSid();

        // Only the root protocol keeps the list of active parties.
        this.active = null;
    }

    /**
     * Returns the root protocol of which this protocol is a
     * descendant.
     *
     * @return Root protocol of which this protocol is a descendant.
     */
    public Protocol getRootProtocol() {
        if (parent == null) {
            return this;
        } else {
            return parent.getRootProtocol();
        }
    }

    /**
     * Returns working directory of this instance.
     *
     * @return Working directory of this instance.
     */
    public File getDirectory() {
        return directory;
    }

    /**
     * Deletes the complete state associated with this protocol,
     * including any allocated resources.
     */
    public void deleteState() {
        ExtIO.delete(directory);
    }

    /**
     * Returns the file corresponding to the input file name in the
     * directory of this instance.
     *
     * @param filename Filename.
     * @return A file with the given name in the directory of this
     * instance.
     */
    public final File getFile(final String filename) {
        return new File(directory, filename);
    }

    /**
     * Returns the qualified name of the class of this instance.
     *
     * @return Name of this class.
     */
    protected final String getName() {
        return this.getClass().getName();
    }

    /**
     * Returns the concatenation of the (unqualified) name of this
     * class and the session identifier, or only the (unqualified)
     * name if session identifier is empty.
     *
     * @return Name of this class concatenated with the session
     * identifier.
     */
    protected final String getNameAndSid() {

        String name = getName();

        final int index = name.lastIndexOf(".");
        if (index >= 0) {
            name = name.substring(index + 1);
        }

        String res;

        if ("".equals(sid)) {
            res = name;
        } else {
            res = name + "." + sid;
        }

        return res.replace("$", ".");
    }

    /**
     * Returns the full name for this instance of the protocol that is
     * unique among all executing subprotocols.
     *
     * @return Full name of this instance.
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Write the given integer to a file with the given name in the
     * working directory of this protocol. It is the responsibility of
     * the programmer to avoid naming conflicts with files created in
     * other ways.
     *
     * @param name Name of integer.
     * @param value Value to be stored.
     *
     * @throws ProtocolError If the value can not be written under the
     * given name.
     * @throws IOError If the file can not be closed.
     */
    public void writeInt(final String name, final int value)
        throws ProtocolError, IOError {

        try {
            final File file = getFile(name);
            ByteTree.intToByteTree(value).writeTo(file);
        } catch (final EIOException eioe) {
            throw new ProtocolError("", eioe);
        }
    }

    /**
     * Read the integer stored in a file with the given name in the
     * working directory of this protocol. If it does not exist, then
     * zero is stored on a file with the given name and zero is
     * returned.
     *
     * @param name Name of counter.
     * @return Stored integer.
     *
     * @throws ProtocolError If the value exists on file, but can not
     * be read.
     * @throws IOError If the file can not be closed.
     */
    public int readInt(final String name) throws ProtocolError, IOError {

        final File file = getFile(name);
        if (file.exists()) {
            try {
                final ByteTree bt = new ByteTree(file);
                return ByteTree.unsafeByteTreeToInt(bt);
            } catch (final EIOException eioe) {
                throw new ProtocolError("Unable to read int from file! ("
                                        + file.toString() + ")", eioe);
            } catch (final IOException ioe) {
                throw new ProtocolError("Unable to read counter! ("
                                        + file.toString() + ")", ioe);
            }
        } else {
            writeInt(name, 0);
            return 0;
        }
    }

    /**
     * Returns the value of a <code>boolean</code> value stored under
     * the given name in the working directory of this protocol. If no
     * value has been stored, then <code>false</code> is returned.
     *
     * @param name Name of value.
     * @return Boolean value of the stored value.
     */
    public boolean readBoolean(final String name) {
        final File file = getFile(name);
        return file.exists();
    }

    /**
     * Stores a <code>boolean</code> value with the given name in the
     * working directory of this protocol. It is the responsibility of
     * the programmer to avoid naming conflicts.
     *
     * @param name Name of value.
     *
     * @throws ProtocolError If the boolean can not be written to file.
     */
    public void writeBoolean(final String name)
        throws ProtocolError {
        try {
            final File file = getFile(name);

            if (file.exists() && !file.delete()) {
                throw new ProtocolError("Failed to delete file!");
            }

            if (!file.createNewFile()) {
                throw new ProtocolError("Failed to create boolean file!");
            }

        } catch (final IOException ioe) {
            throw new ProtocolError("Unable to store boolean value!", ioe);
        }
    }

    /**
     * Returns an array of booleans indicating the currently active
     * parties.
     *
     * @return Array indicating the currently active parties.
     */
    public boolean[] getActives() {

        final boolean[] a = getRootProtocol().active;
        return Arrays.copyOfRange(a, 0, a.length);
    }

    /**
     * Return true if and only if the party with the given index is
     * active.
     *
     * @param l Index of party.
     * @return True if and only if the party with the given index is
     * active.
     *
     * @throws ProtocolError If the input index is illegal.
     */
    public boolean getActive(final int l)
        throws ProtocolError {
        if (l < 1 || k < l) {
            throw new ProtocolError("Illegal index! (" + l + ")");
        }
        return getRootProtocol().active[l];
    }

    /**
     * Return true if and only if this party is active.
     *
     * @return True if and only if this party is active.
     */
    public boolean getActive() {
        return getActive(j);
    }

    /**
     * Returns a string representation of the set of indices of
     * currently active parties.
     *
     * @return String representation of the set of indices of
     * currently active parties.
     */
    public String getActiveString() {

        final boolean[] act = getActives();

        final StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (int i = 1; i <= k; i++) {
            if (act[i]) {
                sb.append(i);
                sb.append(',');
            }
        }

        if (sb.charAt(sb.length() - 1) == ',') {
            sb.deleteCharAt(sb.length() - 1);
        }

        sb.append('}');
        return sb.toString();
    }

    /**
     * Write the array of currently active parties.
     *
     * @param active Array of booleans indicating the currently active
     * parties.
     *
     * @throws ProtocolError If the indices of active parties can not
     * be written to file.
     * @throws IOError If the file storing the indices can not be
     * closed.
     */
    protected void writeActive(final boolean[] active)
        throws ProtocolError, IOError {

        final File file = getFile("active");
        try {
            if (!ExtIO.delete(file)) {
                throw new ProtocolError("Unable to delete activation file!");
            }
            ByteTree.booleanArrayToByteTree(getRootProtocol().active).
                writeTo(file);
        } catch (final EIOException eioe) {
            throw new ProtocolError("Failed to write indices of active "
                                    + "parties to file! ("
                                    + file.toString() + ")", eioe);
        }
    }

    /**
     * Sets the array of booleans indicating the currently active
     * parties.
     *
     * @param active Array of booleans indicating the currently active
     * parties.
     *
     * @throws ProtocolError If the array of indices has the wrong
     * length, or if it can not be written to file.
     * @throws IOError If the file storing the indices can not be
     * closed.
     */
    public void setActive(final boolean[] active)
        throws ProtocolError, IOError {

        if (active.length != k + 1) {
            throw new ProtocolError("Wrong length of array! ("
                                    + active.length + " instead of "
                                    + (k + 1) + ")");
        }

        final Protocol root = getRootProtocol();
        root.active = Arrays.copyOfRange(active, 0, active.length);
        root.writeActive(active);
    }
}
