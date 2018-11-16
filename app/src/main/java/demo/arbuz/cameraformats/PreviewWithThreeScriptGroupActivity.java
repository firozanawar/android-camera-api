/*
 * Copyright (C) 2018 Oleg Shnaydman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package demo.arbuz.cameraformats;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.Matrix4f;
import android.renderscript.RenderScript;
import android.renderscript.ScriptGroup;
import android.renderscript.ScriptIntrinsicBlur;
import android.renderscript.ScriptIntrinsicColorMatrix;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

import java.io.IOException;
import java.util.List;

public class PreviewWithThreeScriptGroupActivity extends AppCompatActivity {

    private static final SparseIntArray ORIENTATION_MAP = new SparseIntArray();

    private Camera      mCamera;
    private Camera.Size mPreviewSize;

    private SurfaceView mSurfaceView;
    private TextureView mTextureView;

    private ScriptGroup mScriptGroup;
    private Allocation  mYuvPreviewAlloc;
    private Allocation  mRgbOutputAlloc;

    private Bitmap mBitmap;
    private Matrix mTransformMatrix;

    static
    {
        ORIENTATION_MAP.put(Surface.ROTATION_0, 0);
        ORIENTATION_MAP.put(Surface.ROTATION_90, 90);
        ORIENTATION_MAP.put(Surface.ROTATION_180, 180);
        ORIENTATION_MAP.put(Surface.ROTATION_270, 270);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_with_three_script_group);

        mSurfaceView = findViewById(R.id.surface_view);
        mTextureView = findViewById(R.id.texture_view);

        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        obtainCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();

        releaseCamera();
    }

    private void initView() {
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                startCamera(holder);

                initScript();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }

        });
    }

    private void initScript() {
        RenderScript mRS = RenderScript.create(this);

        // First script
        ScriptIntrinsicYuvToRGB scriptYuvToRGB = ScriptIntrinsicYuvToRGB.create(mRS, Element.U8_4(mRS));

        // Second script
        ScriptIntrinsicBlur scriptBlur = ScriptIntrinsicBlur.create(mRS, Element.U8_4(mRS));
        scriptBlur.setRadius(15f);

        // Third script
        ScriptIntrinsicColorMatrix scriptColor = ScriptIntrinsicColorMatrix.create(mRS);
        // Color inverse
        scriptColor.setColorMatrix(new Matrix4f(
                new float[]{
                        -1f, 0f, 0f, 0f,
                        0f, -1f, 0f, 0f,
                        0f, 0f, -1f, 0f,
                        0f, 0f, 0f, 1f
                }
        ));
        scriptColor.setAdd(1, 1, 1, 0);

        int yuvDataLength = mPreviewSize.width * mPreviewSize.height * 3 / 2;

        mBitmap = Bitmap.createBitmap(mPreviewSize.width, mPreviewSize.height, Bitmap.Config.ARGB_8888);

        mYuvPreviewAlloc = Allocation.createSized(mRS, Element.U8(mRS), yuvDataLength);

        mRgbOutputAlloc = Allocation.createFromBitmap(mRS, mBitmap);

        ScriptGroup.Builder b = new ScriptGroup.Builder(mRS);
        b.addKernel(scriptYuvToRGB.getKernelID());
        b.addKernel(scriptBlur.getKernelID());
        b.addKernel(scriptColor.getKernelID());

        // Connection type - ARGB8888, from - yuv, to - blur
        b.addConnection(mRgbOutputAlloc.getType(), scriptYuvToRGB.getKernelID(), scriptBlur.getFieldID_Input());
        b.addConnection(mRgbOutputAlloc.getType(), scriptBlur.getKernelID(), scriptColor.getKernelID());
        mScriptGroup = b.create();

        // Set output
        mScriptGroup.setOutput(scriptColor.getKernelID(), mRgbOutputAlloc);
        scriptYuvToRGB.setInput(mYuvPreviewAlloc);
    }

    private void startCamera(SurfaceHolder holder) {
        Camera.Parameters camParameters = mCamera.getParameters();

        // Select size
        List<Camera.Size> sizes = camParameters.getSupportedPreviewSizes();
        // Usually the lowest quality
        mPreviewSize = sizes.get(sizes.size() - 1);

        // Set parameters
        camParameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        camParameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
        if (camParameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
        {
            camParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        mCamera.setParameters(camParameters);

        // Calculate orientation and rotation
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(0, info);

        int orientation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = (info.orientation - ORIENTATION_MAP.get(orientation) + 360) % 360;

        mCamera.setDisplayOrientation(degrees);

        // Prepare rotation and scale matrix for preview frame
        prepareMatrix(degrees);

        try
        {
            mCamera.setPreviewDisplay(holder);
            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    drawFrame(data, camera);
                }
            });
            mCamera.startPreview();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void prepareMatrix(int degrees) {
        mTransformMatrix = new Matrix();
        float scale = (float) mTextureView.getWidth() / (float) mPreviewSize.width;
        mTransformMatrix.setScale(scale, scale);
        mTransformMatrix.postRotate(degrees, mTextureView.getWidth() / 2, mTextureView.getHeight() / 2);
        // In case the view on the left side
        //matrix.postTranslate(-(canvasHeight - (mPreviewSize.height) * scale), 0);
    }

    private void drawFrame(byte[] data, Camera camera) {
        mYuvPreviewAlloc.copyFrom(data);
        mScriptGroup.execute();
        mRgbOutputAlloc.copyTo(mBitmap);

        Canvas canvas = mTextureView.lockCanvas();
        canvas.drawBitmap(mBitmap, mTransformMatrix, null);
        mTextureView.unlockCanvasAndPost(canvas);
    }

    private void obtainCamera() {
        if (mCamera == null)
        {
            mCamera = Camera.open(0);
        }
    }

    private void releaseCamera() {
        if (mCamera != null)
        {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();

            mCamera = null;
        }
    }
}
