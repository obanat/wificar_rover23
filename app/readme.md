这是小车的Android客户端，不支持gradle编译，仅支持放到aosp工程中编译

202206停止维护，代码合并到android_project目录下的gradle工程中，如果需要继续使用aosp编译方式，需要手动合并新的代码。







支持两种运行模式：

1. 直接连接小车热点，只能在wifi范围内运行

2. 接入Relay云，实现超远距离遥控，有延迟，但不明显

相对于默认的遥控APP，优点：

1. 自定义的wheelview控件，模仿王者荣耀这类手游的万向操作滚轮，操作便捷，小孩子也很容易上手

2. 支持local和云端两种模式



遗留问题：
1. 云端模式视频不稳定

2. 不支持local和云端两种模式运行时的切换

3. 没有加设置界面，不支持改云端的地址端口等

4. 没有做业务看板，云端模式下，不支持查看小车的状态

