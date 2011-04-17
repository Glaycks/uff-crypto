package monografia.commons;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class DESedeUtilities {

	private static final String KEY_STRING = "HG58YZ3CR9";
	private static final String ALGORITHM = "DESede";
	private static final String TRANSFORMATION = "DESede/CBC/PKCS5Padding";
	private static final byte[] initial_vector = new byte[8];

	public static byte[] decripta(final byte[] bytesToEncrypt) throws Exception {
		final byte[] keyBytes = geraChave();
		final Cipher cipher = Cipher.getInstance(DESedeUtilities.TRANSFORMATION);
		final SecretKey keySpec = new SecretKeySpec(keyBytes, DESedeUtilities.ALGORITHM);
		final IvParameterSpec ivSpec = new IvParameterSpec(DESedeUtilities.initial_vector);
		cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
		return cipher.doFinal(bytesToEncrypt);
	}

	public static byte[] encripta(final byte[] bytesToEncrypt) throws Exception {
		final byte[] keyBytes = geraChave();
		final Cipher cipher = Cipher.getInstance(DESedeUtilities.TRANSFORMATION);
		final SecretKey keySpec = new SecretKeySpec(keyBytes, DESedeUtilities.ALGORITHM);
		final IvParameterSpec ivSpec = new IvParameterSpec(DESedeUtilities.initial_vector);
		cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
		return cipher.doFinal(bytesToEncrypt);
	}
	
	private static byte[] geraChave() throws NoSuchAlgorithmException, UnsupportedEncodingException {
		final MessageDigest md = MessageDigest.getInstance("md5");
		final byte[] digestOfPassword = md.digest(KEY_STRING.getBytes("utf-8"));
		final byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
		for (int j = 0,  k = 16; j < 8;){
			keyBytes[k++] = keyBytes[j++];
		}
		return keyBytes;
	}

	private DESedeUtilities() {
		// Apenas para prevenir instanciação.
	}

}
