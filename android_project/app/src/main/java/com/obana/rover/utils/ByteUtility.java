package com.obana.rover.utils;

public class ByteUtility {
  static char[] hexDigit = new char[] { 
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 
      'A', 'B', 'C', 'D', 'E', 'F' };
  
  public static String byteArrayToHex(byte[] paramArrayOfbyte, int paramInt1, int paramInt2) {
    byte[] arrayOfByte = new byte[paramInt2];
    for (int i = 0;; i++) {
      if (i >= paramInt2)
        return bytesToHex(arrayOfByte); 
      arrayOfByte[i] = paramArrayOfbyte[i + paramInt1];
    } 
  }
  
  public static int byteArrayToInt(byte[] paramArrayOfbyte, int paramInt) throws Exception {
    return (paramArrayOfbyte[paramInt + 0] << 24) + ((paramArrayOfbyte[paramInt + 1] & 0xFF) << 16) + ((paramArrayOfbyte[paramInt + 2] & 0xFF) << 8) + (paramArrayOfbyte[paramInt + 3] & 0xFF);
  }
  
  public static int byteArrayToInt(byte[] paramArrayOfbyte, int paramInt1, int paramInt2) {
    int i = 0;
    for (int j = 0;; j++) {
      int k;
      if (j >= paramInt2)
        return i; 
      if (j == 0 && paramArrayOfbyte[paramInt2 - 1 + paramInt1 - j] < 0) {
        k = i | paramArrayOfbyte[paramInt2 - 1 + paramInt1 - j] & 0xFFFFFFFF;
      } else {
        k = i | paramArrayOfbyte[paramInt2 - 1 + paramInt1 - j] & 0xFF;
      } 
      i = k;
      if (j < paramInt2 - 1)
        i = k << 8; 
    } 
  }
  
  public static long byteArrayToLong(byte[] paramArrayOfbyte, int paramInt1, int paramInt2) {
    long l = 0L;
    for (int i = 0;; i++) {
      long l1;
      if (i >= paramInt2)
        return l; 
      if (i == 0 && paramArrayOfbyte[paramInt2 - 1 + paramInt1 - i] < 0) {
        l1 = l | (paramArrayOfbyte[paramInt2 - 1 + paramInt1 - i] & 0xFFFFFFFF);
      } else {
        l1 = l | (paramArrayOfbyte[paramInt2 - 1 + paramInt1 - i] & 0xFF);
      } 
      l = l1;
      if (i < paramInt2 - 1)
        l = l1 << 8L; 
    } 
  }
  
  public static String byteToHex(byte paramByte) {
    return new String(new char[] { hexDigit[paramByte >> 4 & 0xF], hexDigit[paramByte & 0xF] });
  }
  
  public static short byteToShort(byte[] paramArrayOfbyte) {
    return (short)((short)(paramArrayOfbyte[0] & 0xFF) | (short)((short)(paramArrayOfbyte[1] & 0xFF) << 8));
  }
  
  public static String bytesToHex(byte[] paramArrayOfbyte) {
    return bytesToHex(paramArrayOfbyte, 0, paramArrayOfbyte.length);
  }
  
  public static String bytesToHex(byte[] paramArrayOfbyte, int paramInt1, int paramInt2) {
    StringBuffer stringBuffer = new StringBuffer();
    for (int i = 0;; i++) {
      if (i >= paramInt2)
        return stringBuffer.toString(); 
      stringBuffer.append(byteToHex(paramArrayOfbyte[paramInt1 + i]));
    } 
  }
  
  public static short[] bytesToShorts(byte[] paramArrayOfbyte) {
    short[] arrayOfShort = new short[paramArrayOfbyte.length / 2];
    for (int i = 0;; i++) {
      if (i >= paramArrayOfbyte.length / 2)
        return arrayOfShort; 
      arrayOfShort[i] = (short)((short)(paramArrayOfbyte[i * 2] & 0xFF) | (short)((short)(paramArrayOfbyte[i * 2 + 1] & 0xFF) << 8));
    } 
  }
  
  public static String convertByteArrayToString(byte[] paramArrayOfbyte) {
    String str = "";
    for (int i = 0;; i++) {
      if (i >= paramArrayOfbyte.length)
        return str; 
      str = String.valueOf(str) + ":" + Byte.toString(paramArrayOfbyte[i]);
    } 
  }
}
