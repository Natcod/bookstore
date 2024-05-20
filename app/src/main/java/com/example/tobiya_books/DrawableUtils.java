package com.example.tobiya_books;

import android.content.Context;
import android.content.res.Resources;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class DrawableUtils {

    public static Map<String, Integer> getDrawableMap(Context context) {
        Map<String, Integer> drawableMap = new HashMap<>();
        try {
            Field[] fields = R.drawable.class.getDeclaredFields();
            for (Field field : fields) {
                if (field.getType().equals(int.class)) {
                    String name = field.getName();
                    int resId = field.getInt(null);
                    drawableMap.put(name, resId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return drawableMap;
    }
}

