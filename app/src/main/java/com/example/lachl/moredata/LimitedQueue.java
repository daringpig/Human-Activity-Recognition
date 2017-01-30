package com.example.lachl.moredata;

import java.util.ArrayList;

/**
 * Created by kazooy on 29/01/17.
 */

public class LimitedQueue<Float> extends ArrayList<Float> {

    private int maxSize;

    public LimitedQueue(int size) {
        this.maxSize = size;
        for (int i = 0; i < size; i++) {
            Object zero = 0.0f;
            Float zero2 = (Float) zero;
            super.add(zero2);
        }
    }

    public boolean add(Float k) {
        boolean r = super.add(k);
        if (size() > maxSize){
            removeRange(0, size() - maxSize - 1);
        }
        return r;
    }

    public Float getYongest() {
        return get(size() - 1);
    }

    public Float getOldest() {
        return get(0);
    }

    public Float getIndex(int index) {
        if (index < size() && index >= 0) {
            return get(index);
        }
        return null;
    }
}

