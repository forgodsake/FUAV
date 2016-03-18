package com.fuav.android.fragments.home;


import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.fuav.android.R;
import com.fuav.android.entity.AnimationUtil;
import com.fuav.android.entity.BadgeView;
import com.fuav.android.entity.Commodity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MallFragment extends Fragment {

    private ListView listView;
    private List<HashMap<String, Object>> list;
    private Commodity commodity = new Commodity();
    private ImageView shopCart;// 购物车
    private BadgeView buyNumView;// 显示购买数量的控件
    private ImageView buyImg;// 这是在界面上跑的小图片

    public MallFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mall, container, false);
        shopCart = (ImageView) view.findViewById(R.id.shopping_img_cart);
        listView = (ListView) view.findViewById(R.id.store_grid_view);
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        buyNumView = new BadgeView(getContext(), shopCart);
        buyNumView.setTextColor(Color.WHITE);
        buyNumView.setBackgroundColor(Color.RED);
        buyNumView.setTextSize(12);
        buyNumView.setText(AnimationUtil.getNum()+ "");//显示数量
        buyNumView.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
        buyNumView.show();

        list = new ArrayList<HashMap<String,Object>>();
        for (int i = 0;i < commodity.getImages().length; i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("image",commodity.getImages()[i]);
            map.put("name", commodity.getNames()[i]);
            map.put("price",commodity.getPrices()[i]);
            list.add(map);
        }

        listView.setAdapter(new ListAdapter());

    }

    public class ListAdapter extends BaseAdapter {

        public ListAdapter() {

        }

        public int getCount() {
            return list.size();
        }

        public Object getItem(int position) {
            return list.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ItemViewTag viewTag;

            if (convertView == null)
            {
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.grid_commodity_item, parent, false);
                // construct an item tag
                viewTag = new ItemViewTag();
                viewTag.mIcon = (ImageView) convertView.findViewById(R.id.imageViewGoods);
                viewTag.mName = (TextView) convertView.findViewById(R.id.textViewName);
                viewTag.mPrice = (TextView) convertView.findViewById(R.id.textViewPrice);
                viewTag.mNum = (TextView) convertView.findViewById(R.id.textViewNum);
                viewTag.mAdd = (Button) convertView.findViewById(R.id.button_add);
                viewTag.mPay = (Button) convertView.findViewById(R.id.button_pay);
                convertView.setTag(viewTag);
            } else
            {
                viewTag = (ItemViewTag) convertView.getTag();
            }

            HashMap<String,Object>  map = list.get(position);

            // set name
            viewTag.mIcon.setImageResource((Integer) map.get("image"));
            viewTag.mName.setText((CharSequence) map.get("name"));
            viewTag.mPrice.setText((CharSequence) map.get("price"));
            viewTag.mAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int num = Integer.parseInt(viewTag.mNum.getText().toString());
                    //动画
                    int[] start_location = new int[2];// 一个整型数组，用来存储按钮的在屏幕的X、Y坐标
                    v.getLocationInWindow(start_location);// 这是获取购买按钮的在屏幕的X、Y坐标（这也是动画开始的坐标）
                    buyImg = new ImageView(getActivity());// buyImg是动画的图片，我的是一个小球（R.drawable.sign）
                    buyImg.setImageResource(R.drawable.sign);// 设置buyImg的图片
                    AnimationUtil.setAnim(buyImg, start_location,getActivity(), buyNumView,shopCart,num);// 开始执行动画
                }
            });

            viewTag.mPay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            // set icon

            return convertView;
        }

        class ItemViewTag
        {
            protected ImageView mIcon;
            protected TextView mName;
            protected TextView mPrice;
            protected TextView mNum;
            protected Button mAdd;
            protected Button mPay;
        }

    }


}
