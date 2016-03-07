package com.fuav.android.fragments.home;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fuav.android.R;
import com.fuav.android.fragments.library.PictureFragment;
import com.fuav.android.fragments.library.VideoFragment;
import com.fuav.android.view.viewPager.TabPageIndicator;

/**
 * A simple {@link Fragment} subclass.
 */
public class LibraryFragment extends Fragment {


    public LibraryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_library, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final LibraryPagerAdapter pagerAdapter = new LibraryPagerAdapter(getActivity()
                .getApplicationContext(), getChildFragmentManager());

        final ViewPager viewPager = (ViewPager) view.findViewById(R.id.view_pager_view_pager);
        viewPager.setAdapter(pagerAdapter);
//        viewPager.setCurrentItem(0);
//        viewPager.setOffscreenPageLimit(2);

        final TabPageIndicator tabIndicator = (TabPageIndicator) view.findViewById(R.id.view_pager_tab_indicator);
        tabIndicator.setViewPager(viewPager);
    }

    private static class LibraryPagerAdapter extends FragmentPagerAdapter {

        private final Context context;

        public LibraryPagerAdapter(Context context, FragmentManager fm) {
            super(fm);
            this.context = context;
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return new PictureFragment();
                case 1:
                    return new VideoFragment();
                default:
                    return new PictureFragment();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return PictureFragment.getTitle(context);
                case 1:
                    return VideoFragment.getTitle(context);
                default:
                    return PictureFragment.getTitle(context);
            }
        }
    }

}
