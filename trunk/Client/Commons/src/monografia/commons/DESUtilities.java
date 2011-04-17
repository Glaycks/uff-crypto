package monografia.commons;

import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class DESUtilities {

    private static final String ALGORITHM = "DES";
    private static final String TRANSFORMATION = "DES/ECB/PKCS5Padding";
    private static final String KEY_STRING = "133457799BBCDFF1";
    private static final byte[] KEY = Utilities.hexStringToByteArray(DESUtilities.KEY_STRING);

    public static byte[] decripta(final byte[] texto) {

        try {

            // criando o objeto que ir� armazenar a chave de 8-bytes
            final KeySpec ks = new DESKeySpec(DESUtilities.KEY);
            // criando o objeto que inst�ncia o algoritmo DES, o modo e o padding
            final SecretKeyFactory kf = SecretKeyFactory.getInstance(DESUtilities.ALGORITHM);
            // convertendo o objeto da chave atrav�s do objeto do algoritmo
            final SecretKey ky = kf.generateSecret(ks);
            // criando o objeto que inst�ncia o algoritmo DES, o modo e o padding
            final Cipher cf = Cipher.getInstance(DESUtilities.TRANSFORMATION);
            // inicializando no modo "encripta��o", usando a chave
            cf.init(Cipher.DECRYPT_MODE, ky);
            // Cifrando os dados de entrada, transformando numa array de bytes e finalizando o m�todo
            final byte[] theCph = cf.doFinal(texto);

            return theCph;

        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] encripta(final byte[] texto) {

        try {

            // criando o objeto que ir� armazenar a chave de 8-bytes
            final KeySpec ks = new DESKeySpec(DESUtilities.KEY);
            // criando o objeto que inst�ncia o algoritmo DES, o modo e o padding
            final SecretKeyFactory kf = SecretKeyFactory.getInstance(DESUtilities.ALGORITHM);
            // convertendo o objeto da chave atrav�s do objeto do algoritmo
            final SecretKey ky = kf.generateSecret(ks);
            // criando o objeto que inst�ncia o algoritmo DES, o modo e o padding
            final Cipher cf = Cipher.getInstance(DESUtilities.TRANSFORMATION);
            // inicializando no modo "encripta��o", usando a chave
            cf.init(Cipher.ENCRYPT_MODE, ky);
            // Cifrando os dados de entrada, transformando numa array de bytes e finalizando o m�todo
            final byte[] theCph = cf.doFinal(texto);

            return theCph;

        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private DESUtilities() {
        // Apenas para prevenir instancia��o.
    }
}
