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
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private ArrayList<String> mIconlist = new ArrayList<String>();
    private ArrayList<String> mNameList = new ArrayList<String>();
    private GridAdapter adapter = new GridAdapter();
    private int index = 0;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

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
                                    getActivity(), view.findViewById(R.id.cardview),"TEST");
                    Intent intent = new Intent(getActivity(), PicPlayActivity.class);
                    intent.putExtra("path",mIconlist.get(position));
                    ActivityCompat.startActivity( getActivity(),intent, options.toBundle());
                }
            }
        });
        gridListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0) {
                    view.findViewById(R.id.pic_item).setVisibility(View.GONE);
                    view.findViewById(R.id.delete_item).setVisibility(View.VISIBLE);
                }
                return true;
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
        header.initWithString("YOU CAN FLY");

        frame.setLoadingMinTime(1000);
        frame.setDurationToCloseHeader(2500);
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
                    mIconlist.add(fi.getPath());
                    mNameList.add(suffixname);
                }
            }
        }
        return mIconlist;
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
            ItemViewTag viewTag;

            if (convertView == null)
            {
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.pic_gridview_item, parent, false);
                // construct an item tag
                viewTag = new ItemViewTag();
                viewTag.mIcon = (ImageView) convertView.findViewById(R.id.grid_imageview);
                viewTag.mName = (TextView) convertView.findViewById(R.id.grid_name);
                viewTag.mDelete = (Button) convertView.findViewById(R.id.delete);
                viewTag.mLinearLayout = (LinearLayout) convertView.findViewById(R.id.delete_item);
                convertView.setTag(viewTag);
            } else
            {
                viewTag = (ItemViewTag) convertView.getTag();
            }

            // set name
            viewTag.mName.setText(mNameList.get(position));

            // set icon
            ImageLoader.getInstance().displayImage("file://"+mIconlist.get(position),viewTag.mIcon,options);

            final View finalConvertView = convertView;

            viewTag.mDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mNameList.remove(position);
                    mIconlist.remove(position);
                    finalConvertView.findViewById(R.id.pic_item).setVisibility(View.VISIBLE);
                    finalConvertView.findViewById(R.id.delete_item).setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                }
            });

            viewTag.mLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finalConvertView.findViewById(R.id.pic_item).setVisibility(View.VISIBLE);
                    finalConvertView.findViewById(R.id.delete_item).setVisibility(View.GONE);
                }
            });

            return convertView;
        }

        class ItemViewTag
        {
            protected ImageView mIcon;
            protected TextView mName;
            protected Button mDelete;
            protected LinearLayout mLinearLayout;
        }

    }


    public static CharSequence getTitle(Context context) {
        return context.getText(R.string.library_pic_title);
    }

}
