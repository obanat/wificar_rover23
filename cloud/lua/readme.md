依赖lua 的socket模块
openwrt上安装opkg install luasocket即可
编写脚本的本意是 在webui的cgi脚本和小车proxy直接传递控制信令
为了实现上述目的，需要完成以下动作
1）建立一个server socket，接收来自cgi脚本的控制指令
2）复用上述socket，接收来自小车proxy的连接
3）转发控制指令到小车proxy

为了实现上述3点，发现无法绕开多线程，尽管lua也有调度机制（协程）来模拟多线程，利用子线程把状态返回给主线程，主线程用suspend/resume来控制子线程的运行，实在太简陋，无法达成目的。
最终转成mjpg-streamer来实现，两个原因
1）搞定控制指令后，视频流依旧绕不开，干脆找个功能全些的，基于高级语言编写
2）mjpg-streamer有cgi接口，基本现成可用
3）mjpg-streamer有多个linux发行版本，移植工作量小，openwrt，ubuntu，armbian均支持，后面对proxy的操作系统选型约束也更小些
