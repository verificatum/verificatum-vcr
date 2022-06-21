
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

package com.verificatum.eio;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides utility functions for file related operations.
 *
 * @author Douglas Wikstrom
 */
public final class ExtIO {

    /**
     * Character encoding used to encode objects as strings.
     */
    public static final String CHARACTER_ENCODING = "UTF-8";

    /**
     * Avoid accidental instantiation.
     */
    private ExtIO() { }

    /**
     * Number of bytes in buffer used for copying.
     */
    static final int COPY_BUFFER_SIZE = 8192;

    /**
     * Returns a human readable representation of the input number of
     * bytes.
     *
     * @param inputBytes Number of input bytes.
     * @return Human readable description of number of bytes.
     */
    public static String bytesToHuman(final long inputBytes) {

        final double gigabytes = ((double) inputBytes) / (1000 * 1000 * 1000);
        final double megabytes = ((double) inputBytes) / (1000 * 1000);
        final double kilobytes = ((double) inputBytes) / 1000;
        final double bytes = ((double) inputBytes);

        double value;
        String postfix;
        if (gigabytes > 1) {
            value = gigabytes;
            postfix = "GB";
        } else if (megabytes > 1) {
            value = megabytes;
            postfix = "MB";
        } else if (kilobytes > 1) {
            value = kilobytes;
            postfix = "KB";
        } else {
            value = bytes;
            postfix = "B ";
        }
        return String.format("%.1f %s", value, postfix);
    }

    /**
     * Returns a reader of a file. Any allocated resources are
     * released before throwing an exception upon error.
     *
     * @param file File to read.
     * @return Reader of given file.
     * @throws IOException If creating the reader fails for some
     * reason.
     */
    public static BufferedReader getBufferedReader(final File file)
        throws IOException {

        FileInputStream fis = null;
        InputStreamReader isr = null;
        try {

            fis = new FileInputStream(file);
            isr = new InputStreamReader(fis, ExtIO.CHARACTER_ENCODING);
            return new BufferedReader(isr);

        } catch (final IOException ioe) {
            strictClose(isr);
            strictClose(fis);
            throw new IOException("Failed to open reader of file! ("
                                  + file.toString() + ")", ioe);
        }
    }

    /**
     * Returns a writer of a file. Any allocated resources are
     * released before throwing an exception upon error.
     *
     * @param file File to write.
     * @return Writer of given file.
     * @throws IOException If creating the writer fails for some
     * reason.
     */
    public static BufferedWriter getBufferedWriter(final File file)
        throws IOException {

        FileOutputStream fos = null;
        OutputStreamWriter osr = null;
        try {

            fos = new FileOutputStream(file);
            osr = new OutputStreamWriter(fos, ExtIO.CHARACTER_ENCODING);
            return new BufferedWriter(osr);

        } catch (final IOException ioe) {
            strictClose(osr);
            strictClose(fos);
            throw new IOException("Failed to open writer of file! ("
                                  + file.toString() + ")", ioe);
        }
    }

    /**
     * Returns an array of bytes representing the input string.
     *
     * @param s Input string.
     * @return Representation of input string as an array of bytes
     * using UTF-8 format.
     */
    public static byte[] getBytes(final String s) {
        try {
            return s.getBytes(CHARACTER_ENCODING);
        } catch (final UnsupportedEncodingException uee) {
            throw new EIOError("Bad encoding!", uee);
        }
    }

    /**
     * Makes the input directories.
     *
     * @param dir Input directory.
     * @throws EIOException If it is not possible to create the
     * directories.
     */
    public static void mkdirs(final File dir) throws EIOException {
        if (dir.exists()) {
            return;
        } else {

            if (!dir.mkdirs()) {
                throw new EIOException("The directory exists! ("
                                       + dir.toString() + ")");
            }
        }
    }

    /**
     * Returns a new array containing the same data as the input, but
     * prepended with the length of the input array as an integer
     * represented by four bytes.
     *
     * @param data Original array.
     * @return Array containing the length.
     */
    public static byte[] lengthEmbedded(final byte[] data) {
        final byte[] res = new byte[data.length + 4];
        ExtIO.writeInt(res, 0, data.length);
        System.arraycopy(data, 0, res, 4, data.length);
        return res;
    }

    /**
     * Removes length embedding, but in a verifiable way.
     *
     * @param data Original array.
     * @return Array without the length as a prefix.
     * @throws EIOException if the embedded length is inconsistent
     *  with the actual length of the input.
     */
    public static byte[] lengthDebedded(final byte[] data) throws EIOException {
        final int len = ExtIO.readInt(data, 0);
        if (len > data.length - 4) {
            throw new EIOException("Embedded length is too large!");
        }
        return Arrays.copyOfRange(data, 4, len + 4);
    }

    /**
     * Writes an <code>int</code> as four bytes.
     *
     * @param result Destination array.
     * @param offset Index where to start writing.
     * @param n Value to write.
     */
    public static void writeInt(final byte[] result,
                                final int offset,
                                final int n) {

        int currentOffset = offset;

        result[currentOffset++] = (byte) (n >>> 24 & 0xff);
        result[currentOffset++] = (byte) (n >>> 16 & 0xff);
        result[currentOffset++] = (byte) (n >>> 8 & 0xff);
        result[currentOffset] = (byte) (n & 0xff);
    }

    /**
     * Writes an <code>int[]</code> as a <code>byte[]</code>.
     *
     * @param result Destination array.
     * @param woffset Index where to start writing.
     * @param ints Values to write.
     * @param ioffset Index where to start reading.
     * @param len Number of integers to read.
     */
    public static void writeInts(final byte[] result,
                                 final int woffset,
                                 final int[] ints,
                                 final int ioffset,
                                 final int len) {
        int currentWoffset = woffset;

        for (int i = ioffset; i < len; i++) {
            result[currentWoffset++] = (byte) ((ints[i] >> 24) & 0xFF);
            result[currentWoffset++] = (byte) ((ints[i] >> 16) & 0xFF);
            result[currentWoffset++] = (byte) ((ints[i] >> 8) & 0xFF);
            result[currentWoffset++] = (byte) (ints[i] & 0xFF);
        }
    }

    /**
     * Reads an <code>int</code> from a <code>byte[]</code>.
     *
     * @param bytes Source of the integer.
     * @param offset Index where to start reading.
     * @return Integer that is read.
     */
    public static int readInt(final byte[] bytes, final int offset) {

        int currentOffset = offset;

        int n = bytes[currentOffset++] & 0xFF;
        n <<= 8;
        n |= bytes[currentOffset++] & 0xFF;
        n <<= 8;
        n |= bytes[currentOffset++] & 0xFF;
        n <<= 8;
        n |= bytes[currentOffset] & 0xFF;
        return n;
    }

    /**
     * Writes an <code>int</code> as four bytes.
     *
     * @param result Destination array.
     * @param offset Index where to start writing.
     * @param n Value to write.
     */
    public static void writeLong(final byte[] result,
                                 final int offset,
                                 final int n) {

        int currentOffset = offset;

        result[currentOffset++] = (byte) (n >>> 56 & 0xff);
        result[currentOffset++] = (byte) (n >>> 48 & 0xff);
        result[currentOffset++] = (byte) (n >>> 40 & 0xff);
        result[currentOffset++] = (byte) (n >>> 32 & 0xff);

        result[currentOffset++] = (byte) (n >>> 24 & 0xff);
        result[currentOffset++] = (byte) (n >>> 16 & 0xff);
        result[currentOffset++] = (byte) (n >>> 8 & 0xff);
        result[currentOffset] = (byte) (n & 0xff);
    }

    /**
     * Reads a <code>long</code> from a <code>byte[]</code>.
     *
     * @param bytes Source of the long integer.
     * @param offset Index where to start reading.
     * @return Long integer that is read.
     */
    public static long readLong(final byte[] bytes, final int offset) {

        int currentOffset = offset;

        long n = bytes[currentOffset++] & 0xFF;
        for (int i = 0; i < 7; i++) {
            n <<= 8;
            n |= bytes[currentOffset++] & 0xFF;
        }
        return n;
    }

    /**
     * Reads an <code>int[]</code> from a <code>byte[]</code>.
     *
     * @param result Destination array.
     * @param woffset Index where to start writing.
     * @param bytes Values to write.
     * @param boffset Index where to start reading.
     * @param len Number of bytes to read.
     */
    public static void readInts(final int[] result,
                                final int woffset,
                                final byte[] bytes,
                                final int boffset,
                                final int len) {

        int currentBoffset = boffset;

        for (int i = woffset; i < len; i++) {

            int n = bytes[currentBoffset++] & 0xFF;
            n <<= 8;
            n |= bytes[currentBoffset++] & 0xFF;
            n <<= 8;
            n |= bytes[currentBoffset++] & 0xFF;
            n <<= 8;
            n |= bytes[currentBoffset++] & 0xFF;

            result[i] = n;
        }
    }

    /**
     * Writes a <code>short</code> as two bytes.
     *
     * @param result Destination array.
     * @param i Index where to start writing.
     * @param n The <code>short</code> value to write.
     */
    public static void writeShort(final byte[] result,
                                  final int i,
                                  final short n) {
        int offset = i;

        result[offset++] = (byte) (n >>> 8 & 0xff);
        result[offset] = (byte) (n & 0xff);
    }

    /**
     * Reads a <code>short</code> from a <code>byte[]</code>. It is
     * assumed that there is a multiple of 2 bytes in the input.
     *
     * @param source Source array.
     * @param i Index where to start reading.
     * @return Integer value at index <code>i</code> in
     *         <code>result</code>.
     */
    public static short readShort(final byte[] source, final int i) {

        int offset = i;
        short n = 0;
        n |= source[offset++] & 0xFF;
        n <<= 8;
        n |= source[offset] & 0xFF;
        return n;
    }

    /**
     * Attempts to close the given object if it is not null, and if
     * this fails it throws an error. Recall that by convention
     * closable objects can be closed any number of times. Thus, this
     * catches only actual errors occurring during closing and not
     * multiple calls.
     *
     * @param closeable Object that can be closed or null.
     * @throws IOError If the object throws an
     *  <code>IOException</code> during closing.
     */
    public static void strictClose(final Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (final IOException ioe) {
            throw new IOError(ioe);
        }
    }

    /**
     * Copies a file.
     *
     * @param sourceFile Contents to be copied.
     * @param destFile Destination of copied content.
     *
     * @throws SecurityException If a security manager exists and its
     *  <code>checkRead</code>/ <code>checkWrite</code>
     *  method denies access to a file.
     * @throws IOException If some other I/O error occurs.
     */
    public static void copyFile(final File sourceFile, final File destFile)
        throws IOException {

        if (!destFile.exists() && !destFile.createNewFile()) {
            throw new EIOError("Unable to create destination file!");
        }

        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();

            long pos = 0;
            final long size = source.size();
            while (pos < size) {

                pos += destination.transferFrom(source, pos, size - pos);
            }

        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    /**
     * Writes the contents of the source stream to the destination
     * stream.
     *
     * @param sourceStream Contents to be copied.
     * @param destStream Destination of copied content.
     *
     * @throws IOException If some I/O error occurs.
     */
    public static void copy(final InputStream sourceStream,
                            final OutputStream destStream)
        throws IOException {

        final byte[] buf = new byte[COPY_BUFFER_SIZE];
        int len = sourceStream.read(buf);
        while (len >= 0) {
            destStream.write(buf, 0, len);
            len = sourceStream.read(buf);
        }
    }

    /**
     * Writes the contents of the source stream to the destination
     * file.
     *
     * @param sourceStream Contents to be copied.
     * @param destFile Destination of copied content.
     *
     * @throws IOException If some I/O error occurs.
     */
    public static void copy(final InputStream sourceStream,
                            final File destFile)
        throws IOException {

        FileOutputStream fos = null;
        try {

            fos = new FileOutputStream(destFile);

            copy(sourceStream, fos);

        } finally {
            ExtIO.strictClose(fos);
        }
    }

    /**
     * Writes the contents of the source file to the destination
     * stream.
     *
     * @param sourceFile Contents to be copied.
     * @param destStream Destination of copied content.
     *
     * @throws IOException If some I/O error occurs.
     */
    public static void copy(final File sourceFile,
                            final OutputStream destStream)
        throws IOException {

        FileInputStream fis = null;
        try {

            fis = new FileInputStream(sourceFile);

            copy(fis, destStream);

        } finally {
            ExtIO.strictClose(fis);
        }
    }

    /**
     * Returns the complete contents of the file as a string. It is
     * the responsibility of the programmer to make sure that the
     * contents of the file is not too large.
     *
     * @param file Location of string.
     * @return Contents of file.
     *
     * @throws IOException If an I/O error occurs.
     */
    public static String readString(final File file) throws IOException {

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            return readString(fis);
        } finally {
            ExtIO.strictClose(fis);
        }
    }

    /**
     * Returns the complete contents of the stream as a string. It is
     * the responsibility of the programmer to make sure that the
     * contents of the stream are not too large.
     *
     * @param is Source of string.
     * @return Contents of stream.
     *
     * @throws IOException If an I/O error occurs.
     */
    public static String readString(final InputStream is)
        throws IOException {

        final StringBuilder sb = new StringBuilder();

        final byte[] buf = new byte[COPY_BUFFER_SIZE];
        int len = is.read(buf);

        while (len >= 0) {
            sb.append(new String(buf, 0, len, CHARACTER_ENCODING));
            len = is.read(buf);
        }
        return sb.toString();
    }

    /**
     * Returns the complete contents of the file as a string. It is
     * the responsibility of the programmer to make sure that the
     * contents of the file is not too large.
     *
     * @param file Location of string.
     * @return Contents of file.
     */
    public static String unsafeReadString(final File file) {
        try {
            return readString(file);
        } catch (final IOException e) {
            throw new EIOError("Unable to read string!", e);
        }
    }

    /**
     * Write string to file.
     *
     * @param file Destination of string.
     * @param s String to be written.
     *
     * @throws IOException If a file can not be opened or written as
     *  needed.
     */
    public static void writeString(final File file, final String s)
        throws IOException {

        final PrintWriter pw = new PrintWriter(file, CHARACTER_ENCODING);
        try {
            pw.write(s);
        } finally {
            pw.close();
        }
    }

    /**
     * Write string to file.
     *
     * @param file Destination of string.
     * @param s String to be written.
     */
    public static void unsafeWriteString(final File file, final String s) {
        try {
            writeString(file, s);
        } catch (final IOException ioe) {
            throw new EIOError("Unable to write string!", ioe);
        }
    }

    /**
     * Write integer to file.
     *
     * @param file Destination of string.
     * @param n Integer to be written.
     *
     * @throws IOException If a file can not be opened or written as
     *  needed.
     */
    public static void writeInt(final File file, final int n)
        throws IOException {
        writeString(file, Integer.toString(n));
    }

    /**
     * Write integer to file.
     *
     * @param file Destination of string.
     * @param n Integer to be written.
     *
     * @throws EIOError If a file can not be opened or written as
     * needed.
     */
    public static void unsafeWriteInt(final File file, final int n)
        throws EIOError {
        try {
            writeString(file, Integer.toString(n));
        } catch (final IOException ioe) {
            throw new EIOError("Unable to write int!", ioe);
        }
    }

    /**
     * Formats and throws an exception thrown when failing to perform
     * an atomic move.
     *
     * @param description Description for the exception.
     * @param orig Original file.
     * @param dest Destination file.
     * @param cause Causing exception.
     * @return EIOException Formatted exception.
     */
    private static EIOException atomicEIOE(final String description,
                                           final File orig,
                                           final File dest,
                                           final Throwable cause) {
        return new EIOException(String.format("%s (%s -> %s)",
                                              description,
                                              orig.toString(),
                                              dest.toString()), cause);
    }

    /**
     * Atomically moves a file (not a directory). This may fail if the
     * destination already exists.
     *
     * @param orig Source file.
     * @param dest Destination name.
     *
     * @throws IOException If a file can not be opened or written as
     *  needed.
     * @throws EIOException Catch-all exception for a number of
     * exceptions thrown by <code>Files.move</code>.
     */
    public static void atomicMove(final File orig, final File dest)
        throws IOException, EIOException {
        try {
            Files.move(orig.toPath(), dest.toPath(),
                       StandardCopyOption.ATOMIC_MOVE);
        } catch (final UnsupportedOperationException uoe) {
            throw new EIOException("Unsupported copy option (bug)!", uoe);
        } catch (final FileAlreadyExistsException faee) {
            throw atomicEIOE("Destination exists!", orig, dest, faee);
        } catch (final DirectoryNotEmptyException dnee) {
            throw atomicEIOE("Unable to move directory!", orig, dest, dnee);
        } catch (final AtomicMoveNotSupportedException amnse) {
            throw atomicEIOE("Unable to move directory!", orig, dest, amnse);
        } catch (final SecurityException se) {
            throw atomicEIOE("Move not allowed!", orig, dest, se);
        }
    }

    /**
     * Atomic write of a string to file. This is "atomic" in the sense
     * that the contents first are written to the temporary file,
     * which is then renamed. Renaming may not be atomic.
     *
     * @param file Final destination of string.
     * @param tmpFile Temporary file used to implement atomic write.
     * @param s String to be written.
     *
     * @throws IOException If a file can not be opened or written as
     *  needed.
     */
    public static void atomicWriteString(final File tmpFile,
                                         final File file,
                                         final String s)
        throws IOException {

        // Delete target file if it exists.
        if (file.exists() && !file.delete()) {
            throw new IOException("Unable to delete file! (" + file + ")");
        }

        // Write contents to temporary file.
        writeString(tmpFile, s);

        // Rename temporary file to target file.
        if (!tmpFile.renameTo(file)) {
            throw new IOException("Unable to rename temporary file! ("
                                  + tmpFile + " to " + file + ")");
        }
    }

    /**
     * Returns a random access file containing the contents of the
     * input stream. No changes made to the random access file are
     * forwarded to the source file. It is the responsibility of the
     * programmer to make sure that the contents are not too large.
     *
     * @param is Stream to be converted.
     * @return Random access file.
     *
     * @throws IOException If the translation fails.
     */
    public static RandomAccessFile asRandomAccessFile(final InputStream is)
        throws IOException {

        final File tmpFile = File.createTempFile("isc", "tmp");
        final RandomAccessFile raf = new RandomAccessFile(tmpFile, "rwd");

        final byte[] buf = new byte[COPY_BUFFER_SIZE];
        int len = is.read(buf);
        while (len >= 0) {
            raf.write(buf, 0, len);
            len = is.read(buf);
        }

        raf.seek(0);

        return raf;
    }

    /**
     * Returns the total size of a file or directory.
     *
     * @param file File or directory to be measured.
     * @param visited Canonical paths already visited to avoid cases
     * of multiple paths to the same file or subdirectory.
     * @return Size of directory.
     *
     * @throws IOException If canonical paths can not be determined.
     */
    private static long fileSizeInner(final File file,
                                      final Set<String> visited)
        throws IOException {

        if (file.isDirectory()) {

            final File[] files = file.listFiles();
            if (files == null) {
                throw new IOException("Unable to list files!");
            }

            long size = 0;

            for (int i = 0; i < files.length; i++) {
                size += fileSizeInner(files[i], visited);
            }
            return size;

        } else {

            final String canonicalPath = file.getCanonicalPath();

            if (visited.contains(canonicalPath)) {
                return 0;
            } else {
                visited.add(canonicalPath);
                return file.length();
            }
        }
    }

    /**
     * Returns the total size of a file or directory.
     *
     * @param file File or directory to be measured.
     * @return Size of file or directory.
     *
     * @throws IOException If canonical paths can not be determined.
     */
    public static long fileSize(final File file) throws IOException {
        return fileSizeInner(file, new HashSet<String>());
    }

    /**
     * Returns true or false depending on the contents of the two
     * files are identical or not.
     *
     * @param file1 First file.
     * @param file2 First file.
     * @return <code>true</code> or <code>false</code> depending on if
     *         the contents of the input files are identical or not.
     *
     * @throws IOException If an I/O error occurs.
     */
    public static boolean equals(final File file1, final File file2)
        throws IOException {

        if (file1.length() != file2.length()) {
            return false;
        }

        BufferedInputStream bis1 = null;
        BufferedInputStream bis2 = null;

        try {

            bis1 = new BufferedInputStream(new FileInputStream(file1));
            bis2 = new BufferedInputStream(new FileInputStream(file2));

            int byte1;
            int byte2;
            do {

                byte1 = bis1.read();
                byte2 = bis2.read();

            } while (byte1 == byte2 && byte1 != -1);

            return byte1 == -1;

        } finally {
            strictClose(bis1);
            strictClose(bis2);
        }
    }

    // NATIVE
    /**
     * Returns true or false depending on the contents of the two
     * files are identical or not.
     *
     * @param file1 First file.
     * @param file2 First file.
     * @return <code>true</code> or <code>false</code> depending on if
     *         the contents of the input files are identical or not.
     *
     * @throws SecurityException If a security manager exists and
     *  doesn't allow creation of a subprocess.
     * @throws IOException If an I/O error occurs.
     * @throws InterruptedException If the current thread is
     *  interrupted by another thread.
     */
    public static boolean oldequals(final File file1, final File file2)
        throws IOException, InterruptedException {

        final Runtime runtime = Runtime.getRuntime();

        final String command =
            "diff " + file2.toString() + " " + file1.toString();

        final Process proc = runtime.exec(command);

        proc.getErrorStream().close();
        proc.getInputStream().close();
        proc.getOutputStream().close();

        // Make sure "diff" has finished processing before returning.
        return proc.waitFor() == 0;
    }

    /**
     * Deletes a file or a directory and all its contents.
     *
     * @return Returns true or false if the input directory can be
     * deleted entirely or not.
     *
     * @param path File or directory to be deleted.
     */
    public static boolean delete(final File path) {

        if (!path.exists()) {
            return true;
        }

        if (path.isFile()) {
            return path.delete();
        }

        final String[] list = path.list();

        if (list != null) {

            for (int i = 0; i < list.length; i++) {

                final File childPath = new File(path, list[i]);

                if (childPath.isDirectory()) {

                    if (!ExtIO.delete(childPath)) {
                        return false;
                    }

                } else {
                    if (!childPath.delete()) {
                        return false;
                    }
                }
            }
        }
        return path.delete();
    }
}
