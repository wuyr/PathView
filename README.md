## Path线条动画效果 (仿bilibili弹幕聊天室后面的线条动画)
### 博客详情： http://blog.csdn.net/u011387817/article/details/78817827

### 使用方式:
#### 添加依赖：
```
implementation 'com.wuyr:pathview:1.0.0'
```

### APIs:
|Method|Description|
|------|-----------|
|setPath(Path path)|加载Path|
|setMode(int mode)|设置条线动画模式(火车模式, 飞机模式)|
|setDuration(long duration)|设置动画时长|
|setRepeat(boolean isRepeat)|设置是否重复播放|
|setLineWidth(float width)|设置线条宽度 (单位: px)|
|setLightLineColor(int color)|设置高亮线条颜色|
|setDarkLineColor(int color)|设置暗色线条颜色|
|setLightLineColorRes(int color)|设置高亮线条颜色|
|setDarkLineColorRes(int color)|设置暗色线条颜色|
|setOnAnimationEndListener(Listener listener)|设置动画播放完毕监听器|
|start()|开始播放动画|
|stop()|停止播放动画|

## Demo下载: [app-debug.apk](https://github.com/wuyr/PathView/raw/master/app-debug.apk)
### 库源码地址： https://github.com/Ifxcyr/PathView
## 效果图:
![preview](https://github.com/wuyr/PathView/raw/master/previews/preview1.gif) ![preview](https://github.com/wuyr/PathView/raw/master/previews/preview2.gif)
![preview](https://github.com/wuyr/PathView/raw/master/previews/preview3.gif) ![preview](https://github.com/wuyr/PathView/raw/master/previews/preview4.gif)
