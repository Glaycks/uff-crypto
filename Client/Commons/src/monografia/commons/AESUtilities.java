package monografia.commons;

import java.math.BigInteger;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESUtilities {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String KEY_STRING = "ddafXA1afcf6b1cf";
    private static final String INITIAL_VECTOR_STRING = "a33dc00670g13edf";

    private static final byte[] key = new BigInteger(Utilities.strToHex(AESUtilities.KEY_STRING), 16).toByteArray();
    private static final byte[] initial_vector = new BigInteger(Utilities.strToHex(AESUtilities.INITIAL_VECTOR_STRING), 16).toByteArray();

    public static byte[] decripta(final byte[] bytesToEncrypt) throws Exception {
        final Cipher cipher = Cipher.getInstance(AESUtilities.TRANSFORMATION);
        final SecretKeySpec keySpec = new SecretKeySpec(AESUtilities.key, AESUtilities.ALGORITHM);
        final IvParameterSpec ivSpec = new IvParameterSpec(AESUtilities.initial_vector);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        return cipher.doFinal(bytesToEncrypt);
    }

    public static byte[] encripta(final byte[] bytesToEncrypt) throws Exception {
        final Cipher cipher = Cipher.getInstance(AESUtilities.TRANSFORMATION);
        final SecretKeySpec keySpec = new SecretKeySpec(AESUtilities.key, AESUtilities.ALGORITHM);
        final IvParameterSpec ivSpec = new IvParameterSpec(AESUtilities.initial_vector);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        return cipher.doFinal(bytesToEncrypt);
    }

    private AESUtilities() {
        // Apenas para prevenir instanciação.
    }

}
