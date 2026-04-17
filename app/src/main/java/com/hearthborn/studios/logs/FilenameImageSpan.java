package com.hearthborn.studios.logs;

import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;

public class FilenameImageSpan extends ImageSpan {
    private String filename;

    public FilenameImageSpan(Drawable drawable, String filename) {
        super(drawable, ALIGN_BASELINE);
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }
}
