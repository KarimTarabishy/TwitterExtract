package com.gp.extract.twitter.util;

import java.util.ArrayList;

public class ArrayUtil {

    public static void expInPlace(double[] a) {
        for (int i = 0; i < a.length; i++) {
            a[i] = Math.exp(a[i]);
        }
    }

    public static double sum(double[] a) {
        if (a == null) {
            return 0.0;
        }
        double result = 0.0;
        for (int i = 0; i < a.length; i++) {
            result += a[i];
        }
        return result;
    }

    public static <T> void  fill(ArrayList<T> s, int count)
    {
        for(int i = 0; i < count; i++)
        {
            s.add(null);
        }
    }
}
