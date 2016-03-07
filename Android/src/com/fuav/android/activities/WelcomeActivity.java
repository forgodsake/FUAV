package com.fuav.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.fuav.android.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/12/4.
 */
public class WelcomeActivity extends Activity{

    private ViewPager viewPager;
    private LinearLayout linearLayout;
    private List<View> views;
    /**将小圆点的图片用数组表示*/
    private ImageView imageView;
    private ImageView[] imageViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);// 横屏
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        views = new ArrayList<View>(4);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        linearLayout = (LinearLayout) findViewById(R.id.container);
        ImageView imageView1 = new ImageView(this);
        imageView1.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView1.setImageResource(R.drawable.aa);
        views.add(imageView1);
        ImageView imageView2 = new ImageView(this);
        imageView2.setImageResource(R.drawable.bb);
        imageView2.setScaleType(ImageView.ScaleType.FIT_XY);
        views.add(imageView2);
        ImageView imageView3 = new ImageView(this);
        imageView3.setImageResource(R.drawable.cc);
        imageView3.setScaleType(ImageView.ScaleType.FIT_XY);
        views.add(imageView3);
        ImageView imageView4 = new ImageView(this);
        imageView4.setImageResource(R.drawable.dd);
        imageView4.setScaleType(ImageView.ScaleType.FIT_XY);
        views.add(imageView4);
        View view = LayoutInflater.from(this).inflate(R.layout.login_layout,null);
        final Button button = (Button)view.findViewById(R.id.button);
        views.add(view);
        imageViews = new ImageView[views.size()];

        //添加小圆点的图片
        for(int i=0;i<views.size();i++){
            imageView = new ImageView(this);
            //设置小圆点imageview的参数
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(20,20);
            lp.setMargins(15,0,15,0);
            imageView.setLayoutParams(lp);//创建一个宽高均为20 的布局
            imageViews[i] = imageView;
            //默认选中的是第一张图片，此时第一个小圆点是选中状态，其他不是
            if(i==0){
                imageViews[i].setBackgroundResource(R.drawable.guide_dot_white);
            }else{
                imageViews[i].setBackgroundResource(R.drawable.guide_dot_black);
            }

            //将imageviews添加到小圆点视图组
            linearLayout.addView(imageViews[i]);
        }
        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return views.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public void destroyItem(ViewGroup container, int position,
                                    Object object) {
                container.removeView(views.get(position));
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                container.addView(views.get(position));
                return views.get(position);
            }
        });
        viewPager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                for(int i=0;i<imageViews.length;i++){
                    //不是当前选中的page，其小圆点设置为未选中的状态
                    imageViews[i].setBackgroundResource(R.drawable.guide_dot_black);
                }
                imageViews[position].setBackgroundResource(R.drawable.guide_dot_white);
                if (position==views.size()-1){
                    button.setVisibility(View.VISIBLE);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            getSharedPreferences("pf",MODE_APPEND).edit().putString("first","right").commit();
                            startActivity(new Intent(WelcomeActivity.this,HomeActivity.class));
                            finish();
                        }
                    });
                }else{
                    button.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
}
