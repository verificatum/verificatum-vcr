
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

package com.verificatum.arithm;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Holds standard named parameters of elliptic curves over prime order fields.
 *
 * @author Douglas Wikstrom
 */

// PMD_ANNOTATION @SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class ECqPGroupParams {

    /**
     * Prime modulus of the underlying field.
     */
    public final LargeInteger p;

    /**
     * x-coefficient of the curve polynomial.
     */
    public final LargeInteger a;

    /**
     * Constant coefficient of the curve polynomial.
     */
    public final LargeInteger b;

    /**
     * x-coordinate of the generator of the subgroup of prime order.
     */
    public final LargeInteger gx;

    /**
     * y-coordinate of the generator of the group.
     */
    public final LargeInteger gy;

    /**
     * Order of the elliptic curve group.
     */
    public final LargeInteger n;

    /**
     * Co-order of the elliptic curve group.
     */
    public final LargeInteger h;

    /**
     * Hashtable containing all named elliptic curve parameters.
     */
    private static final Map<String, ECqPGroupParams> NAMED_PARAMS =
        new ConcurrentHashMap<String, ECqPGroupParams>();

    /**
     * Creates a set of parameters.
     *
     * @param pString Representation of prime.
     * @param aString Representation of x-coefficient.
     * @param bString Representation of constant coefficient.
     * @param gxString Representation of x-coordinate of generator.
     * @param gyString Representation of y-coordinate of generator.
     * @param nString Representation of order of group.
     * @param hString Representation of order of co-group.
     */
    public ECqPGroupParams(final String pString,
                           final String aString,
                           final String bString,
                           final String gxString,
                           final String gyString,
                           final String nString,
                           final String hString) {
        this.p = new LargeInteger("00" + pString, 16);
        this.a = new LargeInteger("00" + aString, 16);
        this.b = new LargeInteger("00" + bString, 16);
        this.gx = new LargeInteger("00" + gxString, 16);
        this.gy = new LargeInteger("00" + gyString, 16);
        this.n = new LargeInteger("00" + nString, 16);
        this.h = new LargeInteger("00" + hString, 16);
    }


    // This code is ugly, since we keep all parameters in memory.
    static {

        // NIST
        NAMED_PARAMS.put("prime192v3",
                         new ECqPGroupParams(
                                             "fffffffffffffffffffffffffffffffeffffffffffffffff",
                                             "fffffffffffffffffffffffffffffffefffffffffffffffc",
                                             "22123dc2395a05caa7423daeccc94760a7d462256bd56916",
                                             "7d29778100c65a1da1783716588dce2b8b4aee8e228f1896",
                                             "38a90f22637337334b49dcb66a6dc8f9978aca7648a943b0",
                                             "ffffffffffffffffffffffff7a62d031c83f4294f640ec13", "1"));
        NAMED_PARAMS.put("prime192v2",
                         new ECqPGroupParams(
                                             "fffffffffffffffffffffffffffffffeffffffffffffffff",
                                             "fffffffffffffffffffffffffffffffefffffffffffffffc",
                                             "cc22d6dfb95c6b25e49c0d6364a4e5980c393aa21668d953",
                                             "eea2bae7e1497842f2de7769cfe9c989c072ad696f48034a",
                                             "6574d11d69b6ec7a672bb82a083df2f2b0847de970b2de15",
                                             "fffffffffffffffffffffffe5fb1a724dc80418648d8dd31", "1"));
        NAMED_PARAMS.put("prime192v1",
                         new ECqPGroupParams(
                                             "fffffffffffffffffffffffffffffffeffffffffffffffff",
                                             "fffffffffffffffffffffffffffffffefffffffffffffffc",
                                             "64210519e59c80e70fa7e9ab72243049feb8deecc146b9b1",
                                             "188da80eb03090f67cbf20eb43a18800f4ff0afd82ff1012",
                                             "7192b95ffc8da78631011ed6b24cdd573f977a11e794811",
                                             "ffffffffffffffffffffffff99def836146bc9b1b4d22831", "1"));
        NAMED_PARAMS.put("prime256v1",
                         new ECqPGroupParams(
                                             "ffffffff00000001000000000000000000000000ffffffffffffffffffffffff",
                                             "ffffffff00000001000000000000000000000000fffffffffffffffffffffffc",
                                             "5ac635d8aa3a93e7b3ebbd55769886bc651d06b0cc53b0f63bce3c3e27d2604b",
                                             "6b17d1f2e12c4247f8bce6e563a440f277037d812deb33a0f4a13945d898c296",
                                             "4fe342e2fe1a7f9b8ee7eb4a7c0f9e162bce33576b315ececbb6406837bf51f5",
                                             "ffffffff00000000ffffffffffffffffbce6faada7179e84f3b9cac2fc632551", "1"));
        NAMED_PARAMS.put("prime239v3",
                         new ECqPGroupParams(
                                             "7fffffffffffffffffffffff7fffffffffff8000000000007fffffffffff",
                                             "7fffffffffffffffffffffff7fffffffffff8000000000007ffffffffffc",
                                             "255705fa2a306654b1f4cb03d6a750a30c250102d4988717d9ba15ab6d3e",
                                             "6768ae8e18bb92cfcf005c949aa2c6d94853d0e660bbf854b1c9505fe95a",
                                             "1607e6898f390c06bc1d552bad226f3b6fcfe48b6e818499af18e3ed6cf3",
                                             "7fffffffffffffffffffffff7fffff975deb41b3a6057c3c432146526551",
                                             "1"));
        NAMED_PARAMS.put("prime239v2",
                         new ECqPGroupParams(
                                             "7fffffffffffffffffffffff7fffffffffff8000000000007fffffffffff",
                                             "7fffffffffffffffffffffff7fffffffffff8000000000007ffffffffffc",
                                             "617fab6832576cbbfed50d99f0249c3fee58b94ba0038c7ae84c8c832f2c",
                                             "38af09d98727705120c921bb5e9e26296a3cdcf2f35757a0eafd87b830e7",
                                             "5b0125e4dbea0ec7206da0fc01d9b081329fb555de6ef460237dff8be4ba",
                                             "7fffffffffffffffffffffff800000cfa7e8594377d414c03821bc582063",
                                             "1"));
        NAMED_PARAMS.put("prime239v1",
                         new ECqPGroupParams(
                                             "7fffffffffffffffffffffff7fffffffffff8000000000007fffffffffff",
                                             "7fffffffffffffffffffffff7fffffffffff8000000000007ffffffffffc",
                                             "6b016c3bdcf18941d0d654921475ca71a9db2fb27d1d37796185c2942c0a",
                                             "ffa963cdca8816ccc33b8642bedf905c3d358573d3f27fbbd3b3cb9aaaf",
                                             "7debe8e4e90a5dae6e4054ca530ba04654b36818ce226b39fccb7b02f1ae",
                                             "7fffffffffffffffffffffff7fffff9e5e9a9f5d9071fbd1522688909d0b",
                                             "1"));

        // SEC
        NAMED_PARAMS.put("secp192k1",
                         new ECqPGroupParams(
                                             "fffffffffffffffffffffffffffffffffffffffeffffee37", "0", "3",
                                             "db4ff10ec057e9ae26b07d0280b7f4341da5d1b1eae06c7d",
                                             "9b2f2f6d9c5628a7844163d015be86344082aa88d95e2f9d",
                                             "fffffffffffffffffffffffe26f2fc170f69466a74defd8d", "1"));
        NAMED_PARAMS.put("secp192r1",
                         new ECqPGroupParams(
                                             "fffffffffffffffffffffffffffffffeffffffffffffffff",
                                             "fffffffffffffffffffffffffffffffefffffffffffffffc",
                                             "64210519e59c80e70fa7e9ab72243049feb8deecc146b9b1",
                                             "188da80eb03090f67cbf20eb43a18800f4ff0afd82ff1012",
                                             "7192b95ffc8da78631011ed6b24cdd573f977a11e794811",
                                             "ffffffffffffffffffffffff99def836146bc9b1b4d22831", "1"));
        NAMED_PARAMS.put("secp224k1", new ECqPGroupParams(
                                                          "fffffffffffffffffffffffffffffffffffffffffffffffeffffe56d",
                                                          "0", "5",
                                                          "a1455b334df099df30fc28a169a467e9e47075a90f7e650eb6b7a45c",
                                                          "7e089fed7fba344282cafbd6f7e319f7c0b0bd59e2ca4bdb556d61a5",
                                                          "10000000000000000000000000001dce8d2ec6184caf0a971769fb1f7",
                                                          "1"));
        NAMED_PARAMS.put("secp224r1",
                         new ECqPGroupParams(
                                             "ffffffffffffffffffffffffffffffff000000000000000000000001",
                                             "fffffffffffffffffffffffffffffffefffffffffffffffffffffffe",
                                             "b4050a850c04b3abf54132565044b0b7d7bfd8ba270b39432355ffb4",
                                             "b70e0cbd6bb4bf7f321390b94a03c1d356c21122343280d6115c1d21",
                                             "bd376388b5f723fb4c22dfe6cd4375a05a07476444d5819985007e34",
                                             "ffffffffffffffffffffffffffff16a2e0b8f03e13dd29455c5c2a3d",
                                             "1"));
        NAMED_PARAMS.put("secp256k1",
                         new ECqPGroupParams(
                                             "fffffffffffffffffffffffffffffffffffffffffffffffffffffffefffffc2f",
                                             "0",
                                             "7",
                                             "79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798",
                                             "483ada7726a3c4655da4fbfc0e1108a8fd17b448a68554199c47d08ffb10d4b8",
                                             "fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364141",
                                             "1"));
        NAMED_PARAMS.put("secp256r1",
                         new ECqPGroupParams(
                                             "ffffffff00000001000000000000000000000000ffffffffffffffffffffffff",
                                             "ffffffff00000001000000000000000000000000fffffffffffffffffffffffc",
                                             "5ac635d8aa3a93e7b3ebbd55769886bc651d06b0cc53b0f63bce3c3e27d2604b",
                                             "6b17d1f2e12c4247f8bce6e563a440f277037d812deb33a0f4a13945d898c296",
                                             "4fe342e2fe1a7f9b8ee7eb4a7c0f9e162bce33576b315ececbb6406837bf51f5",
                                             "ffffffff00000000ffffffffffffffffbce6faada7179e84f3b9cac2fc632551",
                                             "1"));
        NAMED_PARAMS.put("secp384r1",
                         new ECqPGroupParams(
                                             "fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffeffffffff0000000000000000ffffffff",
                                             "fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffeffffffff0000000000000000fffffffc",
                                             "b3312fa7e23ee7e4988e056be3f82d19181d9c6efe8141120314088f5013875ac656398d8a2ed19d2a85c8edd3ec2aef",
                                             "aa87ca22be8b05378eb1c71ef320ad746e1d3b628ba79b9859f741e082542a385502f25dbf55296c3a545e3872760ab7",
                                             "3617de4a96262c6f5d9e98bf9292dc29f8f41dbd289a147ce9da3113b5f0b8c00a60b1ce1d7e819d7a431d7c90ea0e5f",
                                             "ffffffffffffffffffffffffffffffffffffffffffffffffc7634d81f4372ddf581a0db248b0a77aecec196accc52973",
                                             "1"));
        NAMED_PARAMS.put("secp521r1",
                         new ECqPGroupParams(
                                             "1ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
                                             "1fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc",
                                             "51953eb9618e1c9a1f929a21a0b68540eea2da725b99b315f3b8b489918ef109e156193951ec7e937b1652c0bd3bb1bf073573df883d2c34f1ef451fd46b503f00",
                                             "c6858e06b70404e9cd9e3ecb662395b4429c648139053fb521f828af606b4d3dbaa14b5e77efe75928fe1dc127a2ffa8de3348b3c1856a429bf97e7e31c2e5bd66",
                                             "11839296a789a3bc0045c8a5fb42c7d1bd998f54449579b446817afbd17273e662c97ee72995ef42640c550b9013fad0761353c7086a272c24088be94769fd16650",
                                             "1fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffa51868783bf2f966b7fcc0148f709a5d03bb5c9b8899c47aebb6fb71e91386409",
                                             "1"));

        // TeleTrusT
        NAMED_PARAMS.put("brainpoolp512r1",
                         new ECqPGroupParams(
                                             "aadd9db8dbe9c48b3fd4e6ae33c9fc07cb308db3b3c9d20ed6639cca703308717d4d9b009bc66842aecda12ae6a380e62881ff2f2d82c68528aa6056583a48f3",
                                             "7830a3318b603b89e2327145ac234cc594cbdd8d3df91610a83441caea9863bc2ded5d5aa8253aa10a2ef1c98b9ac8b57f1117a72bf2c7b9e7c1ac4d77fc94ca",
                                             "3df91610a83441caea9863bc2ded5d5aa8253aa10a2ef1c98b9ac8b57f1117a72bf2c7b9e7c1ac4d77fc94cadc083e67984050b75ebae5dd2809bd638016f723",
                                             "81aee4bdd82ed9645a21322e9c4c6a9385ed9f70b5d916c1b43b62eef4d0098eff3b1f78e2d0d48d50d1687b93b97d5f7c6d5047406a5e688b352209bcb9f822",
                                             "7dde385d566332ecc0eabfa9cf7822fdf209f70024a57b1aa000c55b881f8111b2dcde494a5f485e5bca4bd88a2763aed1ca2b2fa8f0540678cd1e0f3ad80892",
                                             "aadd9db8dbe9c48b3fd4e6ae33c9fc07cb308db3b3c9d20ed6639cca70330870553e5c414ca92619418661197fac10471db1d381085ddaddb58796829ca90069",
                                             "1"));
        NAMED_PARAMS.put("brainpoolp256r1",
                         new ECqPGroupParams(
                                             "a9fb57dba1eea9bc3e660a909d838d726e3bf623d52620282013481d1f6e5377",
                                             "7d5a0975fc2c3057eef67530417affe7fb8055c126dc5c6ce94a4b44f330b5d9",
                                             "26dc5c6ce94a4b44f330b5d9bbd77cbf958416295cf7e1ce6bccdc18ff8c07b6",
                                             "8bd2aeb9cb7e57cb2c4b482ffc81b7afb9de27e1e3bd23c23a4453bd9ace3262",
                                             "547ef835c3dac4fd97f8461a14611dc9c27745132ded8e545c1d54c72f046997",
                                             "a9fb57dba1eea9bc3e660a909d838d718c397aa3b561a6f7901e0e82974856a7",
                                             "1"));
        NAMED_PARAMS.put("brainpoolp192r1",
                         new ECqPGroupParams(
                                             "c302f41d932a36cda7a3463093d18db78fce476de1a86297",
                                             "6a91174076b1e0e19c39c031fe8685c1cae040e5c69a28ef",
                                             "469a28ef7c28cca3dc721d044f4496bcca7ef4146fbf25c9",
                                             "c0a0647eaab6a48753b033c56cb0f0900a2f5c4853375fd6",
                                             "14b690866abd5bb88b5f4828c1490002e6773fa2fa299b8f",
                                             "c302f41d932a36cda7a3462f9e9e916b5be8f1029ac4acc1", "1"));
        NAMED_PARAMS.put("brainpoolp224r1",
                         new ECqPGroupParams(
                                             "d7c134aa264366862a18302575d1d787b09f075797da89f57ec8c0ff",
                                             "68a5e62ca9ce6c1c299803a6c1530b514e182ad8b0042a59cad29f43",
                                             "2580f63ccfe44138870713b1a92369e33e2135d266dbb372386c400b",
                                             "d9029ad2c7e5cf4340823b2a87dc68c9e4ce3174c1e6efdee12c07d",
                                             "58aa56f772c0726f24c6b89e4ecdac24354b9e99caa3f6d3761402cd",
                                             "d7c134aa264366862a18302575d0fb98d116bc4b6ddebca3a5a7939f",
                                             "1"));
        NAMED_PARAMS.put("brainpoolp320r1",
                         new ECqPGroupParams(
                                             "d35e472036bc4fb7e13c785ed201e065f98fcfa6f6f40def4f92b9ec7893ec28fcd412b1f1b32e27",
                                             "3ee30b568fbab0f883ccebd46d3f3bb8a2a73513f5eb79da66190eb085ffa9f492f375a97d860eb4",
                                             "520883949dfdbc42d3ad198640688a6fe13f41349554b49acc31dccd884539816f5eb4ac8fb1f1a6",
                                             "43bd7e9afb53d8b85289bcc48ee5bfe6f20137d10a087eb6e7871e2a10a599c710af8d0d39e20611",
                                             "14fdd05545ec1cc8ab4093247f77275e0743ffed117182eaa9c77877aaac6ac7d35245d1692e8ee1",
                                             "d35e472036bc4fb7e13c785ed201e065f98fcfa5b68f12a32d482ec7ee8658e98691555b44c59311",
                                             "1"));
        NAMED_PARAMS.put("brainpoolp384r1",
                         new ECqPGroupParams(
                                             "8cb91e82a3386d280f5d6f7e50e641df152f7109ed5456b412b1da197fb71123acd3a729901d1a71874700133107ec53",
                                             "7bc382c63d8c150c3c72080ace05afa0c2bea28e4fb22787139165efba91f90f8aa5814a503ad4eb04a8c7dd22ce2826",
                                             "4a8c7dd22ce28268b39b55416f0447c2fb77de107dcd2a62e880ea53eeb62d57cb4390295dbc9943ab78696fa504c11",
                                             "1d1c64f068cf45ffa2a63a81b7c13f6b8847a3e77ef14fe3db7fcafe0cbd10e8e826e03436d646aaef87b2e247d4af1e",
                                             "8abe1d7520f9c2a45cb1eb8e95cfd55262b70b29feec5864e19c054ff99129280e4646217791811142820341263c5315",
                                             "8cb91e82a3386d280f5d6f7e50e641df152f7109ed5456b31f166e6cac0425a7cf3ab6af6b7fc3103b883202e9046565",
                                             "1"));

        // X9.62
        NAMED_PARAMS.put("P-521",
                         new ECqPGroupParams(
                                             "1ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
                                             "1fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc",
                                             "51953eb9618e1c9a1f929a21a0b68540eea2da725b99b315f3b8b489918ef109e156193951ec7e937b1652c0bd3bb1bf073573df883d2c34f1ef451fd46b503f00",
                                             "c6858e06b70404e9cd9e3ecb662395b4429c648139053fb521f828af606b4d3dbaa14b5e77efe75928fe1dc127a2ffa8de3348b3c1856a429bf97e7e31c2e5bd66",
                                             "11839296a789a3bc0045c8a5fb42c7d1bd998f54449579b446817afbd17273e662c97ee72995ef42640c550b9013fad0761353c7086a272c24088be94769fd16650",
                                             "1fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffa51868783bf2f966b7fcc0148f709a5d03bb5c9b8899c47aebb6fb71e91386409",
                                             "1"));
        NAMED_PARAMS.put("P-256",
                         new ECqPGroupParams(
                                             "ffffffff00000001000000000000000000000000ffffffffffffffffffffffff",
                                             "ffffffff00000001000000000000000000000000fffffffffffffffffffffffc",
                                             "5ac635d8aa3a93e7b3ebbd55769886bc651d06b0cc53b0f63bce3c3e27d2604b",
                                             "6b17d1f2e12c4247f8bce6e563a440f277037d812deb33a0f4a13945d898c296",
                                             "4fe342e2fe1a7f9b8ee7eb4a7c0f9e162bce33576b315ececbb6406837bf51f5",
                                             "ffffffff00000000ffffffffffffffffbce6faada7179e84f3b9cac2fc632551",
                                             "1"));
        NAMED_PARAMS.put("P-224",
                         new ECqPGroupParams(
                                             "ffffffffffffffffffffffffffffffff000000000000000000000001",
                                             "fffffffffffffffffffffffffffffffefffffffffffffffffffffffe",
                                             "b4050a850c04b3abf54132565044b0b7d7bfd8ba270b39432355ffb4",
                                             "b70e0cbd6bb4bf7f321390b94a03c1d356c21122343280d6115c1d21",
                                             "bd376388b5f723fb4c22dfe6cd4375a05a07476444d5819985007e34",
                                             "ffffffffffffffffffffffffffff16a2e0b8f03e13dd29455c5c2a3d",
                                             "1"));
        NAMED_PARAMS.put("P-384",
                         new ECqPGroupParams(
                                             "fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffeffffffff0000000000000000ffffffff",
                                             "fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffeffffffff0000000000000000fffffffc",
                                             "b3312fa7e23ee7e4988e056be3f82d19181d9c6efe8141120314088f5013875ac656398d8a2ed19d2a85c8edd3ec2aef",
                                             "aa87ca22be8b05378eb1c71ef320ad746e1d3b628ba79b9859f741e082542a385502f25dbf55296c3a545e3872760ab7",
                                             "3617de4a96262c6f5d9e98bf9292dc29f8f41dbd289a147ce9da3113b5f0b8c00a60b1ce1d7e819d7a431d7c90ea0e5f",
                                             "ffffffffffffffffffffffffffffffffffffffffffffffffc7634d81f4372ddf581a0db248b0a77aecec196accc52973",
                                             "1"));
        NAMED_PARAMS.put("P-192",
                         new ECqPGroupParams(
                                             "fffffffffffffffffffffffffffffffeffffffffffffffff",
                                             "fffffffffffffffffffffffffffffffefffffffffffffffc",
                                             "64210519e59c80e70fa7e9ab72243049feb8deecc146b9b1",
                                             "188da80eb03090f67cbf20eb43a18800f4ff0afd82ff1012",
                                             "7192b95ffc8da78631011ed6b24cdd573f977a11e794811",
                                             "ffffffffffffffffffffffff99def836146bc9b1b4d22831", "1"));
    }

    /**
     * Returns an array of all available named curves.
     *
     * @return Array of all available named curves.
     */
    public static String[] getCurveNames() {
        final String[] res = NAMED_PARAMS.keySet().toArray(new String[0]);
        Arrays.sort(res);
        return res;
    }

    /**
     * Returns the named group.
     *
     * @param name Name of group.
     * @return Group with the given name.
     * @throws ArithmFormatException if the given name is invalid.
     */
    public static ECqPGroup getECqPGroup(final String name)
        throws ArithmFormatException {

        final ECqPGroupParams params = NAMED_PARAMS.get(name);
        if (params == null) {
            throw new ArithmFormatException("Unknown named curve! (" + name
                                            + ")");
        } else {

            final PField pField = new PField();
            pField.unsafeInit(params.p);

            try {
                return new ECqPGroup(name, params.a, params.b, pField,
                                     params.n, params.gx, params.gy);

            } catch (final ArithmFormatException afe) {
                throw new ArithmError("Unable to instantiate group!", afe);
            }
        }
    }
}
