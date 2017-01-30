package com.example.lachl.moredata;

import java.util.ArrayList;

/**
 * Computes the average, min, max, stdDev, and kurtosis of an ArrayList
 * Created by lachl on 7/12/2016. Modified dgokeffe 25/1/2017.
 */
public class GetValues {
    private float average;
    private float fourthMoment;
    private float secondMoment;
    private float stdDev;
    private float max = -1000000;
    private float min = 1000000;
    private float kurtosis;

    public float[] findValues(ArrayList<Float> makeAverage) {
        max = -1000000;
        min = 1000000;

        for (int i = 0; i < makeAverage.size(); i++) {
            average = average + makeAverage.get(i);
            if(makeAverage.get(i) > max){max = makeAverage.get(i);}
            if(makeAverage.get(i) < min){min = makeAverage.get(i);}
        }
        average = average / makeAverage.size();

        for (int i = 0; i < makeAverage.size(); i++) {
            fourthMoment = fourthMoment + (float)Math.pow((makeAverage.get(i) - average), 4);
            secondMoment = secondMoment + ((makeAverage.get(i)-average)*(makeAverage.get(i)*average));
        }
        stdDev = secondMoment/makeAverage.size();
        kurtosis = (fourthMoment*makeAverage.size())/(secondMoment*secondMoment);
        float[] meanAndKurtosis = new float[5];

        meanAndKurtosis[0] = average;
        meanAndKurtosis[1] = max;
        meanAndKurtosis[2] = min;
        meanAndKurtosis[3] = stdDev;
        meanAndKurtosis[4] = kurtosis;

        return meanAndKurtosis;
    }
}
