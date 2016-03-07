package com.fuav.android.fragments.library;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import com.fuav.android.activities.PicPlayActivity;
import com.fuav.android.utils.LocalDisplay;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.header.StoreHouseHeader;

/**
 * A simple {@link Fragment} subclass.
 */
public class PictureFragment extends Fragment {

    //显示图片的配置
    DisplayImageOptions options ;
    List<String> list = new ArrayList<String>();
    private ArrayList<String> mNameList = new ArrayList<String>();
    private GridAdapter adapter = new GridAdapter();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_storehouse_header, null);

        final GridView gridListView = (GridView) view.findViewById(R.id.rotate_header_grid_view);

        //获取sd卡下的图片并显示
        getPictures(Environment.getExternalStorageDirectory() + "/FUAV");
        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        gridListView.setAdapter(adapter);

        gridListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0) {
                    ActivityOptionsCompat options =
                            ActivityOptionsCompat.makeSceneTransitionAnimation(
                                    getActivity(), view.findViewById(R.id.grid_imageview),"TEST");
                    Intent intent = new Intent(getActivity(), PicPlayActivity.class);
                    intent.putExtra("path",list.get(position));
                    ActivityCompat.startActivity( getActivity(),intent, options.toBundle());
                }
            }
        });

        final PtrFrameLayout frame = (PtrFrameLayout) view.findViewById(R.id.store_house_ptr_frame);
        // header
        final StoreHouseHeader header = new StoreHouseHeader(getContext());
        header.setLayoutParams(new PtrFrameLayout.LayoutParams(-1, -2));
        header.setPadding(0, LocalDisplay.dp2px(15), 0, LocalDisplay.dp2px(10));
        /**
         * using a string, support: A-Z 0-9 - .
         * you can add more letters by {@link in.srain.cube.views.ptr.header.StoreHousePath#addChar}
         */
        header.initWithString("FUAV");

        frame.setLoadingMinTime(1000);
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
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
            }

            @Override
            public void onRefreshBegin(final PtrFrameLayout frame) {
                frame.postDelayed(new Runnable() {
                    @Override
                    public void run() {
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
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.pic_gridview_item, parent, false);
                // construct an item tag
                viewTag = new ItemViewTag();
                viewTag.mIcon = (ImageView) convertView.findViewById(R.id.grid_imageview);
                viewTag.mName = (TextView) convertView.findViewById(R.id.grid_name);
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
        }

    }


    public static CharSequence getTitle(Context context) {
        return context.getText(R.string.library_pic_title);
    }

}
