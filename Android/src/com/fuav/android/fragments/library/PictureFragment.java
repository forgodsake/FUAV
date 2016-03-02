package com.fuav.android.fragments.library;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
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
import com.fuav.android.activities.PicPlayActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.PtrUIHandler;
import in.srain.cube.views.ptr.header.StoreHouseHeader;
import in.srain.cube.views.ptr.indicator.PtrIndicator;

/**
 * A simple {@link Fragment} subclass.
 */
public class PictureFragment extends Fragment {

    final String[] mStringList = { "FUAV"};

    ImageSize mImageSize ;
    //显示图片的配置
    DisplayImageOptions options ;
    List<String> list = new ArrayList<String>();
    List<String> videolist = new ArrayList<String>();
    private ArrayList<String> mNameList = new ArrayList<String>();
    private GridAdapter adapter = new GridAdapter();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_storehouse_header, null);

        final GridView gridListView = (GridView) view.findViewById(R.id.rotate_header_grid_view);

        gridListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0) {
                    Intent intent = new Intent(getActivity(), PicPlayActivity.class);
                    intent.putExtra("path",list.get(position));
                    startActivity(intent);
                }
            }
        });

        //获取sd卡下的图片并显示
        getPictures(Environment.getExternalStorageDirectory() + "/FUAV");
        mImageSize = new ImageSize(100, 100);
        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        gridListView.setAdapter(adapter);

        final PtrFrameLayout frame = (PtrFrameLayout) view.findViewById(R.id.store_house_ptr_frame);
        // header
        final StoreHouseHeader header = new StoreHouseHeader(getContext());
        /**
         * using a string, support: A-Z 0-9 - .
         * you can add more letters by {@link in.srain.cube.views.ptr.header.StoreHousePath#addChar}
         */
        header.initWithString(mStringList[0]);

        // for changing string
        frame.addPtrUIHandler(new PtrUIHandler() {

            private int mLoadTime = 0;

            @Override
            public void onUIReset(PtrFrameLayout frame) {
                mLoadTime++;
                String string = mStringList[mLoadTime % mStringList.length];
                header.initWithString(string);
            }

            @Override
            public void onUIRefreshPrepare(PtrFrameLayout frame) {
            }

            @Override
            public void onUIRefreshBegin(PtrFrameLayout frame) {

            }

            @Override
            public void onUIRefreshComplete(PtrFrameLayout frame) {

            }

            @Override
            public void onUIPositionChange(PtrFrameLayout frame, boolean isUnderTouch, byte status, PtrIndicator ptrIndicator) {

            }
        });

        frame.setDurationToCloseHeader(1500);
        frame.setHeaderView(header);
        frame.addPtrUIHandler(header);
        frame.postDelayed(new Runnable() {
            @Override
            public void run() {
                frame.autoRefresh(false);
            }
        }, 100);

        frame.setPtrHandler(new PtrHandler() {
            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return true;
            }

            @Override
            public void onRefreshBegin(final PtrFrameLayout frame) {
                frame.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        frame.refreshComplete();
                    }
                }, 1000);
            }
        });
        return view;
    }

    //获取SDCard中某个目录下图片路径集合
    public List<String> getPictures(final String strPath) {
        File file = new File(strPath);
        File[] allfiles = file.listFiles();
        if (allfiles == null) {
            return null;
        }
        for(int k = 0; k < allfiles.length; k++) {
            final File fi = allfiles[k];
            if(fi.isFile()) {
                int idx = fi.getPath().lastIndexOf(".");
                int idxname = fi.getPath().lastIndexOf("/");
                if (idx <= 0) {
                    continue;
                }
                String suffixname = fi.getPath().substring(idxname+1,idx);
                String suffix = fi.getPath().substring(idx);
                if (suffix.toLowerCase().equals(".jpg") ||
                        suffix.toLowerCase().equals(".jpeg") ||
                        suffix.toLowerCase().equals(".bmp") ||
                        suffix.toLowerCase().equals(".png") ||
                        suffix.toLowerCase().equals(".gif") ) {
                    list.add(fi.getPath());
                    mNameList.add(suffixname);
                }
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

        public View getView(int position, View convertView, ViewGroup parent) {
            ItemViewTag viewTag;

            if (convertView == null)
            {
                convertView = getLayoutInflater(null).inflate(R.layout.pic_gridview_item, null);

                // construct an item tag
                viewTag = new ItemViewTag((ImageView) convertView.findViewById(R.id.grid_imageview),
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


    public static CharSequence getTitle(Context context) {
        return context.getText(R.string.library_pic_title);
    }

}
