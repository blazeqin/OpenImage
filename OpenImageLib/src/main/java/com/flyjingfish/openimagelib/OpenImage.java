package com.flyjingfish.openimagelib;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.SharedElementCallback;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.flyjingfish.openimagelib.beans.OpenImageUrl;
import com.flyjingfish.openimagelib.beans.OpenImageDetail;
import com.flyjingfish.openimagelib.enums.BackViewType;
import com.flyjingfish.openimagelib.enums.ImageDiskMode;
import com.flyjingfish.openimagelib.enums.MediaType;
import com.flyjingfish.openimagelib.enums.MoreViewShowType;
import com.flyjingfish.openimagelib.listener.ItemLoadHelper;
import com.flyjingfish.openimagelib.listener.OnItemClickListener;
import com.flyjingfish.openimagelib.listener.OnItemLongClickListener;
import com.flyjingfish.openimagelib.listener.OnLoadBigImageListener;
import com.flyjingfish.openimagelib.listener.OnLoadViewFinishListener;
import com.flyjingfish.openimagelib.listener.OnSelectMediaListener;
import com.flyjingfish.openimagelib.listener.SourceImageViewGet;
import com.flyjingfish.openimagelib.listener.SourceImageViewIdGet;
import com.flyjingfish.openimagelib.utils.ActivityCompatHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class OpenImage {
    private Context context;
    private final List<OpenImageUrl> openImageUrls = new ArrayList<>();
    private List<ImageView> imageViews;
    private RecyclerView recyclerView;
    private AbsListView absListView;
    private ViewPager2 viewPager2;
    private ViewPager viewPager;
    private long openPageAnimTimeMs;
    private int clickViewPosition;
    private int clickDataPosition;
    private int errorResId;
    private int openImageStyle;
    private ImageView.ScaleType srcImageViewScaleType;
    private boolean autoSetScaleType;
    private ImageDiskMode imageDiskMode = ImageDiskMode.CONTAIN_ORIGINAL;
    private final HashSet<Integer> srcImageWidthCache = new HashSet<>();
    private final HashSet<Integer> srcImageHeightCache = new HashSet<>();
    private final HashSet<Integer> srcVideoWidthCache = new HashSet<>();
    private final HashSet<Integer> srcVideoHeightCache = new HashSet<>();
    private boolean isStartActivity;
    private boolean isAutoScrollScanPosition = false;
    private boolean wechatExitFillInEffect = false;
    private SourceImageViewIdGet<OpenImageUrl> sourceImageViewIdGet;
    private SourceImageViewGet<OpenImageUrl> sourceImageViewGet;
    private int leftRightShowWidthDp;
    public static boolean isCanOpen = true;
    private String onselectKey;
    private String pageTransformersKey;
    private String onItemClickListenerKey;
    private String onItemLongClickListenerKey;
    private String itemLoadHelperKey;
    private boolean disableClickClose;
    private boolean showSrcImageView = true;
    private final List<MoreViewOption> moreViewOptions = new ArrayList<>();
    private SrcViewType srcViewType;
    private String backViewKey;
    private String moreViewOptionKey;

    private enum SrcViewType {
        RV, AB_LIST, VP, VP2, IV
    }

    public static OpenImage with(Context context) {
        return new OpenImage(context);
    }

    private OpenImage(Context context) {
        if (context instanceof Activity) {
            this.context = context;
        } else {
            throw new IllegalArgumentException("context must be activity");
        }
    }

    /**
     * @param openImageUrls 图片数据组
     * @return
     */
    public OpenImage setImageUrlList(List<? extends OpenImageUrl> openImageUrls) {
        this.openImageUrls.addAll(openImageUrls);
        return this;
    }

    /**
     * @param openImageUrl 单个图片数据可设置这个
     * @return
     */
    public OpenImage setImageUrl(OpenImageUrl openImageUrl) {
        return setImageUrlList(new ArrayList<>(Arrays.asList(openImageUrl)));
    }

    /**
     * @param recyclerView         展示数据的RecyclerView
     * @param sourceImageViewIdGet 展示数据的RecyclerView 的图片Id
     * @return
     */
    public OpenImage setClickRecyclerView(RecyclerView recyclerView, SourceImageViewIdGet<OpenImageUrl> sourceImageViewIdGet) {
        this.recyclerView = recyclerView;
        this.sourceImageViewIdGet = sourceImageViewIdGet;
        return this;
    }

    /**
     * @param viewPager2           展示数据的ViewPager2
     * @param sourceImageViewIdGet 展示数据的ViewPager2 的图片Id
     * @return
     */
    public OpenImage setClickViewPager2(ViewPager2 viewPager2, SourceImageViewIdGet<OpenImageUrl> sourceImageViewIdGet) {
        this.viewPager2 = viewPager2;
        this.sourceImageViewIdGet = sourceImageViewIdGet;
        return this;
    }

    /**
     * @param viewPager          展示数据的ViewPager
     * @param sourceImageViewGet 展示数据的ViewPager 的图片ImageView
     * @return
     */
    public OpenImage setClickViewPager(ViewPager viewPager, SourceImageViewGet<OpenImageUrl> sourceImageViewGet) {
        this.viewPager = viewPager;
        this.sourceImageViewGet = sourceImageViewGet;
        return this;
    }

    /**
     * @param gridView             展示数据的GridView
     * @param sourceImageViewIdGet 展示数据的GridView 的图片Id
     * @return
     */
    public OpenImage setClickGridView(GridView gridView, SourceImageViewIdGet<OpenImageUrl> sourceImageViewIdGet) {
        this.absListView = gridView;
        this.sourceImageViewIdGet = sourceImageViewIdGet;
        return this;
    }

    /**
     * @param listView             展示数据的ListView
     * @param sourceImageViewIdGet 展示数据的ListView 的图片Id
     * @return
     */
    public OpenImage setClickListView(ListView listView, SourceImageViewIdGet<OpenImageUrl> sourceImageViewIdGet) {
        this.absListView = listView;
        this.sourceImageViewIdGet = sourceImageViewIdGet;
        return this;
    }

    /**
     * @param imageViews 自己传展示数据的ImageView组
     * @return
     */
    public OpenImage setClickImageViews(ImageView[] imageViews) {
        return setClickImageViews(new ArrayList<>(Arrays.asList(imageViews)));
    }

    /**
     * @param imageView 自己传展示数据的单个ImageView
     * @return
     */
    public OpenImage setClickImageView(ImageView imageView) {
        return setClickImageViews(new ArrayList<>(Arrays.asList(imageView)));
    }

    /**
     * @param imageViews 自己传展示数据的ImageView组
     * @return
     */
    public OpenImage setClickImageViews(List<ImageView> imageViews) {
        this.imageViews = imageViews;
        return this;
    }

    /**
     * 如果数据下标 和 RecyclerView、ViewPager2、ListView、GridView 的所在位置一致 可调用这个
     *
     * @param clickPosition 点击的图片和View所在的位置
     * @return
     */
    public OpenImage setClickPosition(int clickPosition) {
        return setClickPosition(clickPosition, clickPosition);
    }

    /**
     * 如果数据下标 和 RecyclerView、ViewPager2、ListView、GridView 的所在位置不一致 调用这个
     *
     * @param clickDataPosition 点击的图片所在数据的位置
     * @param clickViewPosition 点击的图片View在RecyclerView或ListView或GridView的位置
     * @return
     */
    public OpenImage setClickPosition(int clickDataPosition, int clickViewPosition) {
        this.clickDataPosition = clickDataPosition;
        this.clickViewPosition = clickViewPosition;
        return this;
    }

    /**
     * @param srcImageViewScaleType 点击的ImageView显示模式
     * @param autoSetScaleType      如果点击的ImageView与您所设置scaleType不相同，则自动设置
     * @return
     */
    public OpenImage setSrcImageViewScaleType(ImageView.ScaleType srcImageViewScaleType, boolean autoSetScaleType) {
        this.srcImageViewScaleType = srcImageViewScaleType;
        this.autoSetScaleType = autoSetScaleType;
        return this;
    }

    /**
     * @param imageDiskMode 点击的ImageView图片所缓存的模式（建议缓存原图）
     * @return
     */
    public OpenImage setImageDiskMode(ImageDiskMode imageDiskMode) {
        this.imageDiskMode = imageDiskMode;
        return this;
    }

    /**
     * 加载大图失败后
     * <p>
     * 一,如果设置此选项则展示这个errorResId图片
     * 二,如果不设置
     * 1> 有缓存则展示缓存图片
     * 2> 没有缓存图片就展示上个页面小图的加载失败图片（可在ItemLoadHelper.loadImage中设置加载失败图片）
     *
     * @param errorResId 大图加载失败后显示的图片
     * @return
     */
    public OpenImage setErrorResId(@DrawableRes int errorResId) {
        this.errorResId = errorResId;
        return this;
    }

    /**
     * @param itemLoadHelper 图片加载器，当图片缓存模式不包含原图时，请设置和前一页面加载图片一样的配置
     * @return
     */
    public OpenImage setItemLoadHelper(ItemLoadHelper itemLoadHelper) {
        itemLoadHelperKey = UUID.randomUUID().toString();
        ImageLoadUtils.getInstance().setItemLoadHelper(itemLoadHelperKey, itemLoadHelper);
        return this;
    }

    /**
     * @param openImageStyle 查看图片显示设置StyleId
     * @return
     */
    public OpenImage setOpenImageStyle(@StyleRes int openImageStyle) {
        this.openImageStyle = openImageStyle;
        return this;
    }

    /**
     * @param openPageAnimTimeMs 打开页面动画的时间
     * @return
     */
    public OpenImage setOpenPageAnimTimeMs(long openPageAnimTimeMs) {
        this.openPageAnimTimeMs = openPageAnimTimeMs;
        return this;
    }

    /**
     * @param onSelectMediaListener 回调查看图片所在数据的位置
     * @return
     */
    public OpenImage setOnSelectMediaListener(OnSelectMediaListener onSelectMediaListener) {
        onselectKey = UUID.randomUUID().toString();
        ImageLoadUtils.getInstance().setOnSelectMediaListener(onselectKey, onSelectMediaListener);
        return this;
    }

    /**
     * 只对传入RecyclerView，ViewPager，ViewPager2， ListView, GridView 有效
     *
     * @param autoScrollScanPosition 自动滑向最后看的图片的位置
     * @return
     */
    public OpenImage setAutoScrollScanPosition(boolean autoScrollScanPosition) {
        isAutoScrollScanPosition = autoScrollScanPosition;
        return this;
    }

    /**
     * @param pageTransformer ViewPager的页面切换效果
     * @return
     */
    public OpenImage addPageTransformer(ViewPager2.PageTransformer... pageTransformer) {
        pageTransformersKey = UUID.randomUUID().toString();
        ImageLoadUtils.getInstance().setPageTransformers(pageTransformersKey, new ArrayList<>(Arrays.asList(pageTransformer)));
        return this;
    }

    /**
     * @param leftRightShowWidthDp 可设置画廊效果，左右漏出的宽度，单位dp
     * @return
     */
    public OpenImage setGalleryEffect(int leftRightShowWidthDp) {
        this.leftRightShowWidthDp = leftRightShowWidthDp;
        return this;
    }

    /**
     * 设置微信补位效果，设置后当退出大图页面时，如果前一页面没有当前图片，则自动回到点击进来的那张图的位置
     * 开启后自动自动滚动效果关闭
     * (只对父容器是RecyclerView, ViewPager2，ListView, GridView 时有效)
     *
     * @param wechatExitFillInEffect 是否设置微信补位效果
     * @return
     */
    public OpenImage setWechatExitFillInEffect(boolean wechatExitFillInEffect) {
        this.wechatExitFillInEffect = wechatExitFillInEffect;
        return this;
    }

    /**
     * 设置点击图片监听
     *
     * @param onItemClickListener
     * @return
     */
    public OpenImage setOnItemClickListener(OnItemClickListener onItemClickListener) {
        onItemClickListenerKey = UUID.randomUUID().toString();
        ImageLoadUtils.getInstance().setOnItemClickListener(onItemClickListenerKey, onItemClickListener);
        return this;
    }

    /**
     * 设置长按图片监听
     *
     * @param onItemLongClickListener
     * @return
     */
    public OpenImage setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        onItemLongClickListenerKey = UUID.randomUUID().toString();
        ImageLoadUtils.getInstance().setOnItemLongClickListener(onItemLongClickListenerKey, onItemLongClickListener);
        return this;
    }

    /**
     * 禁用点击图片关闭页面功能
     *
     * @return
     */
    public OpenImage disableClickClose() {
        disableClickClose = true;
        return this;
    }

    /**
     * 添加View 到大图页面，此方法可多次调用，添加多个View
     *
     * @param layoutRes                添加的图片xml id
     * @param layoutParams             要添加到页面布局的参数
     * @param moreViewShowType         展示类型
     * @param onLoadViewFinishListener 加载完毕View后回调
     * @return
     */
    public OpenImage addMoreView(@LayoutRes int layoutRes, @NonNull FrameLayout.LayoutParams layoutParams, MoreViewShowType moreViewShowType, OnLoadViewFinishListener onLoadViewFinishListener) {
        MoreViewOption moreViewOption = new MoreViewOption(layoutRes, layoutParams, moreViewShowType, onLoadViewFinishListener);
        moreViewOptions.add(moreViewOption);
        return this;
    }

    /**
     * @param showSrcImageView 退出时，前一页面的ImageView是否可见
     * @return
     */
    public OpenImage setShowSrcImageView(boolean showSrcImageView) {
        this.showSrcImageView = showSrcImageView;
        return this;
    }

    /**
     * 打开大图页面
     */
    public void show() {
        if (isCanOpen) {
            Activity activity = ActivityCompatHelper.getActivity(context);
            activity.setExitSharedElementCallback(null);
            show4ParseData();
        }
    }

    private void show4ParseData() {
        if (openImageUrls.size() == 0) {
            throw new IllegalArgumentException("请设置数据");
        }
        if (itemLoadHelperKey == null) {
            throw new IllegalArgumentException("请设置ItemLoadHelper");
        }
        if (clickDataPosition >= openImageUrls.size()) {
            throw new IllegalArgumentException("clickDataPosition不能 >= OpenImageUrl 的个数");
        }
        if (wechatExitFillInEffect) {
            isAutoScrollScanPosition = false;
        }
        Intent intent = new Intent(context, ViewPagerActivity.class);

        intent.putExtra(OpenParams.CLICK_POSITION, clickDataPosition);
        if (onselectKey != null) {
            intent.putExtra(OpenParams.ON_SELECT_KEY, onselectKey);
        }
        if (pageTransformersKey != null) {
            intent.putExtra(OpenParams.PAGE_TRANSFORMERS, pageTransformersKey);
        }
        if (onItemClickListenerKey != null) {
            intent.putExtra(OpenParams.ON_ITEM_CLICK_KEY, onItemClickListenerKey);
        }
        if (onItemLongClickListenerKey != null) {
            intent.putExtra(OpenParams.ON_ITEM_LONG_CLICK_KEY, onItemLongClickListenerKey);
        }
        if (moreViewOptions.size() > 0) {
            moreViewOptionKey = UUID.randomUUID().toString();
            intent.putExtra(OpenParams.MORE_VIEW_KEY, moreViewOptionKey);
            ImageLoadUtils.getInstance().setMoreViewOption(moreViewOptionKey, moreViewOptions);
        }
        intent.putExtra(OpenParams.DISABLE_CLICK_CLOSE, disableClickClose);
        intent.putExtra(OpenParams.AUTO_SCROLL_SELECT, isAutoScrollScanPosition);
        intent.putExtra(OpenParams.DISABLE_TOUCH_CLOSE, OpenImageConfig.getInstance().isDisEnableTouchClose());
        intent.putExtra(OpenParams.SRC_SCALE_TYPE, srcImageViewScaleType);
        intent.putExtra(OpenParams.IMAGE_DISK_MODE, imageDiskMode);
        intent.putExtra(OpenParams.ERROR_RES_ID, errorResId);
        intent.putExtra(OpenParams.ITEM_LOAD_KEY, itemLoadHelperKey);
        intent.putExtra(OpenParams.TOUCH_CLOSE_SCALE, OpenImageConfig.getInstance().getTouchCloseScale());
        intent.putExtra(OpenParams.OPEN_IMAGE_STYLE, openImageStyle);
        intent.putExtra(OpenParams.OPEN_ANIM_TIME_MS, openPageAnimTimeMs);
        intent.putExtra(OpenParams.GALLERY_EFFECT_WIDTH, leftRightShowWidthDp);
        backViewKey = OpenImage.this.toString();
        if (recyclerView != null) {
            if (sourceImageViewIdGet == null) {
                throw new IllegalArgumentException("sourceImageViewIdGet 不能为null");
            }

            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (!(layoutManager instanceof LinearLayoutManager || layoutManager instanceof StaggeredGridLayoutManager)) {
                throw new IllegalArgumentException("只支持使用继承自LinearLayoutManager和StaggeredGridLayoutManager的RecyclerView");
            }
            srcViewType = SrcViewType.RV;

            ArrayList<OpenImageDetail> openImageDetails = new ArrayList<>();
            Pair<View, String> viewPair = initShareView(openImageDetails);
            if (viewPair == null) {
                throw new IllegalArgumentException("请确保是否调用了setClickPosition并且参数设置正确");
            }
            View shareViewClick = viewPair.first;
            String shareNameClick = viewPair.second;
            intent.putExtra(OpenParams.ON_BACK_VIEW, backViewKey);
            ImageLoadUtils.getInstance().setOnBackView(backViewKey, new ExitOnBackView4ListView(shareViewClick, openImageDetails) {
                @Override
                public void onScrollPos(int pos) {
                    if (isAutoScrollScanPosition) {
                        final RecyclerView.LayoutManager layoutManager =
                                recyclerView.getLayoutManager();
                        View viewAtPosition =
                                layoutManager.findViewByPosition(pos);
                        if (viewAtPosition == null
                                || layoutManager.isViewPartiallyVisible(viewAtPosition, true, true)) {
                            recyclerView.post(() -> layoutManager.scrollToPosition(pos));
                        }
                    }
                }
            });

            intent.putExtra(OpenParams.IMAGES, openImageDetails);
            replenishImageUrl(openImageDetails);
            postOpen(intent, viewPair);
        } else if (absListView != null) {
            if (sourceImageViewIdGet == null) {
                throw new IllegalArgumentException("sourceImageViewIdGet 不能为null");
            }
            srcViewType = SrcViewType.AB_LIST;
            ArrayList<OpenImageDetail> openImageDetails = new ArrayList<>();
            Pair<View, String> viewPair = initShareView(openImageDetails);
            if (viewPair == null) {
                throw new IllegalArgumentException("请确保是否调用了setClickPosition并且参数设置正确");
            }
            View shareViewClick = viewPair.first;
            String shareNameClick = viewPair.second;
            intent.putExtra(OpenParams.ON_BACK_VIEW, backViewKey);
            ImageLoadUtils.getInstance().setOnBackView(backViewKey, new ExitOnBackView4ListView(shareViewClick, openImageDetails) {

                @Override
                public void onScrollPos(int pos) {
                    if (isAutoScrollScanPosition) {
                        absListView.post(() -> {
                            absListView.smoothScrollToPosition(pos);
                        });
                    }
                }

            });
            intent.putExtra(OpenParams.IMAGES, openImageDetails);

            replenishImageUrl(openImageDetails);
            postOpen(intent, viewPair);
        } else if (viewPager2 != null) {
            if (sourceImageViewIdGet == null) {
                throw new IllegalArgumentException("sourceImageViewIdGet 不能为null");
            }
            if (clickDataPosition >= openImageUrls.size()) {
                throw new IllegalArgumentException("clickDataPosition 不能 >= OpenImageUrl 数据 size");
            }

            srcViewType = SrcViewType.VP2;
            ArrayList<OpenImageDetail> openImageDetails = new ArrayList<>();

            Pair<View, String> viewPair = initShareView(openImageDetails);
            if (viewPair == null) {
                throw new IllegalArgumentException("请确保是否调用了setClickPosition并且参数设置正确");
            }

            View shareViewClick = viewPair.first;
            String shareNameClick = viewPair.second;

            intent.putExtra(OpenParams.ON_BACK_VIEW, backViewKey);
            ImageLoadUtils.getInstance().setOnBackView(backViewKey, new ExitOnBackView4ListView(shareViewClick, openImageDetails) {
                @Override
                public void onScrollPos(int pos) {
                    if (isAutoScrollScanPosition) {
                        viewPager2.post(() -> {
                            viewPager2.setCurrentItem(pos, false);
                        });
                    }
                }
            });
            intent.putExtra(OpenParams.IMAGES, openImageDetails);
            replenishImageUrl(openImageDetails);
            postOpen(intent, viewPair);
        } else if (viewPager != null) {
            if (sourceImageViewGet == null) {
                throw new IllegalArgumentException("sourceImageViewGet 不能为null");
            }
            if (clickDataPosition >= openImageUrls.size()) {
                throw new IllegalArgumentException("clickDataPosition 不能 >= OpenImageUrl 数据 size");
            }

            srcViewType = SrcViewType.VP;
            ArrayList<OpenImageDetail> openImageDetails = new ArrayList<>();

            Pair<View, String> viewPair = initShareView(openImageDetails);
            if (viewPair == null) {
                throw new IllegalArgumentException("请确保是否调用了setClickPosition并且参数设置正确");
            }
            View shareViewClick = viewPair.first;
            String shareNameClick = viewPair.second;

            intent.putExtra(OpenParams.ON_BACK_VIEW, backViewKey);
            ImageLoadUtils.getInstance().setOnBackView(backViewKey, new ExitOnBackView4ListView(shareViewClick, openImageDetails) {
                @Override
                public void onScrollPos(int pos) {
                    if (isAutoScrollScanPosition) {
                        viewPager.post(() -> {
                            viewPager.setCurrentItem(pos, false);
                        });
                    }
                }
            });
            intent.putExtra(OpenParams.IMAGES, openImageDetails);
            postOpen(intent, viewPair);
        } else if (imageViews != null && imageViews.size() > 0) {
            if (imageViews.size() != openImageUrls.size()) {
                throw new IllegalArgumentException("所传ImageView个数需与数据个数一致");
            }
            if (clickDataPosition != clickViewPosition) {
                throw new IllegalArgumentException("clickDataPosition 和 clickViewPosition不能不相等");
            }
            if (clickViewPosition >= imageViews.size()) {
                throw new IllegalArgumentException("clickViewPosition不能 >= ImageView 的个数");
            }
            srcViewType = SrcViewType.IV;
            ArrayList<OpenImageDetail> openImageDetails = new ArrayList<>();

            Pair<View, String> viewPair = initShareView(openImageDetails);
            if (viewPair == null) {
                throw new IllegalArgumentException("请确保是否调用了setClickPosition并且参数设置正确");
            }

            View shareViewClick = viewPair.first;
            String shareNameClick = viewPair.second;

            intent.putExtra(OpenParams.ON_BACK_VIEW, backViewKey);
            ImageLoadUtils.getInstance().setOnBackView(backViewKey, new ExitOnBackView4ListView(shareViewClick, openImageDetails));
            intent.putExtra(OpenParams.IMAGES, openImageDetails);
            postOpen(intent, viewPair);
        } else {
            throw new IllegalArgumentException("请设置至少一个点击的ImageView");
        }

    }

    private Pair<View, String> initShareView(ArrayList<OpenImageDetail> openImageDetails) {
        Pair<View, String> pair = null;
        if (srcViewType == SrcViewType.RV || srcViewType == SrcViewType.AB_LIST) {
            int[] position = getVisiblePosition();
            int firstPos = position[0];
            int lastPos = position[1];
            if (lastPos < 0 || firstPos < 0) {
                return null;
            }
            int viewIndex = clickViewPosition - clickDataPosition;
            for (int i = 0; i < openImageUrls.size(); i++) {
                OpenImageUrl imageBean = openImageUrls.get(i);
                if (imageBean.getType() == MediaType.IMAGE || imageBean.getType() == MediaType.VIDEO) {
                    OpenImageDetail openImageDetail = new OpenImageDetail();
                    openImageDetail.openImageUrl = imageBean;
                    openImageDetail.dataPosition = i;
                    if (viewIndex >= firstPos && viewIndex <= lastPos) {
                        View view = getItemView(viewIndex);
                        if (view != null) {
                            ImageView shareView = view.findViewById(sourceImageViewIdGet.getImageViewId(imageBean, i));
                            if (shareView == null) {
                                throw new NullPointerException("请确保 SourceImageViewIdGet 返回的 ImageView 的Id正确");
                            }
                            if (autoSetScaleType && shareView.getScaleType() != srcImageViewScaleType) {
                                shareView.setScaleType(srcImageViewScaleType);
                            }
                            String shareName = OpenParams.SHARE_VIEW + openImageDetails.size();
                            if (clickViewPosition == viewIndex) {
                                pair = Pair.create(shareView, shareName);
                            }
                            int shareViewWidth = shareView.getWidth();
                            int shareViewHeight = shareView.getHeight();
                            openImageDetail.srcWidth = shareViewWidth;
                            openImageDetail.srcHeight = shareViewHeight;
                            if (imageBean.getType() == MediaType.IMAGE) {
                                srcImageWidthCache.add(shareViewWidth);
                                srcImageHeightCache.add(shareViewHeight);
                            }
                            if (imageBean.getType() == MediaType.VIDEO) {
                                srcVideoWidthCache.add(shareViewWidth);
                                srcVideoHeightCache.add(shareViewHeight);
                            }
                        }
                    }
                    openImageDetail.viewPosition = viewIndex;
                    openImageDetails.add(openImageDetail);
                }
                viewIndex++;
            }
        } else if (srcViewType == SrcViewType.VP2) {
            int viewIndex = clickViewPosition - clickDataPosition;
            for (int i = 0; i < openImageUrls.size(); i++) {
                OpenImageUrl imageBean = openImageUrls.get(i);
                if (imageBean.getType() == MediaType.IMAGE || imageBean.getType() == MediaType.VIDEO) {
                    OpenImageDetail openImageDetail = new OpenImageDetail();
                    openImageDetail.openImageUrl = imageBean;
                    openImageDetail.dataPosition = i;
                    if (viewIndex >= 0) {
                        View view = getItemView(viewIndex);
                        if (view != null) {
                            ImageView shareView = view.findViewById(sourceImageViewIdGet.getImageViewId(imageBean, i));
                            if (shareView == null) {
                                throw new NullPointerException("请确保 SourceImageViewIdGet 返回的 ImageView 的Id正确");
                            }
                            if (autoSetScaleType && shareView.getScaleType() != srcImageViewScaleType) {
                                shareView.setScaleType(srcImageViewScaleType);
                            }
                            String shareName = OpenParams.SHARE_VIEW + openImageDetails.size();
                            if (clickViewPosition == viewIndex) {
                                pair = Pair.create(shareView, shareName);
                            }
                            int shareViewWidth = shareView.getWidth();
                            int shareViewHeight = shareView.getHeight();
                            openImageDetail.srcWidth = shareViewWidth;
                            openImageDetail.srcHeight = shareViewHeight;
                            if (imageBean.getType() == MediaType.IMAGE) {
                                srcImageWidthCache.add(shareViewWidth);
                                srcImageHeightCache.add(shareViewHeight);
                            }
                            if (imageBean.getType() == MediaType.VIDEO) {
                                srcVideoWidthCache.add(shareViewWidth);
                                srcVideoHeightCache.add(shareViewHeight);
                            }
                        }
                    }
                    openImageDetail.viewPosition = viewIndex;
                    openImageDetails.add(openImageDetail);
                }
                viewIndex++;
            }
        } else if (srcViewType == SrcViewType.VP) {
            ImageView shareView = sourceImageViewGet.getImageView(openImageUrls.get(clickDataPosition), clickDataPosition);
            if (shareView == null) {
                throw new NullPointerException("请确保 SourceImageViewGet 返回的 ImageView 不能为null");
            }
            if (autoSetScaleType && shareView.getScaleType() != srcImageViewScaleType) {
                shareView.setScaleType(srcImageViewScaleType);
            }
            String shareName = OpenParams.SHARE_VIEW + clickDataPosition;
            pair = Pair.create(shareView, shareName);
            int shareViewWidth = shareView.getWidth();
            int shareViewHeight = shareView.getHeight();
            for (int i = 0; i < openImageUrls.size(); i++) {
                OpenImageUrl imageBean = openImageUrls.get(i);
                OpenImageDetail openImageDetail = new OpenImageDetail();
                openImageDetail.openImageUrl = imageBean;
                openImageDetail.dataPosition = i;
                openImageDetail.viewPosition = i;
                openImageDetail.srcWidth = shareViewWidth;
                openImageDetail.srcHeight = shareViewHeight;
                openImageDetails.add(openImageDetail);
            }
        } else {
            for (int i = 0; i < openImageUrls.size(); i++) {
                OpenImageUrl imageBean = openImageUrls.get(i);
                OpenImageDetail openImageDetail = new OpenImageDetail();
                openImageDetail.openImageUrl = imageBean;
                openImageDetail.dataPosition = i;
                openImageDetail.viewPosition = i;
                ImageView shareView = imageViews.get(i);
                if (autoSetScaleType && shareView.getScaleType() != srcImageViewScaleType) {
                    shareView.setScaleType(srcImageViewScaleType);
                }
                String shareName = OpenParams.SHARE_VIEW + i;
                int shareViewWidth = shareView.getWidth();
                int shareViewHeight = shareView.getHeight();
                openImageDetail.srcWidth = shareViewWidth;
                openImageDetail.srcHeight = shareViewHeight;
                openImageDetails.add(openImageDetail);
                if (clickViewPosition == i) {
                    pair = Pair.create(shareView, shareName);
                }
            }
        }

        return pair;
    }

    private int[] getVisiblePosition() {
        int[] position = new int[]{-1, -1};
        if (srcViewType == SrcViewType.RV) {
            int firstPos = 0;
            int lastPos = 0;
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (layoutManager instanceof LinearLayoutManager) {
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
                firstPos = linearLayoutManager.findFirstVisibleItemPosition();
                lastPos = linearLayoutManager.findLastVisibleItemPosition();
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
                int[] firstVisibleItems = null;
                firstVisibleItems = staggeredGridLayoutManager.findFirstVisibleItemPositions(firstVisibleItems);
                int[] lastVisibleItems = null;
                lastVisibleItems = staggeredGridLayoutManager.findLastVisibleItemPositions(lastVisibleItems);
                if (firstVisibleItems == null || lastVisibleItems == null || firstVisibleItems.length == 0 || lastVisibleItems.length == 0) {
                    return position;
                }
                firstPos = Integer.MAX_VALUE;
                lastPos = 0;
                for (int firstVisibleItem : firstVisibleItems) {
                    if (firstVisibleItem < firstPos && firstVisibleItem >= 0) {
                        firstPos = firstVisibleItem;
                    }
                }
                for (int lastVisibleItem : lastVisibleItems) {
                    if (lastVisibleItem > lastPos) {
                        lastPos = lastVisibleItem;
                    }
                }
                if (lastPos < firstPos) {
                    return position;
                }
            }

            position[0] = firstPos;
            position[1] = lastPos;
        } else if (srcViewType == SrcViewType.VP2) {
            int childCount = viewPager2.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childView = viewPager2.getChildAt(i);
                if (childView instanceof RecyclerView) {
                    RecyclerView recyclerView = (RecyclerView) childView;
                    RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                    if (layoutManager instanceof LinearLayoutManager) {
                        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
                        position[0] = linearLayoutManager.findFirstVisibleItemPosition();
                        position[1] = linearLayoutManager.findLastVisibleItemPosition();
                    }
                    break;
                }
            }
        } else if (srcViewType == SrcViewType.AB_LIST) {
            int firstPos = absListView.getFirstVisiblePosition();
            int lastPos = absListView.getLastVisiblePosition();
            position[0] = firstPos;
            position[1] = lastPos;
        }
        return position;
    }

    private View getItemView(int position) {
        View view = null;
        if (srcViewType == SrcViewType.RV) {
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (layoutManager != null) {
                view = layoutManager.findViewByPosition(position);
            }
        } else if (srcViewType == SrcViewType.AB_LIST) {
            if (absListView != null) {
                int firstPos = absListView.getFirstVisiblePosition();
                view = absListView.getChildAt(position - firstPos);
            }
        } else if (srcViewType == SrcViewType.VP2) {
            int childCount = viewPager2.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childView = viewPager2.getChildAt(i);
                if (childView instanceof RecyclerView) {
                    RecyclerView recyclerView = (RecyclerView) childView;
                    RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                    if (layoutManager != null) {
                        view = layoutManager.findViewByPosition(position);
                    }
                    break;
                }
            }
        } else {
            if (imageViews != null) {
                view = imageViews.get(position);
            }
        }
        return view;
    }

    private volatile boolean isMapShareView = true;

    private void postOpen(Intent intent, Pair<View, String> viewPair) {
        isCanOpen = false;
        OpenImageUrl openImageUrl = openImageUrls.get(clickDataPosition);
        Handler handler = new Handler(Looper.getMainLooper());
        OpenImageConfig.getInstance().getBigImageHelper().loadImage(context, ImageLoadUtils.getInstance().getImageLoadSuccess(openImageUrl.getImageUrl()) ? openImageUrl.getImageUrl() : openImageUrl.getCoverImageUrl(), new OnLoadBigImageListener() {
            @Override
            public void onLoadImageSuccess(Drawable drawable) {
                handler.removeCallbacksAndMessages(null);
                String key = UUID.randomUUID().toString();
                intent.putExtra(OpenParams.OPEN_COVER_DRAWABLE, key);
                ImageLoadUtils.getInstance().setCoverDrawable(key, drawable);
                startActivity(intent, viewPair, key);
            }

            @Override
            public void onLoadImageFailed() {
                handler.removeCallbacksAndMessages(null);
                startActivity(intent, viewPair, null);
            }
        });
        handler.postDelayed(() -> startActivity(intent, viewPair, null), 100);
    }
    private String drawableKey;
    private void startActivity(Intent intent, Pair<View, String> viewPair, String drawableKey) {
        if (!isStartActivity) {
            if (ActivityCompatHelper.assertValidRequest(context)) {
                this.drawableKey = drawableKey;
                View shareElementView = viewPair.first;
                String shareElementName = viewPair.second;
                try {
                    if (shareElementView != null && shareElementView.isAttachedToWindow()) {
                        fixAndroid5_7Bug(shareElementView,true);
                        Bundle options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context, shareElementView, shareElementName).toBundle();
                        context.startActivity(intent, options);
                    } else {
                        releaseImageLoadUtilMap();
                    }
                } catch (Exception ignored) {
                    releaseImageLoadUtilMap();
                }
                release();
            } else {
                releaseImageLoadUtilMap();
            }
        } else if (!TextUtils.isEmpty(drawableKey)) {
            ImageLoadUtils.getInstance().clearCoverDrawable(drawableKey);
        }
        isStartActivity = true;
    }

    private void releaseImageLoadUtilMap(){
        isCanOpen = true;
        ImageLoadUtils.getInstance().clearItemLoadHelper(itemLoadHelperKey);
        ImageLoadUtils.getInstance().clearOnSelectMediaListener(onselectKey);
        ImageLoadUtils.getInstance().clearCoverDrawable(drawableKey);
        ImageLoadUtils.getInstance().clearPageTransformers(pageTransformersKey);
        ImageLoadUtils.getInstance().clearOnItemClickListener(onItemClickListenerKey);
        ImageLoadUtils.getInstance().clearOnItemLongClickListener(onItemLongClickListenerKey);
        ImageLoadUtils.getInstance().clearMoreViewOption(moreViewOptionKey);
        ImageLoadUtils.getInstance().clearOnBackView(backViewKey);
        ImageLoadUtils.getInstance().setOnRemoveListener4FixBug(null);
        moreViewOptions.clear();
    }
    private void fixAndroid5_7Bug(final View shareElementView,final boolean isSetExitSharedElementCallback) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
            if (isSetExitSharedElementCallback && context != null){
                Activity activity = ActivityCompatHelper.getActivity(context);
                activity.setExitSharedElementCallback(new SharedElementCallback() {
                    @Override
                    public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                        if (!isMapShareView) {
                            names.clear();
                            sharedElements.clear();
                        }
                        super.onMapSharedElements(names, sharedElements);
                        activity.setExitSharedElementCallback(null);
                    }
                });
            }
            ViewGroup parent = (ViewGroup) shareElementView.getParent();
            ViewTreeObserver parentViewTreeObserver = parent.getViewTreeObserver();
            final View.OnAttachStateChangeListener onAttachStateChangeListener =  new View.OnAttachStateChangeListener() {


                @Override
                public void onViewAttachedToWindow(View v) {

                }

                @Override
                public void onViewDetachedFromWindow(View v) {
                    if (ActivityCompatHelper.getActivity(context) == null) {
                        shareElementView.removeOnAttachStateChangeListener(this);
                        return;
                    }
                    if (v != shareElementView) {
                        return;
                    }
                    shareElementView.removeOnAttachStateChangeListener(this);
                    boolean isCallStop = false;
                    try {
                        Field fieldPassword = ViewTreeObserver.class.getDeclaredField("mOnPreDrawListeners");
                        fieldPassword.setAccessible(true);
                        Object mOnPreDrawListeners = fieldPassword.get(parentViewTreeObserver);

                        Field mDataField = mOnPreDrawListeners.getClass().getDeclaredField("mData");
                        mDataField.setAccessible(true);
                        ArrayList<ViewTreeObserver.OnPreDrawListener> mData = (ArrayList<ViewTreeObserver.OnPreDrawListener>) mDataField.get(mOnPreDrawListeners);
                        for (ViewTreeObserver.OnPreDrawListener mDatum : mData) {
                            if (mDatum.getClass().getName().contains("GhostViewListeners")) {
                                isCallStop = true;
                                parentViewTreeObserver.removeOnPreDrawListener(mDatum);
                            }
                        }
                    } catch (Throwable ignored) {
                    } finally {
                        isMapShareView = false;
                        if (isCallStop) {
                            new Instrumentation().callActivityOnStop(ActivityCompatHelper.getActivity(context));
                        }
                    }

                }

            };
            shareElementView.addOnAttachStateChangeListener(onAttachStateChangeListener);
            ImageLoadUtils.getInstance().setOnRemoveListener4FixBug(() -> shareElementView.removeOnAttachStateChangeListener(onAttachStateChangeListener));
        }
    }


    private void replenishImageUrl(ArrayList<OpenImageDetail> openImageDetails) {
        if (srcImageWidthCache.size() == 1 || srcImageHeightCache.size() == 1) {
            int srcWidth = srcImageWidthCache.iterator().next();
            int srcHeight = srcImageHeightCache.iterator().next();
            for (OpenImageDetail openImageDetail : openImageDetails) {
                if (openImageDetail.getType() == MediaType.IMAGE && (openImageDetail.srcWidth == 0 || openImageDetail.srcHeight == 0)) {
                    openImageDetail.srcWidth = srcWidth;
                    openImageDetail.srcHeight = srcHeight;
                }
            }
        }
        if (srcVideoWidthCache.size() == 1 || srcVideoHeightCache.size() == 1) {
            int srcWidth = srcVideoWidthCache.iterator().next();
            int srcHeight = srcVideoHeightCache.iterator().next();
            for (OpenImageDetail openImageDetail : openImageDetails) {
                if (openImageDetail.getType() == MediaType.VIDEO && (openImageDetail.srcWidth == 0 || openImageDetail.srcHeight == 0)) {
                    openImageDetail.srcWidth = srcWidth;
                    openImageDetail.srcHeight = srcHeight;
                }
            }
        }
    }

    private void release() {
        FragmentActivity fragmentActivity = ActivityCompatHelper.getFragmentActivity(context);
        if (fragmentActivity != null) {
            fragmentActivity.getLifecycle().addObserver(new LifecycleEventObserver() {
                @Override
                public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                    if (event == Lifecycle.Event.ON_DESTROY) {
                        context = null;
                        recyclerView = null;
                        absListView = null;
                        viewPager = null;
                        viewPager2 = null;
                        if (imageViews != null) {
                            imageViews.clear();
                            imageViews = null;
                        }
                        source.getLifecycle().removeObserver(this);
                        releaseImageLoadUtilMap();
                    }
                }
            });
        }

    }

    private class ExitOnBackView4ListView extends ExitOnBackView {
        private final ArrayList<OpenImageDetail> openImageDetails;
        protected View showCurrentView;
        protected Float showCurrentViewStartAlpha;

        public ExitOnBackView4ListView(View transitionView, ArrayList<OpenImageDetail> openImageDetails) {
            super(transitionView);
            this.openImageDetails = openImageDetails;
        }

        @Override
        public BackViewType onBack(int showPosition) {
            BackViewType backViewType = BackViewType.NO_SHARE;
            Activity activity = ActivityCompatHelper.getActivity(context);
            if (activity == null) {
                return backViewType;
            }
            ImageView shareExitView = null;
            ShareExitViewBean shareExitViewBean = getShareExitViewBean(showPosition);
            if (shareExitViewBean != null) {
                backViewType = shareExitViewBean.backViewType;
                shareExitView = shareExitViewBean.shareExitView;
            }
            activity.setExitSharedElementCallback(new ExitSharedElementCallback2(context, shareExitView, showSrcImageView, shareExitView == showCurrentView ? showCurrentViewStartAlpha : null));
            return backViewType;
        }

        @Override
        public void onStartTouchScale(int showPosition) {
            super.onStartTouchScale(showPosition);
            if (!showSrcImageView) {
                ShareExitViewBean shareExitViewBean = getShareExitViewBean(showPosition);
                if (shareExitViewBean != null) {
                    showCurrentView = shareExitViewBean.shareExitView;
                    showCurrentViewStartAlpha = showCurrentView.getAlpha();
                    showCurrentView.setAlpha(0f);
                }
            }
        }

        @Override
        public void onEndTouchScale(int showPosition) {
            super.onEndTouchScale(showPosition);
            if (!showSrcImageView) {
                if (showCurrentView != null && showCurrentViewStartAlpha != null) {
                    showCurrentView.setAlpha(showCurrentViewStartAlpha);
                }
                showCurrentView = null;
                showCurrentViewStartAlpha = null;
            }
        }

        private boolean checkViewAvailable() {
            if (srcViewType == SrcViewType.RV) {
                return recyclerView != null && recyclerView.isAttachedToWindow();
            } else if (srcViewType == SrcViewType.AB_LIST) {
                return absListView != null && absListView.isAttachedToWindow();
            } else if (srcViewType == SrcViewType.VP2) {
                return viewPager2 != null && viewPager2.isAttachedToWindow();
            } else if (srcViewType == SrcViewType.VP) {
                return viewPager != null && viewPager.isAttachedToWindow();
            } else if (srcViewType == SrcViewType.IV) {
                return true;
            }
            return false;
        }

        private ShareExitViewBean getShareExitViewBean(int showPosition) {
            Activity activity = ActivityCompatHelper.getActivity(context);
            if (activity == null || !checkViewAvailable()) {
                return null;
            }
            ShareExitViewBean shareExitViewBean = null;
            if (srcViewType == SrcViewType.RV || srcViewType == SrcViewType.AB_LIST || srcViewType == SrcViewType.VP2) {
                int[] position = getVisiblePosition();
                int firstPos = position[0];
                int lastPos = position[1];
                if (lastPos < 0 || firstPos < 0) {
                    return null;
                }
                ImageView shareExitView = null;

                OpenImageDetail openImageDetail = openImageDetails.get(showPosition);
                OpenImageUrl openImageUrl = openImageDetail.openImageUrl;
                int viewPosition = openImageDetail.viewPosition;
                int dataPosition = openImageDetail.dataPosition;

                View view = getItemView(viewPosition);

                if (view != null) {
                    ImageView shareView = view.findViewById(sourceImageViewIdGet.getImageViewId(openImageUrl, dataPosition));
                    if (shareView != null) {
                        if (autoSetScaleType && shareView.getScaleType() != srcImageViewScaleType) {
                            shareView.setScaleType(srcImageViewScaleType);
                        }
                        boolean isAttachedToWindow = shareView.isAttachedToWindow();
                        if (isAttachedToWindow) {
                            shareExitView = shareView;
                            shareExitViewBean = new ShareExitViewBean(BackViewType.SHARE_NORMAL, shareExitView);
                        }
                    }
                }

                if (shareExitView == null && wechatExitFillInEffect) {
                    openImageUrl = openImageUrls.get(clickDataPosition);
                    viewPosition = clickViewPosition;
                    dataPosition = clickDataPosition;
                    View wechatView = getItemView(viewPosition);
                    if (wechatView != null) {
                        ImageView shareView = wechatView.findViewById(sourceImageViewIdGet.getImageViewId(openImageUrl, dataPosition));
                        if (shareView != null) {
                            if (autoSetScaleType && shareView.getScaleType() != srcImageViewScaleType) {
                                shareView.setScaleType(srcImageViewScaleType);
                            }
                            boolean isAttachedToWindow = shareView.isAttachedToWindow();
                            if (isAttachedToWindow) {
                                shareExitView = shareView;
                                shareExitViewBean = new ShareExitViewBean(BackViewType.SHARE_WECHAT, shareExitView);
                            }
                        }
                    }
                }
            } else if (srcViewType == SrcViewType.VP) {
                OpenImageDetail openImageDetail = openImageDetails.get(showPosition);
                OpenImageUrl openImageUrl = openImageDetail.openImageUrl;
                int viewPosition = openImageDetail.viewPosition;
                int dataPosition = openImageDetail.dataPosition;

                ImageView shareView = sourceImageViewGet.getImageView(openImageUrl, dataPosition);
                if (shareView != null) {
                    if (autoSetScaleType && shareView.getScaleType() != srcImageViewScaleType) {
                        shareView.setScaleType(srcImageViewScaleType);
                    }
                    boolean isAttachedToWindow = shareView.isAttachedToWindow();
                    if (isAttachedToWindow) {
                        shareExitViewBean = new ShareExitViewBean(BackViewType.SHARE_NORMAL, shareView);
                    }
                }

            } else {
                ImageView shareExitView = null;
                ImageView shareView = imageViews.get(showPosition);
                if (shareView != null && shareView.isAttachedToWindow()) {
                    shareExitView = shareView;
                    shareExitViewBean = new ShareExitViewBean(BackViewType.SHARE_NORMAL, shareExitView);
                }

                if (shareExitView == null && wechatExitFillInEffect) {
                    ImageView wechatView = imageViews.get(clickViewPosition);
                    if (wechatView != null && wechatView.isAttachedToWindow()) {
                        shareExitView = wechatView;
                        shareExitViewBean = new ShareExitViewBean(BackViewType.SHARE_WECHAT, shareExitView);
                    }
                }
            }
            return shareExitViewBean;
        }

    }

}
