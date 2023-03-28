package com.wificar.util;

/* loaded from: classes.dex */
public class ByteUtility {
    static char[] hexDigit = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static String convertByteArrayToString(byte[] bytes) {
        String temp = "";
        for (int i = 0; i < bytes.length; i++) {
            temp = String.valueOf(temp) + ":" + Byte.toString(bytes[i]);
        }
        return temp;
    }

    public static String bytesToHex(byte[] b) {
        return bytesToHex(b, 0, b.length);
    }

    public static String bytesToHex(byte[] b, int off, int len) {
        StringBuffer buf = new StringBuffer();
        for (int j = 0; j < len; j++) {
            buf.append(byteToHex(b[off + j]));
        }
        return buf.toString();
    }

    public static String byteToHex(byte b) {
        char[] a = {hexDigit[(b >> 4) & 15], hexDigit[b & 15]};
        return new String(a);
    }

    public static String byteArrayToHex(byte[] inByteArray, int iOffset, int iLen) {
        byte[] ch = new byte[iLen];
        for (int x = 0; x < iLen; x++) {
            ch[x] = inByteArray[x + iOffset];
        }
        return bytesToHex(ch);
    }

    public static short byteToShort(byte[] b) {
        short s0 = (short) (b[0] & 255);
        short s1 = (short) (b[1] & 255);
        short s = (short) (s0 | ((short) (s1 << 8)));
        return s;
    }

    public static short[] bytesToShorts(byte[] b) {
        short[] s = new short[b.length / 2];
        for (int i = 0; i < b.length / 2; i++) {
            short s0 = (short) (b[i * 2] & 255);
            short s1 = (short) (b[(i * 2) + 1] & 255);
            s[i] = (short) (s0 | ((short) (s1 << 8)));
        }
        return s;
    }

    public static int byteArrayToInt(byte[] inByteArray, int iOffset, int iLen) {
        int iResult = 0;
        for (int x = 0; x < iLen; x++) {
            if (x == 0 && inByteArray[((iLen - 1) + iOffset) - x] < 0) {
                iResult |= inByteArray[((iLen - 1) + iOffset) - x] & (-1);
            } else {
                iResult |= inByteArray[((iLen - 1) + iOffset) - x] & 255;
            }
            if (x < iLen - 1) {
                iResult <<= 8;
            }
        }
        return iResult;
    }

    public static int byteArrayToInt(byte[] b, int offset) throws Exception {
        return (b[offset + 0] << 24) + ((b[offset + 1] & 255) << 16) + ((b[offset + 2] & 255) << 8) + (b[offset + 3] & 255);
    }

    public static long byteArrayToLong(byte[] inByteArray, int iOffset, int iLen) {
        long iResult = 0;
        for (int x = 0; x < iLen; x++) {
            if (x == 0 && inByteArray[((iLen - 1) + iOffset) - x] < 0) {
                iResult |= inByteArray[((iLen - 1) + iOffset) - x] & (-1);
            } else {
                iResult |= inByteArray[((iLen - 1) + iOffset) - x] & 255;
            }
            if (x < iLen - 1) {
                iResult <<= 8;
            }
        }
        return iResult;
    }
}
