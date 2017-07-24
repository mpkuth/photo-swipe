package com.mpkuth.photoswipe;

import android.net.Uri;
import android.support.annotation.NonNull;

class Image {

    @NonNull
    final Uri uri;

    @NonNull
    final String data;

    @NonNull
    final String mimeType;

    Image(@NonNull Uri uri, @NonNull String data, @NonNull String mimeType) {
        this.mimeType = mimeType;
        this.data = data;
        this.uri = uri;
    }

}
