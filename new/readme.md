
仿照webrtc架构重新搭建的远程遥控小车系统，硬件继续使用原来的brookstone小车的硬件，系统分为三部分：

1. proxy
proxy部署在小车上，硬件是一个摘掉屏幕的Android手机，里面跑一个UI相当简单的APK，作用是连接小车wifi，按小车的交互协议获取视频流，并接收client端发过来的控制指令，UI的作用是调试用途，实际没什么作用。Android手机里还插入了一张4G SIM卡，proxy连接小车wifi的同时还建立了一个蜂窝连接，把wifi网卡上的视频流通过蜂窝连接发给client，并把蜂窝连接上来自于client的控制指令转发到wifi连接上，最终传递给小车。proxy顾名思义就是一个代理，位于广域网里的client可以通过这个代理连接小车。这个proxy除了代理网络外，它还理解这台小车的内部运行逻辑，能把来自client的控制指令翻译成小车能理解的指令，它也是小车运行逻辑的代理。


2. signal server
signal server的作用是接收proxy和client的注册指令，根据小车的MAC地址实现两者的两两配对，配对后，把client的公网ip和端口传递给proxy，实现P2P直连。后续的小车控制指令和视频流，均通过P2P直传，其功能与webrtc中的signal server是类似的。


3. client
client是一个Android的APP，运行在任意Android设备上，除了作为小车的遥控器以外，里面包含了一个简单的upnp打洞逻辑，在signal server的帮助下实现P2P打洞，确保能让proxy与自身建立socket连接。
    
    

目录说明：

cloud：
signal server的实现，为了简单，实验系统是找了个openwrt系统部署上，目前还是作为mjpg-streamer的插件运行，通过建立一个server socket，接收proxy和client的数据，传递ip地址和端口，实现双方的握手(注册)和P2P打洞，实验系统的目的是快速验证可行性。最终是托管在阿里云上，跑一个后台服务，基于公网IP起两个端口，一个用于proxy和client注册，一个用于打洞。

proxy:
小车内proxy端，Android app

client：
小车的控制端，Android app，界面与老版本的app类似，增加了远程连接状态的显示，新版本不支持本地控制，仅支持远程遥控。

