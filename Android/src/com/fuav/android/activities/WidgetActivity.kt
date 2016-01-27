package com.fuav.android.activities

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.v4.app.Fragment
import com.fuav.android.R
import com.fuav.android.activities.helpers.SuperUI
import com.fuav.android.fragments.FlightMapFragment
import com.fuav.android.fragments.actionbar.ActionBarTelemFragment
import com.fuav.android.fragments.widget.TowerWidget
import com.fuav.android.fragments.widget.TowerWidgets
import com.fuav.android.fragments.widget.video.FullWidgetSoloLinkVideo

/**
 * Created by Fredia Huya-Kouadio on 7/19/15.
 */
public class WidgetActivity : SuperUI() {

    companion object {
        const val EXTRA_WIDGET_ID = "extra_widget_id"
    }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE// 横屏
        setContentView(R.layout.activity_widget)

        val fm = supportFragmentManager
        var flightMapFragment = fm.findFragmentById(R.id.map_view) as FlightMapFragment?
        if(flightMapFragment == null){
            flightMapFragment = FlightMapFragment()
            fm.beginTransaction().add(R.id.map_view, flightMapFragment).commit()
        }
        fm.beginTransaction().replace(R.id.widget_view, FullWidgetSoloLinkVideo() ).commit()

//        handleIntent(intent)
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
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

    override fun onNewIntent(intent: Intent?){
        super.onNewIntent(intent)
        if(intent != null)
            handleIntent(intent)
    }

    private fun handleIntent(intent: Intent){
        val widgetId = intent.getIntExtra(EXTRA_WIDGET_ID, 0)
        val fm = supportFragmentManager

        val widget = TowerWidgets.getWidgetById(widgetId)
        if(widget != null){
            setToolbarTitle(widget.labelResId)

            val currentWidgetType = (fm.findFragmentById(R.id.widget_view) as TowerWidget?)?.getWidgetType()

            if(widget == currentWidgetType)
                return

            val widgetFragment = widget.getMaximizedFragment()
            fm.beginTransaction().replace(R.id.widget_view, widgetFragment).commit()
        }
    }

    override fun getToolbarId() = R.id.actionbar_container


}