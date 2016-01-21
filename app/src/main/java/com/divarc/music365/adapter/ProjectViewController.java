package com.divarc.music365.adapter;

import android.view.View;
import java.util.HashMap;

public class ProjectViewController {
    private static ProjectViewController instance;


    HashMap<String, View> integerViewHashMap;


    private ProjectViewController() {
        integerViewHashMap  = new HashMap<>();
    }

    public static ProjectViewController getInstance() {
        if(instance == null){
            instance = new ProjectViewController();
        }
        return instance;
    }
}
