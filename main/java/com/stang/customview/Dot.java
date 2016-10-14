package com.stang.customview;

import android.util.Log;

import static java.lang.Math.abs;

/**
 * Created by StanG on 13.10.2016.
 */

public class Dot {
    private int x;
    private int y;

    private int startX;
    private int endX;
    private int startY;
    private int endY;

    public boolean rotated = false;


    public Dot(int sx, int sy, int ex, int ey, boolean rotated) {
        this.startX = sx;
        this.startY = sy;
        this.endX = ex;
        this.endY = ey;

        this.x = sx;
        this.y = sy;

        this.rotated = rotated;
    }


    public void calcPosition(float p) {

        int percent = (int)p;

        if(p < 1 ) {
            percent = 0;
        } else if(p > 99) {
            percent = 100;
        }

        if((percent >= 0) && (percent < 50)) {
            if(rotated) {
                y = startY;
                x = (int)(startX + (double)(endX - startX) * ((double)percent / 100D * 2D));
            } else {
                x = startX;
                y = (int)(startY + (double)(endY - startY) * ((double)percent / 100D * 2D));
            };
        } else if((percent >= 50) && (percent <= 100)) {
            if(rotated) {
                x = endX;
                y = (int)(startY + (double)(endY - startY) * ((double)(percent - 50) / 100D * 2D));
            } else {
                y = endY;
                x = (int)(startX + (double)(endX - startX) * ((double)(percent - 50) / 100D * 2D));
            };
        }

    }


    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public String toString() {
        return "start: " + startX + "," + startY + "; end: " + endX + "," + endY + "; current: " + x + "," + y;
    }
}
