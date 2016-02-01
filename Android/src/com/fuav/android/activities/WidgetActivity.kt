package com.fuav.android.activities

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.v4.app.Fragment
import android.widget.FrameLayout
import com.fuav.android.R
import com.fuav.android.fragments.actionbar.ActionBarTelemFragment
import com.fuav.android.fragments.widget.VideoControlFragment
import com.fuav.android.fragments.widget.video.FullWidgetSoloLinkVideo
import com.fuav.android.maps.providers.baidu_map.BaiduMapFragment

/**
 * Created by Fredia Huya-Kouadio on 7/19/15.
 */
public class WidgetActivity : DrawerNavigationUI() {

    override fun getNavigationDrawerMenuItemId(): Int {
        return 0;
    }

    companion object {
        const val EXTRA_WIDGET_ID = "extra_widget_id"
    }
    private var control_frame: FrameLayout? =null

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE// 横屏
        setContentView(R.layout.activity_widget)

        control_frame = findViewById(R.id.control_view) as FrameLayout?
        control_frame?.setOnClickListener({
            startActivity(Intent(this, FlightActivity::class.java))
        })
        val fm = supportFragmentManager
        var flightMapFragment = BaiduMapFragment()
            fm.beginTransaction().replace(R.id.map_view, flightMapFragment).commit()
        fm.beginTransaction().replace(R.id.widget_view, FullWidgetSoloLinkVideo() ).commit()
        fm.beginTransaction().replace(R.id.control_view,VideoControlFragment()).commit()
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
    }

    override fun addToolbarFragment() {
        val toolbarId = toolbarId
        val fm = supportFragmentManager
        var actionBarTelem: Fragment? = fm.findFragmentById(toolbarId)
        if (actionBarTelem == null) {
            actionBarTelem = ActionBarTelemFragment()
            fm.beginTransaction().add(toolbarId, actionBarTelem).commit()
        }
    }


    override fun getToolbarId() = R.id.actionbar_container

}