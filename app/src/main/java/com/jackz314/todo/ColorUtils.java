package com.jackz314.todo;

import android.graphics.Color;

import static java.lang.Math.sqrt;

/**
 * Created by zhang on 2017/7/1.
 */

public class ColorUtils {//modifies colors, input (int)color and (double)fraction to change
    public static int lighten(int color, double fraction) {
        double luma = determineBrightness(color);
        if(luma >= 220){//determine brightness and see if needed to convert to lighten from 0-255, 0 is black, 255 is white
            System.out.println(luma + " luma");
            return darken(color, fraction);
        }else {
            int red = Color.red(color);
            int green = Color.green(color);
            int blue = Color.blue(color);
            red = lightenColor(red, fraction);
            green = lightenColor(green, fraction);
            blue = lightenColor(blue, fraction);
            int alpha = Color.alpha(color);
            return Color.argb(alpha, red, green, blue);
        }
    }

    public static double determineBrightness(int color){
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        double luma = 0.2126 * red + 0.7152 * green + 0.0722 * blue;
        return luma;
    }

    public static double determineSimilarColor(int color1, int color2){
        int r1 = Color.red(color1);
        int g1 = Color.green(color1);
        int b1 = Color.blue(color1);
        int r2 = Color.red(color2);
        int g2 = Color.green(color2);
        int b2 = Color.blue(color2);
        double difference = sqrt((r2-r1)^2+(g2-g1)^2+(b2-b1)^2);
        double percentage = difference/sqrt(3*((255)^2));
        return percentage;
    }

    public static int darken(int color, double fraction) {
        double luma = determineBrightness(color);
        if(luma <= 30 ){//determine brightness and see if needed to convert to lighten
            return lighten(color, fraction);
        } else {
            int red = Color.red(color);
            int green = Color.green(color);
            int blue = Color.blue(color);
            red = darkenColor(red, fraction);
            green = darkenColor(green, fraction);
            blue = darkenColor(blue, fraction);
            int alpha = Color.alpha(color);
            return Color.argb(alpha, red, green, blue);
        }
    }

    private static int darkenColor(int color, double fraction) {
        return (int)Math.max(color - (color * fraction), 0);
    }

    private static int lightenColor(int color, double fraction) {
        return (int) Math.min(color + (255 * fraction), 255);
    }
}
