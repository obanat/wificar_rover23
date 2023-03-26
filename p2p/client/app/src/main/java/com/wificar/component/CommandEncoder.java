package com.wificar.component;

import android.util.Log;
import com.wificar.WificarActivity;
import com.wificar.util.AppLog;
import com.wificar.util.BlowFish;
import com.wificar.util.ByteUtility;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/* loaded from: classes.dex */
public class CommandEncoder {
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

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static class Protocol {
        byte[] content;
        int contentLength;
        byte[] header;
        int op;
        byte preserve1;
        byte[] preserve2;
        long preserve3;

        public Protocol(byte[] header, int op, int contentLength, byte[] content) {
            this.op = 0;
            this.preserve1 = (byte) 0;
            this.preserve2 = new byte[8];
            this.contentLength = 0;
            this.preserve3 = 0L;
            this.content = new byte[0];
            this.header = header;
            this.op = op;
            this.contentLength = contentLength;
            this.content = content;
        }

        public int getOp() {
            return this.op;
        }

        public byte[] getContent() {
            return this.content;
        }

        public Protocol(byte[] packet) {
            this(packet, 0);
        }

        public Protocol(byte[] packet, int offset) {
            this.op = 0;
            this.preserve1 = (byte) 0;
            this.preserve2 = new byte[8];
            this.contentLength = 0;
            this.preserve3 = 0L;
            this.content = new byte[0];
            this.header = CommandEncoder.WIFICAR_VIDEO_OP.getBytes();
            this.op = CommandEncoder.byteArrayToInt(packet, offset + 4, 2);
            this.contentLength = CommandEncoder.byteArrayToInt(packet, offset + 15, 4);
            if (this.contentLength > 0) {
                this.content = new byte[this.contentLength];
                System.arraycopy(packet, offset + 23, this.content, 0, this.contentLength);
            }
        }

        public byte[] output() throws IOException {
            int length = this.content.length;
            byte[] opCode = CommandEncoder.int16ToByteArray(this.op);
            byte[] extendLengthByte = CommandEncoder.int32ToByteArray(this.content.length);
            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            bOut.write(this.header);
            bOut.write(opCode);
            bOut.write(new byte[1]);
            bOut.write(new byte[8]);
            bOut.write(extendLengthByte);
            bOut.write(new byte[4]);
            bOut.write(this.content);
            return bOut.toByteArray();
        }
    }

    public static byte[] int32ToByteArray(int value) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            int offset = i * 8;
            b[i] = (byte) ((value >>> offset) & KEEP_ALIVE);
        }
        return b;
    }

    public static String int32ToByteHex(int value) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            int offset = i * 8;
            b[i] = (byte) ((value >>> offset) & KEEP_ALIVE);
        }
        return ByteUtility.bytesToHex(b);
    }

    public static String int32ToByteHexR(int value) {
        byte[] b = {(byte) ((value >> 24) & KEEP_ALIVE), (byte) ((value >> 16) & KEEP_ALIVE), (byte) ((value >> 8) & KEEP_ALIVE), (byte) ((value >> 0) & KEEP_ALIVE)};
        return ByteUtility.bytesToHex(b);
    }

    public static byte[] int32ToByteArrayR(int value) {
        return new byte[]{(byte) ((value >> 24) & KEEP_ALIVE), (byte) ((value >> 16) & KEEP_ALIVE), (byte) ((value >> 8) & KEEP_ALIVE), (byte) ((value >> 0) & KEEP_ALIVE)};
    }

    public static byte[] int16ToByteArray(int value) {
        byte[] b = new byte[2];
        for (int i = 0; i < 2; i++) {
            int offset = i * 8;
            b[i] = (byte) ((value >>> offset) & KEEP_ALIVE);
        }
        return b;
    }

    public static byte[] longToByteArray(long data) {
        return new byte[]{(byte) ((data >> 56) & 255), (byte) ((data >> 48) & 255), (byte) ((data >> 40) & 255), (byte) ((data >> 32) & 255), (byte) ((data >> 24) & 255), (byte) ((data >> 16) & 255), (byte) ((data >> 8) & 255), (byte) ((data >> 0) & 255)};
    }

    public static byte[] int8ToByteArray(int value) {
        byte[] b = new byte[1];
        for (int i = 0; i < 1; i++) {
            int offset = i * 8;
            b[i] = (byte) ((value >>> offset) & KEEP_ALIVE);
        }
        return b;
    }

    public static String byteArrayToString(byte[] inByteArray, int iOffset, int iLen) {
        byte[] ch = new byte[iLen];
        for (int x = 0; x < iLen; x++) {
            ch[x] = inByteArray[x + iOffset];
        }
        return new String(ch).trim();
    }

    public static String byteArrayToHex(byte[] inByteArray, int iOffset, int iLen) {
        byte[] ch = new byte[iLen];
        for (int x = 0; x < iLen; x++) {
            ch[x] = inByteArray[x + iOffset];
        }
        return ByteUtility.bytesToHex(ch);
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

    public static byte[] cmdFetchBatteryPowerReq() throws IOException {
        Protocol cmd = new Protocol(WIFICAR_OP.getBytes(), FETCH_BATTERY_POWER_REQ, 0, new byte[0]);
        return cmd.output();
    }

    public static byte[] cmdMediaLoginReq(int linkId) throws IOException {
        byte[] vb1 = int32ToByteArray(linkId);
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        bOut.write(vb1);
        Protocol cmd = new Protocol(WIFICAR_VIDEO_OP.getBytes(), 0, bOut.size(), bOut.toByteArray());
        return cmd.output();
    }

    public static byte[] cmdLoginReq(int v1, int v2, int v3, int v4) throws IOException {
        byte[] vb1 = int32ToByteArray(v1);
        byte[] vb2 = int32ToByteArray(v2);
        byte[] vb3 = int32ToByteArray(v3);
        byte[] vb4 = int32ToByteArray(v4);
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        bOut.write(vb1);
        bOut.write(vb2);
        bOut.write(vb3);
        bOut.write(vb4);
        Protocol cmd = new Protocol(WIFICAR_OP.getBytes(), 0, 16, bOut.toByteArray());
        return cmd.output();
    }

    public static Protocol createTalkData(TalkData data) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] tickTime = int32ToByteArray(data.getTicktime());
        byte[] serial = int32ToByteArray(data.getSerial());
        byte[] timestamp = int32ToByteArray(data.getTimestamp());
        byte[] audioFormat = int8ToByteArray(0);
        byte[] length = int32ToByteArray(data.getData().length);
        out.write(tickTime);
        out.write(serial);
        out.write(timestamp);
        out.write(audioFormat);
        out.write(length);
        out.write(data.getData());
        Protocol cmd = new Protocol(WIFICAR_VIDEO_OP.getBytes(), 3, out.size(), out.toByteArray());
        return cmd;
    }

    public static byte[] parseFetchBatteryPowerResp(WifiCar wificar, byte[] packet, int type) throws IOException {
        Protocol cmd;
        if (type == 0) {
            cmd = new Protocol(packet, 0);
        } else {
            cmd = new Protocol(WIFICAR_OP.getBytes(), FETCH_BATTERY_POWER_RESP, packet.length, packet);
        }
        byte[] data = cmd.getContent();
        byteArrayToInt(data, 0, 1);
        return cmd.output();
    }

    public static boolean parseLoginResp(WifiCar wificar, byte[] packet, int type) {
        Protocol cmd;
        try {
            if (type == 0) {
                Protocol cmd2 = new Protocol(packet, 0);
                cmd = cmd2;
            } else {
                Protocol cmd3 = new Protocol(WIFICAR_OP.getBytes(), 1, packet.length, packet);
                cmd = cmd3;
            }
            byte[] data = cmd.getContent();
            int result = byteArrayToInt(data, 0, 2);
            if (result == 0) {
                String cameraId = byteArrayToString(data, 2, 13);
                int[] cameraVer = new int[4];
                for (int x = 0; x < 4; x++) {
                    cameraVer[x] = byteArrayToInt(data, x + 23, 1);
                }
                String deviceId = String.valueOf(cameraVer[0]) + "." + cameraVer[1] + "." + cameraVer[2] + "." + cameraVer[3];
                wificar.setDeviceId(deviceId);
                wificar.setCameraId(cameraId);
                int val1 = byteArrayToInt(data, 27);
                int val2 = byteArrayToInt(data, 31);
                int val3 = byteArrayToInt(data, 35);
                int val4 = byteArrayToInt(data, 39);
                int rt1 = byteArrayToInt(data, 43, 4);
                int rt2 = byteArrayToInt(data, 47, 4);
                int rt3 = byteArrayToInt(data, 51, 4);
                int rt4 = byteArrayToInt(data, 55, 4);
                wificar.setChallengeReverse(0, rt1);
                wificar.setChallengeReverse(1, rt2);
                wificar.setChallengeReverse(2, rt3);
                wificar.setChallengeReverse(3, rt4);
                BlowFish bf = new BlowFish();
                bf.InitBlowfish(wificar.getKey().getBytes(), wificar.getKey().length());
                int c1 = wificar.getChallenge(0);
                int c2 = wificar.getChallenge(1);
                int c3 = wificar.getChallenge(2);
                int c4 = wificar.getChallenge(3);
                int[] l1 = {c1};
                int[] r1 = {c2};
                int[] l2 = {c3};
                int[] r2 = {c4};
                bf.Blowfish_encipher(l1, r1);
                bf.Blowfish_encipher(l2, r2);
                String bfl1 = int32ToByteHexR(l1[0]);
                String bfr1 = int32ToByteHexR(r1[0]);
                String bfl2 = int32ToByteHexR(l2[0]);
                String bfr2 = int32ToByteHexR(r2[0]);
                String bfl1Return = int32ToByteHex(val1);
                String bfr1Return = int32ToByteHex(val2);
                String bfl2Return = int32ToByteHex(val3);
                String bfr2Return = int32ToByteHex(val4);
                if (bfl1.equals(bfl1Return) && bfr1.equals(bfr1Return) && bfl2.equals(bfl2Return) && bfr2.equals(bfr2Return)) {
                    AppLog.d("wild0", "--->===============================");
                    wificar.verifyCommand();
                    return true;
                }
                AppLog.i("", "--->:000000000");
                return false;
            }
            WificarActivity wificarActivity = WificarActivity.getInstance();
            WificarActivity.getInstance();
            wificarActivity.sendMessage(WificarActivity.MESSAGE_CONNECT_TO_CAR_FAIL);
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static byte[] cmdVerifyReq(String key, int v1, int v2, int v3, int v4) throws IOException {
        AppLog.d("wild0", "cmdVerifyReq");
        BlowFish bf = new BlowFish();
        int[] l1 = {v1};
        int[] r1 = {v2};
        int[] l2 = {v3};
        int[] r2 = {v4};
        bf.InitBlowfish(key.getBytes(), key.length());
        bf.Blowfish_encipher(l1, r1);
        bf.Blowfish_encipher(l2, r2);
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        bOut.write(int32ToByteArray(l1[0]));
        bOut.write(int32ToByteArray(r1[0]));
        bOut.write(int32ToByteArray(l2[0]));
        bOut.write(int32ToByteArray(r2[0]));
        Protocol cmd = new Protocol(WIFICAR_OP.getBytes(), 2, bOut.size(), bOut.toByteArray());
        AppLog.d("wild0", "============================verify");
        return cmd.output();
    }

    public static int parseVerifyResp(WifiCar wificar, byte[] packet, int type) {
        Protocol cmd;
        AppLog.d("wild0", "--->cmdVerifyResp");
        if (type == 0) {
            cmd = new Protocol(packet, 0);
        } else {
            cmd = new Protocol(WIFICAR_OP.getBytes(), 3, packet.length, packet);
        }
        byte[] data = cmd.getContent();
        int result = byteArrayToInt(data, 0, 2);
        AppLog.i("wild0", "--->Video Resp:" + result);
        try {
            AppLog.d("wild0", "--->enableVideo");
            wificar.enableVideo();
        } catch (IOException e) {
            AppLog.d("wild0", "--->parseVerifyResp:" + e);
            WificarActivity.getInstance().sendMessage(WificarActivity.MESSAGE_CONNECT_TO_CAR_FAIL);
            e.printStackTrace();
        }
        return result;
    }

    public static byte[] cmdKeepAlive() throws IOException {
        Protocol cmd = new Protocol(WIFICAR_OP.getBytes(), KEEP_ALIVE, 0, new byte[0]);
        return cmd.output();
    }

    public static byte[] cmdVideoStartReq() throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.put(int8ToByteArray(1));
        Protocol cmd = new Protocol(WIFICAR_OP.getBytes(), 4, 1, bb.array());
        return cmd.output();
    }

    public static void parseVideoStartResp(WifiCar wificar, byte[] packet, int type) throws IOException {
        byteArrayToInt(packet, 0, 2);
        int linkId = byteArrayToInt(packet, 2, 4);
        wificar.connectMediaReceiver(linkId);
        wificar.enableAudio();
    }

    public static byte[] cmdVideoEnd() throws IOException {
        Protocol cmd = new Protocol(WIFICAR_OP.getBytes(), 6, 0, new byte[0]);
        return cmd.output();
    }

    public static byte[] cmdVideoFrameInterval(int v1) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.put(int32ToByteArray(1));
        Protocol cmd = new Protocol(WIFICAR_OP.getBytes(), 7, bb.capacity(), bb.array());
        return cmd.output();
    }

    public static byte[] cmdAudioStartReq() throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(1);
        bb.put(int8ToByteArray(1));
        Protocol cmd = new Protocol(WIFICAR_OP.getBytes(), 8, 1, bb.array());
        return cmd.output();
    }

    public static byte[] cmdAudioEnd() throws IOException {
        Protocol cmd = new Protocol(WIFICAR_OP.getBytes(), 10, 0, new byte[0]);
        return cmd.output();
    }

    public static void parseAudioStartResp(WifiCar wificar, byte[] packet, int type) {
        int result = byteArrayToInt(packet, 0, 2);
        if (result == 0) {
            WificarActivity wificarActivity = WificarActivity.getInstance();
            WificarActivity.getInstance();
            wificarActivity.sendMessage(WificarActivity.MESSAGE_GET_SETTING_INFO);
        }
    }

    public static byte[] cmdTalkStartReq(int arg) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(1);
        bb.put(int8ToByteArray(arg));
        Protocol cmd = new Protocol(WIFICAR_OP.getBytes(), 11, 1, bb.array());
        return cmd.output();
    }

    public static void parseTalkStartResp(WifiCar car, byte[] packet, int type) {
        int result = byteArrayToInt(packet, 0, 2);
        if (result == 0 && packet.length > 2) {
            byteArrayToInt(packet, 2, 4);
        }
        if (result == 0) {
            car.getAudioComponent().startRecord();
        }
    }

    public static byte[] cmdTalkEnd() throws IOException {
        Protocol cmd = new Protocol(WIFICAR_OP.getBytes(), 13, 1, new byte[0]);
        return cmd.output();
    }

    public static byte[] cmdDecoderControlReq(int val) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(1);
        bb.put(int8ToByteArray(val));
        Protocol cmd = new Protocol(WIFICAR_OP.getBytes(), 14, 1, bb.array());
        return cmd.output();
    }

    public static byte[] cmdDeviceControlReq(int key, int val) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.put(int8ToByteArray(key));
        bb.put(int8ToByteArray(val));
        Protocol cmd = new Protocol(WIFICAR_OP.getBytes(), DEVICE_CONTROL_REQ, 2, bb.array());
        return cmd.output();
    }

    public static ByteArrayOutputStream parseCommand(WifiCar car, ByteArrayOutputStream packet) throws IOException {
        byte[] data = packet.toByteArray();
        if (data.length > 23) {
            ByteUtility.byteArrayToInt(data, 4, 2);
            int contentLength = ByteUtility.byteArrayToInt(data, 15, 4);
            if (data.length >= contentLength + 23) {
                int totalLength = contentLength + 23;
                Protocol cmd = new Protocol(data, 0);
                packet.reset();
                packet.write(data, totalLength, data.length - totalLength);
                switch (cmd.getOp()) {
                    case 1:
                        boolean login = parseLoginResp(car, cmd.getContent(), 1);
                        AppLog.d("wild1", "--->login:" + login);
                        if (!login) {
                            WificarActivity wificarActivity = WificarActivity.getInstance();
                            WificarActivity.getInstance();
                            wificarActivity.sendMessage(WificarActivity.MESSAGE_CONNECT_TO_CAR_FAIL);
                            break;
                        }
                        break;
                    case 3:
                        int result = parseVerifyResp(car, cmd.getContent(), 1);
                        AppLog.d("wild1", "--->VERIFY_RESP:" + result);
                        break;
                    case VIDEO_START_RESP /* 5 */:
                        parseVideoStartResp(car, cmd.getContent(), 1);
                        break;
                    case AUDIO_START_RESP /* 9 */:
                        parseAudioStartResp(car, cmd.getContent(), 1);
                        break;
                    case TALK_START_RESP /* 12 */:
                        parseTalkStartResp(car, cmd.getContent(), 1);
                        break;
                    case FETCH_BATTERY_POWER_RESP /* 252 */:
                        parseFetchBatteryPowerResp(car, cmd.getContent(), 1);
                        break;
                }
            }
        }
        return packet;
    }

    public static byte[] cmdDataLoginReq(int id) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(4);
        Protocol cmd = new Protocol(WIFICAR_VIDEO_OP.getBytes(), 0, bb.capacity(), bb.array());
        return cmd.output();
    }

    public static void parseVideoData(WifiCar car, byte[] packet) {
        //Protocol cmd = new Protocol(packet, 0);
        byte[] data = packet;
        int timestamp = byteArrayToInt(data, 0, 4);
        int frametime = byteArrayToInt(data, 4, 4);
        byteArrayToInt(data, 8, 1);
        int dataLength = byteArrayToInt(data, 9, 4);
        ByteArrayOutputStream bImage = new ByteArrayOutputStream();
        bImage.write(data, 13, dataLength);
        byte[] bArrayImage = bImage.toByteArray();
        Log.e("media", "timestamp:" + timestamp + " frametime:" + frametime);
        VideoData vData = new VideoData(timestamp, frametime, bArrayImage);
        try {
            car.appendVideoDataToFlim(vData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("media","media data process end========>");
    }

    public static void parseAudioData(WifiCar wificar, byte[] packet) {
        wificar.enableAudioFlag();
        //Protocol cmd = new Protocol(packet, 0);
        byte[] data =packet;
        int timestamp = byteArrayToInt(data, 0, 4);
        int packetSeq = byteArrayToInt(data, 4, 4);
        int graspstamp = byteArrayToInt(data, 8, 4);
        int format = byteArrayToInt(data, 12, 1);
        int dataLength = byteArrayToInt(data, 13, 4);
        ByteArrayOutputStream bAudio = new ByteArrayOutputStream();
        bAudio.write(data, 17, dataLength);
        int adpcmParaSample = byteArrayToInt(data, dataLength + 17, 2);
        int adpcmParaIndex = byteArrayToInt(data, dataLength + 19, 1);
        AudioData aData = new AudioData(timestamp, packetSeq, graspstamp, format, bAudio.toByteArray(), adpcmParaSample, adpcmParaIndex);
        byte[] bDecoded = AudioComponent.decodeADPCMToPCM(bAudio.toByteArray(), bAudio.size(), adpcmParaSample, adpcmParaIndex);
        aData.setPCMData(bDecoded);
        try {
            wificar.appendAudioDataToFlim(aData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static byte mediaRecvBuf[] = new byte[0];
    public static void parseMediaCommand(WifiCar wificar, byte[] buf, int i) throws IOException {

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
                    parseVideoData(wificar,arrayCopy(mediaRecvBuf, 23, mediaRecvBuf.length - 23));
                    // Audio bytes: call processing routine
                } else if ((int)mediaRecvBuf[4] == 2){
                    parseAudioData(wificar,arrayCopy(mediaRecvBuf, 23, mediaRecvBuf.length - 23));
                }
                // Start over with new bytes
                //mediaRecvBuf = buf[k:]
                mediaRecvBuf = arrayCopy(buf, k, i/*buf.length*/ - k);

                // No media bytes yet: start with new bytes
            }else{
                //mediaRecvBuf = buf[k:]
                mediaRecvBuf = arrayCopy(buf, k, i/*buf.length*/ - k);
            }
            // No: accumulate media bytes
        } else{
            //mediaRecvBuf += buf;
            byte[] tmp = arrayCopy2(mediaRecvBuf, 0, mediaRecvBuf.length,
                    buf, 0, i/*buf.length*/);
            mediaRecvBuf = tmp;
        }

    }

    private static int findstr(byte[] buf, int bufLen, String str) {
        int strLen = str.length();
        if (strLen == 4) {
            byte[] str2byte = str.getBytes();

            for (int i = 0; i < bufLen -3; i++) {
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

    public static int getPrefixPosition1(byte[] data, int startPosition) {
        return getPrefixPosition1(data, startPosition, data.length);
    }

    public static int getPrefixPosition1(byte[] data, int startPosition, int endPosition) {
        for (int i = startPosition; i < endPosition - 4 && i <= endPosition; i++) {
            if (data[i] == 77 && data[i + 1] == 79 && data[i + 2] == 95 && data[i + 3] == 86) {
                return i;
            }
        }
        return -1;
    }

    public static int getPrefixCount(byte[] data, int startPosition) {
        int count = 0;
        for (int i = startPosition; i < data.length - 4; i++) {
            if (data[i] == 77 && data[i + 1] == 79 && data[i + 2] == 95 && data[i + 3] == 86) {
                count++;
            }
        }
        return count;
    }
}
