package com.fuav.android.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Window;
import android.view.WindowManager;

import com.fuav.android.R;
import com.fuav.android.fragments.SensorSetupFragment;

/**
 * This class implements and handles the various ui used for the drone
 * configuration.
 */
public class ConfigurationActivity extends DrawerNavigationUI {

	/**
	 * Used as logging tag.
	 */
	private static final String TAG = ConfigurationActivity.class.getSimpleName();

	public static final String EXTRA_CONFIG_SCREEN_ID = ConfigurationActivity.class.getPackage()
			.getName() + ".EXTRA_CONFIG_SCREEN_ID";

    private int mConfigScreenId = R.id.navigation_calibration;

	@Override
	public void onCreate(Bundle savedInstanceState) {

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);// 横屏
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_configuration);

        if(savedInstanceState != null){
            mConfigScreenId = savedInstanceState.getInt(EXTRA_CONFIG_SCREEN_ID, mConfigScreenId);
        }

		handleIntent(getIntent());
	}

    @Override
    protected int getToolbarId() {
        return R.id.actionbar_toolbar;
    }

    @Override
    protected int getNavigationDrawerMenuItemId() {
        return mConfigScreenId;
    }

    @Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
	}

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_CONFIG_SCREEN_ID, mConfigScreenId);
    }

	private void handleIntent(Intent intent) {
		final int configScreenId = intent.getIntExtra(EXTRA_CONFIG_SCREEN_ID, mConfigScreenId);
        final Fragment currentFragment = getCurrentFragment();
        if(currentFragment == null || getIdForFragment(currentFragment) != configScreenId){
            mConfigScreenId = configScreenId;
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.configuration_screen, getFragmentForId(configScreenId))
                    .commit();
        }
	}

    private Fragment getCurrentFragment(){
        return getSupportFragmentManager().findFragmentById(R.id.configuration_screen);
    }

    private Fragment getFragmentForId(int fragmentId){
        final Fragment fragment;
        switch(fragmentId){
            case R.id.navigation_calibration:
                fragment = new SensorSetupFragment();
                break;
            default:
                fragment = new SensorSetupFragment();
                break;
        }

        return fragment;
    }

    private int getIdForFragment(Fragment fragment){
        return fragment.getId();
    }

    @Override
    public void onApiConnected() {
        super.onApiConnected();
    }
}
