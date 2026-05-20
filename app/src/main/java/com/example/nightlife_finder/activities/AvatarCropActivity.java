package com.example.nightlife_finder.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.nightlife_finder.R;
import com.example.nightlife_finder.utils.AppSettings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class AvatarCropActivity extends BaseActivity {

    public static final String EXTRA_IMAGE_URI = "IMAGE_URI";

    private ImageView cropImageView;
    private View cropFrame;

    private Bitmap sourceBitmap;

    private final Matrix imageMatrix = new Matrix();
    private final Matrix savedMatrix = new Matrix();

    private final PointF startPoint = new PointF();

    private ScaleGestureDetector scaleDetector;

    private float currentScale = 1f;
    private static final float MIN_SCALE = 0.8f;
    private static final float MAX_SCALE = 5f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar_crop);

        cropImageView = findViewById(R.id.cropImageView);
        cropFrame = findViewById(R.id.cropFrame);

        scaleDetector = new ScaleGestureDetector(this, new ScaleListener());

        findViewById(R.id.btnCancelCrop).setOnClickListener(v -> finish());
        findViewById(R.id.btnSaveCrop).setOnClickListener(v -> saveCroppedAvatar());

        loadImage();
        setupTouch();
    }

    private void loadImage() {
        String uriString = getIntent().getStringExtra(EXTRA_IMAGE_URI);

        if (uriString == null) {
            Toast.makeText(this, "Không tìm thấy ảnh", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            Uri uri = Uri.parse(uriString);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), uri);
                sourceBitmap = ImageDecoder.decodeBitmap(source);
            } else {
                sourceBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            }

            cropImageView.setImageBitmap(sourceBitmap);

            cropImageView.post(this::fitImageToCenter);

        } catch (Exception e) {
            Toast.makeText(this, "Không thể mở ảnh", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fitImageToCenter() {
        if (sourceBitmap == null) return;

        float viewWidth = cropImageView.getWidth();
        float viewHeight = cropImageView.getHeight();

        float bitmapWidth = sourceBitmap.getWidth();
        float bitmapHeight = sourceBitmap.getHeight();

        float scale = Math.max(viewWidth / bitmapWidth, viewHeight / bitmapHeight);

        float dx = (viewWidth - bitmapWidth * scale) / 2f;
        float dy = (viewHeight - bitmapHeight * scale) / 2f;

        imageMatrix.reset();
        imageMatrix.postScale(scale, scale);
        imageMatrix.postTranslate(dx, dy);

        currentScale = scale;

        cropImageView.setImageMatrix(imageMatrix);
    }

    private void setupTouch() {
        cropImageView.setOnTouchListener((v, event) -> {
            scaleDetector.onTouchEvent(event);

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    savedMatrix.set(imageMatrix);
                    startPoint.set(event.getX(), event.getY());
                    return true;

                case MotionEvent.ACTION_MOVE:
                    if (!scaleDetector.isInProgress()) {
                        imageMatrix.set(savedMatrix);
                        float dx = event.getX() - startPoint.x;
                        float dy = event.getY() - startPoint.y;
                        imageMatrix.postTranslate(dx, dy);
                        cropImageView.setImageMatrix(imageMatrix);
                    }
                    return true;
            }

            return true;
        });
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            float newScale = currentScale * scaleFactor;

            if (newScale < MIN_SCALE || newScale > MAX_SCALE) {
                return true;
            }

            imageMatrix.postScale(
                    scaleFactor,
                    scaleFactor,
                    detector.getFocusX(),
                    detector.getFocusY()
            );

            currentScale = newScale;
            cropImageView.setImageMatrix(imageMatrix);

            return true;
        }
    }

    private void saveCroppedAvatar() {
        if (sourceBitmap == null) return;

        try {
            RectF cropRect = new RectF(
                    cropFrame.getLeft(),
                    cropFrame.getTop(),
                    cropFrame.getRight(),
                    cropFrame.getBottom()
            );

            Matrix inverse = new Matrix();
            imageMatrix.invert(inverse);
            inverse.mapRect(cropRect);

            int x = Math.max(0, Math.round(cropRect.left));
            int y = Math.max(0, Math.round(cropRect.top));
            int width = Math.min(sourceBitmap.getWidth() - x, Math.round(cropRect.width()));
            int height = Math.min(sourceBitmap.getHeight() - y, Math.round(cropRect.height()));

            if (width <= 0 || height <= 0) {
                Toast.makeText(this, "Vui lòng căn lại ảnh", Toast.LENGTH_SHORT).show();
                return;
            }

            Bitmap cropped = Bitmap.createBitmap(sourceBitmap, x, y, width, height);
            Bitmap finalAvatar = Bitmap.createScaledBitmap(cropped, 512, 512, true);

            File file = new File(getFilesDir(), "avatar_profile.png");

            FileOutputStream outputStream = new FileOutputStream(file);
            finalAvatar.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

            AppSettings.setAvatarPath(this, file.getAbsolutePath());

            Intent resultIntent = new Intent();
            resultIntent.putExtra("AVATAR_PATH", file.getAbsolutePath());
            setResult(RESULT_OK, resultIntent);

            finish();

        } catch (Exception e) {
            Toast.makeText(this, "Lưu ảnh thất bại", Toast.LENGTH_SHORT).show();
        }
    }
}