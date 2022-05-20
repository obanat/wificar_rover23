package com.obana.rover.utils;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import com.obana.rover.WifiCar;

//import org.apache.http.util.ByteArrayBuffer;

// Referenced classes of package com.wificar.component:
//            TalkData, WifiCar, AudioData, AudioComponent, 
//            VideoData

public class CommandEncoder
{
    public static final int AUDIO_DATA = 2;
    public static final int AUDIO_END = 10;
    public static final int AUDIO_START_REQ = 8;
    public static final int AUDIO_START_RESP = 9;
    public static final int DECODER_CONTROL_REQ = 14;
    public static final int DEVICE_CONTROL_REQ = 250;
    public static final int FETCH_BATTERY_POWER_REQ = 251;
    public static final int FETCH_BATTERY_POWER_RESP = 252;
    public static final int HEAD_LEN = 23;
    public static final int KEEP_ALIVE = 255;
    public static final int LOGIN_REQ = 0;
    public static final int LOGIN_RESP = 1;
    public static final int MEDIA_LOGIN_REQ = 0;
    public static final int TALK_DATA = 3;
    public static final int TALK_END = 13;
    public static final int TALK_START_REQ = 11;
    public static final int TALK_START_RESP = 12;
    public static final int VERIFY_REQ = 2;
    public static final int VERIFY_RESP = 3;
    public static final int VIDEO_DATA = 1;
    public static final int VIDEO_END = 6;
    public static final int VIDEO_FRAMEINTERVAL = 7;
    public static final int VIDEO_START_REQ = 4;
    public static final int VIDEO_START_RESP = 5;
    public static final String WIFICAR_OP = "MO_O";
    public static final String WIFICAR_VIDEO_OP = "MO_V";
    private static final String TAG = "WifiCar_CommandEncoder";

    static class Protocol
    {

        public byte[] getContent()
        {
            return content;
        }

        public int getOp()
        {
            return op;
        }

        public byte[] output()
            throws IOException
        {
            int i = content.length;
            byte abyte0[] = CommandEncoder.int16ToByteArray(op);
            byte abyte1[] = CommandEncoder.int32ToByteArray(content.length);
            ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
            bytearrayoutputstream.write(header);
            bytearrayoutputstream.write(abyte0);
            bytearrayoutputstream.write(new byte[1]);
            bytearrayoutputstream.write(new byte[8]);
            bytearrayoutputstream.write(abyte1);
            bytearrayoutputstream.write(new byte[4]);
            bytearrayoutputstream.write(content);
            return bytearrayoutputstream.toByteArray();
        }

        byte content[];
        int contentLength;
        byte header[];
        int op;
        byte preserve1;
        byte preserve2[];
        long preserve3;

        public Protocol(byte abyte0[])
        {
            this(abyte0, 0);
        }

        public Protocol(byte abyte0[], int i)
        {
            op = 0;
            preserve1 = 0;
            preserve2 = new byte[8];
            contentLength = 0;
            preserve3 = 0L;
            content = new byte[0];
            header = "MO_V".getBytes();
            op = CommandEncoder.byteArrayToInt(abyte0, i + 4, 2);
            contentLength = CommandEncoder.byteArrayToInt(abyte0, i + 15, 4);
            if(contentLength > 0)
            {
                content = new byte[contentLength];
                System.arraycopy(abyte0, i + 23, content, 0, contentLength);
            }
        }

        public Protocol(byte abyte0[], int i, int j, byte abyte1[])
        {
            op = 0;
            preserve1 = 0;
            preserve2 = new byte[8];
            contentLength = 0;
            preserve3 = 0L;
            content = new byte[0];
            header = abyte0;
            op = i;
            contentLength = j;
            content = abyte1;
        }
    }


    public CommandEncoder()
    {
    }

    public static String byteArrayToHex(byte abyte0[], int i, int j)
    {
        byte abyte1[] = new byte[j];
        int k = 0;
        do
        {
            if(k >= j)
                return ByteUtility.bytesToHex(abyte1);
            abyte1[k] = abyte0[k + i];
            k++;
        } while(true);
    }

    public static int byteArrayToInt(byte abyte0[], int i)
    {
        return (abyte0[i + 0] << 24) + ((abyte0[i + 1] & 0xff) << 16) + ((abyte0[i + 2] & 0xff) << 8) + (abyte0[i + 3] & 0xff);
    }

    public static int byteArrayToInt(byte abyte0[], int i, int j)
    {
        int k = 0;
        int i1 = 0;
        do
        {
            if(i1 >= j)
                return k;
            int l;
            if(i1 == 0 && abyte0[((j - 1) + i) - i1] < 0)
                l = k | abyte0[((j - 1) + i) - i1] & -1;
            else
                l = k | abyte0[((j - 1) + i) - i1] & 0xff;
            k = l;
            if(i1 < j - 1)
                k = l << 8;
            i1++;
        } while(true);
    }

    public static long byteArrayToLong(byte abyte0[], int i, int j)
    {
        long l1 = 0L;
        int k = 0;
        do
        {
            if(k >= j)
                return l1;
            long l;
            if(k == 0 && abyte0[((j - 1) + i) - k] < 0)
                l = l1 | (long)(abyte0[((j - 1) + i) - k] & -1);
            else
                l = l1 | (long)(abyte0[((j - 1) + i) - k] & 0xff);
            l1 = l;
            if(k < j - 1)
                l1 = l << 8;
            k++;
        } while(true);
    }

    public static String byteArrayToString(byte abyte0[], int i, int j)
    {
        byte abyte1[] = new byte[j];
        int k = 0;
        do
        {
            if(k >= j)
                return (new String(abyte1)).trim();
            abyte1[k] = abyte0[k + i];
            k++;
        } while(true);
    }

    public static byte[] cmdAudioEnd()
        throws IOException
    {
        return (new Protocol("MO_O".getBytes(), 10, 0, new byte[0])).output();
    }

    public static byte[] cmdAudioStartReq()
        throws IOException
    {
        ByteBuffer bytebuffer = ByteBuffer.allocate(1);
        bytebuffer.put(int8ToByteArray(1));
        return (new Protocol("MO_O".getBytes(), 8, 1, bytebuffer.array())).output();
    }

    public static byte[] cmdDataLoginReq(int i)
        throws IOException
    {
        ByteBuffer bytebuffer = ByteBuffer.allocate(4);
        return (new Protocol("MO_V".getBytes(), 0, bytebuffer.capacity(), bytebuffer.array())).output();
    }

    public static byte[] cmdDecoderControlReq(int i)
        throws IOException
    {
        ByteBuffer bytebuffer = ByteBuffer.allocate(1);
        bytebuffer.put(int8ToByteArray(i));
        return (new Protocol("MO_O".getBytes(), 14, 1, bytebuffer.array())).output();
    }

    public static byte[] cmdDeviceControlReq(int i, int j)
        throws IOException
    {
        ByteBuffer bytebuffer = ByteBuffer.allocate(2);
        bytebuffer.put(int8ToByteArray(i));
        bytebuffer.put(int8ToByteArray(j));
        return (new Protocol("MO_O".getBytes(), 250, 2, bytebuffer.array())).output();
    }

    public static byte[] cmdFetchBatteryPowerReq()
        throws IOException
    {
        return (new Protocol("MO_O".getBytes(), 251, 0, new byte[0])).output();
    }

    public static byte[] cmdKeepAlive()
        throws IOException
    {
        return (new Protocol("MO_O".getBytes(), 255, 0, new byte[0])).output();
    }

    public static byte[] cmdLoginReq(int i, int j, int k, int l)
        throws IOException
    {
        byte abyte0[] = int32ToByteArray(i);
        byte abyte1[] = int32ToByteArray(j);
        byte abyte2[] = int32ToByteArray(k);
        byte abyte3[] = int32ToByteArray(l);
        ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
        bytearrayoutputstream.write(abyte0);
        bytearrayoutputstream.write(abyte1);
        bytearrayoutputstream.write(abyte2);
        bytearrayoutputstream.write(abyte3);
        return (new Protocol("MO_O".getBytes(), 0, 16, bytearrayoutputstream.toByteArray())).output();
    }

    public static byte[] cmdMediaLoginReq(int i)
        throws IOException
    {
        byte abyte0[] = int32ToByteArray(i);
        ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
        bytearrayoutputstream.write(abyte0);
        return (new Protocol("MO_V".getBytes(), 0, bytearrayoutputstream.size(), bytearrayoutputstream.toByteArray())).output();
    }

    public static byte[] cmdTalkEnd()
        throws IOException
    {
        return (new Protocol("MO_O".getBytes(), 13, 1, new byte[0])).output();
    }

    public static byte[] cmdTalkStartReq(int i)
        throws IOException
    {
        ByteBuffer bytebuffer = ByteBuffer.allocate(1);
        bytebuffer.put(int8ToByteArray(i));
        return (new Protocol("MO_O".getBytes(), 11, 1, bytebuffer.array())).output();
    }

    public static byte[] cmdVerifyReq(String s, int i, int j, int k, int l)
        throws IOException
    {
        AppLog.d(TAG, "cmdVerifyReq");
        BlowFish blowfish = new BlowFish();
        int ai[] = new int[1];
        ai[0] = i;
        int ai1[] = new int[1];
        ai1[0] = j;
        int ai2[] = new int[1];
        ai2[0] = k;
        int ai3[] = new int[1];
        ai3[0] = l;
        blowfish.InitBlowfish(s.getBytes(), s.length());
        blowfish.Blowfish_encipher(ai, ai1);
        blowfish.Blowfish_encipher(ai2, ai3);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(int32ToByteArray(ai[0]));
        out.write(int32ToByteArray(ai1[0]));
        out.write(int32ToByteArray(ai2[0]));
        out.write(int32ToByteArray(ai3[0]));
        Protocol ret = new Protocol("MO_O".getBytes(), 2, out.size(), out.toByteArray());
        AppLog.d(TAG, "============================verify");
        return ret.output();
    }

    public static byte[] cmdVideoEnd()
        throws IOException
    {
        return (new Protocol("MO_O".getBytes(), 6, 0, new byte[0])).output();
    }

    public static byte[] cmdVideoFrameInterval(int i)
        throws IOException
    {
        ByteBuffer bytebuffer = ByteBuffer.allocate(4);
        bytebuffer.put(int32ToByteArray(1));
        return (new Protocol("MO_O".getBytes(), 7, bytebuffer.capacity(), bytebuffer.array())).output();
    }

    public static byte[] cmdVideoStartReq()
        throws IOException
    {
        ByteBuffer bytebuffer = ByteBuffer.allocate(4);
        bytebuffer.put(int8ToByteArray(1));
        return (new Protocol("MO_O".getBytes(), 4, 1, bytebuffer.array())).output();
    }

    /*public static Protocol createTalkData(TalkData talkdata)
        throws IOException
    {
        ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
        byte abyte0[] = int32ToByteArray(talkdata.getTicktime());
        byte abyte1[] = int32ToByteArray(talkdata.getSerial());
        byte abyte2[] = int32ToByteArray(talkdata.getTimestamp());
        byte abyte3[] = int8ToByteArray(0);
        byte abyte4[] = int32ToByteArray(talkdata.getData().length);
        bytearrayoutputstream.write(abyte0);
        bytearrayoutputstream.write(abyte1);
        bytearrayoutputstream.write(abyte2);
        bytearrayoutputstream.write(abyte3);
        bytearrayoutputstream.write(abyte4);
        bytearrayoutputstream.write(talkdata.getData());
        return new Protocol("MO_V".getBytes(), 3, bytearrayoutputstream.size(), bytearrayoutputstream.toByteArray());
    }*/

    public static int getPrefixCount(byte abyte0[], int i)
    {
        int j = 0;
        do
        {
            if(i >= abyte0.length - 4)
                return j;
            int k = j;
            if(abyte0[i] == 77)
            {
                k = j;
                if(abyte0[i + 1] == 79)
                {
                    k = j;
                    if(abyte0[i + 2] == 95)
                    {
                        k = j;
                        if(abyte0[i + 3] == 86)
                            k = j + 1;
                    }
                }
            }
            i++;
            j = k;
        } while(true);
    }



    public static byte[] int16ToByteArray(int i)
    {
        byte abyte0[] = new byte[2];
        int j = 0;
        do
        {
            if(j >= 2)
                return abyte0;
            abyte0[j] = (byte)(i >>> j * 8 & 0xff);
            j++;
        } while(true);
    }

    public static byte[] int32ToByteArray(int i)
    {
        byte abyte0[] = new byte[4];
        int j = 0;
        do
        {
            if(j >= 4)
                return abyte0;
            abyte0[j] = (byte)(i >>> j * 8 & 0xff);
            j++;
        } while(true);
    }

    public static byte[] int32ToByteArrayR(int i)
    {
        return (new byte[] {
            (byte)(i >> 24 & 0xff), (byte)(i >> 16 & 0xff), (byte)(i >> 8 & 0xff), (byte)(i >> 0 & 0xff)
        });
    }

    public static String int32ToByteHex(int i)
    {
        byte abyte0[] = new byte[4];
        int j = 0;
        do
        {
            if(j >= 4)
                return ByteUtility.bytesToHex(abyte0);
            abyte0[j] = (byte)(i >>> j * 8 & 0xff);
            j++;
        } while(true);
    }

    public static String int32ToByteHexR(int i)
    {
        return ByteUtility.bytesToHex(new byte[] {
            (byte)(i >> 24 & 0xff), (byte)(i >> 16 & 0xff), (byte)(i >> 8 & 0xff), (byte)(i >> 0 & 0xff)
        });
    }

    public static byte[] int8ToByteArray(int i)
    {
        byte abyte0[] = new byte[1];
        int j = 0;
        do
        {
            if(j >= 1)
                return abyte0;
            abyte0[j] = (byte)(i >>> j * 8 & 0xff);
            j++;
        } while(true);
    }

    public static byte[] longToByteArray(long l)
    {
        return (new byte[] {
            (byte)(int)(l >> 56 & 255L), (byte)(int)(l >> 48 & 255L), (byte)(int)(l >> 40 & 255L), (byte)(int)(l >> 32 & 255L), (byte)(int)(l >> 24 & 255L), (byte)(int)(l >> 16 & 255L), (byte)(int)(l >> 8 & 255L), (byte)(int)(l >> 0 & 255L)
        });
    }

    public static void parseAudioData(WifiCar wificar, byte abyte0[])
    {
        //wificar.enableAudioFlag();
        byte abyte1[] = (new Protocol(abyte0, 0)).getContent();
        int i = byteArrayToInt(abyte1, 0, 4);
        int j = byteArrayToInt(abyte1, 4, 4);
        int k = byteArrayToInt(abyte1, 8, 4);
        int l = byteArrayToInt(abyte1, 12, 1);
        int j1 = byteArrayToInt(abyte1, 13, 4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(abyte1, 17, j1);
        int i1 = byteArrayToInt(abyte1, j1 + 17, 2);
        j1 = byteArrayToInt(abyte1, j1 + 19, 1);
        /*AudioData audiodata = new AudioData(i, j, k, l, abyte0.toByteArray(), i1, j1);
        audiodata.setPCMData(AudioComponent.decodeADPCMToPCM(abyte0.toByteArray(), abyte0.size(), i1, j1));
        try
        {
            wificar.appendAudioDataToFlim(audiodata);
            return;
        }
        // Misplaced declaration of an exception variable
        catch(WifiCar wificar)
        {
            wificar.printStackTrace();
        }*/
    }

    public static void parseAudioStartResp(WifiCar wificar, byte abyte0[], int i)
    {
        if(byteArrayToInt(abyte0, 0, 2) == 0)
        {
            //wificar = WificarActivity.getInstance();
            //WificarActivity.getInstance();
            //wificar.sendMessage(8701);
        }
    }

    public static ByteArrayOutputStream parseCommand(WifiCar wificar, ByteArrayOutputStream bytearraybuffer)
        throws IOException
    {
        byte abyte0[] = bytearraybuffer.toByteArray();
        if(abyte0.length <= 23) return bytearraybuffer;

        int i;
        ByteUtility.byteArrayToInt(abyte0, 4, 2);
        i = ByteUtility.byteArrayToInt(abyte0, 15, 4);
        if(abyte0.length >= i + 23) return bytearraybuffer;

        i += 23;
        Protocol protocol = new Protocol(abyte0, 0);
        bytearraybuffer.reset();
        bytearraybuffer.write(abyte0, i, abyte0.length - i);
        switch(protocol.getOp())
        {
        default:
            return bytearraybuffer;

        case 1: // '\001'
            boolean flag = parseLoginResp(wificar, protocol.getContent(), 1);
            AppLog.d(TAG, (new StringBuilder("--->login:")).append(flag).toString());
            return bytearraybuffer;

        case 3: // '\003'
            int j = parseVerifyResp(wificar, protocol.getContent(), 1);
            AppLog.d(TAG, (new StringBuilder("--->VERIFY_RESP:")).append(j).toString());
            return bytearraybuffer;

        case 5: // '\005'
            parseVideoStartResp(wificar, protocol.getContent(), 1);
            return bytearraybuffer;

        case 9: // '\t'
            parseAudioStartResp(wificar, protocol.getContent(), 1);
            return bytearraybuffer;

        case 12: // '\f'
            parseTalkStartResp(wificar, protocol.getContent(), 1);
            return bytearraybuffer;

        case 252: 
            parseFetchBatteryPowerResp(wificar, protocol.getContent(), 1);
            return bytearraybuffer;
        }
        //return bytearraybuffer;
    }

    public static byte[] parseFetchBatteryPowerResp(WifiCar wificar, byte abyte0[], int i)
        throws IOException
    {
        Protocol ret;
        if(i == 0)
            ret = new Protocol(abyte0, 0);
        else
            ret = new Protocol("MO_O".getBytes(), 252, abyte0.length, abyte0);
        byteArrayToInt(ret.getContent(), 0, 1);
        return ret.output();
    }

    public static boolean parseLoginResp(WifiCar wificar, byte abyte0[], int i)
    {
        if(i != 0) return false;
        
        Protocol prot = new Protocol(abyte0, 0);

        abyte0 = prot.getContent();
        if(byteArrayToInt(abyte0, 0, 2) != 0) return false;

        String s;
        int obj[];
        s = byteArrayToString(abyte0, 2, 13);
        obj = new int[4];
        i = 0;

        if(i < 4) return false;


        int[] L1 = new int[1];
        int[] R1 = new int[1];
        int[] L2 = new int[1];
        int[] R2 = new int[1];

        //wificar.setDeviceId((new StringBuilder(String.valueOf(obj[0]))).append(".").append(obj[1]).append(".").append(obj[2]).append(".").append(obj[3]).toString());
        wificar.setCameraId(s);

        L1[0] = byteArrayToInt(abyte0, 43, 4);
        R1[0] = byteArrayToInt(abyte0, 47, 4);
        L2[0] = byteArrayToInt(abyte0, 51, 4);
        R2[0] = byteArrayToInt(abyte0, 55, 4);
        //wificar.setChallengeReverse(0, i1);
        //wificar.setChallengeReverse(1, j1);
        //wificar.setChallengeReverse(2, k1);
        //wificar.setChallengeReverse(3, l1);
        BlowFish bf = new BlowFish();
        bf.InitBlowfish(wificar.getKey().getBytes(), wificar.getKey().length());


        int[] ret = bf.Blowfish_encipher(L1, R1);
        L1[0] = ret[0]; R1[0] = ret[1];
        ret = bf.Blowfish_encipher(L2, R2);
        L2[0] = ret[0]; R2[0] = ret[1];

        AppLog.d(TAG, "--->===============================");
        wificar.verifyCommand(L1[0],R1[0],L2[0],R2[0]);
        return true;

    }

    public static ByteArrayOutputStream parseMediaCommand(WifiCar wificar, ByteArrayOutputStream bytearraybuffer, int i)
        throws IOException
    {/*
        int j;
        int k;
        int l;
        byte abyte0[];
        abyte0 = bytearraybuffer.toByteArray();
        j = 0;
        k = 0;
        if(0 < 0)
            return bytearraybuffer;
        l = 0;
_L6:
        if(j >= 0) goto _L2; else goto _L1
_L1:
        bytearraybuffer.clear();
        bytearraybuffer.append(abyte0, k, abyte0.length - k);
        return bytearraybuffer;
_L2:
        int j1 = l + 1;
        if(abyte0.length - j < 23) goto _L1; else goto _L3
_L3:
        ByteUtility.byteArrayToInt(abyte0, j + 4, 2);
        l = ByteUtility.byteArrayToInt(abyte0, j + 15, 4);
        if(abyte0.length - j < l + 23) goto _L1; else goto _L4
_L4:
        Protocol protocol;
        if(i < 20000 && bytearraybuffer.length() < 0x10000)
        {
            protocol = new Protocol(abyte0, j);
            if(protocol != null)
            {
                switch(protocol.getOp())
                {
                default:
                    break;

                case 2: // '\002'
                    break; /* Loop/switch isn't completed 

                case 1: // '\001'
                    break;
                }
                break MISSING_BLOCK_LABEL_213;
            }
        }
_L7:
        int i1 = j + (l + 23);
        l = j1;
        j = i1;
        if(i1 >= 0)
        {
            k = i1;
            l = j1;
            j = i1;
        }
        if(true) goto _L6; else goto _L5
_L5:
        Log.e("media", "audio data");
        parseAudioData(wificar, protocol.output());
          goto _L7
        parseVideoData(wificar, protocol.output());
          goto _L7*/
          return null;
    }

    public static void parseTalkStartResp(WifiCar wificar, byte abyte0[], int i)
    {
        i = byteArrayToInt(abyte0, 0, 2);
        if(i == 0 && abyte0.length > 2)
            byteArrayToInt(abyte0, 2, 4);
        /*if(i == 0)
            wificar.getAudioComponent().startRecord();*/
    }

    public static int parseVerifyResp(WifiCar wificar, byte abyte0[], int i)
    {
        AppLog.d(TAG, "--->cmdVerifyResp");
        Protocol prot;
        if(i == 0)
            prot = new Protocol(abyte0, 0);
        else
            prot = new Protocol("MO_O".getBytes(), 3, abyte0.length, abyte0);
        i = byteArrayToInt(prot.getContent(), 0, 2);
        AppLog.i(TAG, (new StringBuilder("--->Video Resp:")).append(i).toString());
        try
        {
            AppLog.d(TAG, "--->enableVideo");
            wificar.enableVideo();
        }
        // Misplaced declaration of an exception variable
        catch(IOException e)
        {
            AppLog.d(TAG,"enableVideo error!");
            //WificarActivity.getInstance().sendMessage(8903);
            e.printStackTrace();
            return i;
        }
        return i;
    }

    public static void parseVideoData(WifiCar wificar, byte abyte0[])
    {
        abyte0 = (new Protocol(abyte0, 0)).getContent();
        int i = byteArrayToInt(abyte0, 0, 4);
        int j = byteArrayToInt(abyte0, 4, 4);
        byteArrayToInt(abyte0, 8, 1);
        int k = byteArrayToInt(abyte0, 9, 4);
        ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
        bytearrayoutputstream.write(abyte0, 13, k);
        abyte0 = bytearrayoutputstream.toByteArray();
        /*abyte0 = new VideoData(i, j, abyte0);
        try
        {
            wificar.appendVideoDataToFlim(abyte0);
            return;
        }
        // Misplaced declaration of an exception variable
        catch(WifiCar wificar)
        {
            wificar.printStackTrace();
        }*/
    }

    public static void parseVideoStartResp(WifiCar wificar, byte abyte0[], int i)
        throws IOException
    {
        byteArrayToInt(abyte0, 0, 2);
        /*wificar.connectMediaReceiver(byteArrayToInt(abyte0, 2, 4));
        wificar.enableAudio();*/
    }
}