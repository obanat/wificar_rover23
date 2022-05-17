2.0的move在WifiCar.class, move函数


public void connectCommand() throws IOException {


class Rover:
    def __init__(self):

对应java代码
public static byte[] cmdLoginReq(int paramInt1, int paramInt2, int paramInt3, int paramInt4) throws IOException {
python：        self._sendCommandIntRequest(0, [0, 0, 0, 0])

java：
ByteArrayBuffer bytearraybuffer = new ByteArrayBuffer(0x100000);
i = dataInputStream.available();
byte abyte1[] = new byte[i];
bytearraybuffer.append(abyte1, 0, dataInputStream.read(abyte1, 0, i));
bytearraybuffer = CommandEncoder.parseCommand(instance, bytearraybuffer);
python：        
reply = self._receiveCommandReply(82)


解析过程：
java：          
case 1:
            bool = parseLoginResp(paramWifiCar, protocol.getContent(), 1);

        cameraID = reply[25:37].decode('utf-8')
        key = TARGET_ID + ':' + cameraID + '-save-private:' + TARGET_PASSWORD
        L1 = bytes_to_int(reply, 66)
        R1 = bytes_to_int(reply, 70)
        L2 = bytes_to_int(reply, 74)
        R2 = bytes_to_int(reply, 78)

再发送响应：
java：
public void verifyCommand()
    public static byte[] cmdVerifyReq(String s, int i, int j, int k, int l)
s = new Protocol("MO_O".getBytes(), 2, s.size(), s.toByteArray());
        dataOutputStream.write(abyte0);
        dataOutputStream.flush();

python：
        # Make Blowfish cipher from key
        bf = _RoverBlowfish(key)
        
        # Encrypt inputs from reply
        L1,R1 = bf.encrypt(L1, R1)
        L2,R2 = bf.encrypt(L2, R2)
        
        # Send encrypted reply to Rover
        self._sendCommandIntRequest(2, [L1, R1, L2, R2])     


再接收response：
java:
 case 3: // '\003'
            int j = parseVerifyResp(wificar, protocol.getContent(), 1);
            AppLog.d("wild1", (new StringBuilder("--->VERIFY_RESP:")).append(j).toString());
            return bytearraybuffer;
python:
        # Ignore reply from Rover
        self._receiveCommandReply(26)

发送camera-start：
java:
        try
        {
            AppLog.d("wild0", "--->enableVideo");
            wificar.enableVideo();
        }
python：
        # Send video-start request
        self._sendCommandIntRequest(4, [1])  


设置速度：
python：
def setTreads(self, left, right):-1~1，左的index=4，右=1
    def _spinWheels(self, wheeldir, speed):    高级接口
        # 1: Right, forward
        # 2: Right, backward
        # 4: Left, forward
        # 5: Left, backward    
4，0 表示左停，4，-0.5左轮半速倒转
全速前进 setTreads（1，1），全速后退（-1，-1）
原地左拐 setTreads（-1，1）,原地右拐 setTreads（1，-1）
        self._sendDeviceControlRequest(wheeldir, speed) //实际的接口
        self._sendCommandByteRequest(250, [a,b])


设置相机角度：
python：
def move(self, where):
self.rover._sendCameraRequest(self.stopcmd)
4，左摆，5，停，6右摆  
0上摆 1，停，2下摆

