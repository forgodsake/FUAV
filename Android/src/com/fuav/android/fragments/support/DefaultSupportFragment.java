package com.fuav.android.fragments.support;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;

import com.fuav.android.R;
import com.fuav.android.activities.SupportActivity;
import com.fuav.android.view.CustListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class DefaultSupportFragment extends Fragment {

    private int [] images = new int [] {
            R.drawable.more1,R.drawable.more2,R.drawable.more3,
            R.drawable.more4,R.drawable.more5,R.drawable.more6
    };
    private String [] des = new String [] {
            "联系客服","常见问题","服务范围","关于我们","用户协议","意见反馈"
    };

    private CustListView supportList;
    private SimpleAdapter adapter;
    private List<HashMap<String, Object>> list;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_default_support, container, false);
        supportList = (CustListView) view.findViewById(R.id.listViewSupport);
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
        supportList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), SupportActivity.class);
                switch (position){
                    case 0:
                        intent.putExtra("detail","ContactUs");
                        startActivity(intent);
                        break;
                    case 1:
                        intent.putExtra("detail","FAQ");
                        startActivity(intent);
                        break;
                    case 2:
                        intent.putExtra("detail","ScopeOfService");
                        startActivity(intent);
                        break;
                    case 3:
                        intent.putExtra("detail","AboutUs");
                        startActivity(intent);
                        break;
                    case 4:
                        intent.putExtra("detail","UserAgreement");
                        startActivity(intent);
                        break;
                    case 5:
                        intent.putExtra("detail","FeedBack");
                        startActivity(intent);
                        break;
                    default:
                        break;
                }
            }
        });
    }



}
