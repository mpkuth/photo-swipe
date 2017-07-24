package com.mpkuth.photoswipe;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.util.ArrayList;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.provider.MediaStore.Images.Media._ID;
import static android.provider.MediaStore.Images.Media.DATA;
import static android.provider.MediaStore.Images.Media.MIME_TYPE;
import static android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

public class MainActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback,
        SnackbarCallback.OnDismissedListener,
        SwipeDetector.OnSwipeListener {

    private int mIndex;
    private String mChooserTitle;
    private Snackbar mSnackbar;
    private SubsamplingScaleImageView mImageView;

    private final ArrayList<Image> mImages = new ArrayList<>();

    private static final int REQUEST_CODE_PERMISSIONS = 1234;
    private static final String URI = "uri";
    private static final String[] PROJECTION = { _ID, DATA, MIME_TYPE };
    private static final String[] PERMISSIONS = { READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mChooserTitle = getString(R.string.share_photo);

        mImageView = (SubsamplingScaleImageView) findViewById(R.id.image_view);
        mImageView.setOnTouchListener(new SwipeDetector(this, this));

        if (savedInstanceState != null && savedInstanceState.containsKey(URI)) {
            loadImages((Uri) savedInstanceState.getParcelable(URI));
        } else {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mIndex < mImages.size()) {
            Image image = mImages.get(mIndex);
            outState.putParcelable(URI, image.uri);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0, l = permissions.length; i < l; i++) {
            if (permissions[i].equals(READ_EXTERNAL_STORAGE)) {
                if (grantResults[i] == PERMISSION_GRANTED) {
                    loadImages(null);
                }
                break;
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    private void loadImages(@Nullable Uri previouslyShownImageUri) {
        Cursor cursor = getContentResolver().query(EXTERNAL_CONTENT_URI, PROJECTION, null, null, null);

        if (cursor != null) {
            int idColumn = cursor.getColumnIndexOrThrow(_ID);
            int dataColumn = cursor.getColumnIndexOrThrow(DATA);
            int mimeTypeColumn = cursor.getColumnIndexOrThrow(MIME_TYPE);

            mImages.clear();
            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColumn);
                String data = cursor.getString(dataColumn);
                String mimeType = cursor.getString(mimeTypeColumn);
                Uri uri = ContentUris.withAppendedId(EXTERNAL_CONTENT_URI, id);
                Image image = new Image(uri, data, mimeType);
                if (image.uri.equals(previouslyShownImageUri)) {
                    mIndex = mImages.size();
                }
                mImages.add(image);
            }

            cursor.close();
        }

        showImage(mIndex);
    }

    private void showImage(int i) {
        int size = mImages.size();

        if (i < 0) {
            mIndex = 0;
        } else if (i < size) {
            mIndex = i;
        } else {
            mIndex = size - 1;
        }

        Image image = mImages.get(mIndex);
        mImageView.setImage(ImageSource.uri(image.data));
    }

    private void clearSnackbar() {
        if (mSnackbar != null) {
            if (mSnackbar.isShown()) {
                mSnackbar.dismiss();
            }
            mSnackbar = null;
        }
    }

    @Override
    public boolean onSwipeUp() {
        Image image = mImages.get(mIndex);
        Intent shareIntent = new Intent();
        shareIntent.setType(image.mimeType);
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, image.uri);
        Intent chooserIntent = Intent.createChooser(shareIntent, mChooserTitle);
        startActivity(chooserIntent);
        clearSnackbar();
        return true;
    }

    @Override
    public boolean onSwipeLeft() {
        showImage(mIndex + 1);
        clearSnackbar();
        return true;
    }

    @Override
    public boolean onSwipeRight() {
        showImage(mIndex - 1);
        clearSnackbar();
        return true;
    }

    @Override
    public boolean onSwipeDown() {
        if (ActivityCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED) {
            clearSnackbar();
            mSnackbar = Snackbar.make(mImageView, R.string.image_deleted, Snackbar.LENGTH_INDEFINITE);
            mSnackbar.addCallback(new SnackbarCallback(this, mImages.remove(mIndex), mIndex));
            mSnackbar.setAction(R.string.undo, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // If listener is null the action doesn't show
                }
            });
            showImage(mIndex);
            mSnackbar.show();
        } else {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
        return true;
    }

    @Override
    public void onRollback(@NonNull Image image, int index) {
        mImages.add(index, image);
        showImage(index);
    }

    @Override
    public void onCommit(@NonNull Image image, int index) {
        getContentResolver().delete(image.uri, null, null);
    }

}
