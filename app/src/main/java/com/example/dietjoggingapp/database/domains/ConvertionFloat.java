package com.example.dietjoggingapp.database.domains;

import java.util.ArrayList;
import java.util.List;

public class ConvertionFloat {
    public float[] converter(ArrayList<Float> data) {
        List<Float> floatList = new ArrayList<Float>();
        floatList = data;
        float[] floatArray = new float[floatList.size()];
        int i = 0;

        for (Float f : floatList) {
            floatArray[i++] = (f != null ? f : Float.NaN); // Or whatever default you want.
        }
        return floatArray;
    }
}
