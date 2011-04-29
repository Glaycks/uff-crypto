package monografia.commons;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import monografia.desempenho.ThreadTimes;

public class BlowFishUtilities {

	private static final String AGORITHM = "Blowfish";
	private static final String CHAVE = "MONOGRAFIA_MONOG";

	public static byte[] encripta(final byte[] bloco) {
		try {
			
			long id = java.lang.Thread.currentThread().getId();
			ThreadTimes tt = new ThreadTimes();

			SecretKeySpec key = new SecretKeySpec(CHAVE.getBytes(), AGORITHM);
			Cipher cipher = Cipher.getInstance(AGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] cripta = cipher.doFinal(bloco); 

            //	Métodos para cálculo do desempenho
			long taskUserTimeNano = tt.getUserTime(id);
            long taskSystemTimeNano = tt.getSystemTime(id);
            long taskCpuTimeNamo = tt.getCpuTime(id);
            
            System.out.println("taskUserTimeNano: " + taskUserTimeNano);
            System.out.println("taskSystemTimeNano: " + taskSystemTimeNano);
            System.out.println("taskCpuTimeNamo: " + taskCpuTimeNamo);

            return cripta;
            
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
