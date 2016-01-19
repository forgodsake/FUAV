package com.fuav.android.fragments.home;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fuav.android.R;
import com.fuav.android.fragments.Devices.DeviceGimbalFragment;
import com.fuav.android.fragments.Devices.DeviceSeraphiFragment;
import com.fuav.android.fragments.Devices.DeviceSmargleFragment;

import java.util.ArrayList;
import java.util.List;


public class DeviceFragment extends Fragment {

    private ViewPager viewpager;
    private List<Fragment> fragments;


    public DeviceFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_device, container, false);

        viewpager = (ViewPager) view.findViewById(R.id.content);

        fragments = new ArrayList<>();
        initViews();

        viewpager.setAdapter(new CustomPagerAdapter(getChildFragmentManager()));
        return view;
    }

    private void initViews() {
        fragments.add(new DeviceSeraphiFragment());
        fragments.add(new DeviceSmargleFragment());
        fragments.add(new DeviceGimbalFragment());

    }



    private class CustomPagerAdapter extends FragmentPagerAdapter {

        public CustomPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }




}
