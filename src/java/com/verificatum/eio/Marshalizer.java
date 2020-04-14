
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

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.verificatum.arithm.LargeInteger;
import com.verificatum.arithm.PGroup;
import com.verificatum.crypto.CryptoError;
import com.verificatum.crypto.CryptoKeyGen;
import com.verificatum.crypto.CryptoPKey;
import com.verificatum.crypto.CryptoSKey;
import com.verificatum.crypto.Hashfunction;
import com.verificatum.crypto.HashfunctionFixedLength;
import com.verificatum.crypto.PRG;
import com.verificatum.crypto.RandomSource;
import com.verificatum.crypto.SignatureKeyGen;
import com.verificatum.crypto.SignatureKeyPair;
import com.verificatum.crypto.SignaturePKey;
import com.verificatum.crypto.SignatureSKey;
import com.verificatum.ui.gen.GeneratorTemplate;


/**
 * This class provides a uniform way of marshalling an instance of a
 * class that implements {@link Marshalizable} into a {@link
 * ByteTreeBasic}. It also provides a uniform way of recovering the
 * converted instance from such a representation using a {@link
 * ByteTreeReader}.
 *
 * <p>
 *
 * This is implemented using the Java reflection API. Using this API
 * it is straightforward to provide the first functionality. We simply
 * embed the class name of an instance as part of a slightly larger
 * representation.
 *
 * <p>
 *
 * The second functionality is also straightforward to implement if
 * one is willing to explicitly verify the class of an instance and
 * cast the output from the conversion method to the appropriate type
 * every time an instance is recovered. The drawback of this naive
 * approach is that there is no type safety.
 *
 * <p>
 *
 * We instead encapsulate all needed explicit type casts in
 * methods of the form:<br>
 *
 * <code>
 * public static TYPE unmarshal_TYPE({@link ByteTreeReader} btr)
 * </code>
 * <br>
 * <code>
 * public static TYPE unmarshalAux_TYPE({@link ByteTreeReader} btr,
 *                                      {@link RandomSource} rs,
 *                                      int certainty)
 * </code>
 * <br>
 *
 * for some type TYPE.
 *
 * @author Douglas Wikstrom
 */
// PMD_ANNOTATION @SuppressWarnings("PMD.MethodNamingConventions")
public final class Marshalizer {

    /**
     * Error message used when an instantiated object does not match
     * the expected type.
     */
    public static final String TYPE_DOES_NOT_MATCH_CAST =
        "Type does not match cast!";

    /**
     * Avoid accidental instantiation.
     */
    private Marshalizer() { }

    /**
     * Maximal number of characters in a class name.
     */
    static final int MAX_CLASSNAME_LENGTH = 2048;

    /**
     * Reads a <code>ByteTreeBasic</code> representation output by
     * {@link #marshal(Marshalizable)} and returns the corresponding
     * instance. The verification of the input may be probabilistic,
     * but must in that case guarantee that correctness holds with
     * probability <i>2<sup>-<code>certainty</code></sup></i>.
     *
     * @param btr An instance to be converted.
     * @param rs Random source used to probabilistically check the
     * validity of an input.
     * @param certainty Certainty with which an input is deemed
     * correct, i.e., an incorrect input is accepted with probability
     * at most 2<sup>-<code>certainty</code></sup>.
     * @return Instance recovered from the input representation.
     *
     * @throws EIOException If the input can not be
     * interpreted as an instance.
     */
    @SuppressWarnings("unchecked")
        protected static Object unmarshalAux(final ByteTreeReader btr,
                                             final RandomSource rs,
                                             final int certainty)
        throws EIOException {

        String className = "ingen";
        try {

            final ByteTreeReader cnbtr = btr.getNextChild();
            if (cnbtr.getRemaining() > MAX_CLASSNAME_LENGTH) {
                throw new EIOException("Too long classname!");
            }
            className = cnbtr.readString();
            final Class klass = Class.forName(className);

            try {
                final Method method = klass.getMethod("newInstance",
                                                      ByteTreeReader.class,
                                                      RandomSource.class,
                                                      java.lang.Integer.TYPE);
                final Object res =
                    method.invoke(null, btr.getNextChild(), rs, certainty);

                return res;

            } catch (final NoSuchMethodException nsme) {
                final Method method =
                    klass.getMethod("newInstance", ByteTreeReader.class);
                return method.invoke(null, btr.getNextChild());
            }
        } catch (final InvocationTargetException ite) {
            throw new EIOException("Unable to interpret, unknown target!", ite);
        } catch (final IllegalAccessException iae) {
            throw new EIOException("Unable to interpret, illegal access!", iae);
        } catch (final ClassNotFoundException cnfe) {
            throw new EIOException("Unable to interpret, unknown class ("
                                   + className + ")!", cnfe);
        } catch (final NoSuchMethodException nsme) {
            throw new EIOException("Unable to interpret, no method!", nsme);
        }
    }

    /**
     * Reads a <code>ByteTreeBasic</code> representation output by
     * {@link #marshal(Marshalizable)} and returns the corresponding
     * instance.
     *
     * @param btr Representation of instance.
     * @return Instance recovered from the input representation.
     *
     * @throws EIOException If the input can not be
     * interpreted as an instance.
     */
    @SuppressWarnings("unchecked")
        protected static Object unmarshal(final ByteTreeReader btr)
        throws EIOException {
        String className = "";
        try {

            final ByteTreeReader cnbtr = btr.getNextChild();
            if (cnbtr.getRemaining() > MAX_CLASSNAME_LENGTH) {
                throw new EIOException("Too long classname!");
            }
            className = cnbtr.readString();
            final Class klass = Class.forName(className);
            final Method method =
                klass.getMethod("newInstance", ByteTreeReader.class);

            return method.invoke(null, btr.getNextChild());

        } catch (final InvocationTargetException ite) {
            throw new EIOException("Unable to interpret, unknown target!", ite);
        } catch (final IllegalAccessException iae) {
            throw new EIOException("Unable to interpret, illegal access!", iae);
        } catch (final ClassNotFoundException cnfe) {
            throw new EIOException("Unable to interpret, unknown class ("
                                   + className + ")!", cnfe);
        } catch (final NoSuchMethodException nsme) {
            throw new EIOException("Unable to interpret, no method!", nsme);
        }
    }

    /**
     * Recovers an instance from the given <code>byte[]</code>
     * representation.
     *
     * @param bytes Representation of an instance.
     * @param rs Random source used to probabilistically check the
     * validity of an input.
     * @param certainty Certainty with which an input is deemed
     * correct, i.e., an incorrect input is accepted with probability
     * at most 2<sup>-<code>certainty</code></sup>.
     * @return Unmarshalled object.
     *
     * @throws EIOException If the input can not be
     * interpreted as an instance.
     */
    protected static Object unmarshalBytesAux(final byte[] bytes,
                                              final RandomSource rs,
                                              final int certainty)
        throws EIOException {
        return unmarshalAux(new ByteTreeReaderBT(new ByteTree(bytes, null)),
                            rs,
                            certainty);
    }

    /**
     * Recovers an instance from the given <code>byte[]</code>
     * representation.
     *
     * @param bytes Representation of an instance.
     * @return Unmarshalled object.
     *
     * @throws EIOException If the input can not be
     * interpreted as an instance.
     */
    protected static Object unmarshalBytes(final byte[] bytes)
        throws EIOException {
        return unmarshal(new ByteTree(bytes, null).getByteTreeReader());
    }

    /**
     * Recovers an instance from the given hexidecimal representation.
     *
     * @param hex Representation of an instance.
     * @param rs Random source used to probabilistically check the
     * validity of an input.
     * @param certainty Certainty with which an input is deemed
     * correct, i.e., an incorrect input is accepted with probability
     * at most 2<sup>-<code>certainty</code></sup>.
     * @return Unmarshalled object.
     *
     * @throws EIOException If the input can not be
     * interpreted as an instance.
     */
    protected static Object unmarshalHexAux(final String hex,
                                            final RandomSource rs,
                                            final int certainty)
        throws EIOException {

        String currentHex = hex;
        final int index = currentHex.lastIndexOf("::");
        if (index != -1) {
            currentHex = currentHex.substring(index + 2);
        }
        return unmarshalBytesAux(Hex.toByteArray(currentHex), rs, certainty);
    }

    /**
     * Recovers an instance from the given hexadecimal representation.
     *
     * @param hex Representation of an instance.
     * @return Unmarshalled object.
     *
     * @throws EIOException If the input can not be
     * interpreted as an instance.
     */
    protected static Object unmarshalHex(final String hex)
        throws EIOException {

        String currentHex = hex;

        final int index = currentHex.lastIndexOf("::");
        if (index != -1) {
            currentHex = currentHex.substring(index + 2);
        }
        return unmarshalBytes(Hex.toByteArray(currentHex));
    }

    /**
     * Outputs a representation of this instance.  This method uses
     * Java reflection to encode the class name of the input instance
     * as part of the output. This allows recovering the correct type
     * of instance.
     *
     * @param m An instance to be marshalled.
     * @return Representation of input.
     */
    public static ByteTreeBasic marshal(final Marshalizable m) {
        final Class klass = m.getClass();
        try {

            final String className = klass.getName();
            final ByteTree bt = new ByteTree(className.getBytes("UTF-8"));
            return new ByteTreeContainer(bt, m.toByteTree());

        } catch (final UnsupportedEncodingException uee) {
            throw new CryptoError("This is a bug!", uee);
        }
    }

    /**
     * Outputs a representation of this instance.  This method uses
     * Java reflection to encode the class name of the input instance
     * as part of the output. This allows recovering the correct type
     * of instance.
     *
     * @param m An instance to be marshalled.
     * @return Representation of input.
     */
    public static byte[] marshalToBytes(final Marshalizable m) {
        return marshal(m).toByteArray();
    }

    /**
     * Outputs a hexadecimal encoded representation of this instance.
     * This method uses Java reflection to encode the class name of
     * the input instance as part of the output. This allows
     * recovering the correct type of instance.
     *
     * @param m An instance to be marshalled.
     * @return Representation of input.
     */
    public static String marshalToHex(final Marshalizable m) {
        return Hex.toHexString(marshalToBytes(m));
    }

    /**
     * Outputs a hexadecimal encoded representation of this instance
     * prepended with brief content description.  This method uses
     * Java reflection to encode the class name of the input instance
     * as part of the output. This allows recovering the correct type
     * of instance.
     *
     * @param m An instance to be marshalled.
     * @param verbose Use verbose human readable comment string.
     * @return Representation of input.
     */
    public static String marshalToHexHuman(final Marshalizable m,
                                           final boolean verbose) {
        return m.humanDescription(verbose) + "::" + marshalToHex(m);
    }


    /*
**************************************************************************
**************** METHODS BELOW THIS LINE ARE GENERATED! ******************
**************************************************************************
*/


    /**
     * Converts the input into an instance of {@link CryptoKeyGen}.
     *
     * @param btr Representation of the output.
     * @param rs Source of randomness used in testing.
     * @param certainty Determines the probability of accepting a
     * an incorrect input.
     * @return An instance corresponding to the input.
     *
     * @throws EIOException If the input does not represent an
     * instance.
     */
    public static CryptoKeyGen
        unmarshalAux_CryptoKeyGen(final ByteTreeReader btr,
                                  final RandomSource rs,
                                  final int certainty)
        throws EIOException {
        final Object obj = unmarshalAux(btr, rs, certainty);
        if (obj instanceof CryptoKeyGen) {
            return (CryptoKeyGen) obj;
        } else {
            throw new EIOException(TYPE_DOES_NOT_MATCH_CAST);
        }
    }

    /**
     * Converts the input into an instance of {@link CryptoPKey}.
     *
     * @param btr Representation of the output.
     * @param rs Source of randomness used in testing.
     * @param certainty Determines the probability of accepting a
     * an incorrect input.
     * @return An instance corresponding to the input.
     *
     * @throws EIOException If the input does not represent an
     * instance.
     */
    public static CryptoPKey unmarshalAux_CryptoPKey(final ByteTreeReader btr,
                                                     final RandomSource rs,
                                                     final int certainty)
        throws EIOException {
        final Object obj = unmarshalAux(btr, rs, certainty);
        if (obj instanceof CryptoPKey) {
            return (CryptoPKey) obj;
        } else {
            throw new EIOException(TYPE_DOES_NOT_MATCH_CAST);
        }
    }

    /**
     * Converts the input into an instance of {@link CryptoSKey}.
     *
     * @param btr Representation of the output.
     * @param rs Source of randomness used in testing.
     * @param certainty Determines the probability of accepting a
     * an incorrect input.
     * @return An instance corresponding to the input.
     *
     * @throws EIOException If the input does not represent an
     * instance.
     */
    public static CryptoSKey unmarshalAux_CryptoSKey(final ByteTreeReader btr,
                                                     final RandomSource rs,
                                                     final int certainty)
        throws EIOException {
        final Object obj = unmarshalAux(btr, rs, certainty);
        if (obj instanceof CryptoSKey) {
            return (CryptoSKey) obj;
        } else {
            throw new EIOException(TYPE_DOES_NOT_MATCH_CAST);
        }
    }

    /**
     * Converts the input into an instance of {@link Hashfunction}.
     *
     * @param btr Representation of the output.
     * @param rs Source of randomness used in testing.
     * @param certainty Determines the probability of accepting a
     * an incorrect input.
     * @return An instance corresponding to the input.
     *
     * @throws EIOException If the input does not represent an
     * instance.
     */
    public static Hashfunction
        unmarshalAux_Hashfunction(final ByteTreeReader btr,
                                  final RandomSource rs,
                                  final int certainty)
        throws EIOException {
        final Object obj = unmarshalAux(btr, rs, certainty);
        if (obj instanceof Hashfunction) {
            return (Hashfunction) obj;
        } else {
            throw new EIOException(TYPE_DOES_NOT_MATCH_CAST);
        }
    }

    /**
     * Converts the input into an instance of {@link HashfunctionFixedLength}.
     *
     * @param btr Representation of the output.
     * @param rs Source of randomness used in testing.
     * @param certainty Determines the probability of accepting a
     * an incorrect input.
     * @return An instance corresponding to the input.
     *
     * @throws EIOException If the input does not represent an
     * instance.
     */
    public static HashfunctionFixedLength
        unmarshalAux_HashfunctionFixedLength(final ByteTreeReader btr,
                                             final RandomSource rs,
                                             final int certainty)
        throws EIOException {
        final Object obj = unmarshalAux(btr, rs, certainty);
        if (obj instanceof HashfunctionFixedLength) {
            return (HashfunctionFixedLength) obj;
        } else {
            throw new EIOException(TYPE_DOES_NOT_MATCH_CAST);
        }
    }

    /**
     * Converts the input into an instance of {@link PGroup}.
     *
     * @param btr Representation of the output.
     * @param rs Source of randomness used in testing.
     * @param certainty Determines the probability of accepting a
     * an incorrect input.
     * @return An instance corresponding to the input.
     *
     * @throws EIOException If the input does not represent an
     * instance.
     */
    public static PGroup
        unmarshalAux_PGroup(final ByteTreeReader btr,
                            final RandomSource rs,
                            final int certainty)
        throws EIOException {
        final Object obj = unmarshalAux(btr, rs, certainty);
        if (obj instanceof PGroup) {
            return (PGroup) obj;
        } else {
            throw new EIOException(TYPE_DOES_NOT_MATCH_CAST);
        }
    }

    /**
     * Converts the input into an instance of {@link PRG}.
     *
     * @param btr Representation of the output.
     * @param rs Source of randomness used in testing.
     * @param certainty Determines the probability of accepting a
     * an incorrect input.
     * @return An instance corresponding to the input.
     *
     * @throws EIOException If the input does not represent an
     * instance.
     */
    public static PRG
        unmarshalAux_PRG(final ByteTreeReader btr,
                         final RandomSource rs,
                         final int certainty)
        throws EIOException {
        final Object obj = unmarshalAux(btr, rs, certainty);
        if (obj instanceof PRG) {
            return (PRG) obj;
        } else {
            throw new EIOException(TYPE_DOES_NOT_MATCH_CAST);
        }
    }

    /**
     * Converts the input into an instance of {@link SignaturePKey}.
     *
     * @param btr Representation of the output.
     * @param rs Source of randomness used in testing.
     * @param certainty Determines the probability of accepting a
     * an incorrect input.
     * @return An instance corresponding to the input.
     *
     * @throws EIOException If the input does not represent an
     * instance.
     */
    public static SignaturePKey
        unmarshalAux_SignaturePKey(final ByteTreeReader btr,
                                   final RandomSource rs,
                                   final int certainty)
        throws EIOException {
        final Object obj = unmarshalAux(btr, rs, certainty);
        if (obj instanceof SignaturePKey) {
            return (SignaturePKey) obj;
        } else {
            throw new EIOException(TYPE_DOES_NOT_MATCH_CAST);
        }
    }

    /**
     * Converts the input into an instance of {@link SignatureSKey}.
     *
     * @param btr Representation of the output.
     * @param rs Source of randomness used in testing.
     * @param certainty Determines the probability of accepting a
     * an incorrect input.
     * @return An instance corresponding to the input.
     *
     * @throws EIOException If the input does not represent an
     * instance.
     */
    public static SignatureSKey
        unmarshalAux_SignatureSKey(final ByteTreeReader btr,
                                   final RandomSource rs,
                                   final int certainty)
        throws EIOException {
        final Object obj = unmarshalAux(btr, rs, certainty);
        if (obj instanceof SignatureSKey) {
            return (SignatureSKey) obj;
        } else {
            throw new EIOException(TYPE_DOES_NOT_MATCH_CAST);
        }
    }

    /**
     * Converts the input into an instance of {@link Hashfunction}.
     *
     * @param btr Representation of the output.
     * @return An instance corresponding to the input.
     *
     * @throws EIOException If the input does not represent an
     * instance.
     */
    public static Hashfunction
        unmarshal_Hashfunction(final ByteTreeReader btr)
        throws EIOException {
        final Object obj = unmarshal(btr);
        if (obj instanceof Hashfunction) {
            return (Hashfunction) obj;
        } else {
            throw new EIOException(TYPE_DOES_NOT_MATCH_CAST);
        }
    }

    /**
     * Converts the input into an instance of {@link PRG}.
     *
     * @param btr Representation of the output.
     * @return An instance corresponding to the input.
     *
     * @throws EIOException If the input does not represent an
     * instance.
     */
    public static PRG
        unmarshal_PRG(final ByteTreeReader btr)
        throws EIOException {
        final Object obj = unmarshal(btr);
        if (obj instanceof PRG) {
            return (PRG) obj;
        } else {
            throw new EIOException(TYPE_DOES_NOT_MATCH_CAST);
        }
    }

    /**
     * Converts the input into an instance of {@link RandomSource}.
     *
     * @param btr Representation of the output.
     * @return An instance corresponding to the input.
     *
     * @throws EIOException If the input does not represent an
     * instance.
     */
    public static RandomSource
        unmarshal_RandomSource(final ByteTreeReader btr)
        throws EIOException {
        final Object obj = unmarshal(btr);
        if (obj instanceof RandomSource) {
            return (RandomSource) obj;
        } else {
            throw new EIOException(TYPE_DOES_NOT_MATCH_CAST);
        }
    }

    /**
     * Converts the input into an instance of {@link CryptoKeyGen}.
     *
     * @param hex Hex code representation of the output.
     * @param rs Source of randomness used in testing.
     * @param certainty Determines the probability of accepting a
     * an incorrect input.
     * @return An instance corresponding to the input.
     *
     * @throws EIOException If the input does not represent an
     * instance.
     */
    public static CryptoKeyGen
        unmarshalHexAux_CryptoKeyGen(final String hex,
                                     final RandomSource rs,
                                     final int certainty)
        throws EIOException {
        final Object obj = unmarshalHexAux(hex, rs, certainty);
        if (obj instanceof CryptoKeyGen) {
            return (CryptoKeyGen) obj;
        } else {
            throw new EIOException(TYPE_DOES_NOT_MATCH_CAST);
        }
    }

    /**
     * Converts the input into an instance of {@link Hashfunction}.
     *
     * @param hex Hex code representation of the output.
     * @param rs Source of randomness used in testing.
     * @param certainty Determines the probability of accepting a
     * an incorrect input.
     * @return An instance corresponding to the input.
     *
     * @throws EIOException If the input does not represent an
     * instance.
     */
    public static Hashfunction
        unmarshalHexAux_Hashfunction(final String hex,
                                     final RandomSource rs,
                                     final int certainty)
        throws EIOException {
        final Object obj = unmarshalHexAux(hex, rs, certainty);
        if (obj instanceof Hashfunction) {
            return (Hashfunction) obj;
        } else {
            throw new EIOException(TYPE_DOES_NOT_MATCH_CAST);
        }
    }

    /**
     * Converts the input into an instance of {@link HashfunctionFixedLength}.
     *
     * @param hex Hex code representation of the output.
     * @param rs Source of randomness used in testing.
     * @param certainty Determines the probability of accepting a
     * an incorrect input.
     * @return An instance corresponding to the input.
     *
     * @throws EIOException If the input does not represent an
     * instance.
     */
    public static HashfunctionFixedLength
        unmarshalHexAux_HashfunctionFixedLength(final String hex,
                                                final RandomSource rs,
                                                final int certainty)
        throws EIOException {
        final Object obj = unmarshalHexAux(hex, rs, certainty);
        if (obj instanceof HashfunctionFixedLength) {
            return (HashfunctionFixedLength) obj;
        } else {
            throw new EIOException(TYPE_DOES_NOT_MATCH_CAST);
        }
    }

    /**
     * Converts the input into an instance of {@link PGroup}.
     *
     * @param hex Hex code representation of the output.
     * @param rs Source of randomness used in testing.
     * @param certainty Determines the probability of accepting a
     * an incorrect input.
     * @return An instance corresponding to the input.
     *
     * @throws EIOException If the input does not represent an
     * instance.
     */
    public static PGroup unmarshalHexAux_PGroup(final String hex,
                                                final RandomSource rs,
                                                final int certainty)
        throws EIOException {
        final Object obj = unmarshalHexAux(hex, rs, certainty);
        if (obj instanceof PGroup) {
            return (PGroup) obj;
        } else {
            throw new EIOException(TYPE_DOES_NOT_MATCH_CAST);
        }
    }

    /**
     * Converts the input into an instance of {@link PRG}.
     *
     * @param hex Hex code representation of the output.
     * @param rs Source of randomness used in testing.
     * @param certainty Determines the probability of accepting a
     * an incorrect input.
     * @return An instance corresponding to the input.
     *
     * @throws EIOException If the input does not represent an
     * instance.
     */
    public static PRG unmarshalHexAux_PRG(final String hex,
                                          final RandomSource rs,
                                          final int certainty)
        throws EIOException {
        final Object obj = unmarshalHexAux(hex, rs, certainty);
        if (obj instanceof PRG) {
            return (PRG) obj;
        } else {
            throw new EIOException(TYPE_DOES_NOT_MATCH_CAST);
        }
    }

    /**
     * Converts the input into an instance of {@link SignatureKeyGen}.
     *
     * @param hex Hex code representation of the output.
     * @param rs Source of randomness used in testing.
     * @param certainty Determines the probability of accepting a
     * an incorrect input.
     * @return An instance corresponding to the input.
     *
     * @throws EIOException If the input does not represent an
     * instance.
     */
    public static SignatureKeyGen
        unmarshalHexAux_SignatureKeyGen(final String hex,
                                        final RandomSource rs,
                                        final int certainty)
        throws EIOException {
        final Object obj = unmarshalHexAux(hex, rs, certainty);
        if (obj instanceof SignatureKeyGen) {
            return (SignatureKeyGen) obj;
        } else {
            throw new EIOException(TYPE_DOES_NOT_MATCH_CAST);
        }
    }

    /**
     * Converts the input into an instance of {@link SignatureKeyPair}.
     *
     * @param hex Hex code representation of the output.
     * @param rs Source of randomness used in testing.
     * @param certainty Determines the probability of accepting a
     * an incorrect input.
     * @return An instance corresponding to the input.
     *
     * @throws EIOException If the input does not represent an
     * instance.
     */
    public static SignatureKeyPair
        unmarshalHexAux_SignatureKeyPair(final String hex,
                                         final RandomSource rs,
                                         final int certainty)
        throws EIOException {
        final Object obj = unmarshalHexAux(hex, rs, certainty);
        if (obj instanceof SignatureKeyPair) {
            return (SignatureKeyPair) obj;
        } else {
            throw new EIOException(TYPE_DOES_NOT_MATCH_CAST);
        }
    }

    /**
     * Converts the input into an instance of {@link SignaturePKey}.
     *
     * @param hex Hex code representation of the output.
     * @param rs Source of randomness used in testing.
     * @param certainty Determines the probability of accepting a
     * an incorrect input.
     * @return An instance corresponding to the input.
     *
     * @throws EIOException If the input does not represent an
     * instance.
     */
    public static SignaturePKey
        unmarshalHexAux_SignaturePKey(final String hex,
                                      final RandomSource rs,
                                      final int certainty)
        throws EIOException {
        final Object obj = unmarshalHexAux(hex, rs, certainty);
        if (obj instanceof SignaturePKey) {
            return (SignaturePKey) obj;
        } else {
            throw new EIOException(TYPE_DOES_NOT_MATCH_CAST);
        }
    }

    /**
     * Converts the input into an instance of {@link SignatureSKey}.
     *
     * @param hex Hex code representation of the output.
     * @param rs Source of randomness used in testing.
     * @param certainty Determines the probability of accepting a
     * an incorrect input.
     * @return An instance corresponding to the input.
     *
     * @throws EIOException If the input does not represent an
     * instance.
     */
    public static SignatureSKey
        unmarshalHexAux_SignatureSKey(final String hex,
                                      final RandomSource rs,
                                      final int certainty)
        throws EIOException {
        final Object obj = unmarshalHexAux(hex, rs, certainty);
        if (obj instanceof SignatureSKey) {
            return (SignatureSKey) obj;
        } else {
            throw new EIOException(TYPE_DOES_NOT_MATCH_CAST);
        }
    }

    /**
     * Converts the input into an instance of {@link GeneratorTemplate}.
     *
     * @param hex Hex code representation of the output.
     * @return An instance corresponding to the input.
     *
     * @throws EIOException If the input does not represent an
     * instance.
     */
    public static GeneratorTemplate
        unmarshalHex_GeneratorTemplate(final String hex)
        throws EIOException {
        final Object obj = unmarshalHex(hex);
        if (obj instanceof GeneratorTemplate) {
            return (GeneratorTemplate) obj;
        } else {
            throw new EIOException(TYPE_DOES_NOT_MATCH_CAST);
        }
    }

    /**
     * Converts the input into an instance of {@link LargeInteger}.
     *
     * @param hex Hex code representation of the output.
     * @return An instance corresponding to the input.
     *
     * @throws EIOException If the input does not represent an
     * instance.
     */
    public static LargeInteger
        unmarshalHex_LargeInteger(final String hex)
        throws EIOException {
        final Object obj = unmarshalHex(hex);
        if (obj instanceof LargeInteger) {
            return (LargeInteger) obj;
        } else {
            throw new EIOException(TYPE_DOES_NOT_MATCH_CAST);
        }
    }

    /**
     * Converts the input into an instance of {@link RandomSource}.
     *
     * @param hex Hex code representation of the output.
     * @return An instance corresponding to the input.
     *
     * @throws EIOException If the input does not represent an
     * instance.
     */
    public static RandomSource
        unmarshalHex_RandomSource(final String hex)
        throws EIOException {
        final Object obj = unmarshalHex(hex);
        if (obj instanceof RandomSource) {
            return (RandomSource) obj;
        } else {
            throw new EIOException(TYPE_DOES_NOT_MATCH_CAST);
        }
    }

    /**
     * Converts the input into an instance of {@link SignaturePKey}.
     *
     * @param hex Hex code representation of the output.
     * @return An instance corresponding to the input.
     *
     * @throws EIOException If the input does not represent an
     * instance.
     */
    public static SignaturePKey
        unmarshalHex_SignaturePKey(final String hex)
        throws EIOException {
        final Object obj = unmarshalHex(hex);
        if (obj instanceof SignaturePKey) {
            return (SignaturePKey) obj;
        } else {
            throw new EIOException(TYPE_DOES_NOT_MATCH_CAST);
        }
    }

    /**
     * Converts the input into an instance of {@link SignatureSKey}.
     *
     * @param hex Hex code representation of the output.
     * @return An instance corresponding to the input.
     *
     * @throws EIOException If the input does not represent an
     * instance.
     */
    public static SignatureSKey
        unmarshalHex_SignatureSKey(final String hex)
        throws EIOException {
        final Object obj = unmarshalHex(hex);
        if (obj instanceof SignatureSKey) {
            return (SignatureSKey) obj;
        } else {
            throw new EIOException(TYPE_DOES_NOT_MATCH_CAST);
        }
    }



}
