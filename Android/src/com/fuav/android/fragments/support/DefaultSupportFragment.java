package com.fuav.android.fragments.support;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.fuav.android.R;
import com.fuav.android.activities.SupportActivity;
import com.fuav.android.chat.DemoHelper;
import com.fuav.android.chat.ui.LoginActivity;
import com.fuav.android.chat.ui.MainActivity;
import com.fuav.android.chat.ui.RegisterActivity;
import com.fuav.android.view.CircleImageView;
import com.fuav.android.view.CustListView;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.utils.EaseUserUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.onekeyshare.OnekeyShareTheme;

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
    private CircleImageView head_portrait;
    private TextView textview;
    private Button btn_register,btn_login;
    private ImageView img_share;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_default_support, container, false);
        supportList = (CustListView) view.findViewById(R.id.listViewSupport);
        head_portrait = (CircleImageView) view.findViewById(R.id.head_portrait);
        textview = (TextView) view.findViewById(R.id.nickname);
        btn_register = (Button) view.findViewById(R.id.btn_register);
        btn_login = (Button) view.findViewById(R.id.btn_login);
        img_share = (ImageView) view.findViewById(R.id.img_share);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // 初始化ShareSDK
        ShareSDK.initSDK(getActivity());
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

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), RegisterActivity.class));
            }
        });

        initListener();

        // 如果登录成功过，直接进入主页面
        if (DemoHelper.getInstance().isLoggedIn()) {
            btn_login.setVisibility(View.GONE);
            btn_register.setVisibility(View.GONE);
            head_portrait.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getActivity(), MainActivity.class));
                }
            });
        }

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), LoginActivity.class));
            }
        });

        img_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showShare(getActivity(),null,true);
            }
        });

    }

    private void initListener() {
        String username = EMClient.getInstance().getCurrentUser();
        if(username != null){
            if (username.equals(EMClient.getInstance().getCurrentUser())) {
                EaseUserUtils.setUserNick(username, textview);
                EaseUserUtils.setUserAvatar(getActivity(), username, head_portrait);
            } else {
                EaseUserUtils.setUserNick(username, textview);
                EaseUserUtils.setUserAvatar(getActivity(), username, head_portrait);
                asyncFetchUserInfo(username);
            }
        }
    }

    public void asyncFetchUserInfo(String username){
        DemoHelper.getInstance().getUserProfileManager().asyncGetUserInfo(username, new EMValueCallBack<EaseUser>() {

            @Override
            public void onSuccess(EaseUser user) {
                if (user != null) {
                    DemoHelper.getInstance().saveContact(user);
                    if(getActivity().isFinishing()){
                        return;
                    }
                    textview.setText(user.getNick());
                    if(!TextUtils.isEmpty(user.getAvatar())){
                        Glide.with(getActivity()).load(user.getAvatar()).placeholder(R.drawable.em_default_avatar).into(head_portrait);
                    }else{
                        Glide.with(getActivity()).load(R.drawable.em_default_avatar).into(head_portrait);
                    }
                }
            }

            @Override
            public void onError(int error, String errorMsg) {
            }
        });
    }

    /**
     * 演示调用ShareSDK执行分享
     *
     * @param context
     * @param platformToShare  指定直接分享平台名称（一旦设置了平台名称，则九宫格将不会显示）
     * @param showContentEdit  是否显示编辑页
     */
    public static void showShare(Context context, String platformToShare, boolean showContentEdit) {
        OnekeyShare oks = new OnekeyShare();
        oks.setSilent(!showContentEdit);
        if (platformToShare != null) {
            oks.setPlatform(platformToShare);
        }
        //ShareSDK快捷分享提供两个界面第一个是九宫格 CLASSIC  第二个是SKYBLUE
        oks.setTheme(OnekeyShareTheme.CLASSIC);
        // 令编辑页面显示为Dialog模式
        oks.setDialogMode();
        // 在自动授权时可以禁用SSO方式
        oks.disableSSOWhenAuthorize();
        //oks.setAddress("12345678901"); //分享短信的号码和邮件的地址
        oks.setTitle("FUAV");
        oks.setTitleUrl("http://www.fuav.com");
        oks.setText("FUAV");
        //oks.setImagePath("/sdcard/test-pic.jpg");  //分享sdcard目录下的图片
//        oks.setImageUrl("http://");
        oks.setUrl("http://www.fuav.com"); //微信不绕过审核分享链接
        //oks.setFilePath("/sdcard/test-pic.jpg");  //filePath是待分享应用程序的本地路劲，仅在微信（易信）好友和Dropbox中使用，否则可以不提供
        oks.setComment("分享"); //我对这条分享的评论，仅在人人网和QQ空间使用，否则可以不提供
        oks.setSite("FUAV");  //QZone分享完之后返回应用时提示框上显示的名称
        oks.setSiteUrl("http://www.fuav.com");//QZone分享参数
        oks.setVenueName("ShareSDK");
        oks.setVenueDescription("This is a beautiful place!");
        // 将快捷分享的操作结果将通过OneKeyShareCallback回调
        //oks.setCallback(new OneKeyShareCallback());
        // 去自定义不同平台的字段内容
        //oks.setShareContentCustomizeCallback(new ShareContentCustomizeDemo());
        // 在九宫格设置自定义的图标
//        Bitmap logo = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
//        String label = "ShareSDK";
//        View.OnClickListener listener = new View.OnClickListener() {
//            public void onClick(View v) {
//
//            }
//        };
//        oks.setCustomerLogo(logo, label, listener);

        // 为EditPage设置一个背景的View
        //oks.setEditPageBackground(getPage());
        // 隐藏九宫格中的新浪微博
        // oks.addHiddenPlatform(SinaWeibo.NAME);

        // String[] AVATARS = {
        // 		"http://99touxiang.com/public/upload/nvsheng/125/27-011820_433.jpg",
        // 		"http://img1.2345.com/duoteimg/qqTxImg/2012/04/09/13339485237265.jpg",
        // 		"http://diy.qqjay.com/u/files/2012/0523/f466c38e1c6c99ee2d6cd7746207a97a.jpg",
        // 		"http://diy.qqjay.com/u2/2013/0422/fadc08459b1ef5fc1ea6b5b8d22e44b4.jpg",
        // 		"http://img1.2345.com/duoteimg/qqTxImg/2012/04/09/13339510584349.jpg",
        // 		"http://diy.qqjay.com/u2/2013/0401/4355c29b30d295b26da6f242a65bcaad.jpg" };
        // oks.setImageArray(AVATARS);              //腾讯微博和twitter用此方法分享多张图片，其他平台不可以

        // 启动分享
        oks.show(context);
    }


}
