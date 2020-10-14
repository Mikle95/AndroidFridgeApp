package com.fridgeapp;

import android.graphics.Bitmap;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

public class Item {
    public String name;
    public Date BBD;
    public Bitmap image = null;
    public float k = 0;

    public Item(String name, Date bbd) {
        this.name = name;
        BBD = bbd;

        BigDecimal time = new BigDecimal(BBD.getTime() - new Date().getTime());
        long week = 7*24*60*60*1000;
        BigDecimal wk = BigDecimal.valueOf(week);
        time = time.divide(wk, 2, RoundingMode.HALF_UP);

        if(time.compareTo(BigDecimal.ZERO) < 0)
            k = 1;
        else
            if(time.compareTo(BigDecimal.ONE) > 0)
                k = 0;
            else {
                time = new BigDecimal(1).subtract(time);
                k = time.floatValue();
            }
    }

    public Item(String name, Date bbd, Bitmap im){
        this(name, bbd);
        image = im;
    }
}
