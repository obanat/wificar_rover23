package com.wificar.external;

/* loaded from: classes.dex */
public final class ADPCM {
    private static final int[] StepSize = {7, 8, 9, 10, 11, 12, 13, 14, 16, 17, 19, 21, 23, 25, 28, 31, 34, 37, 41, 45, 50, 55, 60, 66, 73, 80, 88, 97, 107, 118, 130, 143, 157, 173, 190, 209, 230, 253, 279, 307, 337, 371, 408, 449, 494, 544, 598, 658, 724, 796, 876, 963, 1060, 1166, 1282, 1411, 1552, 1707, 1878, 2066, 2272, 2499, 2749, 3024, 3327, 3660, 4026, 4428, 4871, 5358, 5894, 6484, 7132, 7845, 8630, 9493, 10442, 11487, 12635, 13899, 15289, 16818, 18500, 20350, 22385, 24623, 27086, 29794, 32767};
    private static final int[][] DeltaTable = {new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, new int[]{-1, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, new int[]{-1, -1, 2, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, new int[]{-1, -1, -1, -1, 2, 4, 6, 8, -1, -1, -1, -1, -1, -1, -1, -1}, new int[]{-1, -1, -1, -1, -1, -1, -1, -1, 1, 2, 4, 6, 8, 10, 13, 16}};

    public static byte[] compress(byte[] sound, int numberOfChannels, int sampleSize, int compressedSize) {
        int numberOfSamples = sound.length / sampleSize;
        int samplesPerChannel = numberOfSamples / numberOfChannels;
        int numberOfFrames = (samplesPerChannel + 4095) / 4096;
        int frameSize = ((4096 * compressedSize) + 22) * numberOfChannels;
        int i = (((samplesPerChannel % 4096) * compressedSize) + 22) * numberOfChannels;
        int bytesPerFrame = (frameSize + 7) >> 3;
        byte[] out = new byte[numberOfFrames * bytesPerFrame];
        int[] samples = new int[numberOfSamples];
        FSCoder soundIn = new FSCoder(0, sound);
        for (int i2 = 0; i2 < numberOfSamples; i2++) {
            samples[i2] = soundIn.readWord(sampleSize * 8, true);
        }
        int[] value = new int[numberOfChannels];
        int[] tableIndex = new int[numberOfChannels];
        int[] step = new int[numberOfChannels];
        for (int chan = 0; chan < numberOfChannels; chan++) {
            value[chan] = 0;
            tableIndex[chan] = 0;
            step[chan] = 0;
        }
        int currentSample = 0;
        FSCoder compressedData = new FSCoder(0, new byte[numberOfSamples * sampleSize]);
        compressedData.writeBits(compressedSize - 2, 2);
        for (int i3 = 0; i3 < samplesPerChannel - 1; i3++) {
            if (i3 % 4096 == 0) {
                compressedData.alignToByte();
                int chan2 = 0;
                while (chan2 < numberOfChannels) {
                    value[chan2] = samples[currentSample];
                    int diff = Math.abs(samples[currentSample + numberOfChannels] - value[chan2]);
                    int index = 0;
                    while (StepSize[index] < diff && index < 63) {
                        index++;
                    }
                    tableIndex[chan2] = index;
                    step[chan2] = StepSize[index];
                    compressedData.writeBits(value[chan2], 16);
                    compressedData.writeBits(tableIndex[chan2], 6);
                    chan2++;
                    currentSample++;
                }
            } else {
                int chan3 = 0;
                while (chan3 < numberOfChannels) {
                    int diff2 = samples[currentSample] - value[chan3];
                    int sign = diff2 < 0 ? 1 << (compressedSize - 1) : 0;
                    if (sign > 0) {
                        diff2 = -diff2;
                    }
                    int delta = 0;
                    int vpdiff = step[chan3] >> (compressedSize - 1);
                    int j = compressedSize - 2;
                    while (j >= 0) {
                        if (diff2 >= step[chan3]) {
                            delta |= 1 << j;
                            vpdiff += step[chan3];
                            if (j > 0) {
                                diff2 -= step[chan3];
                            }
                        }
                        j--;
                        step[chan3] = step[chan3] >> 1;
                    }
                    if (sign > 0) {
                        value[chan3] = value[chan3] - vpdiff;
                    } else {
                        value[chan3] = value[chan3] + vpdiff;
                    }
                    if (value[chan3] > 32767) {
                        value[chan3] = 32767;
                    }
                    if (value[chan3] < -32768) {
                        value[chan3] = -32768;
                    }
                    tableIndex[chan3] = tableIndex[chan3] + DeltaTable[compressedSize][delta];
                    if (tableIndex[chan3] < 0) {
                        tableIndex[chan3] = 0;
                    }
                    if (tableIndex[chan3] > 88) {
                        tableIndex[chan3] = 88;
                    }
                    step[chan3] = StepSize[tableIndex[chan3]];
                    compressedData.writeBits(delta | sign, compressedSize);
                    chan3++;
                    currentSample++;
                }
            }
        }
        int compressedLength = compressedData.getPointer() / 8;
        for (int i4 = 0; i4 < compressedLength; i4++) {
            out[i4] = compressedData.getData()[i4];
        }
        return out;
    }
}
