首先，这个项目的背景是看到越来越多的4G远控小车如同雨后春笋般涌现，大多是在前几年比较流行的智能小车基础上，叠加物联网芯片，使得STM32也具备了web控制接口。
    接着，在小车上面固定一个看家摄像头，内置云台，支持360度旋转的。最后在小车内部加一个4G无线卡托，通过USB口给其供电，给物联网芯片和摄像头提供WIFI网络。

控制端是通过Android APP调用web接口，控制小车的前后左右，再准备一个电脑或者平板，观看摄像头传回的视频。


整个系统基于几个独立的产品，组装起来，实际运行起来也不错，被DY平台的直播节目上镜后，可以去超市购物，逐渐成为网红小车。

去年闲鱼收了一个16年左右的小车，叫brokestone rover，支持手机wifi连接后，用APP控制其运行，22年中花了一段时间通过抓包加逆向，把其运行的信令研究清楚了，自己写了一个APP，可以控制其运行，但功能简单，只能控制其前后左右，不能控制灯光，录音，扬声器。

受到4G遥控小车的启发，何不把这个小车改造成远程遥控版呢。

此工程即是在此背景下产生，因为要实现远控，那么遥控器和小车需要互相知道其IP地址，这个springboot的作用就是提供了一个注册服务，让小车能注册自己的IP地址，同时也能让遥控器获知这个IP，然后产生交互。

项目实现比较简单，提供了注册和查询的接口，让小车携带自身物理地址（WIFI MAC）和IP地址向服务器注册，服务器把这两个信息保存在内存中，后续给控制端APP提供查询接口，只要能提供正确的物理地址，就返回小车的IP地址。