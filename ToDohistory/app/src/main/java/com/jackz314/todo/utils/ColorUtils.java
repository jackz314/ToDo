package com.jackz314.todo.utils;

import android.graphics.Color;

import static java.lang.Math.sqrt;

/**
 * Created by zhang on 2017/7/1.
 */

public class ColorUtils {//modifies colors, input (int)color and (double)fraction to change
    public static int lighten(int color, double fraction) {
        double luma = determineBrightness(color);
        if(luma >= 220){//determine brightness and see if needed to convert to lighten from 0-255, 0 is black, 255 is white
            //System.out.println(luma + " luma");
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

    public static int makeTransparent(int color, double fraction){
        return android.support.v4.graphics.ColorUtils.setAlphaComponent(color,((int)(255 * fraction)));
    }

    public static double determineBrightness(int color){//from 0 to 1, 0 is black, 1 is white
        /*int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        double luma = 0.2126 * red + 0.7152 * green + 0.0722 * blue;
        return luma;*/
        double red = Color.red(color) / 255.0;
        red = red < 0.03928 ? red / 12.92 : Math.pow((red + 0.055) / 1.055, 2.4);
        double green = Color.green(color) / 255.0;
        green = green < 0.03928 ? green / 12.92 : Math.pow((green + 0.055) / 1.055, 2.4);
        double blue = Color.blue(color) / 255.0;
        blue = blue < 0.03928 ? blue / 12.92 : Math.pow((blue + 0.055) / 1.055, 2.4);
        return (float) ((0.2126 * red) + (0.7152 * green) + (0.0722 * blue));
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

    public static boolean colorIsSimilar(int color1, int color2){
        int red1 = Color.red(color1);
        int green1 = Color.green(color1);
        int blue1 = Color.blue(color1);
        int red2 = Color.red(color2);
        int green2 = Color.green(color2);
        int blue2 = Color.blue(color2);
        double distance = Math.sqrt(Math.pow((red1 - red2),2) + Math.pow((green1 - green2),2) + Math.pow((blue1 - blue2),2));
        //System.out.println(distance + "DISTANCE");
        if(distance < 100){
            return true;
        }else{
            return false;
        }
    }

    private static int darkenColor(int color, double fraction) {
        return (int)Math.max(color - (color * fraction), 0);
    }

    private static int lightenColor(int color, double fraction) {
        return (int) Math.min(color + (255 * fraction), 255);
    }
}
