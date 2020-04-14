
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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.verificatum.eio.ByteTree;
import com.verificatum.protocol.com.BullBoard;
import com.verificatum.protocol.com.BullBoardBasic;
import com.verificatum.ui.Log;
import com.verificatum.ui.UI;
import com.verificatum.ui.info.PrivateInfo;
import com.verificatum.ui.info.ProtocolInfo;


/**
 * Adds a bulletin board to {@link Protocol}, i.e., it implements the
 * basic functionality required by cryptographic protocols that uses a
 * bulletin board for communication between the parties.
 *
 * @author Douglas Wikstrom
 */
public class ProtocolBB extends Protocol {

    /**
     * Name of bulletin board tag.
     */
    public static final String BULLBOARD = "bullboard";

    /**
     * Default maximal amount of time waiting to download a message.
     */
    public static final int DEFAULT_MAXIMAL_WAIT_TIME = -1;

    /**
     * Default upper bound on the number of bytes in a message. This
     * is set to 1024 GB, so it is virtually unbounded.
     */
    public static final long DEFAULT_MAXIMAL_BYTE_LENGTH =
        1024L * 1024L * 1024L * 1024L;

    /**
     * Default upper bound on the recursion depth of messages.
     */
    public static final int DEFAULT_MAXIMAL_RECURSION_DEPTH = 10;

    /**
     * Time to wait in milliseconds after we have downloaded all other
     * servers completion acknowledgement to allow all other servers
     * to download our own completion acknowledgment.
     */
    public static final int WAIT_FOR_OTHERS_TIME = 1000;

    /**
     * Bulletin board used to communicate.
     */
    protected final BullBoard bullBoard;

    /**
     * Used to synchronize when shutting down.
     */
    protected Shutdown shutdown;

    /**
     * Creates a root protocol. This constructor should normally only
     * be called once in each application. All other protocols should
     * be constructed by calling a constructor of a subclass of this
     * class that makes a super call to {@link
     * #ProtocolBB(String,ProtocolBB)}.
     *
     * @param privateInfo Information about this party.
     * @param protocolInfo Information about the protocol executed,
     * including information about other parties.
     * @param ui User interface.
     *
     * @throws ProtocolError If the protocol can not be instantiated
     * because the parameters are illegal or resources can not be
     * allocated. This error can safely be caught, i.e., any resources
     * allocated while attempting to instantiate this class are
     * released before throwing the error.
     */
    public ProtocolBB(final PrivateInfo privateInfo,
                      final ProtocolInfo protocolInfo,
                      final UI ui)
        throws ProtocolError {

        super(privateInfo, protocolInfo, ui);

        // Fetch name of class implementing the bulletin board.
        final String className = protocolInfo.getStringValue(BULLBOARD);

        // Instantiate basic bulletin board.
        BullBoardBasic bullBoardBasic = null;
        try {

            final Class<?> klass = Class.forName(className);
            final Constructor<?> constructor =
                klass.getConstructor(PrivateInfo.class,
                                     ProtocolInfo.class,
                                     UI.class);
            bullBoardBasic =
                (BullBoardBasic) constructor.newInstance(privateInfo,
                                                         protocolInfo,
                                                         ui);
        } catch (final InvocationTargetException ite) {
            throw new ProtocolError("Unable to interpret, unknown target!",
                                    ite);
        } catch (final InstantiationException ie) {
            throw new ProtocolError("Unable to interpret, can not instantiate!",
                                    ie);
        } catch (final IllegalAccessException iae) {
            throw new ProtocolError("Unable to interpret, illegal access!",
                                    iae);
        } catch (final ClassNotFoundException cnfe) {
            throw new ProtocolError("Bulletin board class can not be found ("
                                    + className + ")!", cnfe);
        } catch (final NoSuchMethodException nsme) {
            throw new ProtocolError("No appropriate constructor!", nsme);
        }

        // Set bulletin board that allows instantiating a tree of
        // bulletin boards with local scope.
        bullBoard = new BullBoard(this,
                                  bullBoardBasic,
                                  DEFAULT_MAXIMAL_WAIT_TIME,
                                  DEFAULT_MAXIMAL_BYTE_LENGTH,
                                  DEFAULT_MAXIMAL_RECURSION_DEPTH);
    }

    /**
     * Creates a child instance of <code>protocol</code> with session
     * identifier <code>sid</code>. It copies most of the fields of
     * the input protocol, which is convenient when implementing
     * subprotocols. It also initializes an fresh instance of a
     * bulletin board with local scope that can be used by the
     * constructed instance.
     *
     * @param sid Session identifier for this instance.
     * @param prot Protocol that invokes this protocol as a
     * subprotocol.
     */
    public ProtocolBB(final String sid, final ProtocolBB prot) {
        super(sid, prot);
        this.bullBoard = new BullBoard(this, prot.bullBoard);
    }

    /**
     * Returns the total milliseconds of network activity after the
     * initial successful message. This does not include the time
     * waiting for a party to complete its computations.
     *
     * @return Time used for communication so far.
     */
    public long getTotalNetworkTime() {
        return bullBoard.getTotalNetworkTime();
    }

    /**
     * Returns the total milliseconds of waiting for computations to
     * be performed by other parties.
     *
     * @return Time used for waiting so far.
     */
    public long getTotalWaitingTime() {
        return bullBoard.getTotalWaitingTime();
    }

    /**
     * Returns number of received bytes.
     *
     * @return Number of received bytes.
     */
    public long getReceivedBytes() {
        return bullBoard.getReceivedBytes();
    }

    /**
     * Returns the total number of bytes sent.
     *
     * @return Total number of bytes sent.
     */
    public long getSentBytes() {
        return bullBoard.getSentBytes();
    }

    @Override
    public void setActive(final boolean[] active) {
        super.setActive(active);
        bullBoard.setActive(active);
    }

    /**
     * Hook that can be used by subclasses to embed an entry at the
     * top of the log.
     */
    public void hookLogEntry() {
    }

    /**
     * Starts the underlying servers.
     */
    public void startServers() {

        shutdown = new Shutdown("shutdown", this);
        shutdown.deleteState();

        // Put header of log.
        licenseLogEntry();
        hookLogEntry();

        final Log log = ui.getLog();

        bullBoard.start(ui.getLog());
        log.info("Synchronizing with the other servers. Please wait.");
        if (j == 1) {
            bullBoard.publish("Synchronize", new ByteTree(), log);
        } else {
            bullBoard.waitFor(1, "Synchronize", log);
        }

        // It makes no sense to count delays during the
        // synchronization as network time.
        bullBoard.clearTotalNetworkTime();
    }

    /**
     * Perform a graceful shutdown of the protocol. Every execution of
     * the protocol should end with a call to this method, since
     * before it returns it <b>tries</b> to guarantee that all other
     * parties have successfully completed the execution as well. It
     * also shuts down all servers and releases all allocated
     * resources.
     *
     * <p>
     *
     * Note that it is <b>impossible</b> to be sure that all parties
     * received the last message in a protocol. Thus, the goal of this
     * method is to exchange some dummy messages until we are fairly
     * sure that no party is stuck in a real protocol step. We wait
     * for others to complete.
     *
     * @param log Logging context.
     */
    public void shutdown(final Log log) {

        if (shutdown == null) {
            shutdown = new Shutdown("shutdown", this);
            shutdown.deleteState();
        }
        shutdown.execute(log);
    }

    /**
     * Deletes the complete state associated with this protocol. This
     * can be used to reset the parent protocol to a previous state.
     */
    @Override
    public void deleteState() {

        bullBoard.unpublish();
        super.deleteState();
    }
}
