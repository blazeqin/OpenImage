package com.flyjingfish.openimage.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;

import com.flyjingfish.openimage.DataUtils;
import com.flyjingfish.openimage.MyApplication;
import com.flyjingfish.openimage.R;
import com.flyjingfish.openimage.adapter.MsgRvAdapter;
import com.flyjingfish.openimage.bean.ImageEntity;
import com.flyjingfish.openimage.bean.MessageBean;
import com.flyjingfish.openimage.databinding.ActivityViewPagerDemoBinding;
import com.flyjingfish.openimage.databinding.ItemImageBinding;
import com.flyjingfish.openimage.databinding.ItemMsgImageBinding;
import com.flyjingfish.openimage.databinding.ItemVideoBinding;
import com.flyjingfish.openimage.imageloader.MyImageLoader;
import com.flyjingfish.openimagelib.OpenImage;
import com.flyjingfish.openimagelib.beans.OpenImageUrl;
import com.flyjingfish.openimagelib.enums.MediaType;
import com.flyjingfish.openimagelib.listener.ItemLoadHelper;
import com.flyjingfish.openimagelib.listener.OnLoadCoverImageListener;
import com.flyjingfish.openimagelib.listener.OnSelectMediaListener;
import com.flyjingfish.openimagelib.listener.SourceImageViewGet;
import com.flyjingfish.openimagelib.listener.SourceImageViewIdGet;
import com.flyjingfish.openimagelib.transformers.ScaleInTransformer;
import com.flyjingfish.openimagelib.utils.ScreenUtils;
import com.youth.banner.adapter.BannerAdapter;
import com.youth.banner.indicator.CircleIndicator;
import com.youth.banner.indicator.RectangleIndicator;
import com.youth.banner.util.BannerUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerDemoActivity extends AppCompatActivity {

    private ActivityViewPagerDemoBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewPagerDemoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadData();
    }

    private void loadData() {
        MyApplication.cThreadPool.submit(() -> {
            List<MessageBean> datas = new ArrayList<>();

            String response1 = DataUtils.getFromAssets(ViewPagerDemoActivity.this, "message_data.json");
            try {
                JSONArray jsonArray = new JSONArray(response1);
                for (int i = 0; i < jsonArray.length(); i++) {
                    MessageBean itemData = new MessageBean();
                    JSONObject jsonObject = jsonArray.optJSONObject(i);
                    int type = jsonObject.getInt("type");
                    itemData.type = type;
                    if (type == MessageBean.IMAGE){
                        itemData.imageUrl = jsonObject.getString("imageUrl");
                        itemData.coverUrl = jsonObject.getString("coverUrl");
                        datas.add(itemData);
                    }else if (type == MessageBean.VIDEO){
                        itemData.videoUrl = jsonObject.getString("videoUrl");
                        itemData.coverUrl = jsonObject.getString("coverUrl");
                        datas.add(itemData);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            setData(datas);
        });
    }

    private void setData(List<MessageBean> datas) {
        runOnUiThread(() -> {
            binding.viewPager.setAdapter(new ViewPagerAdapter(datas));
            binding.viewPager2.setAdapter(new ViewPager2Adapter(datas));
            binding.banner.setBannerGalleryEffect(50,10);
            binding.banner.setIndicator(new CircleIndicator(this));
            binding.banner.addBannerLifecycleObserver(this);
            binding.banner.setAdapter(new MyBannerAdapter(datas));
            binding.banner.isAutoLoop(true);
            binding.banner.start();
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.banner.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        binding.banner.stop();
    }

    private class ViewPagerAdapter extends PagerAdapter implements SourceImageViewGet<OpenImageUrl> {
        List<MessageBean> datas;
        private View mItemView;

        public ViewPagerAdapter(List<MessageBean> datas) {
            this.datas = datas;
        }

        @Override
        public int getCount() {
            return datas.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            ItemVideoBinding imageBinding = ItemVideoBinding.inflate(LayoutInflater.from(container.getContext()), container, true);
            imageBinding.ivPlay.setVisibility(datas.get(position).getType() == MediaType.VIDEO?View.VISIBLE:View.GONE);
            View view = imageBinding.getRoot();
            MyImageLoader.getInstance().load(imageBinding.ivImage, datas.get(position).getCoverImageUrl(), R.mipmap.img_load_placeholder, R.mipmap.img_load_placeholder);
            imageBinding.ivImage.setOnClickListener(v -> {
                OpenImage.with(ViewPagerDemoActivity.this).setClickViewPager(binding.viewPager, ViewPagerAdapter.this)
                        .setShowSrcImageView(true)
                        .setAutoScrollScanPosition(true)
                        .setSrcImageViewScaleType(ImageView.ScaleType.CENTER_CROP, true)
                        .setImageUrlList(datas).setImageDiskMode(MyImageLoader.imageDiskMode)
                        .setItemLoadHelper(new ItemLoadHelper() {
                            @Override
                            public void loadImage(Context context, OpenImageUrl openImageUrl, String imageUrl, ImageView imageView, int overrideWidth, int overrideHeight, OnLoadCoverImageListener onLoadCoverImageListener) {

                                MyImageLoader.getInstance().load(imageView, imageUrl, overrideWidth, overrideHeight, R.mipmap.img_load_placeholder, R.mipmap.img_load_placeholder, new MyImageLoader.OnImageLoadListener() {
                                    @Override
                                    public void onSuccess() {
                                        onLoadCoverImageListener.onLoadImageSuccess();
                                    }

                                    @Override
                                    public void onFailed() {
                                        onLoadCoverImageListener.onLoadImageFailed();
                                    }
                                });
                            }
                        }).addPageTransformer(new ScaleInTransformer())
                        .setOpenImageStyle(R.style.DefaultPhotosTheme)
                        .setClickPosition(position).show();
            });
            return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            mItemView = (View) object;
        }

        @Override
        public ImageView getImageView(OpenImageUrl data, int position) {
            ImageView imageView = null;
            if (mItemView != null) {
                imageView = mItemView.findViewById(R.id.iv_image);
            }
            return imageView;
        }

    }

    private class ViewPager2Adapter extends RecyclerView.Adapter<ViewPager2Adapter.MyHolder> {
        List<MessageBean> datas;

        public ViewPager2Adapter(List<MessageBean> datas) {
            this.datas = datas;
        }

        @NonNull
        @Override
        public ViewPager2Adapter.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
            return new ViewPager2Adapter.MyHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewPager2Adapter.MyHolder holder, int position) {
            ItemVideoBinding videoBinding = ItemVideoBinding.bind(holder.itemView);
            videoBinding.ivPlay.setVisibility(datas.get(position).getType() == MediaType.VIDEO?View.VISIBLE:View.GONE);
            MyImageLoader.getInstance().load(holder.ivImage, datas.get(position).getCoverImageUrl(), R.mipmap.img_load_placeholder, R.mipmap.img_load_placeholder);
            holder.ivImage.setOnClickListener(v -> {
                OpenImage.with(ViewPagerDemoActivity.this).setClickViewPager2(binding.viewPager2, new SourceImageViewIdGet<OpenImageUrl>() {
                    @Override
                    public int getImageViewId(OpenImageUrl data, int position) {
                        return R.id.iv_image;
                    }
                }).setShowSrcImageView(true)
                        .setAutoScrollScanPosition(true)
                        .setSrcImageViewScaleType(ImageView.ScaleType.CENTER_CROP, true)
                        .setImageUrlList(datas).setImageDiskMode(MyImageLoader.imageDiskMode)
                        .setItemLoadHelper(new ItemLoadHelper() {
                            @Override
                            public void loadImage(Context context, OpenImageUrl openImageUrl, String imageUrl, ImageView imageView, int overrideWidth, int overrideHeight, OnLoadCoverImageListener onLoadCoverImageListener) {

                                MyImageLoader.getInstance().load(imageView, imageUrl, overrideWidth, overrideHeight, R.mipmap.img_load_placeholder, R.mipmap.img_load_placeholder, new MyImageLoader.OnImageLoadListener() {
                                    @Override
                                    public void onSuccess() {
                                        onLoadCoverImageListener.onLoadImageSuccess();
                                    }

                                    @Override
                                    public void onFailed() {
                                        onLoadCoverImageListener.onLoadImageFailed();
                                    }
                                });
                            }
                        }).addPageTransformer(new ScaleInTransformer())
                        .setOpenImageStyle(R.style.DefaultPhotosTheme)
                        .setClickPosition(position).show();
            });
        }

        @Override
        public int getItemCount() {
            return datas.size();
        }

        class MyHolder extends RecyclerView.ViewHolder {
            ImageView ivImage;

            public MyHolder(@NonNull View itemView) {
                super(itemView);
                ivImage = itemView.findViewById(R.id.iv_image);
            }
        }
    }

    public class MyBannerAdapter extends BannerAdapter<MessageBean, MyBannerAdapter.MyBannerHolder> {

        public MyBannerAdapter(List<MessageBean> datas) {
            super(datas);
        }

        @Override
        public MyBannerHolder onCreateHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
            return new MyBannerHolder(view);
        }

        @Override
        public void onBindView(MyBannerHolder holder, MessageBean data, int position, int size) {
            ItemVideoBinding videoBinding = ItemVideoBinding.bind(holder.itemView);
            videoBinding.ivPlay.setVisibility(data.getType() == MediaType.VIDEO?View.VISIBLE:View.GONE);
            MyImageLoader.getInstance().load(holder.ivImage, mDatas.get(position).getCoverImageUrl(), R.mipmap.img_load_placeholder, R.mipmap.img_load_placeholder);
            holder.ivImage.setOnClickListener(v -> {
                OpenImage.with(ViewPagerDemoActivity.this).setClickViewPager2(binding.banner.getViewPager2(), new SourceImageViewIdGet<OpenImageUrl>() {
                    @Override
                    public int getImageViewId(OpenImageUrl data, int position) {
                        return R.id.iv_image;
                    }
                }).setShowSrcImageView(true)
                        .setSrcImageViewScaleType(ImageView.ScaleType.CENTER_CROP, true)
                        .setImageUrlList(mDatas).setImageDiskMode(MyImageLoader.imageDiskMode)
                        .setItemLoadHelper(new ItemLoadHelper() {
                            @Override
                            public void loadImage(Context context, OpenImageUrl openImageUrl, String imageUrl, ImageView imageView, int overrideWidth, int overrideHeight, OnLoadCoverImageListener onLoadCoverImageListener) {

                                MyImageLoader.getInstance().load(imageView, imageUrl, overrideWidth, overrideHeight, R.mipmap.img_load_placeholder, R.mipmap.img_load_placeholder, new MyImageLoader.OnImageLoadListener() {
                                    @Override
                                    public void onSuccess() {
                                        onLoadCoverImageListener.onLoadImageSuccess();
                                    }

                                    @Override
                                    public void onFailed() {
                                        onLoadCoverImageListener.onLoadImageFailed();
                                    }
                                });
                            }
                        })
                        .setAutoScrollScanPosition(false)
                        .setOnSelectMediaListener(new OnSelectMediaListener() {
                            @Override
                            public void onSelect(OpenImageUrl openImageUrl, int position) {
                                binding.banner.setCurrentItem(position + 1);
                            }
                        })
                        .addPageTransformer(new ScaleInTransformer())
                        .setOpenImageStyle(R.style.DefaultPhotosTheme)
                        .setClickPosition(position).show();
            });
        }

        public class MyBannerHolder extends RecyclerView.ViewHolder {
            public ImageView ivImage;

            public MyBannerHolder(@NonNull View view) {
                super(view);
                this.ivImage = view.findViewById(R.id.iv_image);
            }
        }
    }
}
