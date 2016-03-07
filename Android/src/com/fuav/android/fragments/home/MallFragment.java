package com.fuav.android.fragments.home;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import com.fuav.android.R;
import com.fuav.android.entity.Commodity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MallFragment extends Fragment {

    private GridView gridView;
    private SimpleAdapter adapter;
    private List<HashMap<String, Object>> list;
    private Commodity commodity = new Commodity();

    public MallFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mall, container, false);
        gridView = (GridView) view.findViewById(R.id.store_grid_view);
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        list = new ArrayList<HashMap<String,Object>>();
        for (int i = 0;i < commodity.getImages().length; i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("image",commodity.getImages()[i]);
            map.put("name", commodity.getNames()[i]);
            map.put("price",commodity.getPrices()[i]);
            list.add(map);
        }
        adapter = new SimpleAdapter(getActivity(), list, R.layout.grid_commodity_item,
                new String[]{"image","name","price"}, new int[]{R.id.imageViewGoods,R.id.textViewName,R.id.textViewPrice});
        gridView.setAdapter(adapter);
    }


}
