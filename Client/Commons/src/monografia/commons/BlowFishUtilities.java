package monografia.commons;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class BlowFishUtilities {

	private static final String AGORITHM = "Blowfish";
	private static final String CHAVE = "MONOGRAFIA";

	public static byte[] encripta(final byte[] bloco) {
		try {
			SecretKeySpec key = new SecretKeySpec(CHAVE.getBytes(), AGORITHM);
			Cipher cipher = Cipher.getInstance(AGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, key);
			return cipher.doFinal(bloco);
		} catch (Exception e) { 
			return null; 
		}
	}

	public static byte[] decripta(final byte[] bloco) {
		try {
			SecretKeySpec key = new SecretKeySpec(CHAVE.getBytes(), AGORITHM);
			Cipher cipher = Cipher.getInstance(AGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, key);
			return cipher.doFinal(bloco);
		} catch (Exception e) { 
			return null; 
		}
	}
}
