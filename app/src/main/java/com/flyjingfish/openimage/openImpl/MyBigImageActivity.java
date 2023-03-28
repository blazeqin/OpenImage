package com.flyjingfish.openimage.openImpl;

import android.view.View;
import android.widget.FrameLayout;

import androidx.viewpager2.widget.ViewPager2;

import com.flyjingfish.openimage.databinding.MyActivityViewpagerBinding;
import com.flyjingfish.openimagelib.OpenImageActivity;
import com.flyjingfish.openimagelib.widget.TouchCloseLayout;

public class MyBigImageActivity extends OpenImageActivity {

    private MyActivityViewpagerBinding rootBinding;

    @Override
    public View getContentView() {
        rootBinding = MyActivityViewpagerBinding.inflate(getLayoutInflater());
        return rootBinding.getRoot();
    }

    @Override
    public View getBgView() {
        return rootBinding.vBg;
    }


    @Override
    public FrameLayout getViewPager2Container() {
        return rootBinding.flTouchView;
    }

    @Override
    public ViewPager2 getViewPager2() {
        return rootBinding.viewPager;
    }

    @Override
    public TouchCloseLayout getTouchCloseLayout() {
        return rootBinding.getRoot();
    }
}
