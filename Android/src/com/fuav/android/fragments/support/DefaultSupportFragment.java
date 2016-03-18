<<<<<<< HEAD
package com.fuav.android.fragments.support;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.fuav.android.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class DefaultSupportFragment extends Fragment {

    private FragmentManager fragmentManager;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_default_support, container, false);
        supportList = (ListView) view.findViewById(R.id.listViewSupport);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        list = new ArrayList<HashMap<String,Object>>();
        fragmentManager = getChildFragmentManager();
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
                switch (position){
                    case 0:
                        fragmentManager.beginTransaction().replace(R.id.support_list_frame,new ContactUsFragment()).commit();
                        break;
                    case 1:
                        fragmentManager.beginTransaction().replace(R.id.support_list_frame,new FAQFragment()).commit();
                        break;
                    case 2:
                        fragmentManager.beginTransaction().replace(R.id.support_list_frame,new ScopeOfServiceFragment()).commit();
                        break;
                    case 3:
                        fragmentManager.beginTransaction().replace(R.id.support_list_frame,new AboutUsFragment()).commit();
                        break;
                    case 4:
                        fragmentManager.beginTransaction().replace(R.id.support_list_frame,new UserAgreementFragment()).commit();
                        break;
                    case 5:
                        fragmentManager.beginTransaction().replace(R.id.support_list_frame,new FeedBackFragment()).commit();
                        break;
                    default:
                        break;
                }
            }
        });
    }

}
=======
package com.fuav.android.fragments.support;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.fuav.android.R;
import com.fuav.android.activities.SupportActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class DefaultSupportFragment extends Fragment {

    private FragmentManager fragmentManager;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_default_support, container, false);
        supportList = (ListView) view.findViewById(R.id.listViewSupport);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        list = new ArrayList<HashMap<String,Object>>();
        fragmentManager = getParentFragment().getChildFragmentManager();
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
>>>>>>> 17c16ff529b34972c2c5e8d3a26372621c3a067e
