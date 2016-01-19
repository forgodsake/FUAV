package com.fuav.android.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.fuav.android.R;
import com.fuav.android.fragments.home.DeviceFragment;
import com.fuav.android.fragments.home.LibraryFragment;
import com.fuav.android.fragments.home.MallFragment;
import com.fuav.android.fragments.home.SupportFragment;

public class HomeActivity extends AppCompatActivity {

    private RadioGroup radioGroup;
    private FragmentManager manager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        manager = getSupportFragmentManager();
        radioGroup = (RadioGroup) findViewById(R.id.menu);
        radioGroup.setOnCheckedChangeListener(new CustomCheckedChangeListener());
        manager.beginTransaction().add(R.id.content,new DeviceFragment()).commit();
    }


    private class CustomCheckedChangeListener implements OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId){
                case R.id.radioButton:
                    manager.beginTransaction().replace(R.id.content,new DeviceFragment()).commit();
                    break;
                case R.id.radioButton2:
                    manager.beginTransaction().replace(R.id.content,new LibraryFragment()).commit();
                    break;
                case R.id.radioButton3:
                    manager.beginTransaction().replace(R.id.content,new MallFragment()).commit();
                    break;
                case R.id.radioButton4:
                    manager.beginTransaction().replace(R.id.content,new SupportFragment()).commit();
                    break;
                default:
                    break;

            }

        }
    }

}
