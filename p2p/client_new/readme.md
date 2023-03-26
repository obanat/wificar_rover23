
wifi 小车的android gradle版本

梳理完整个工程的依赖，发现基本没有依赖，所以就切换到gradle上编译了，避免使用android完整aosp工程编译带来的大量准备工作，老的代码不再维护了，但目录结构是一致的，只需要做简单的代码合并即可。

app支持2.0和3.0的rover，主要差别除了信令上略微差异，最大的变化是小车返回的camera数据在3.0版本上切换成了H264码流。

相对于2.0的jpeg，h264复杂些，需要解码，需要分析I帧、P帧，在原版的rover app里是引入了一个修改过的ffmpeg库（libffmpeg.so）提供了把码流变成surface显示的库函数。

上述过程看似复杂，所幸的是，经过10年，Android生态的手机，估计连几百元的超低端机型都已经具备h264解码（甚至硬解码）能力，android的媒体框架提供了一个很简单的方式把一个view对应的surface与一个h264的MediaCodec关联起来。

关联后的MediaCodec仅需要从外部拿到码流，就可以源源不断的往指定的屏幕区域绘制图像。

基于上述分析，新版的rover app放弃了用ndk编译开源ffmpeg库，再用jni调用native开源库的方式。切换到Android原生的MediaCodec解码上，减少外部依赖。为此，新增了H264SurfaceView.java，与2.0版本的MjpegView.java并列（界面上加了一个按钮来实现切换），里面调用了h264格式的MediaCodec（对应mimetype为video/avc），从创建好的receiverMediaSocket中获取码流数据，并送入解码器，这些，都是Android媒体框架的功劳。

其代码流程为CommandEncoder.java（parseMediaCommandV3） -》WifiCar.java（refreshH264View）-》H264SurfaceView.java（decodeOneFrame）-》MediaCodec（dequeueOutputBuffer），然后就进入Android媒体框架中。

工程基于26版本的Android SDK（Android 8.0）编译成功，在nexus 6p上运行成功，当然近期新版本的Android设备也是没问题的，不需要引入任何三方的maven库，所以，编译配置相对简单。
