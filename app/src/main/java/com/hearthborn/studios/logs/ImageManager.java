package com.hearthborn.studios.logs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class ImageManager {

    private static final String IMAGE_DIR = "user_images";

    public static String saveImage(Context context, Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
            if (bitmap == null) return null;

            File imageDir = new File(context.getFilesDir(), IMAGE_DIR);
            if (!imageDir.exists()) {
                imageDir.mkdirs();
            }

            String filename = "img_" + UUID.randomUUID().toString() + ".png";
            File imageFile = new File(imageDir, filename);

            try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            }

            return filename;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap loadImage(Context context, String filename) {
        try {
            File imageDir = new File(context.getFilesDir(), IMAGE_DIR);
            File imageFile = new File(imageDir, filename);

            if (!imageFile.exists()) {
                return null;
            }

            return BitmapFactory.decodeFile(imageFile.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void deleteImage(Context context, String filename) {
        if (filename == null || filename.isEmpty()) {
            return;
        }
        try {
            File imageDir = new File(context.getFilesDir(), IMAGE_DIR);
            File imageFile = new File(imageDir, filename);
            if (imageFile.exists()) {
                imageFile.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
