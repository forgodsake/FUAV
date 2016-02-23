package com.fuav.android.activities

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.FrameLayout
import android.widget.ImageView
import com.baidu.platform.comapi.map.B
import com.baidu.platform.comapi.map.C
import com.fuav.android.R
import com.fuav.android.fragments.FlightMapFragment
import com.fuav.android.fragments.actionbar.ActionBarTelemFragment
import com.fuav.android.fragments.widget.VideoControlFragment
import com.fuav.android.fragments.widget.video.FullWidgetSoloLinkVideo
import com.fuav.android.utils.prefs.DroidPlannerPrefs

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
    private var shot_switch_left:ImageView?=null
    private var shot_switch_right:ImageView?=null
    private var video:ImageView?=null
    private var camera:ImageView?=null
    private var control_frame: FrameLayout? =null
    private var index :Int = 0;

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE// 横屏
        setContentView(R.layout.activity_widget)

        control_frame = findViewById(R.id.control_view) as FrameLayout?
        control_frame?.setOnClickListener({
            var intent = Intent(this, FlightActivity::class.java)
//            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        })


        var animationl:Animation = TranslateAnimation(0f,200f,0f,0f)
        animationl.duration = 800
        var animationr:Animation = TranslateAnimation(0f,-200f,0f,0f)
        animationr.duration = 800
        shot_switch_left = findViewById(R.id.shot_switch_left) as ImageView
        shot_switch_right = findViewById(R.id.shot_switch_right) as ImageView
        video = findViewById(R.id.video) as ImageView
        camera = findViewById(R.id.camera) as ImageView
        shot_switch_left?.setOnClickListener({
            if(index%2==0){
                shot_switch_left?.startAnimation(animationl)
                video?.setImageResource(R.drawable.video_off)
                camera?.setImageResource(R.drawable.camera_on)
                shot_switch_left?.visibility= View.GONE
                Handler().postDelayed({
                    shot_switch_right?.visibility= View.VISIBLE
                }, 800)//delay 2000ms
                index++
            }

        })
        shot_switch_right?.setOnClickListener({
            if(index%2==1){
                shot_switch_right?.startAnimation(animationr)
                video?.setImageResource(R.drawable.video_on)
                camera?.setImageResource(R.drawable.camera_off)
                shot_switch_right?.visibility= View.GONE
                Handler().postDelayed({
                    shot_switch_left?.visibility= View.VISIBLE
                }, 800)//delay 2000ms
                index++
            }

        })

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        val fm = supportFragmentManager
        var flightMapFragment = FlightMapFragment()

        var dpPrefs = DroidPlannerPrefs(this)
        if(dpPrefs.mapProviderName.equals("百度地图")){
            fm.beginTransaction().replace(R.id.map_view, flightMapFragment).commit()
        }else{
            fm.beginTransaction().replace(R.id.map_view2, flightMapFragment).commit()
        }
        fm.beginTransaction().replace(R.id.widget_view, FullWidgetSoloLinkVideo()).commit()
        fm.beginTransaction().replace(R.id.mission_view, VideoControlFragment()).commit()
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

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
    }

}