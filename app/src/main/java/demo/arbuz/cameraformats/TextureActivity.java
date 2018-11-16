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

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;

import java.io.IOException;
import java.util.List;

public class TextureActivity extends AppCompatActivity {
    private static final SparseIntArray ORIENTATION_MAP = new SparseIntArray();

    private Camera      mCamera;
    private TextureView mTextureView;

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
        setContentView(R.layout.activity_texture);

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
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                startCamera(surface);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
    }

    private void startCamera(SurfaceTexture surface) {
        Camera.Parameters camParameters = mCamera.getParameters();

        List<Camera.Size> sizes = camParameters.getSupportedPreviewSizes();
        // Usually the highest quality
        Camera.Size s = sizes.get(0);

        camParameters.setPreviewSize(s.width, s.height);
        camParameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
        if (camParameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
        {
            camParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        mCamera.setParameters(camParameters);

        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(0, info);

        int orientation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = (info.orientation - ORIENTATION_MAP.get(orientation) + 360) % 360;

        mCamera.setDisplayOrientation(degrees);

        try
        {
            mCamera.setPreviewTexture(surface);
            mCamera.startPreview();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
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
