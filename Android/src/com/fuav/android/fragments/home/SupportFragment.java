package com.fuav.android.fragments.home;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fuav.android.R;
import com.fuav.android.fragments.support.DefaultSupportFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class SupportFragment extends Fragment {

    private FragmentManager fragmentManager;
    private DefaultSupportFragment defaultSupportFragment;

    public SupportFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_support, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fragmentManager = getChildFragmentManager();
        defaultSupportFragment = new DefaultSupportFragment();
        fragmentManager.beginTransaction().replace(R.id.support_frame,defaultSupportFragment).commit();

    }
}
