rover3.0新的类WificarNew

    public boolean initSocket()

BlowfishKeyInit --传入hangzhou字符串，以及长度，初始化

//起一个接收进程
AppInforToSystem.mHandler_Receiver = new Handler(AppInforToSystem.mThread_Receiver.getLooper());

//发送为零的数据包
        AppInforToSystem.ConnectStatus = AppCommand.getAppCommandInstace().sendCommand(0).booleanValue();

//启动5s的timer,等iStatus变为2
        AppInforToSystem.iStatus = 1;
        i = 5000;
2.5s后会再发一次

//收到回应后，发2（对应2.0版本的VERIFY消息），客户端状态改为3，等iStatus变为4
        AppLog.e("connect", "STATE_RESP_RECEIVE");
        AppInforToSystem.ConnectStatus = AppCommand.getAppCommandInstace().sendCommand(2).booleanValue();
        AppLog.e("connect", "STATE_VERI_SEND");
        AppInforToSystem.iStatus = 3;
同样启动5s的timer，2.5s会再发一次

//收到回应后，打开camera
            AppLog.e("connect", "STATE_VERI_RECEIVE");
            AppInforToSystem.iStatus = 5;
            AppInforToSystem.ConnectStatus = true;
            AppVideoFunction.getAppVideoFunctionInstance().VideoEnable();


//camera move
对应的int是14，然后一个参数，与2.0定义相同

//move
DeviceControlCommand
    public Boolean DeviceControlCommand(int i, int j)
对应的int是250，然后是左右，参数与2.0相同

信令解析
1）python:__init__.py
def _sendRequest(self, sock, c, id, n, contents):  
MO_O+id(例如250，一个字节) + 10个0 + 数组长度（一个字节） + 7个0 + 数组内容
2) java(3.0代码) AppCommand.jad
    public Boolean DeviceControlCommand(int i, int j)
MO_O+id(4个字节) + 7个0 + 数字2(4个字节) + 4个0 + [i,j]
3) java(2.0代码) WifiCar.jad
  public boolean move(int paramInt1, int paramInt2) throws IOException {  //对i，j有所修正
public static byte[] cmdDeviceControlReq(int paramInt1, int paramInt2) throws IOException {
    return (new Protocol("MO_O".getBytes(), 250, 2, byteBuffer.array())).output();
output里面的逻辑：header+ 250 + 10个0 + 数组长度 + 7个0 + 数组内容
说明：op共11个字节；数组长度共8字节

connect初始化过程信令解析：

1）python:__init__.py
self._sendCommandIntRequest(0, [0, 0, 0, 0])
然后就等reply，解析reply中的camera及LI,L2，RI，R2参数，bf加密后，然后发op:2,[LI，L2，RI，R2]
bf的key：AC13:cameraIdsave-private:AC13
然后等reply，收到后不解析，直接发op:4, [1]
        self._sendCommandIntRequest(4, [1]) 


2) java(3.0)WificarNew.jad
BlowfishKeyInit --传入hangzhou字符串，以及长度，初始化
发0，AppInforToSystem.ConnectStatus = AppCommand.getAppCommandInstace().sendCommand(0).booleanValue();
//收到回应后，发2（对应2.0版本的VERIFY消息），与2不同，没有额外的参数
AppInforToSystem.ConnectStatus = AppCommand.getAppCommandInstace().sendCommand(2).booleanValue();
//收到回应后，打开camera
AppVideoFunction.getAppVideoFunctionInstance().VideoEnable();
先发op:4，obj = AppCommand.getAppCommandInstace().sendCommand(4);
然后起camera数据的socket


3）java(2.0) WifiCar.jad
public void connectCommand()
        byte abyte0[] = CommandEncoder.cmdLoginReq(v1, v2, v3, v4); //就是op:0, [0, 0, 0, 0]
//收到回应后，执行parseLoginResp
2-13是camearaId, s = byteArrayToString(abyte0, 2, 13);
13-17是ip地址，wificar.setDeviceId((new StringBuilder(String.valueOf(obj[0]))).append(".").append(obj[1]).append(".").append(obj[2]).append(".").append(obj[3]).toString());

        abyte0.InitBlowfish(wificar.getKey().getBytes(), wificar.getKey().length());
加密：        blowfish.Blowfish_encipher(ai, ai1);//对应python的        L1,R1 = bf.encrypt(L1, R1)
然后发op:2 , 带4个int [L1,R1,L2,R2]

接收到3之后，进入parseVerifyResp
没有解析数据包内容，直接enable video（op：4，[1]），以及启动保活线程（60s）

接收到5之后，进入parseVideoStartResp
处理图像数据，然后enableAudio

结束！

接收器：（CommandEncoder.jad）
CommandEncoder.parseCommand，创建socket后，起了一个接收线程，反复执行parseCommand
1：parseLoginResp
3：parseVerifyResp
5：parseVideoStartResp
