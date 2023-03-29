package com.flyjingfish.openimagelib;

import android.animation.AnimatorSet;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.flyjingfish.openimagelib.beans.OpenImageDetail;
import com.flyjingfish.openimagelib.beans.OpenImageUrl;
import com.flyjingfish.openimagelib.enums.ImageDiskMode;
import com.flyjingfish.openimagelib.listener.ItemLoadHelper;
import com.flyjingfish.openimagelib.listener.OnItemClickListener;
import com.flyjingfish.openimagelib.listener.OnItemLongClickListener;
import com.flyjingfish.shapeimageviewlib.ShapeImageView;

abstract class BaseFragment extends BaseInnerFragment {

    protected OpenImageDetail imageDetail;
    protected OpenImageUrl openImageUrl;
    protected int showPosition,clickPosition;
    protected PhotosViewModel photosViewModel;
    protected Handler mHandler = new Handler(Looper.getMainLooper());
    protected boolean isLoadSuccess;
    protected boolean disableClickClose;
    protected ImageDiskMode imageDiskMode;
    protected int errorResId;
    protected Drawable coverDrawable;
    protected boolean isNoneClickView;
    private String dataKey;

    public abstract View getExitImageView();
    protected void onTransitionEnd(){}
    protected boolean isTransitionEnd;
    protected boolean isInitImage;
    protected boolean isLoading;
    protected boolean isStartCoverAnim = true;
    protected ShapeImageView.ShapeScaleType srcScaleType;
    protected AnimatorSet coverAnim;
    protected ItemLoadHelper itemLoadHelper;
    protected float autoAspectRadio;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null){
            dataKey = bundle.getString(OpenParams.IMAGE);
            imageDetail = ImageLoadUtils.getInstance().getOpenImageDetail(dataKey);
            openImageUrl = imageDetail.openImageUrl;
            int imageDiskModeInt = bundle.getInt(OpenParams.IMAGE_DISK_MODE,-1);
            imageDiskMode = imageDiskModeInt == -1 ? ImageDiskMode.NONE : ImageDiskMode.values()[imageDiskModeInt];
            showPosition = bundle.getInt(OpenParams.SHOW_POSITION);
            clickPosition = bundle.getInt(OpenParams.CLICK_POSITION);
            int srcScaleTypeInt = bundle.getInt(OpenParams.SRC_SCALE_TYPE,-1);
            srcScaleType = srcScaleTypeInt == -1 ? null : ShapeImageView.ShapeScaleType.values()[srcScaleTypeInt];;
            errorResId = bundle.getInt(OpenParams.ERROR_RES_ID,0);
            String itemLoadKey = bundle.getString(OpenParams.ITEM_LOAD_KEY);
            itemLoadHelper = ImageLoadUtils.getInstance().getItemLoadHelper(itemLoadKey);
            disableClickClose = getArguments().getBoolean(OpenParams.DISABLE_CLICK_CLOSE,false);
            String onItemCLickKey = getArguments().getString(OpenParams.ON_ITEM_CLICK_KEY);
            String onItemLongCLickKey = getArguments().getString(OpenParams.ON_ITEM_LONG_CLICK_KEY);
            OnItemClickListener onItemClickListener = ImageLoadUtils.getInstance().getOnItemClickListener(onItemCLickKey);
            OnItemLongClickListener onItemLongClickListener = ImageLoadUtils.getInstance().getOnItemLongClickListener(onItemLongCLickKey);
            if (onItemClickListener != null){
                onItemClickListeners.add(onItemClickListener);
            }
            if (onItemLongClickListener != null){
                onItemLongClickListeners.add(onItemLongClickListener);
            }
            coverDrawable = ImageLoadUtils.getInstance().getCoverDrawable(bundle.getString(OpenParams.OPEN_COVER_DRAWABLE));

            autoAspectRadio = bundle.getFloat(OpenParams.AUTO_ASPECT_RATIO,0);
            isNoneClickView = bundle.getBoolean(OpenParams.NONE_CLICK_VIEW,false);
        }
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        photosViewModel = new ViewModelProvider(requireActivity()).get(PhotosViewModel.class);
        photosViewModel.transitionEndLiveData.observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean){
                isTransitionEnd = true;
                onTransitionEnd();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (coverAnim != null){
            coverAnim.removeAllListeners();
            coverAnim.cancel();
        }
        itemLoadHelper = null;
        coverDrawable = null;
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ImageLoadUtils.getInstance().clearOpenImageDetail(dataKey);
    }
}
