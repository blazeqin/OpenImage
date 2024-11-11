package com.flyjingfish.openimagelib.utils;

import android.util.Pair;

public class DataCollectEventLiveData extends SingleLiveEvent<Pair<String,String>> {
    private static DataCollectEventLiveData instance;

    public static DataCollectEventLiveData getInstance() {
        if (instance == null) {
            instance = new DataCollectEventLiveData();
        }
        return instance;
    }
}