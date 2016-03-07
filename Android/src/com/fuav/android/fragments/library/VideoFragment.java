package com.fuav.android.fragments.library;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.fuav.android.R;
import com.fuav.android.activities.VideoPlayActivity;
import com.fuav.android.utils.LocalDisplay;
import com.fuav.android.view.header.RentalsSunHeaderView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;

/**
 * A simple {@link Fragment} subclass.
 */
public class VideoFragment extends Fragment {

    private long mStartLoadingTime = -1;
    private boolean mImageHasLoaded = false;
    //显示图片的配置
    DisplayImageOptions options ;
    List<String> list = new ArrayList<String>();
    private ArrayList<String> mNameList = new ArrayList<String>();
    private GridAdapter adapter = new GridAdapter();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_materail_style, null);

        final GridView gridListView = (GridView) view.findViewById(R.id.video_grid_view);

        gridListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0) {
                    ActivityOptionsCompat options =
                            ActivityOptionsCompat.makeSceneTransitionAnimation(
                                    getActivity(), view.findViewById(R.id.grid_videoview),"VIDEO");
                    Intent intent = new Intent(getActivity(), VideoPlayActivity.class);
                    intent.putExtra("path",list.get(position));
                    ActivityCompat.startActivity( getActivity(),intent, options.toBundle());
                }
            }
        });
        //获取sd卡下的图片并显示
        getVideos(Environment.getExternalStorageDirectory() + "/FUAV");
        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        gridListView.setAdapter(adapter);

        final PtrFrameLayout frame = (PtrFrameLayout) view.findViewById(R.id.material_style_ptr_frame);
        // header
        final RentalsSunHeaderView header = new RentalsSunHeaderView(getContext());
        header.setLayoutParams(new PtrFrameLayout.LayoutParams(-1, -2));
        header.setPadding(0, LocalDisplay.dp2px(15), 0, LocalDisplay.dp2px(10));
        header.setUp(frame);

        frame.setLoadingMinTime(1000);
        frame.setDurationToCloseHeader(1500);
        frame.setHeaderView(header);
        frame.addPtrUIHandler(header);
        frame.setPtrHandler(new PtrHandler() {
            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
            }

            @Override
            public void onRefreshBegin(final PtrFrameLayout frame) {
                if (mImageHasLoaded) {
                    long delay = 1500;
                    frame.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            frame.refreshComplete();
                            adapter.notifyDataSetChanged();
                        }
                    }, delay);
                } else {
                    mStartLoadingTime = System.currentTimeMillis();
                }
            }
        });
        return view;
    }

    // 获取SDCard中某个目录下图片路径集合
    public List<String> getVideos(final String strPath) {
        File file = new File(strPath);
        File[] allfiles = file.listFiles();
        if (allfiles == null) {
            return null;
        }
        for (final File fi : allfiles) {
            if (fi.getPath().endsWith(".mp4")) {
                int idx = fi.getPath().lastIndexOf(".");
                int idxname = fi.getPath().lastIndexOf("/");
                if (idx <= 0) {
                    continue;
                }
                String suffixname = fi.getPath().substring(idxname + 1, idx);
                list.add(fi.getPath());
                mNameList.add(suffixname);
            }
        }
        return list;
    }

    public class GridAdapter extends BaseAdapter {

        public GridAdapter() {
        }

        public int getCount() {
            return mNameList.size();
        }

        public Object getItem(int position) {
            return mNameList.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            final ItemViewTag viewTag;

            if (convertView == null)
            {
                convertView = getLayoutInflater(null).inflate(R.layout.video_gridview_item, null);

                // construct an item tag
                viewTag = new ItemViewTag((ImageView) convertView.findViewById(R.id.grid_videoview),
                        (TextView) convertView.findViewById(R.id.grid_name));
                convertView.setTag(viewTag);
            } else
            {
                viewTag = (ItemViewTag) convertView.getTag();
            }

            // set name
            viewTag.mName.setText(mNameList.get(position));

            // set icon
            ImageLoader.getInstance().displayImage("file://"+list.get(position),viewTag.mIcon,options);
//            viewTag.mIcon.setImageBitmap(getVideoThumbnail(list.get(position), MediaStore.Images.Thumbnails.MINI_KIND));
            mImageHasLoaded = true;
            return convertView;
        }

        class ItemViewTag
        {
            protected ImageView mIcon;
            protected TextView mName;

            public ItemViewTag(ImageView icon, TextView name)
            {
                this.mName = name;
                this.mIcon = icon;
            }
        }

    }

    private Bitmap getVideoThumbnail(String videoPath, int kind) {
        // 获取视频的缩略图
        return ThumbnailUtils.createVideoThumbnail(videoPath, kind);
    }


    public static CharSequence getTitle(Context context) {
        return context.getText(R.string.library_video_title);
    }

}
