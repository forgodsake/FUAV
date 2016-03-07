package com.fuav.android.fragments.home;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.fuav.android.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class SupportFragment extends Fragment {

    private int [] images = new int [] {
            R.drawable.more1,R.drawable.more2,R.drawable.more3,
            R.drawable.more4,R.drawable.more5,R.drawable.more6
    };
    private String [] des = new String [] {
            "联系客服","常见问题","服务范围","关于我们","用户协议","意见反馈"
    };

    private ListView supportList;
    private SimpleAdapter adapter;
    private List<HashMap<String, Object>> list;


    public SupportFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_support, container, false);
        supportList = (ListView) view.findViewById(R.id.listViewSupport);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        list = new ArrayList<HashMap<String,Object>>();
        for (int i = 0; i < images.length; i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("image", images[i]);
            map.put("des", des[i]);
            list.add(map);
        }
        adapter = new SimpleAdapter(getActivity(), list, R.layout.more_item, new String[]{"image","des"}, new int []{R.id.imageViewIcon,R.id.textViewDescri});
        supportList.setAdapter(adapter);
    }
}
