
这个项目的背景是看到越来越多的4G远控小车如同雨后春笋般涌现，大多是在前几年比较流行的智能小车基础上，叠加物联网芯片，使得STM32也具备了web控制接口。 接着，在小车上面固定一个看家摄像头，内置云台，支持360度旋转的。最后在小车内部加一个4G无线卡托，通过USB口给其供电，给物联网芯片和摄像头提供WIFI网络。

控制端是通过Android APP调用web接口，控制小车的前后左右，再准备一个电脑或者平板，观看摄像头传回的视频。

整个系统基于几个独立的产品，组装起来，实际运行起来也不错，被DY平台的直播节目上镜后，可以去超市购物，逐渐成为网红小车。

去年闲鱼收了一个16年左右的小车，叫brokestone rover，支持手机wifi连接后，用APP控制其运行，22年中花了一段时间通过抓包加逆向，把其运行的信令研究清楚了，自己写了一个APP，可以控制其运行，但功能简单，只能控制其前后左右，不能控制灯光，录音，扬声器。

受到4G遥控小车的启发，本项目把WIFI小车改造成远程遥控版。

springboot-wificar：小车的redis服务，负责接受proxy的注册指令，记录其IP，并提供访问接口，可以让遥控器获取proxy的IP

proxy：
proxy是一个小车的代理，负责连接小车的wifi，并创建两个server socket，等待遥控器的连接，在小车的socket和server socket之间转发数据。
proxy是安卓的APP，可以运行在任何安卓手机上，实测华为、荣耀、三星均没有问题，要求手机连接小车的Rover打头的热点，同时开启蜂窝数据。