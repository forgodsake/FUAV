package com.fuav.android.entity;

import com.fuav.android.R;

/**
 * Created by Administrator on 2016/3/7 0007.
 */
public class Commodity {
    public int[] getImages() {
        return images;
    }
    public void setImages(int[] images) {
        this.images = images;
    }
    public String[] getNames() {
        return names;
    }
    public void setNames(String[] names) {
        this.names = names;
    }
    public String[] getPrices() {
        return prices;
    }
    public void setPrices(String[] prices) {
        this.prices = prices;
    }
    //初始化填充数据
    private int []images = new int []{
            R.drawable.smargle,R.drawable.smargle1,R.drawable.gimbal,
            R.drawable.dynamo,R.drawable.camera,R.drawable.tuchuan,
            R.drawable.protectcover,R.drawable.shuchuan
    };
    private String [] names = new String[]{
            "smargle","seraphi","手持云台","无刷电机","相机","5.8G图传","保护罩","433数传"
    };
    private String [] prices = new String[]{
            "2888","2288","1200","280","680","899","100","250"
    };
}
