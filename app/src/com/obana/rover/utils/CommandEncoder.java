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
    public static final String WIFICAR_MEDIA = "WIFI_MEDIA";
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

    static byte mediaRecvBuf[] = new byte[0];
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

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(int32ToByteArray(i));
        out.write(int32ToByteArray(j));
        out.write(int32ToByteArray(k));
        out.write(int32ToByteArray(l));
        Protocol ret = new Protocol("MO_O".getBytes(), 2, out.size(), out.toByteArray());
        AppLog.d(TAG, "========> start send verify req");
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

    public static int parseCommand(WifiCar wificar, byte abyte0[], int i)
        throws IOException
    {

        int k = findstr(abyte0, i, "MO_O");
        if (k < 0) return -1;

        if(i <= 23) return -1;

        int op = ByteUtility.byteArrayToInt(abyte0, 4, 2);
        int len = ByteUtility.byteArrayToInt(abyte0, 15, 4);
        AppLog.d(TAG, "--->receive [" + i + "] bytes data op:" + op + " len:" + len);
        //if(abyte0.length >= i + 23) return bytearraybuffer;

        Protocol protocol = new Protocol(abyte0, 0);
        switch(protocol.getOp())
        {
        default:
            return -1;

        case 1: // '\001'
            boolean flag = parseLoginResp(wificar, protocol.getContent(), 1);
            AppLog.d(TAG, (new StringBuilder("--->login:")).append(flag).toString());
            return 1;

        case 3: // '\003'
            int j = parseVerifyResp(wificar, protocol.getContent(), 1);
            AppLog.d(TAG, (new StringBuilder("--->VERIFY_RESP:")).append(j).toString());
            return 1;

        case 5: // '\005'
            parseVideoStartResp(wificar, protocol.getContent(), 1);
            return 1;

        case 9: // '\t'
            parseAudioStartResp(wificar, protocol.getContent(), 1);
            return 1;

        case 12: // '\f'
            parseTalkStartResp(wificar, protocol.getContent(), 1);
            return 1;

        case 252: 
            parseFetchBatteryPowerResp(wificar, protocol.getContent(), 1);
            return 1;
        case 18: 
            return 1;
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
        AppLog.d(TAG, "--->parseLoginResp start");

        if(byteArrayToInt(abyte0, 0, 2) != 0) return false;
        int minLen = wificar.isVersion20()?0x3B:0x3E;
        if (abyte0.length < minLen) {/*0x3B for 2.0,0x3E for 3.0*/
            AppLog.d(TAG, "UNKNOW LoginResp,JUST RETURN!");
            return false;
        }

        String cameraId;
        int L1, L2, R1, R2;
        if (wificar.isVersion20()) {
            cameraId = byteArrayToString(abyte0, 2, 13);
            L1 = byteArrayToInt(abyte0, 43, 4);
            R1 = byteArrayToInt(abyte0, 47, 4);
            L2 = byteArrayToInt(abyte0, 51, 4);
            R2 = byteArrayToInt(abyte0, 55, 4);
        } else {
            cameraId = byteArrayToString(abyte0, 2, 19);
            L1 = byteArrayToInt(abyte0, 46, 4);
            R1 = byteArrayToInt(abyte0, 50, 4);
            L2 = byteArrayToInt(abyte0, 54, 4);
            R2 = byteArrayToInt(abyte0, 58, 4);
        }

        AppLog.d(TAG, "--->camera id:" + cameraId);
        wificar.setCameraId(cameraId);

        BlowFish bf = new BlowFish();
        AppLog.d(TAG, "--->BlowFish init start");
        bf.InitBlowfish(wificar.getKey().getBytes(), wificar.getKey().length());
        int[] ret = bf.Blowfish_encipher(L1, R1);
        L1 = ret[0]; R1 = ret[1];
        ret = bf.Blowfish_encipher(L2, R2);
        L2 = ret[0]; R2 = ret[1];

        AppLog.d(TAG, "--->======ready to send verifyCommand");
        wificar.verifyCommand(L1,R1,L2,R2);
        return true;
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

        try
        {
            AppLog.d(TAG, "--->enableVideo to off");
            wificar.enableVideo(false, false);
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
        //byteArrayToInt(abyte0, 0, 2);
        wificar.connectMediaReceiver(byteArrayToInt(abyte0, 2, 4));
        //wificar.enableAudio();
    }
    
    public static void parseMediaCommand(WifiCar wificar, byte[] buf, int i)
        throws IOException
    {
        int k = findstr(buf, i, "MO_V");

        // Yes
        if (k  >= 0) {
            // Already have media bytes?
            if (mediaRecvBuf != null && mediaRecvBuf.length > 0) {
                    
                // Yes: add to media bytes up through start of new
                //mediaRecvBuf += buf[0:k];
                byte[] tmp = arrayCopy2(mediaRecvBuf, 0, mediaRecvBuf.length,
                                    buf, 0, k);
                mediaRecvBuf = tmp;

                // Both video and audio messages are time-stamped in 10msec units
                //timestamp = bytes_to_uint(mediabytes, 23)

                // Video bytes: call processing routine
                if ((int)mediaRecvBuf[4] == 1) {
                    //add WIFICAR_MEDIA for server side processing 
                    wificar.refreshView(arrayCopy(mediaRecvBuf, 36, mediaRecvBuf.length - 36));
                // Audio bytes: call processing routine
                } else{
                    /*
                    //ignor audio data
                    audsize = bytes_to_uint(mediabytes, 36)
                    sampend = 40 + audsize
                    offset = bytes_to_short(mediabytes, sampend)
                    index  = ord(mediabytes[sampend+2])
                    pcmsamples = decodeADPCMToPCM(mediabytes[40:sampend], offset, index)
                    self.rover.processAudio(pcmsamples, timestamp) 
                    */ 
                }
                // Start over with new bytes    
                //mediaRecvBuf = buf[k:]
                mediaRecvBuf = arrayCopy(buf, k, buf.length - k);
             
            // No media bytes yet: start with new bytes
            }else{
                //mediaRecvBuf = buf[k:]
                mediaRecvBuf = arrayCopy(buf, k, buf.length - k);
            }
        // No: accumulate media bytes
        } else{
            //mediaRecvBuf += buf;
            byte[] tmp = arrayCopy2(mediaRecvBuf, 0, mediaRecvBuf.length,
                                    buf, 0, buf.length);
            mediaRecvBuf = tmp;
        }
    }

    private static int findstr(byte[] buf, int bufLen, String str) {
        int strLen = str.length();
        if (strLen == 4) {
            byte[] str2byte = str.getBytes();
            int i;
            int len = bufLen;
            for (i = 0; i < len -3; i++) {
                if (buf[i] == str2byte[0] && buf[i+1] == str2byte[1]
                    && buf[i+2] == str2byte[2] && buf[i+3] == str2byte[3]) {
                    return i;
                }
            }
        } 
        return -1;
    }

    private static byte[] arrayCopy(byte[] src, int start, int len) {
        if (src == null || start < 0 || len <= 0 || start + len > src.length) {
            return null;
        }
        byte[] ret = new byte[len];
        System.arraycopy(src, start, ret, 0, len);
        return ret;
    }

    private static byte[] arrayCopy2(byte[] src1, int start1, int len1, byte[] src2, int start2, int len2) {
        if (src1 == null || start1 < 0 ) {
            return null;
        }

        if (src2 == null || start2 < 0 ) {
            return null;
        }
        if (len1 + len2 <= 0) {
            return null;
        }
        byte ret[] = new byte[len1 + len2];
        System.arraycopy(src1, start1, ret, 0, len1);
        System.arraycopy(src2, start2, ret, len1, len2);
        return ret;
    }

    private static final String CLOUD_CMD_PREFIX = "MO_T";
    public static void parseCloudCommand(WifiCar wificar, byte[] buf, int len)
        throws IOException
    {
        //TODO: make a copy
        String cmd = new String(buf);
        AppLog.i(TAG, "parseCloudCommand:receive cloud socket data:" + cmd);
        if (cmd.startsWith(CLOUD_CMD_PREFIX)) {
            byte[] tmp = arrayCopy(buf, 4, len -4);
            switch (tmp[0]) {
                case 'M' :{
                    AppLog.i(TAG, "parseCloudCommand:M");
                    String a = new String(tmp);
                    a = a.substring(1);
                    String [] values = a.split("_");
                    if (values.length == 2) {
                        int left = Integer.parseInt(values[0]);
                        int right = Integer.parseInt(values[1]);
                        AppLog.i(TAG, "parseCloudCommand:move M:" + left + " :" + right);
                        wificar.move(0, 10);
                    }
                    }
                    break;
                case 'C' :{
                    AppLog.i(TAG, "parseCloudCommand:C");
                    String a = new String(tmp);
                    a = a.substring(1);
                    if (a.equals("ON")) {
                        wificar.enableVideo(true, false);
                    } else {
                        wificar.enableVideo(false, false);
                    }
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
