/*************************************************************************
 *                                                                       *
 *  CESeCore: CE Security Core                                           *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/

package org.cesecore.certificates.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.cms.CMSSignedGenerator;
import org.bouncycastle.jce.ECGOST3410NamedCurveTable;
import org.cesecore.keys.util.KeyTools;
import org.cesecore.util.CertTools;
import org.cesecore.util.CryptoProviderTools;
import org.junit.BeforeClass;
import org.junit.Test;

import com.keyfactor.util.crypto.algorithm.AlgorithmConfigurationCache;

/**
 * Tests for AlgorithmTools. Mostly tests border cases.
 */
public class AlgorithmToolsTest {
    private static final Logger log = Logger.getLogger(AlgorithmToolsTest.class);

    private static final String PRESIGN_VALIDATION_KEY_RSA_PRIV =
            "-----BEGIN RSA PRIVATE KEY-----\n" +
                    "MIIEogIBAAKCAQEAy0d3OgaScTQrYT2ujMYESueWv4Iz7OnuuX17tYvlSYpEc75I\n" +
                    "xPexlt0hXFneqi7MC787tXfD7ZJCNbXT1YP9bd4+pOhBONR3Mwg01Ig1sZ9826Vo\n" +
                    "1NR4YxO+NFi1noV8qUVsGV5NBs7i/R6lJIcO05KFa1JCYShETl+V9RMg6zEekJNS\n" +
                    "9Ds6lzFuudwOnz/8ldZ85iZxG7ssbDI5zz3FDJ1HOSofJ8llP6D97nYJBf/kXmPu\n" +
                    "G3KE9pF9Cto3KkPViDbTmuwx2RfISvdqbJESTvcPhk4K7J+yx2XwIFjzAT6SGP4I\n" +
                    "NDnNGXt79PUyefXWzIqyafOXDD/JPkaMCEN/0wIDAQABAoIBAFGvOxK/F1OUEiZ2\n" +
                    "IdEBtTHgU+xKxtDZxAsXiIGQYKenfxA/k4BKxDsKSuCQYHBkc6v4wWaPZNTvY9mv\n" +
                    "Yhs3ebwPhX7AsYzDm86O6qPIxELHAuZEVpbHdkTh5xmj1/+GRmzCr8iV4z/sHLx3\n" +
                    "9wZxmxybkS9qE7B0/NW9hUXA1QaMs13uPsaQnYStoeyaGTp8fqNImTxUOWkYFS1C\n" +
                    "D7guA5Pq3SoUm9PEy5dv0GyE5oXEDnLOmQIzdftilzleY4Zxe8BiqWf4k5FJiLQI\n" +
                    "T1PUQaqtf3Ei6WykQnUuX5iHyS8hkKbOfQFc88uEjKUVAPUMyMcSLWB9mPwDJfB0\n" +
                    "d0KXriECgYEA+SMRzeAUL+MmE+PsAFeQtFiRKFsLBU3SrUyIQYRwNl4upV7CAvdZ\n" +
                    "J1ipPkDxvuJt12Tpcw3I6VRsWy2Sdu881ue2/AJ7wj0HrYGnNkr1Zqv76LbeXWTI\n" +
                    "8E/aFIu0Z+is+F/iigyVe//roMN+l5S/HX6TeJKxV+pS5ahplS5TtwMCgYEA0OEA\n" +
                    "9rfKV6up2SqRU8TiBisjl/pePEQZkKgpnYQcOyGBAQL5Zk60Cqa/Xm04NCTPJPyK\n" +
                    "Qm5aD1y7c0526vIj0LJrs9X5AmqBN5f4SMbx/L0g8gAMCvjn4wwS2qX7K0mc92Ff\n" +
                    "9/qJizxq8cJO5RC6H3t9OWgZuasWBMRGye4yEvECgYBdL3ncWIEUfFDkxa6jXh1Y\n" +
                    "53u77XnMzRQNEAAzCVdzbnziC/RjaaMmLWp4R5BkhorxMuSCzVglthclb4FGDSvj\n" +
                    "ch4mWsNxnqQ9iK5Dh3wMoC2EGMpJgoYKJMP8RVkAOK5h5HN2kUhkbg/zPMwf5For\n" +
                    "rQl54tyEdrf1AK4lR4O2gwKBgA6CElcQnPVJ7xouYrm2yxwykt5TfYgiEsSBaaKP\n" +
                    "MobI5PT1B+2bOdYjjtc4LtcwV1LyV4gVshuvDTYNFSVsfCBaxDBRhGIuk5sQ6yXi\n" +
                    "65vqZwdoCW4Zq8GRbR3SuYdgLY7hLJFEzZjmMWdpX6F5b/QP17rNCDxlLbpXB7Ou\n" +
                    "37uBAoGAFQSOOBpuihRekEHhkQdu8p1HrPxEhXPrzWvLrOjIezRU9/3oU32cfKS/\n" +
                    "LflobGIhsqsQzdAtpfZdEZmRq6hPQ4tw+6qaql5a5164AteOrq6UjMLuuxJyGVNQ\n" +
                    "qB53/QNbrXSLAf100bBgotfutynTW4f37t0IPGG7i+44wEdj6gU=\n" +
                    "-----END RSA PRIVATE KEY-----\n";

    private static final String PRESIGN_VALIDATION_KEY_EC_SECP256R1_PRIV =
            "-----BEGIN EC PRIVATE KEY-----\n" +
                    "MHcCAQEEIEGrpEiJQlvnnPWqPVOT7LVD+h2RNw1orVXdu/HumkWqoAoGCCqGSM49\n" +
                    "AwEHoUQDQgAEjFHZzIXCz4W+BGV3V3lAoXMqISc4I39tgH5ErOWKMdU6pzpKWlXi\n" +
                    "gx9+SNtdz0OucKFLuGs9J0xHLJhTcLkuyQ==\n" +
                    "-----END EC PRIVATE KEY-----\n";

    private static final String PRESIGN_VALIDATION_KEY_EC_SECP384R1_PRIV =
            "-----BEGIN EC PRIVATE KEY-----\n" +
                    "MIGkAgEBBDCoT+vJRt9bVUD2zk5r2s6MAfoQOZW1mPAGazJIyTxjF+QpFJuSsTt9\n" +
                    "MHK5e3JKswOgBwYFK4EEACKhZANiAASXpPMP3vBs9isr8ssU91Ex93XIiwyMQ77l\n" +
                    "r5FLJamnT5+eL7RwEPiK/rfFrJJS7glgbBAmzDlkxlw67EAd2gz3tyW9UoxF8jpe\n" +
                    "ojP8Ay3AJ3Ms1cAT+uYp+ySa1LPNsOk=\n" +
                    "-----END EC PRIVATE KEY-----";

    private static final String PRESIGN_VALIDATION_KEY_DSA_PRIV =
            "-----BEGIN DSA PRIVATE KEY-----\n" +
                    "MIIBvAIBAAKBgQD9f1OBHXUSKVLfSpwu7OTn9hG3UjzvRADDHj+AtlEmaUVdQCJR\n" +
                    "+1k9jVj6v8X1ujD2y5tVbNeBO4AdNG/yZmC3a5lQpaSfn+gEexAiwk+7qdf+t8Yb\n" +
                    "+DtX58aophUPBPuD9tPFHsMCNVQTWhaRMvZ1864rYdcq7/IiAxmd0UgBxwIVAJdg\n" +
                    "UI8VIwvMspK5gqLrhAvwWBz1AoGBAPfhoIXWmz3ey7yrXDa4V7l5lK+7+jrqgvlX\n" +
                    "TAs9B4JnUVlXjrrUWU/mcQcQgYC0SRZxI+hMKBYTt88JMozIpuE8FnqLVHyNKOCj\n" +
                    "rh4rs6Z1kW6jfwv6ITVi8ftiegEkO8yk8b6oUZCJqIPf4VrlnwaSi2ZegHtVJWQB\n" +
                    "TDv+z0kqAoGBAJRiL6UUbPHmkKbfYeCUAgKfQhDkOydXe5A6+s84M0fnNqdxj6Dx\n" +
                    "s3xdkycSp/nHb1heQY37cAEhp0z6WnMwksDtlq7aIZeqMCxkvaz57bDUumVzMkV1\n" +
                    "T/wuZztd3gz7p70NyDkt/1JfwlKGcC+wNVMF4T1a/Y7xLloTq3yH32h7AhRTckHA\n" +
                    "LPjKPKEFrG18K7yFkH5xGg==\n" +
                    "-----END DSA PRIVATE KEY-----\n";

    private static final String PRESIGN_VALIDATION_KEY_ED25519_PRIV =
            "-----BEGIN PRIVATE KEY-----\n" +
                    "MC4CAQAwBQYDK2VwBCIEIErU1sdUkfufFIiIjeyB6XCqEKR4dFtTYejBjH/jeM4O\n" +
                    "-----END PRIVATE KEY-----\n";

    private static final String PRESIGN_VALIDATION_KEY_ED448_PRIV =
            "-----BEGIN PRIVATE KEY-----\n" +
                    "MEcCAQAwBQYDK2VxBDsEOaEFdMTDqYgfCBO+L1X1gkY/MtsRCkkqRIRaf/w0sZL8\n" +
                    "MHdS7JohG5RxniPplORiTi/F/bIkJ8GZ7g==\n" +
                    "-----END PRIVATE KEY-----\n";
    
    @BeforeClass
    public static void beforeClass() throws Exception {
        CryptoProviderTools.installBCProviderIfNotAvailable();
    }

    @Test
    public void testGetKeyAlgorithm() {
        assertNull("null if no match", AlgorithmTools.getKeyAlgorithm(new MockNotSupportedPublicKey()));
        assertEquals("Should find DSA key",
                AlgorithmConstants.KEYALGORITHM_DSA,
                KeyTools.getKeyPairFromPEM(PRESIGN_VALIDATION_KEY_DSA_PRIV).getPublic().getAlgorithm());
        assertEquals("Should find RSA key",
                AlgorithmConstants.KEYALGORITHM_RSA,
                KeyTools.getKeyPairFromPEM(PRESIGN_VALIDATION_KEY_RSA_PRIV).getPublic().getAlgorithm());
        assertEquals("Should find secp256r1 key",
                AlgorithmConstants.KEYALGORITHM_ECDSA,
                KeyTools.getKeyPairFromPEM(PRESIGN_VALIDATION_KEY_EC_SECP256R1_PRIV).getPublic().getAlgorithm());
        assertEquals("Should find secp384r1 key",
                AlgorithmConstants.KEYALGORITHM_ECDSA,
                KeyTools.getKeyPairFromPEM(PRESIGN_VALIDATION_KEY_EC_SECP384R1_PRIV).getPublic().getAlgorithm());
        assertEquals("Should find Ed25519 key",
                AlgorithmConstants.KEYALGORITHM_ED25519,
                KeyTools.getKeyPairFromPEM(PRESIGN_VALIDATION_KEY_ED25519_PRIV).getPublic().getAlgorithm());
        assertEquals("Should find Ed448 key",
                AlgorithmConstants.KEYALGORITHM_ED448,
                KeyTools.getKeyPairFromPEM(PRESIGN_VALIDATION_KEY_ED448_PRIV).getPublic().getAlgorithm());
    }

    @Test
    public void testGetSignatureAlgorithmsNotSupportedKey() {
        final List<String> algs = AlgorithmTools.getSignatureAlgorithms(new MockNotSupportedPublicKey());
        assertNotNull("should not return null", algs);
        assertEquals("no supported algs", 0, algs.size());
    }

    @Test
    public void testDigestFromAlgoName() throws Exception {
        final byte[] someBytes = new byte[] {};
        // SHA2-{256,384,512}
        AlgorithmTools.getDigestFromAlgoName(AlgorithmConstants.SIGALG_SHA256_WITH_ECDSA).digest(someBytes);
        AlgorithmTools.getDigestFromAlgoName(AlgorithmConstants.SIGALG_SHA256_WITH_RSA).digest(someBytes);
        AlgorithmTools.getDigestFromAlgoName(AlgorithmConstants.SIGALG_SHA256_WITH_RSA_AND_MGF1).digest(someBytes);

        AlgorithmTools.getDigestFromAlgoName(AlgorithmConstants.SIGALG_SHA384_WITH_ECDSA).digest(someBytes);
        AlgorithmTools.getDigestFromAlgoName(AlgorithmConstants.SIGALG_SHA384_WITH_RSA).digest(someBytes);
        AlgorithmTools.getDigestFromAlgoName(AlgorithmConstants.SIGALG_SHA384_WITH_RSA_AND_MGF1).digest(someBytes);

        AlgorithmTools.getDigestFromAlgoName(AlgorithmConstants.SIGALG_SHA512_WITH_ECDSA).digest(someBytes);
        AlgorithmTools.getDigestFromAlgoName(AlgorithmConstants.SIGALG_SHA512_WITH_RSA).digest(someBytes);
        AlgorithmTools.getDigestFromAlgoName(AlgorithmConstants.SIGALG_SHA512_WITH_RSA_AND_MGF1).digest(someBytes);
        // SHA3-{256,384,512}
        AlgorithmTools.getDigestFromAlgoName(AlgorithmConstants.SIGALG_SHA3_256_WITH_ECDSA).digest(someBytes);
        AlgorithmTools.getDigestFromAlgoName(AlgorithmConstants.SIGALG_SHA3_256_WITH_RSA).digest(someBytes);

        AlgorithmTools.getDigestFromAlgoName(AlgorithmConstants.SIGALG_SHA3_384_WITH_ECDSA).digest(someBytes);
        AlgorithmTools.getDigestFromAlgoName(AlgorithmConstants.SIGALG_SHA3_384_WITH_RSA).digest(someBytes);

        AlgorithmTools.getDigestFromAlgoName(AlgorithmConstants.SIGALG_SHA3_512_WITH_ECDSA).digest(someBytes);
        AlgorithmTools.getDigestFromAlgoName(AlgorithmConstants.SIGALG_SHA3_512_WITH_RSA).digest(someBytes);

        // There is no digest defined for Ed25519 and Ed448
        try {
            AlgorithmTools.getDigestFromAlgoName(AlgorithmConstants.SIGALG_ED25519).digest(someBytes);
            fail("should have thrown for Ed25519");
        } catch (NoSuchAlgorithmException e) {} // NOPMD
        try {
            AlgorithmTools.getDigestFromAlgoName(AlgorithmConstants.SIGALG_ED448).digest(someBytes);
            fail("should have thrown for Ed448");
        } catch (NoSuchAlgorithmException e) {} // NOPMD

    }

    @Test
    public void testGetKeyAlgorithmFromSigAlg() {

        // Test that key algorithm is RSA for all of its signature algorithms
        for (final String s : AlgorithmTools.getSignatureAlgorithms(new MockRSAPublicKey()) ) {
            assertEquals(AlgorithmTools.getKeyAlgorithm(new MockRSAPublicKey()), AlgorithmTools.getKeyAlgorithmFromSigAlg(s));
        }

        // Test that key algorithm is DSA for all of its signature algorithms
        for (final String s : AlgorithmTools.getSignatureAlgorithms(new MockDSAPublicKey())) {
            assertEquals(AlgorithmTools.getKeyAlgorithm(new MockDSAPublicKey()), AlgorithmTools.getKeyAlgorithmFromSigAlg(s));
        }

        // Test that key algorithm is ECDSA for all of its signature algorithms
        for (final String s : AlgorithmTools.getSignatureAlgorithms(new MockECDSAPublicKey())) {
            assertEquals(AlgorithmTools.getKeyAlgorithm(new MockECDSAPublicKey()), AlgorithmTools.getKeyAlgorithmFromSigAlg(s));
        }

        // EdDSA have specific signature algorithms per key
        PublicKey pk = KeyTools.getKeyPairFromPEM(PRESIGN_VALIDATION_KEY_ED25519_PRIV).getPublic();
        List<String> algos = AlgorithmTools.getSignatureAlgorithms(pk);
        assertEquals("There should be exactly one signatur ealgo for Ed25519", 1, algos.size());
        assertEquals("Not Ed25519 algo returned", AlgorithmConstants.SIGALG_ED25519, algos.get(0));
        assertEquals("Should be Ed25519", AlgorithmConstants.KEYALGORITHM_ED25519, AlgorithmTools.getKeyAlgorithmFromSigAlg(algos.get(0)));
        assertEquals("Should be Ed25519", AlgorithmConstants.KEYALGORITHM_ED25519, AlgorithmTools.getKeyAlgorithm(pk));
        pk = KeyTools.getKeyPairFromPEM(PRESIGN_VALIDATION_KEY_ED448_PRIV).getPublic();
        algos = AlgorithmTools.getSignatureAlgorithms(pk);
        assertEquals("There should be exactly one signatur ealgo for Ed448", 1, algos.size());
        assertEquals("Not Ed448 algo returned", AlgorithmConstants.SIGALG_ED448, algos.get(0));
        assertEquals("Should be Ed448", AlgorithmConstants.KEYALGORITHM_ED448, AlgorithmTools.getKeyAlgorithmFromSigAlg(algos.get(0)));
        assertEquals("Should be Ed448", AlgorithmConstants.KEYALGORITHM_ED448, AlgorithmTools.getKeyAlgorithm(pk));

        // should return a default value
        assertNotNull("should return a default value", AlgorithmTools.getKeyAlgorithmFromSigAlg("_NonExistingAlg"));

    }

    @Test
    public void testGetKeySpecification() throws Exception {
        assertNull("null if the key algorithm is not supported", AlgorithmTools.getKeySpecification(new MockNotSupportedPublicKey()));
        assertEquals("unknown", AlgorithmTools.getKeySpecification(new MockECDSAPublicKey()));
        assertEquals("10", AlgorithmTools.getKeySpecification(new MockRSAPublicKey()));
        KeyPair pair = KeyTools.genKeys("prime192v1", "ECDSA");
        final String ecNamedCurve = AlgorithmTools.getKeySpecification(pair.getPublic());
        assertTrue("Key was generated with the right curve.", AlgorithmTools.getEcKeySpecAliases(ecNamedCurve).contains("prime192v1"));
        assertTrue("Key was generated with the right curve.", AlgorithmTools.getEcKeySpecAliases(ecNamedCurve).contains("secp192r1"));
        // We can't really say if "secp192r1" or "prime192v1" should be the preferred name on this system, since it depends on available providers.
        //assertEquals("Unexpected preferred named curve alias.", "secp192r1", ecNamedCurve);
        pair = KeyTools.genKeys("1024", "DSA");
        assertEquals("1024", AlgorithmTools.getKeySpecification(pair.getPublic()));
        PublicKey pk = KeyTools.getKeyPairFromPEM(PRESIGN_VALIDATION_KEY_ED25519_PRIV).getPublic();
        assertEquals("Ed25519", AlgorithmTools.getKeySpecification(pk));
        pk = KeyTools.getKeyPairFromPEM(PRESIGN_VALIDATION_KEY_ED448_PRIV).getPublic();
        assertEquals("Ed448", AlgorithmTools.getKeySpecification(pk));

    }

    @Test
    public void testGetKeySpecificationGOST3410() throws Exception {
        assumeTrue(AlgorithmConfigurationCache.INSTANCE.isGost3410Enabled());
        final String keyspec = "GostR3410-2001-CryptoPro-B";
        KeyPairGenerator keygen = KeyPairGenerator.getInstance("ECGOST3410", "BC");
        AlgorithmParameterSpec ecSpec = ECGOST3410NamedCurveTable.getParameterSpec(keyspec);
        keygen.initialize(ecSpec);
        KeyPair keys = keygen.generateKeyPair();
        assertEquals(keyspec, AlgorithmTools.getKeySpecification(keys.getPublic()));
    }

    @Test
    public void testGetKeySpecificationDSTU4145() throws Exception {
        assumeTrue(AlgorithmConfigurationCache.INSTANCE.isDstu4145Enabled());
        final String keyspec = "2.5";
        KeyPairGenerator keygen = KeyPairGenerator.getInstance("DSTU4145", "BC");
        AlgorithmParameterSpec ecSpec = KeyTools.dstuOidToAlgoParams(keyspec);
        keygen.initialize(ecSpec);
        KeyPair keys = keygen.generateKeyPair();
        assertEquals(keyspec, AlgorithmTools.getKeySpecification(keys.getPublic()));
    }
   

    @Test
    public void testGetEncSigAlgFromSigAlgRSA() throws InvalidAlgorithmParameterException {
        PublicKey publicKey = KeyTools.genKeys("1024", "RSA").getPublic();
        assertEquals(AlgorithmConstants.SIGALG_SHA512_WITH_RSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_SHA512_WITH_ECDSA, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA384_WITH_RSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_SHA384_WITH_ECDSA, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA256_WITH_RSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_SHA256_WITH_ECDSA, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA256_WITH_RSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_SHA224_WITH_ECDSA, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA1_WITH_RSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_SHA1_WITH_ECDSA, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA1_WITH_RSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_SHA1_WITH_DSA, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA1_WITH_RSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_SHA1_WITH_RSA, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA256_WITH_RSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_SHA256_WITH_RSA, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA256_WITH_RSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_SHA256_WITH_DSA, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA384_WITH_RSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_SHA384_WITH_RSA, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA512_WITH_RSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_SHA512_WITH_RSA, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA1_WITH_RSA_AND_MGF1, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_SHA1_WITH_RSA_AND_MGF1, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA256_WITH_RSA_AND_MGF1, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_SHA256_WITH_RSA_AND_MGF1, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA384_WITH_RSA_AND_MGF1, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_SHA384_WITH_RSA_AND_MGF1, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA512_WITH_RSA_AND_MGF1, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_SHA512_WITH_RSA_AND_MGF1, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA1_WITH_RSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_GOST3411_WITH_ECGOST3410, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA1_WITH_RSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_GOST3411_WITH_DSTU4145, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA3_256_WITH_RSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_SHA3_256_WITH_RSA, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA3_384_WITH_RSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_SHA3_384_WITH_RSA, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA3_512_WITH_RSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_SHA3_512_WITH_RSA, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA3_256_WITH_RSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_SHA3_256_WITH_ECDSA, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA3_384_WITH_RSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_SHA3_384_WITH_ECDSA, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA3_512_WITH_RSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_SHA3_512_WITH_ECDSA, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA256_WITH_RSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_ED25519, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA256_WITH_RSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_ED448, publicKey));
        assertEquals("Foobar", AlgorithmTools.getEncSigAlgFromSigAlg("Foobar", publicKey));
    }
    
    @Test
    public void testGetEncSigAlgFromSigAlgECDSA() throws InvalidAlgorithmParameterException {
        PublicKey publicKey = KeyTools.genKeys("secp256k1", "ECDSA").getPublic();
        assertEquals(AlgorithmConstants.SIGALG_SHA512_WITH_ECDSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_SHA512_WITH_ECDSA, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA384_WITH_ECDSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_SHA384_WITH_ECDSA, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA256_WITH_ECDSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_SHA256_WITH_ECDSA, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA224_WITH_ECDSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_SHA224_WITH_ECDSA, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA1_WITH_ECDSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_SHA1_WITH_ECDSA, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA1_WITH_RSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_SHA1_WITH_DSA, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA1_WITH_ECDSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_SHA1_WITH_RSA, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA256_WITH_ECDSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_SHA256_WITH_RSA, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA256_WITH_RSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_SHA256_WITH_DSA, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA384_WITH_ECDSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_SHA384_WITH_RSA, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA512_WITH_ECDSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_SHA512_WITH_RSA, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA1_WITH_ECDSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_SHA1_WITH_RSA_AND_MGF1, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA256_WITH_ECDSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_SHA256_WITH_RSA_AND_MGF1, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA384_WITH_ECDSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_SHA384_WITH_RSA_AND_MGF1, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA512_WITH_ECDSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_SHA512_WITH_RSA_AND_MGF1, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA1_WITH_RSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_GOST3411_WITH_ECGOST3410, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA1_WITH_RSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_GOST3411_WITH_DSTU4145, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA3_256_WITH_ECDSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_SHA3_256_WITH_RSA, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA3_384_WITH_ECDSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_SHA3_384_WITH_RSA, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA3_512_WITH_ECDSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_SHA3_512_WITH_RSA, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA3_256_WITH_ECDSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_SHA3_256_WITH_ECDSA, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA3_384_WITH_ECDSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_SHA3_384_WITH_ECDSA, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA3_512_WITH_ECDSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_SHA3_512_WITH_ECDSA, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA256_WITH_RSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_ED25519, publicKey));
        assertEquals(AlgorithmConstants.SIGALG_SHA256_WITH_RSA, AlgorithmTools.getEncSigAlgFromSigAlg(AlgorithmConstants.SIGALG_ED448, publicKey));
        assertEquals("Foobar", AlgorithmTools.getEncSigAlgFromSigAlg("Foobar", publicKey));
    }
    
  
    
    @Test
    public void testGetAlgorithmNameFromDigestAndKey() {
        assertEquals(AlgorithmConstants.SIGALG_SHA1_WITH_RSA, AlgorithmTools.getAlgorithmNameFromDigestAndKey(CMSSignedGenerator.DIGEST_SHA1, AlgorithmConstants.KEYALGORITHM_RSA));
        assertEquals(AlgorithmConstants.SIGALG_SHA1_WITH_DSA, AlgorithmTools.getAlgorithmNameFromDigestAndKey(CMSSignedGenerator.DIGEST_SHA1, AlgorithmConstants.KEYALGORITHM_DSA));
        assertEquals(AlgorithmConstants.SIGALG_SHA256_WITH_RSA, AlgorithmTools.getAlgorithmNameFromDigestAndKey(CMSSignedGenerator.DIGEST_SHA256, AlgorithmConstants.KEYALGORITHM_RSA));
        assertEquals(AlgorithmConstants.SIGALG_SHA256_WITH_DSA, AlgorithmTools.getAlgorithmNameFromDigestAndKey(CMSSignedGenerator.DIGEST_SHA256, AlgorithmConstants.KEYALGORITHM_DSA));
        assertEquals(AlgorithmConstants.SIGALG_SHA384_WITH_RSA, AlgorithmTools.getAlgorithmNameFromDigestAndKey(CMSSignedGenerator.DIGEST_SHA384, AlgorithmConstants.KEYALGORITHM_RSA));
        assertEquals(AlgorithmConstants.SIGALG_SHA512_WITH_RSA, AlgorithmTools.getAlgorithmNameFromDigestAndKey(CMSSignedGenerator.DIGEST_SHA512, AlgorithmConstants.KEYALGORITHM_RSA));
        assertEquals(AlgorithmConstants.SIGALG_SHA1_WITH_ECDSA, AlgorithmTools.getAlgorithmNameFromDigestAndKey(CMSSignedGenerator.DIGEST_SHA1, AlgorithmConstants.KEYALGORITHM_EC));
        assertEquals(AlgorithmConstants.SIGALG_SHA224_WITH_ECDSA, AlgorithmTools.getAlgorithmNameFromDigestAndKey(CMSSignedGenerator.DIGEST_SHA224, AlgorithmConstants.KEYALGORITHM_EC));
        assertEquals(AlgorithmConstants.SIGALG_SHA256_WITH_ECDSA, AlgorithmTools.getAlgorithmNameFromDigestAndKey(CMSSignedGenerator.DIGEST_SHA256, AlgorithmConstants.KEYALGORITHM_EC));
        assertEquals(AlgorithmConstants.SIGALG_SHA384_WITH_ECDSA, AlgorithmTools.getAlgorithmNameFromDigestAndKey(CMSSignedGenerator.DIGEST_SHA384, AlgorithmConstants.KEYALGORITHM_EC));
        assertEquals(AlgorithmConstants.SIGALG_SHA512_WITH_ECDSA, AlgorithmTools.getAlgorithmNameFromDigestAndKey(CMSSignedGenerator.DIGEST_SHA512, AlgorithmConstants.KEYALGORITHM_EC));
        assertEquals(AlgorithmConstants.SIGALG_SHA3_256_WITH_RSA, AlgorithmTools.getAlgorithmNameFromDigestAndKey(NISTObjectIdentifiers.id_sha3_256.getId(), AlgorithmConstants.KEYALGORITHM_RSA));
        assertEquals(AlgorithmConstants.SIGALG_SHA3_384_WITH_RSA, AlgorithmTools.getAlgorithmNameFromDigestAndKey(NISTObjectIdentifiers.id_sha3_384.getId(), AlgorithmConstants.KEYALGORITHM_RSA));
        assertEquals(AlgorithmConstants.SIGALG_SHA3_512_WITH_RSA, AlgorithmTools.getAlgorithmNameFromDigestAndKey(NISTObjectIdentifiers.id_sha3_512.getId(), AlgorithmConstants.KEYALGORITHM_RSA));
        assertEquals(AlgorithmConstants.SIGALG_SHA3_256_WITH_ECDSA, AlgorithmTools.getAlgorithmNameFromDigestAndKey(NISTObjectIdentifiers.id_sha3_256.getId(), AlgorithmConstants.KEYALGORITHM_EC));
        assertEquals(AlgorithmConstants.SIGALG_SHA3_384_WITH_ECDSA, AlgorithmTools.getAlgorithmNameFromDigestAndKey(NISTObjectIdentifiers.id_sha3_384.getId(), AlgorithmConstants.KEYALGORITHM_EC));
        assertEquals(AlgorithmConstants.SIGALG_SHA3_512_WITH_ECDSA, AlgorithmTools.getAlgorithmNameFromDigestAndKey(NISTObjectIdentifiers.id_sha3_512.getId(), AlgorithmConstants.KEYALGORITHM_EC));
        // Default is SHA1 with RSA
        assertEquals(AlgorithmConstants.SIGALG_SHA256_WITH_RSA, AlgorithmTools.getAlgorithmNameFromDigestAndKey("Foobar", "Foo"));
    }

    @Test
    public void testIsCompatibleSigAlg() {
    	assertTrue(AlgorithmTools.isCompatibleSigAlg(new MockRSAPublicKey(), AlgorithmConstants.SIGALG_SHA1_WITH_RSA));
    	assertTrue(AlgorithmTools.isCompatibleSigAlg(new MockRSAPublicKey(), AlgorithmConstants.SIGALG_SHA256_WITH_RSA));
    	assertTrue(AlgorithmTools.isCompatibleSigAlg(new MockRSAPublicKey(), AlgorithmConstants.SIGALG_SHA384_WITH_RSA));
    	assertTrue(AlgorithmTools.isCompatibleSigAlg(new MockRSAPublicKey(), AlgorithmConstants.SIGALG_SHA512_WITH_RSA));
    	assertTrue(AlgorithmTools.isCompatibleSigAlg(new MockRSAPublicKey(), AlgorithmConstants.SIGALG_SHA1_WITH_RSA_AND_MGF1));
    	assertTrue(AlgorithmTools.isCompatibleSigAlg(new MockRSAPublicKey(), AlgorithmConstants.SIGALG_SHA256_WITH_RSA_AND_MGF1));
        assertTrue(AlgorithmTools.isCompatibleSigAlg(new MockRSAPublicKey(), AlgorithmConstants.SIGALG_SHA384_WITH_RSA_AND_MGF1));
        assertTrue(AlgorithmTools.isCompatibleSigAlg(new MockRSAPublicKey(), AlgorithmConstants.SIGALG_SHA512_WITH_RSA_AND_MGF1));
        assertTrue(AlgorithmTools.isCompatibleSigAlg(new MockRSAPublicKey(), AlgorithmConstants.SIGALG_SHA3_256_WITH_RSA));
        assertTrue(AlgorithmTools.isCompatibleSigAlg(new MockRSAPublicKey(), AlgorithmConstants.SIGALG_SHA3_384_WITH_RSA));
        assertTrue(AlgorithmTools.isCompatibleSigAlg(new MockRSAPublicKey(), AlgorithmConstants.SIGALG_SHA3_512_WITH_RSA));
    	assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockRSAPublicKey(), AlgorithmConstants.SIGALG_SHA224_WITH_ECDSA));
    	assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockRSAPublicKey(), AlgorithmConstants.SIGALG_SHA256_WITH_ECDSA));
    	assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockRSAPublicKey(), AlgorithmConstants.SIGALG_SHA384_WITH_ECDSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockRSAPublicKey(), AlgorithmConstants.SIGALG_SHA512_WITH_ECDSA));
    	assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockRSAPublicKey(), AlgorithmConstants.SIGALG_SHA1_WITH_ECDSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockRSAPublicKey(), AlgorithmConstants.SIGALG_SHA3_256_WITH_ECDSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockRSAPublicKey(), AlgorithmConstants.SIGALG_SHA3_384_WITH_ECDSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockRSAPublicKey(), AlgorithmConstants.SIGALG_SHA3_512_WITH_ECDSA));
    	assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockRSAPublicKey(), AlgorithmConstants.SIGALG_SHA1_WITH_DSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockRSAPublicKey(), AlgorithmConstants.SIGALG_SHA256_WITH_DSA));

    	assertTrue(AlgorithmTools.isCompatibleSigAlg(new MockECDSAPublicKey(), AlgorithmConstants.SIGALG_SHA224_WITH_ECDSA));
    	assertTrue(AlgorithmTools.isCompatibleSigAlg(new MockECDSAPublicKey(), AlgorithmConstants.SIGALG_SHA256_WITH_ECDSA));
    	assertTrue(AlgorithmTools.isCompatibleSigAlg(new MockECDSAPublicKey(), AlgorithmConstants.SIGALG_SHA384_WITH_ECDSA));
        assertTrue(AlgorithmTools.isCompatibleSigAlg(new MockECDSAPublicKey(), AlgorithmConstants.SIGALG_SHA512_WITH_ECDSA));
    	assertTrue(AlgorithmTools.isCompatibleSigAlg(new MockECDSAPublicKey(), AlgorithmConstants.SIGALG_SHA1_WITH_ECDSA));
        assertTrue(AlgorithmTools.isCompatibleSigAlg(new MockECDSAPublicKey(), AlgorithmConstants.SIGALG_SHA3_256_WITH_ECDSA));
        assertTrue(AlgorithmTools.isCompatibleSigAlg(new MockECDSAPublicKey(), AlgorithmConstants.SIGALG_SHA3_384_WITH_ECDSA));
        assertTrue(AlgorithmTools.isCompatibleSigAlg(new MockECDSAPublicKey(), AlgorithmConstants.SIGALG_SHA3_512_WITH_ECDSA));
    	assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockECDSAPublicKey(), AlgorithmConstants.SIGALG_SHA1_WITH_DSA));
    	assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockECDSAPublicKey(), AlgorithmConstants.SIGALG_SHA1_WITH_RSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockECDSAPublicKey(), AlgorithmConstants.SIGALG_SHA256_WITH_DSA));
    	assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockECDSAPublicKey(), AlgorithmConstants.SIGALG_SHA256_WITH_RSA));
    	assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockECDSAPublicKey(), AlgorithmConstants.SIGALG_SHA384_WITH_RSA));
    	assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockECDSAPublicKey(), AlgorithmConstants.SIGALG_SHA512_WITH_RSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockECDSAPublicKey(), AlgorithmConstants.SIGALG_SHA3_256_WITH_RSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockECDSAPublicKey(), AlgorithmConstants.SIGALG_SHA3_384_WITH_RSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockECDSAPublicKey(), AlgorithmConstants.SIGALG_SHA3_512_WITH_RSA));

    	assertTrue(AlgorithmTools.isCompatibleSigAlg(new MockDSAPublicKey(), AlgorithmConstants.SIGALG_SHA1_WITH_DSA));
        assertTrue(AlgorithmTools.isCompatibleSigAlg(new MockDSAPublicKey(), AlgorithmConstants.SIGALG_SHA256_WITH_DSA));
    	assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockDSAPublicKey(), AlgorithmConstants.SIGALG_SHA1_WITH_RSA));
    	assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockDSAPublicKey(), AlgorithmConstants.SIGALG_SHA256_WITH_RSA));
    	assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockDSAPublicKey(), AlgorithmConstants.SIGALG_SHA384_WITH_RSA));
    	assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockDSAPublicKey(), AlgorithmConstants.SIGALG_SHA512_WITH_RSA));
    	assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockDSAPublicKey(), AlgorithmConstants.SIGALG_SHA224_WITH_ECDSA));
    	assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockDSAPublicKey(), AlgorithmConstants.SIGALG_SHA256_WITH_ECDSA));
    	assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockDSAPublicKey(), AlgorithmConstants.SIGALG_SHA384_WITH_ECDSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockDSAPublicKey(), AlgorithmConstants.SIGALG_SHA512_WITH_ECDSA));
    	assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockDSAPublicKey(), AlgorithmConstants.SIGALG_SHA1_WITH_ECDSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockDSAPublicKey(), AlgorithmConstants.SIGALG_SHA3_256_WITH_RSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockDSAPublicKey(), AlgorithmConstants.SIGALG_SHA3_384_WITH_RSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockDSAPublicKey(), AlgorithmConstants.SIGALG_SHA3_512_WITH_RSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockDSAPublicKey(), AlgorithmConstants.SIGALG_SHA3_256_WITH_ECDSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockDSAPublicKey(), AlgorithmConstants.SIGALG_SHA3_384_WITH_ECDSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockDSAPublicKey(), AlgorithmConstants.SIGALG_SHA3_512_WITH_ECDSA));

        PublicKey pk = KeyTools.getKeyPairFromPEM(PRESIGN_VALIDATION_KEY_ED25519_PRIV).getPublic();
        assertTrue(AlgorithmTools.isCompatibleSigAlg(pk, AlgorithmConstants.SIGALG_ED25519));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(pk, AlgorithmConstants.SIGALG_SHA1_WITH_RSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(pk, AlgorithmConstants.SIGALG_SHA256_WITH_RSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(pk, AlgorithmConstants.SIGALG_SHA384_WITH_RSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(pk, AlgorithmConstants.SIGALG_SHA512_WITH_RSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(pk, AlgorithmConstants.SIGALG_SHA224_WITH_ECDSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(pk, AlgorithmConstants.SIGALG_SHA256_WITH_ECDSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(pk, AlgorithmConstants.SIGALG_SHA384_WITH_ECDSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(pk, AlgorithmConstants.SIGALG_SHA512_WITH_ECDSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(pk, AlgorithmConstants.SIGALG_SHA1_WITH_ECDSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(pk, AlgorithmConstants.SIGALG_SHA3_256_WITH_RSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(pk, AlgorithmConstants.SIGALG_SHA3_384_WITH_RSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(pk, AlgorithmConstants.SIGALG_SHA3_512_WITH_RSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(pk, AlgorithmConstants.SIGALG_SHA3_256_WITH_ECDSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(pk, AlgorithmConstants.SIGALG_SHA3_384_WITH_ECDSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(pk, AlgorithmConstants.SIGALG_SHA3_512_WITH_ECDSA));

        pk = KeyTools.getKeyPairFromPEM(PRESIGN_VALIDATION_KEY_ED448_PRIV).getPublic();
        assertTrue(AlgorithmTools.isCompatibleSigAlg(pk, AlgorithmConstants.SIGALG_ED448));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(pk, AlgorithmConstants.SIGALG_SHA1_WITH_RSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(pk, AlgorithmConstants.SIGALG_SHA256_WITH_RSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(pk, AlgorithmConstants.SIGALG_SHA384_WITH_RSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(pk, AlgorithmConstants.SIGALG_SHA512_WITH_RSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(pk, AlgorithmConstants.SIGALG_SHA224_WITH_ECDSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(pk, AlgorithmConstants.SIGALG_SHA256_WITH_ECDSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(pk, AlgorithmConstants.SIGALG_SHA384_WITH_ECDSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(pk, AlgorithmConstants.SIGALG_SHA512_WITH_ECDSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(pk, AlgorithmConstants.SIGALG_SHA1_WITH_ECDSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(pk, AlgorithmConstants.SIGALG_SHA3_256_WITH_RSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(pk, AlgorithmConstants.SIGALG_SHA3_384_WITH_RSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(pk, AlgorithmConstants.SIGALG_SHA3_512_WITH_RSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(pk, AlgorithmConstants.SIGALG_SHA3_256_WITH_ECDSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(pk, AlgorithmConstants.SIGALG_SHA3_384_WITH_ECDSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(pk, AlgorithmConstants.SIGALG_SHA3_512_WITH_ECDSA));

    }

    @Test
    public void testIsCompatibleSigAlgGOST3410() {
        assumeTrue(AlgorithmConfigurationCache.INSTANCE.isGost3410Enabled());
    	assertTrue(AlgorithmTools.isCompatibleSigAlg(new MockGOST3410PublicKey(), AlgorithmConstants.SIGALG_GOST3411_WITH_ECGOST3410));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockGOST3410PublicKey(), AlgorithmConstants.SIGALG_SHA1_WITH_DSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockGOST3410PublicKey(), AlgorithmConstants.SIGALG_SHA256_WITH_DSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockGOST3410PublicKey(), AlgorithmConstants.SIGALG_SHA1_WITH_RSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockGOST3410PublicKey(), AlgorithmConstants.SIGALG_SHA256_WITH_RSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockGOST3410PublicKey(), AlgorithmConstants.SIGALG_SHA384_WITH_RSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockGOST3410PublicKey(), AlgorithmConstants.SIGALG_SHA512_WITH_RSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockGOST3410PublicKey(), AlgorithmConstants.SIGALG_SHA3_256_WITH_RSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockGOST3410PublicKey(), AlgorithmConstants.SIGALG_SHA3_384_WITH_RSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockGOST3410PublicKey(), AlgorithmConstants.SIGALG_SHA3_512_WITH_RSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockGOST3410PublicKey(), AlgorithmConstants.SIGALG_SHA3_256_WITH_ECDSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockGOST3410PublicKey(), AlgorithmConstants.SIGALG_SHA3_384_WITH_ECDSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockGOST3410PublicKey(), AlgorithmConstants.SIGALG_SHA3_512_WITH_ECDSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockGOST3410PublicKey(), AlgorithmConstants.SIGALG_GOST3411_WITH_DSTU4145));
    }

    @Test
    public void testIsCompatibleSigAlgDSTU4145() {
        assumeTrue(AlgorithmConfigurationCache.INSTANCE.isDstu4145Enabled());
        assertTrue(AlgorithmTools.isCompatibleSigAlg(new MockDSTU4145PublicKey(), AlgorithmConstants.SIGALG_GOST3411_WITH_DSTU4145));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockDSTU4145PublicKey(), AlgorithmConstants.SIGALG_SHA1_WITH_DSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockDSTU4145PublicKey(), AlgorithmConstants.SIGALG_SHA256_WITH_DSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockDSTU4145PublicKey(), AlgorithmConstants.SIGALG_SHA1_WITH_RSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockDSTU4145PublicKey(), AlgorithmConstants.SIGALG_SHA256_WITH_RSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockDSTU4145PublicKey(), AlgorithmConstants.SIGALG_SHA384_WITH_RSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockDSTU4145PublicKey(), AlgorithmConstants.SIGALG_SHA512_WITH_RSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockDSTU4145PublicKey(), AlgorithmConstants.SIGALG_GOST3411_WITH_ECGOST3410));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockDSTU4145PublicKey(), AlgorithmConstants.SIGALG_SHA3_256_WITH_RSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockDSTU4145PublicKey(), AlgorithmConstants.SIGALG_SHA3_384_WITH_RSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockDSTU4145PublicKey(), AlgorithmConstants.SIGALG_SHA3_512_WITH_RSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockDSTU4145PublicKey(), AlgorithmConstants.SIGALG_SHA3_256_WITH_ECDSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockDSTU4145PublicKey(), AlgorithmConstants.SIGALG_SHA3_384_WITH_ECDSA));
        assertFalse(AlgorithmTools.isCompatibleSigAlg(new MockDSTU4145PublicKey(), AlgorithmConstants.SIGALG_SHA3_512_WITH_ECDSA));
    }

    @Test
    public void testCertSignatureAlgorithmAsString() throws Exception {
        // X.509
    	KeyPair keyPair = KeyTools.genKeys("2048", "RSA"); // 2048 needed for MGF1 with SHA512
    	Certificate sha1rsa = CertTools.genSelfCert("CN=TEST", 10L, null, keyPair.getPrivate(), keyPair.getPublic(), "SHA1WithRSA", true);
    	Certificate md5rsa = CertTools.genSelfCert("CN=TEST", 10L, null, keyPair.getPrivate(), keyPair.getPublic(), "MD5WithRSA", true);
    	Certificate sha256rsa = CertTools.genSelfCert("CN=TEST", 10L, null, keyPair.getPrivate(), keyPair.getPublic(), "SHA256WithRSA", true);
    	Certificate sha384rsa = CertTools.genSelfCert("CN=TEST", 10L, null, keyPair.getPrivate(), keyPair.getPublic(), "SHA384WithRSA", true);
    	Certificate sha512rsa = CertTools.genSelfCert("CN=TEST", 10L, null, keyPair.getPrivate(), keyPair.getPublic(), "SHA512WithRSA", true);
    	Certificate sha1rsamgf = CertTools.genSelfCert("CN=TEST", 10L, null, keyPair.getPrivate(), keyPair.getPublic(), "SHA1WithRSAAndMGF1", true);
    	Certificate sha256rsamgf = CertTools.genSelfCert("CN=TEST", 10L, null, keyPair.getPrivate(), keyPair.getPublic(), "SHA256WithRSAAndMGF1", true);
        Certificate sha384rsamgf = CertTools.genSelfCert("CN=TEST", 10L, null, keyPair.getPrivate(), keyPair.getPublic(), "SHA384WithRSAAndMGF1", true);
        Certificate sha512rsamgf = CertTools.genSelfCert("CN=TEST", 10L, null, keyPair.getPrivate(), keyPair.getPublic(), "SHA512WithRSAAndMGF1", true);
        Certificate sha3_256_rsa = CertTools.genSelfCert("CN=TEST", 10L, null, keyPair.getPrivate(), keyPair.getPublic(), AlgorithmConstants.SIGALG_SHA3_256_WITH_RSA, true);
        Certificate sha3_384_rsa = CertTools.genSelfCert("CN=TEST", 10L, null, keyPair.getPrivate(), keyPair.getPublic(), AlgorithmConstants.SIGALG_SHA3_384_WITH_RSA, true);
        Certificate sha3_512_rsa = CertTools.genSelfCert("CN=TEST", 10L, null, keyPair.getPrivate(), keyPair.getPublic(), AlgorithmConstants.SIGALG_SHA3_512_WITH_RSA, true);
    	assertEquals("SHA1WITHRSA", CertTools.getCertSignatureAlgorithmNameAsString(sha1rsa));
    	assertEquals("MD5WITHRSA", CertTools.getCertSignatureAlgorithmNameAsString(md5rsa));
    	assertEquals("SHA256WITHRSA", CertTools.getCertSignatureAlgorithmNameAsString(sha256rsa));
    	assertEquals("SHA384WITHRSA", CertTools.getCertSignatureAlgorithmNameAsString(sha384rsa));
    	assertEquals("SHA512WITHRSA", CertTools.getCertSignatureAlgorithmNameAsString(sha512rsa));
    	assertEquals(AlgorithmConstants.SIGALG_SHA1_WITH_RSA_AND_MGF1, CertTools.getCertSignatureAlgorithmNameAsString(sha1rsamgf));
    	assertEquals(AlgorithmConstants.SIGALG_SHA256_WITH_RSA_AND_MGF1, CertTools.getCertSignatureAlgorithmNameAsString(sha256rsamgf));
        assertEquals(AlgorithmConstants.SIGALG_SHA384_WITH_RSA_AND_MGF1, CertTools.getCertSignatureAlgorithmNameAsString(sha384rsamgf));
        assertEquals(AlgorithmConstants.SIGALG_SHA512_WITH_RSA_AND_MGF1, CertTools.getCertSignatureAlgorithmNameAsString(sha512rsamgf));
        assertEquals("SHA3-256WITHRSA", CertTools.getCertSignatureAlgorithmNameAsString(sha3_256_rsa));
        assertEquals("SHA3-384WITHRSA", CertTools.getCertSignatureAlgorithmNameAsString(sha3_384_rsa));
        assertEquals("SHA3-512WITHRSA", CertTools.getCertSignatureAlgorithmNameAsString(sha3_512_rsa));

    	assertEquals("SHA1WithRSA", AlgorithmTools.getSignatureAlgorithm(sha1rsa));
    	assertEquals("MD5WithRSA", AlgorithmTools.getSignatureAlgorithm(md5rsa));
    	assertEquals("SHA256WithRSA", AlgorithmTools.getSignatureAlgorithm(sha256rsa));
    	assertEquals("SHA384WithRSA", AlgorithmTools.getSignatureAlgorithm(sha384rsa));
    	assertEquals("SHA512WithRSA", AlgorithmTools.getSignatureAlgorithm(sha512rsa));
    	assertEquals(AlgorithmConstants.SIGALG_SHA1_WITH_RSA_AND_MGF1, AlgorithmTools.getSignatureAlgorithm(sha1rsamgf));
    	assertEquals(AlgorithmConstants.SIGALG_SHA256_WITH_RSA_AND_MGF1, AlgorithmTools.getSignatureAlgorithm(sha256rsamgf));
        assertEquals(AlgorithmConstants.SIGALG_SHA384_WITH_RSA_AND_MGF1, AlgorithmTools.getSignatureAlgorithm(sha384rsamgf));
        assertEquals(AlgorithmConstants.SIGALG_SHA512_WITH_RSA_AND_MGF1, AlgorithmTools.getSignatureAlgorithm(sha512rsamgf));
        assertEquals(AlgorithmConstants.SIGALG_SHA3_256_WITH_RSA, AlgorithmTools.getSignatureAlgorithm(sha3_256_rsa));
        assertEquals(AlgorithmConstants.SIGALG_SHA3_512_WITH_RSA, AlgorithmTools.getSignatureAlgorithm(sha3_512_rsa));

    	// DSA
    	keyPair = KeyTools.genKeys("1024", "DSA");
    	Certificate sha1rsadsa = CertTools.genSelfCert("CN=TEST", 10L, null, keyPair.getPrivate(), keyPair.getPublic(), "SHA1WithDSA", true);
    	assertEquals("SHA1withDSA", CertTools.getCertSignatureAlgorithmNameAsString(sha1rsadsa));
    	assertEquals("SHA1WithDSA", AlgorithmTools.getSignatureAlgorithm(sha1rsadsa));
        Certificate sha256rsadsa = CertTools.genSelfCert("CN=TEST", 10L, null, keyPair.getPrivate(), keyPair.getPublic(), "SHA256WithDSA", true);
        assertEquals("SHA256WITHDSA", CertTools.getCertSignatureAlgorithmNameAsString(sha256rsadsa));
        assertEquals("SHA256WithDSA", AlgorithmTools.getSignatureAlgorithm(sha256rsadsa));

        // ECC
    	keyPair = KeyTools.genKeys("prime192v1", "ECDSA");
    	Certificate sha1ecc = CertTools.genSelfCert("CN=TEST", 10L, null, keyPair.getPrivate(), keyPair.getPublic(), "SHA1WithECDSA", true);
    	Certificate sha224ecc = CertTools.genSelfCert("CN=TEST", 10L, null, keyPair.getPrivate(), keyPair.getPublic(), "SHA224WithECDSA", true);
    	Certificate sha256ecc = CertTools.genSelfCert("CN=TEST", 10L, null, keyPair.getPrivate(), keyPair.getPublic(), "SHA256WithECDSA", true);
    	Certificate sha384ecc = CertTools.genSelfCert("CN=TEST", 10L, null, keyPair.getPrivate(), keyPair.getPublic(), "SHA384WithECDSA", true);
        Certificate sha512ecc = CertTools.genSelfCert("CN=TEST", 10L, null, keyPair.getPrivate(), keyPair.getPublic(), "SHA512WithECDSA", true);
        Certificate sha3_256_ecc = CertTools.genSelfCert("CN=TEST", 10L, null, keyPair.getPrivate(), keyPair.getPublic(), AlgorithmConstants.SIGALG_SHA3_256_WITH_ECDSA, true);
        Certificate sha3_384_ecc = CertTools.genSelfCert("CN=TEST", 10L, null, keyPair.getPrivate(), keyPair.getPublic(), AlgorithmConstants.SIGALG_SHA3_384_WITH_ECDSA, true);
        Certificate sha3_512_ecc = CertTools.genSelfCert("CN=TEST", 10L, null, keyPair.getPrivate(), keyPair.getPublic(), AlgorithmConstants.SIGALG_SHA3_512_WITH_ECDSA, true);
    	assertEquals("ECDSA", CertTools.getCertSignatureAlgorithmNameAsString(sha1ecc));
    	assertEquals("SHA224WITHECDSA", CertTools.getCertSignatureAlgorithmNameAsString(sha224ecc));
    	assertEquals("SHA256WITHECDSA", CertTools.getCertSignatureAlgorithmNameAsString(sha256ecc));
    	assertEquals("SHA384WITHECDSA", CertTools.getCertSignatureAlgorithmNameAsString(sha384ecc));
        assertEquals("SHA512WITHECDSA", CertTools.getCertSignatureAlgorithmNameAsString(sha512ecc));
        assertEquals("SHA3-256WITHECDSA", CertTools.getCertSignatureAlgorithmNameAsString(sha3_256_ecc));
        assertEquals("SHA3-384WITHECDSA", CertTools.getCertSignatureAlgorithmNameAsString(sha3_384_ecc));
        assertEquals("SHA3-512WITHECDSA", CertTools.getCertSignatureAlgorithmNameAsString(sha3_512_ecc));

    	assertEquals("SHA1withECDSA", AlgorithmTools.getSignatureAlgorithm(sha1ecc));
    	assertEquals("SHA224withECDSA", AlgorithmTools.getSignatureAlgorithm(sha224ecc));
    	assertEquals("SHA256withECDSA", AlgorithmTools.getSignatureAlgorithm(sha256ecc));
    	assertEquals("SHA384withECDSA", AlgorithmTools.getSignatureAlgorithm(sha384ecc));
        assertEquals("SHA512withECDSA", AlgorithmTools.getSignatureAlgorithm(sha512ecc));
        assertEquals("SHA3-256withECDSA", AlgorithmTools.getSignatureAlgorithm(sha3_256_ecc));
        assertEquals("SHA3-384withECDSA", AlgorithmTools.getSignatureAlgorithm(sha3_384_ecc));
        assertEquals("SHA3-512withECDSA", AlgorithmTools.getSignatureAlgorithm(sha3_512_ecc));

        // EdDSA
        keyPair = KeyTools.genKeys(null, AlgorithmConstants.KEYALGORITHM_ED25519);
        Certificate ed25519 = CertTools.genSelfCert("CN=TEST", 10L, null, keyPair.getPrivate(), keyPair.getPublic(), AlgorithmConstants.SIGALG_ED25519, true);
        assertEquals("Ed25519", CertTools.getCertSignatureAlgorithmNameAsString(ed25519));
        keyPair = KeyTools.genKeys(null, AlgorithmConstants.KEYALGORITHM_ED448);
        Certificate ed448 = CertTools.genSelfCert("CN=TEST", 10L, null, keyPair.getPrivate(), keyPair.getPublic(), AlgorithmConstants.SIGALG_ED448, true);
        assertEquals("Ed448", CertTools.getCertSignatureAlgorithmNameAsString(ed448));
        
    }

    @Test
    public void testCertSignatureAlgorithmAsStringGOST3410() throws Exception {
        assumeTrue(AlgorithmConfigurationCache.INSTANCE.isGost3410Enabled());
        KeyPair keyPair = KeyTools.genKeys("GostR3410-2001-CryptoPro-B", AlgorithmConstants.KEYALGORITHM_ECGOST3410);
        Certificate gost3411withgost3410 = CertTools.genSelfCert("CN=TEST", 10L, null, keyPair.getPrivate(), keyPair.getPublic(), AlgorithmConstants.SIGALG_GOST3411_WITH_ECGOST3410, true);
        assertEquals("GOST3411WITHECGOST3410", CertTools.getCertSignatureAlgorithmNameAsString(gost3411withgost3410));
        assertEquals("GOST3411withECGOST3410", AlgorithmTools.getSignatureAlgorithm(gost3411withgost3410));
    }

    @Test
    public void testCertSignatureAlgorithmAsStringDSTU4145() throws Exception {
        assumeTrue(AlgorithmConfigurationCache.INSTANCE.isDstu4145Enabled());
        KeyPair keyPair = KeyTools.genKeys("2.5", AlgorithmConstants.KEYALGORITHM_DSTU4145);
        Certificate gost3411withgost3410 = CertTools.genSelfCert("CN=TEST", 10L, null, keyPair.getPrivate(), keyPair.getPublic(), AlgorithmConstants.SIGALG_GOST3411_WITH_DSTU4145, true);
        assertEquals("GOST3411WITHDSTU4145", CertTools.getCertSignatureAlgorithmNameAsString(gost3411withgost3410));
        assertEquals("GOST3411withDSTU4145", AlgorithmTools.getSignatureAlgorithm(gost3411withgost3410));
    }

    @Test
    public void testGetWellKnownCurveOids() {
        // Extracted from debugger
        final String[] wellKnownCurveNames = new String[] { "secp224r1", "brainpoolp224t1", "c2pnb368w1", "sect409k1", "brainpoolp224r1",
                "c2tnb359v1", "sect233r1", "sect571k1", "c2pnb304w1", "brainpoolp512r1", "brainpoolp320r1", "brainpoolp512t1", "brainpoolp320t1",
                "secp256k1", "c2tnb239v3", "c2tnb239v2", "c2tnb239v1", "prime239v3", "prime239v2", "sect283k1", "sect409r1", "prime239v1",
                "prime256v1", "brainpoolp256t1", "sect283r1", "FRP256v1", "brainpoolp256r1", "secp384r1", "secp521r1", "brainpoolp384t1", "secp224k1",
                "c2tnb431r1", "brainpoolp384r1", "sect239k1", "c2pnb272w1", "sm2p256v1", "sect233k1", "sect571r1"
        };
        for (final String wellKnownCurveName : wellKnownCurveNames) {
            assertNotEquals("Could not retrieve OID for curve " + wellKnownCurveName, AlgorithmTools.getEcKeySpecOidFromBcName(wellKnownCurveName),
                    wellKnownCurveName);
            log.info("Successfully retrieved EC curve OID: " + AlgorithmTools.getEcKeySpecOidFromBcName(wellKnownCurveName));
        }
    }

    /** A simple test that just checks that we have items in EcCurvesMap, and can be used to 
     * (debug) print out for manual inspection.
     */
    @Test
    public void testGetNamedEcCurves() {
        final Map<String,List<String>> list = AlgorithmTools.getNamedEcCurvesMap(false);
        assertNotNull("getNamedEcCurvesMap can not be null", list);
        assertFalse("getNamedEcCurvesMap can not be empty", list.isEmpty());
        final Set<String> keySet = list.keySet();
        assertNotNull("getNamedEcCurvesMap keySet can not be null", keySet);
        assertFalse("getNamedEcCurvesMap keySet can not be empty", keySet.isEmpty());
        for (String name : keySet) {
            log.debug("testGetNamedEcCurves: " + name);
        }
    }
    
    private static class MockPublicKey implements PublicKey {
        private static final long serialVersionUID = 1L;
        @Override
        public String getAlgorithm() { return null; }
        @Override
        public byte[] getEncoded() { return null; }
        @Override
        public String getFormat() { return null; }
    }

    private static class MockNotSupportedPublicKey extends MockPublicKey {
        private static final long serialVersionUID = 1L;
    }

    private static class MockRSAPublicKey extends MockPublicKey implements RSAPublicKey {
        private static final long serialVersionUID = 1L;
        @Override
        public BigInteger getPublicExponent() { return BigInteger.valueOf(1); }
        @Override
        public BigInteger getModulus() { return BigInteger.valueOf(1000); }
    }

    private static class MockDSAPublicKey extends MockPublicKey implements DSAPublicKey {
        private static final long serialVersionUID = 1L;
        @Override
        public BigInteger getY() { return BigInteger.valueOf(1); }
        @Override
        public DSAParams getParams() { return null; }
    }

    private static class MockECDSAPublicKey extends MockPublicKey implements ECPublicKey {
        private static final long serialVersionUID = 1L;
        @Override
        public ECPoint getW() { return null; }
        @Override
        public ECParameterSpec getParams() { return null; }
        @Override
        public String getAlgorithm() {
            return "ECDSA mock";
        }
    }

    private static class MockGOST3410PublicKey extends MockPublicKey implements ECPublicKey {
        private static final long serialVersionUID = 1L;
        @Override
        public ECPoint getW() { return null; }
        @Override
        public ECParameterSpec getParams() { return null; }
        @Override
        public String getAlgorithm() {
            return "GOST mock";
        }
    }

    private static class MockDSTU4145PublicKey extends MockPublicKey implements ECPublicKey {
        private static final long serialVersionUID = 1L;
        @Override
        public ECPoint getW() { return null; }
        @Override
        public ECParameterSpec getParams() { return null; }
        @Override
        public String getAlgorithm() {
            return "DSTU mock";
        }
    }
}