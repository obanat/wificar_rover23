package com.wificar.external;

import java.io.UnsupportedEncodingException;

/* loaded from: classes.dex */
public class FSCoder {
    static final int Action = 1;
    static final int ArrayCountExtended = 11;
    public static final int BIG_ENDIAN = 1;
    static final int CodingError = 14;
    static final int DecodeActions = 18;
    static final int DecodeGlyphs = 20;
    static final int DecodeShapes = 19;
    static final int Delta = 13;
    static final int Empty = 4;
    static final int ExpectedLength = 17;
    static final int Identifier = 5;
    public static final int LITTLE_ENDIAN = 0;
    static final int NumberOfAdvanceBits = 8;
    static final int NumberOfFillBits = 6;
    static final int NumberOfGlyphBits = 9;
    static final int NumberOfLineBits = 7;
    static final int NumberOfShapeBits = 10;
    static final int StartOfError = 16;
    public static final int TransparentColors = 0;
    static final int Type = 3;
    static final int TypeInError = 15;
    public static final int Version = 2;
    static final int WideCodes = 12;
    private int byteOrder;
    int[] context;
    private byte[] data;
    String encoding;
    private int end;
    private int ptr;

    static int size(int value, boolean signed) {
        int mask = Integer.MIN_VALUE;
        if (signed) {
            if (value < 0) {
                value = -value;
            }
            int i = 32;
            while ((value & mask) == 0 && i > 0) {
                mask >>>= 1;
                i--;
            }
            if (i < 32) {
                int size = i + 1;
                return size;
            }
            int size2 = i;
            return size2;
        }
        int i2 = 32;
        while ((value & mask) == 0 && i2 > 0) {
            mask >>>= 1;
            i2--;
        }
        int size3 = i2;
        return size3;
    }

    static int size(int[] values, boolean signed) {
        int size = 0;
        for (int i : values) {
            size = Math.max(size, size(i, signed));
        }
        return size;
    }

    static int fixedShortSize(float aNumber) {
        float floatValue = aNumber * 256.0f;
        return size((int) floatValue, true);
    }

    static int fixedShortSize(float[] values) {
        int size = 0;
        for (float f : values) {
            size = Math.max(size, fixedShortSize(f));
        }
        return size;
    }

    static int fixedSize(float aNumber) {
        float floatValue = aNumber * 65536.0f;
        return size((int) floatValue, true);
    }

    static int fixedSize(float[] values) {
        int size = 0;
        for (float f : values) {
            size = Math.max(size, fixedSize(f));
        }
        return size;
    }

    static int strlen(String string, String encoding, boolean appendNull) {
        if (string != null) {
            try {
                int length = 0 + string.getBytes(encoding).length;
                return length + (appendNull ? 1 : 0);
            } catch (UnsupportedEncodingException e) {
                return 0;
            }
        }
        return 0;
    }

    static int strlen(String string, boolean appendNull) {
        if (string != null) {
            try {
                int length = 0 + string.getBytes("UTF8").length;
                return length + (appendNull ? 1 : 0);
            } catch (UnsupportedEncodingException e) {
                return 0;
            }
        }
        return 0;
    }

    public FSCoder(int order, int size) {
        this.encoding = "UTF8";
        this.byteOrder = 0;
        this.data = null;
        this.ptr = 0;
        this.end = 0;
        this.context = new int[21];
        clearContext();
        this.byteOrder = order;
        this.data = new byte[size];
        for (int i = 0; i < size; i++) {
            this.data[i] = 0;
        }
        this.ptr = 0;
        this.end = this.data.length << 3;
    }

    public FSCoder(int order, byte[] bytes) {
        this.encoding = "UTF8";
        this.byteOrder = 0;
        this.data = null;
        this.ptr = 0;
        this.end = 0;
        this.context = new int[21];
        clearContext();
        this.byteOrder = order;
        this.data = bytes;
        this.ptr = 0;
        this.end = this.data.length << 3;
    }

    public String getEncoding() {
        return this.encoding;
    }

    public void setEncoding(String enc) {
        this.encoding = enc;
    }

    public byte[] getData() {
        int length = (this.ptr + 7) >> 3;
        byte[] bytes = new byte[length];
        System.arraycopy(this.data, 0, bytes, 0, length);
        return bytes;
    }

    public void setData(int order, byte[] bytes) {
        this.byteOrder = order;
        this.data = new byte[bytes.length];
        System.arraycopy(bytes, 0, this.data, 0, bytes.length);
        this.ptr = 0;
        this.end = this.data.length << 3;
    }

    public void addCapacity(int size) {
        int length = (this.end >>> 3) + size;
        byte[] bytes = new byte[length];
        System.arraycopy(this.data, 0, bytes, 0, this.data.length);
        this.data = bytes;
        this.end = this.data.length << 3;
    }

    public int getCapacity() {
        return this.end >>> 3;
    }

    public int getPointer() {
        return this.ptr;
    }

    public void setPointer(int location) {
        if (location < 0 || location > this.end) {
            throw new IllegalArgumentException();
        }
        this.ptr = location;
    }

    public void adjustPointer(int offset) {
        this.ptr += offset;
        if (this.ptr < 0) {
            this.ptr = 0;
        } else if (this.ptr >= this.end) {
            this.ptr = this.end;
        }
    }

    public void alignToByte() {
        this.ptr = (this.ptr + 7) & (-8);
    }

    public boolean eof() {
        return this.ptr >= this.end;
    }

    public int readBits(int numberOfBits, boolean signed) {
        int value;
        int value2 = 0;
        if (numberOfBits < 0 || numberOfBits > 32) {
            throw new IllegalArgumentException("Number of bits must be in the range 1..32.");
        }
        if (numberOfBits == 0) {
            return 0;
        }
        int index = this.ptr >> 3;
        int base = this.data.length - index <= 4 ? (4 - (this.data.length - index)) * 8 : 0;
        int i = 32;
        while (i > base) {
            value2 |= (this.data[index] & 255) << (i - 8);
            i -= 8;
            index++;
        }
        int value3 = value2 << (this.ptr % 8);
        if (signed) {
            value = value3 >> (32 - numberOfBits);
        } else {
            value = value3 >>> (32 - numberOfBits);
        }
        this.ptr += numberOfBits;
        if (this.ptr > (this.data.length << 3)) {
            this.ptr = this.data.length << 3;
        }
        int base2 = value;
        return base2;
    }

    public void writeBits(int value, int numberOfBits) {
        if (numberOfBits < 0 || numberOfBits > 32) {
            throw new IllegalArgumentException("Number of bits must be in the range 1..32.");
        }
        if (this.ptr + 32 > this.end) {
            addCapacity((this.data.length / 2) + 4);
        }
        int index = this.ptr >> 3;
        int value2 = ((value << (32 - numberOfBits)) >>> (this.ptr % 8)) | (this.data[index] << 24);
        int i = 24;
        while (i >= 0) {
            this.data[index] = (byte) (value2 >>> i);
            i -= 8;
            index++;
        }
        this.ptr += numberOfBits;
        if (this.ptr > (this.data.length << 3)) {
            this.ptr = this.data.length << 3;
        }
    }

    public int readWord(int numberOfBytes, boolean signed) {
        int value = 0;
        if (numberOfBytes < 0 || numberOfBytes > 4) {
            throw new IllegalArgumentException("Number of bytes must be in the range 1..4.");
        }
        int index = this.ptr >> 3;
        if (index + numberOfBytes > this.data.length) {
            numberOfBytes = this.data.length - index;
        }
        int numberOfBits = numberOfBytes * 8;
        if (this.byteOrder == 0) {
            int i = 0;
            while (i < numberOfBits) {
                value += (this.data[index] & 255) << i;
                i += 8;
                this.ptr += 8;
                index++;
            }
        } else {
            int i2 = 0;
            while (i2 < numberOfBits) {
                value = (value << 8) + (this.data[index] & 255);
                i2 += 8;
                this.ptr += 8;
                index++;
            }
        }
        if (signed) {
            return (value << (32 - numberOfBits)) >> (32 - numberOfBits);
        }
        return value;
    }

    public void writeWord(int value, int numberOfBytes) {
        if (numberOfBytes < 0 || numberOfBytes > 4) {
            throw new IllegalArgumentException("Number of bytes must be in the range 1..4.");
        }
        int numberOfBits = numberOfBytes * 8;
        if (this.ptr + numberOfBits > this.end) {
            addCapacity((this.data.length / 2) + numberOfBytes);
        }
        if (this.byteOrder == 0) {
            int index = this.ptr >>> 3;
            int i = 0;
            while (i < numberOfBits) {
                this.data[index] = (byte) value;
                i += 8;
                this.ptr += 8;
                value >>>= 8;
                index++;
            }
            return;
        }
        int index2 = ((this.ptr + numberOfBits) - 8) >>> 3;
        int i2 = 0;
        while (i2 < numberOfBits) {
            this.data[index2] = (byte) value;
            i2 += 8;
            this.ptr += 8;
            value >>>= 8;
            index2--;
        }
    }

    public int readBytes(byte[] bytes) {
        int bytesRead = 0;
        if (bytes == null || bytes.length == 0) {
            return 0;
        }
        int index = this.ptr >>> 3;
        int numberOfBytes = bytes.length;
        if (index + numberOfBytes > this.data.length) {
            numberOfBytes = this.data.length - index;
        }
        int i = 0;
        while (i < numberOfBytes) {
            bytes[i] = this.data[index];
            i++;
            this.ptr += 8;
            index++;
            bytesRead++;
        }
        return bytesRead;
    }

    public int writeBytes(byte[] bytes) {
        int bytesWritten = 0;
        if (this.ptr + (bytes.length << 3) > this.end) {
            addCapacity((this.data.length / 2) + bytes.length);
        }
        if (bytes == null || bytes.length == 0) {
            return 0;
        }
        int index = this.ptr >>> 3;
        int numberOfBytes = bytes.length;
        int i = 0;
        while (i < numberOfBytes) {
            this.data[index] = bytes[i];
            i++;
            this.ptr += 8;
            index++;
            bytesWritten++;
        }
        return bytesWritten;
    }

    public int scanBits(int numberOfBits, boolean signed) {
        int start = this.ptr;
        int value = readBits(numberOfBits, signed);
        this.ptr = start;
        return value;
    }

    public int scanWord(int numberOfBytes, boolean signed) {
        int start = this.ptr;
        int value = readWord(numberOfBytes, signed);
        this.ptr = start;
        return value;
    }

    public float readFixedBits(int numberOfBits, int fractionSize) {
        float divisor = 1 << fractionSize;
        float value = readBits(numberOfBits, true) / divisor;
        return value;
    }

    public void writeFixedBits(float value, int numberOfBits, int fractionSize) {
        float multiplier = 1 << fractionSize;
        writeBits((int) (value * multiplier), numberOfBits);
    }

    public float readFixedWord(int mantissaSize, int fractionSize) {
        float divisor = 1 << (fractionSize * 8);
        int fraction = readWord(fractionSize, false);
        int mantissa = readWord(mantissaSize, true) << (fractionSize * 8);
        return (mantissa + fraction) / divisor;
    }

    public void writeFixedWord(float value, int mantissaSize, int fractionSize) {
        float multiplier = 1 << (fractionSize * 8);
        int fraction = (int) (value * multiplier);
        int mantissa = (int) value;
        writeWord(fraction, fractionSize);
        writeWord(mantissa, mantissaSize);
    }

    public double readDouble() {
        int upperInt = readWord(4, false);
        int lowerInt = readWord(4, false);
        long longValue = upperInt << 32;
        return Double.longBitsToDouble(longValue | (lowerInt & 4294967295L));
    }

    public void writeDouble(double value) {
        long longValue = Double.doubleToLongBits(value);
        int lowerInt = (int) longValue;
        int upperInt = (int) (longValue >>> 32);
        writeWord(upperInt, 4);
        writeWord(lowerInt, 4);
    }

    public String readString(int length) {
        return readString(length, this.encoding);
    }

    public String readString(int length, String enc) {
        if (length == 0) {
            return "";
        }
        byte[] str = new byte[length];
        int len = readBytes(str);
        try {
            return new String(str, 0, len, enc);
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    public String readString() {
        return readString(this.encoding);
    }

    public String readString(String enc) {
        String value;
        int start = this.ptr >> 3;
        int length = 0;
        while (start < this.data.length) {
            int start2 = start + 1;
            if (this.data[start] == 0) {
                break;
            }
            length++;
            start = start2;
        }
        byte[] str = new byte[length];
        int len = readBytes(str);
        try {
            value = new String(str, 0, len, enc);
        } catch (UnsupportedEncodingException e) {
            value = "";
        }
        readWord(1, false);
        int i = len + 1;
        return value;
    }

    public int writeString(String str) {
        return writeString(str, this.encoding);
    }

    public int writeString(String str, String enc) {
        try {
            int bytesWritten = writeBytes(str.getBytes(enc));
            return bytesWritten;
        } catch (UnsupportedEncodingException e) {
            return 0;
        }
    }

    public boolean findBits(int value, int numberOfBits, int step) {
        boolean found = false;
        int start = this.ptr;
        while (true) {
            if (this.ptr >= this.end) {
                break;
            } else if (scanBits(numberOfBits, false) != value) {
                this.ptr += step;
            } else {
                found = true;
                break;
            }
        }
        if (!found) {
            this.ptr = start;
        }
        return found;
    }

    public boolean findWord(int value, int numberOfBytes, int step) {
        while (this.ptr < this.end) {
            if (scanWord(numberOfBytes, false) != value) {
                this.ptr += step;
            } else {
                return true;
            }
        }
        return false;
    }

    private void clearContext() {
        for (int i = 0; i < this.context.length; i++) {
            this.context[i] = 0;
        }
    }

    public int getContext(int key) {
        return this.context[key];
    }

    public void setContext(int key, int value) {
        this.context[key] = value;
    }
}
