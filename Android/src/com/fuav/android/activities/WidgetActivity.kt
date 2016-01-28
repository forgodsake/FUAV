package com.fuav.android.activities

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.v4.app.Fragment
import android.widget.Button
import com.fuav.android.R
import com.fuav.android.activities.helpers.SuperUI
import com.fuav.android.fragments.FlightMapFragment
import com.fuav.android.fragments.actionbar.ActionBarTelemFragment
import com.fuav.android.fragments.widget.video.FullWidgetSoloLinkVideo

/**
 * Created by Fredia Huya-Kouadio on 7/19/15.
 */
public class WidgetActivity : SuperUI() {

    companion object {
        const val EXTRA_WIDGET_ID = "extra_widget_id"
    }

    private var button_take_off: Button? = null
    private var button_go_home: Button? = null
    private var button_hover: Button? = null
    private var button_follow_me: Button? = null
    private var index: Int=0;

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
        button_take_off = findViewById(R.id.button_take_off) as Button
        button_take_off?.setOnClickListener({
            initBackground()
            if(index%2==0){
                button_take_off?.setBackgroundResource(R.drawable.button_land)
            }
            index++;
        })
        button_go_home = findViewById(R.id.button_go_home) as Button
        button_go_home?.setOnClickListener({
            initBackground()
            button_go_home?.setBackgroundResource(R.drawable.go_home_on)
        })
        button_hover = findViewById(R.id.button_hover) as Button
        button_hover?.setOnClickListener({
            initBackground()
            button_hover?.setBackgroundResource(R.drawable.hover_on)
        })
        button_follow_me = findViewById(R.id.button_follow_me) as Button
        button_follow_me?.setOnClickListener({
            initBackground()
            button_follow_me?.setBackgroundResource(R.drawable.follow_me_on)
        })
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
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

//    override fun onNewIntent(intent: Intent?){
//        super.onNewIntent(intent)
//        if(intent != null)
//            handleIntent(intent)
//    }
//
//    private fun handleIntent(intent: Intent){
//        val widgetId = intent.getIntExtra(EXTRA_WIDGET_ID, 0)
//        val fm = supportFragmentManager
//
//        val widget = TowerWidgets.getWidgetById(widgetId)
//        if(widget != null){
//            setToolbarTitle(widget.labelResId)
//
//            val currentWidgetType = (fm.findFragmentById(R.id.widget_view) as TowerWidget?)?.getWidgetType()
//
//            if(widget == currentWidgetType)
//                return
//
//            val widgetFragment = widget.getMaximizedFragment()
//            fm.beginTransaction().replace(R.id.widget_view, widgetFragment).commit()
//        }
//    }

    override fun getToolbarId() = R.id.actionbar_container

    fun initBackground(){
        button_take_off?.setBackgroundResource(R.drawable.button_take_off)
        button_go_home?.setBackgroundResource(R.drawable.button_go_home)
        button_hover?.setBackgroundResource(R.drawable.button_hover)
        button_follow_me?.setBackgroundResource(R.drawable.button_follow_me)
    }

}