package monografia.commons;

public class Utilities {

    private static final int KEY_SHIFT = 6;
	private static final String HEX_DIGITS = "0123456789abcdef";

    public static String byteArrayToHexString(final byte[] b) {
        final StringBuffer buf = new StringBuffer();

        for (int i = 0; i < b.length; i++) {
            final int j = b[i] & 0xFF;
            buf.append(Utilities.HEX_DIGITS.charAt(j / 16));
            buf.append(Utilities.HEX_DIGITS.charAt(j % 16));
        }

        return buf.toString();
    }

    public static byte[] hexStringToByteArray(final String hexa) throws IllegalArgumentException {

        // verifica se a String possui uma quantidade par de elementos
        if (hexa.length() % 2 != 0) {
            throw new IllegalArgumentException("String hexa inválida");
        }

        final byte[] b = new byte[hexa.length() / 2];

        for (int i = 0; i < hexa.length(); i += 2) {
            b[i / 2] = (byte) (Utilities.HEX_DIGITS.indexOf(hexa.charAt(i)) << 4 | Utilities.HEX_DIGITS.indexOf(hexa.charAt(i + 1)));
        }
        return b;
    }

    public static String strToHex(final String str) {
        final char[] chars = str.toCharArray();
        final StringBuffer hexStr = new StringBuffer();

        for (int i = 0; i < chars.length; i++) {
            hexStr.append(Integer.toHexString(chars[i]));
        }
        return hexStr.toString();
    }

    // ---------------------------------------------
    // XOR - Faz um deslocamento à esquerda de seis bits
    // ---------------------------------------------
    public static byte[] xor(final byte[] texto) {
        final byte[] e = new byte[texto.length];
        for (int i = 0; i < texto.length; i++) {
            e[i] = (byte) (texto[i] ^ (byte) KEY_SHIFT);
        }

        return e;
    }

    private Utilities() {
        // Apenas para prevenir instanciação.
    }
}
