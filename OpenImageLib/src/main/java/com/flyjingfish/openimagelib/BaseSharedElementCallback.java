package com.flyjingfish.openimagelib;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.SharedElementCallback;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.View;

import com.flyjingfish.openimagelib.utils.ActivityCompatHelper;

import java.util.List;

class BaseSharedElementCallback extends SharedElementCallback {
    protected Context context;

    public BaseSharedElementCallback(Context context) {
        this.context = context;
    }
    @Override
    public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
        super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots);
        Activity activity = ActivityCompatHelper.getActivity(context);
        if (activity != null){
            activity.setExitSharedElementCallback(null);
            fixAndroid12Bug();
        }
    }
    private void fixAndroid12Bug(){
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R && OpenImageConfig.getInstance().isFixAndroid12OnBackPressed()) {
            context.startActivity(new Intent(context, FixAndroid12BugActivity.class), ActivityOptions.makeSceneTransitionAnimation((Activity) context).toBundle());
        }
    }
}