package com.example.practica;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

public class ImageUtils {
    public static String saveImage(Context context, Bitmap bitmap) {
        File directory = context.getDir("profile_images", Context.MODE_PRIVATE);
        File file = new File(directory, "user_avatar.jpg");

        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            return file.getAbsolutePath();
        } catch (Exception e) {
            Log.e("ImageUtils", "Error saving image: " + e.getMessage());
            return null;
        }
    }

    public static Bitmap loadImage(Context context) {
        File directory = context.getDir("profile_images", Context.MODE_PRIVATE);
        File file = new File(directory, "user_avatar.jpg");
        return BitmapFactory.decodeFile(file.getAbsolutePath());
    }
}