# Camera API Example
This is a collection of Activities with sample code for different Camera API, preview methods and processing with RenderScript.

## About
### Code
All relevant code, to the example, is inside the Activity class, so it's easier to copy and try it in your project.
### API
Right now all examples are for old Camera and not Camera2.

## Features
* **Display on SurfaceView** - simple camera stream drawn on SurfaceView.
* **Display on TextureView** - simple camera stream drawn on TextureView.
* **Preview on Texture (YuvImage)** - camera stream on SurfaceView with a small TextureView preview window. The window shows the stream from PreviewCallback converted from YUV to RGB, using YuvImage. YuvImage is pretty slow and should be used for signle images and not real time preview.
* **Preview on Texture (RenderScript)** - camera stream on SurfaceView with a small TextureView preview window. The window shows the stream from PreviewCallback converted from YUV to RGB, using RenderScript, which is much faster than YuvImage.
* **Preview on Texture (2 RenderScript group)** - same as the previous RenderScript example,  only now there is a group of RenderScripts. Using groups allows increasing perforamnce in case you need multiple effects. This example converts YUV to RGB and applies blur effec to the image.
* **Preview on Texture (3 RenderScript group)** - more complicated example with a group of 3 RenderScripts. In this case, there are 2 script connections. This example converts YUV to RGB, applies blur effect and a color matrix with color inversion.

![https://github.com/mega-arbuz/android-camera-api/blob/master/media/screenshot_display.png](https://github.com/mega-arbuz/android-camera-api/blob/master/media/screenshot_display.png "Screenshot - Simple Camera Stream")
![https://github.com/mega-arbuz/android-camera-api/blob/master/media/screenshot_preview.png](https://github.com/mega-arbuz/android-camera-api/blob/master/media/screenshot_preview.png "Screenshot - Preview Window")
![https://github.com/mega-arbuz/android-camera-api/blob/master/media/screenshot_script.png](https://github.com/mega-arbuz/android-camera-api/blob/master/media/screenshot_script.png "Screenshot - RenderScript")
![https://github.com/mega-arbuz/android-camera-api/blob/master/media/screenshot_script2.png](https://github.com/mega-arbuz/android-camera-api/blob/master/media/screenshot_script2.png "Screenshot - RenderScript Group")
