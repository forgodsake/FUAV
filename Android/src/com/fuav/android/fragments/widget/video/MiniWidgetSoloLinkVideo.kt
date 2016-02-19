package com.fuav.android.fragments.widget.video

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.demo.sdk.DisplayView
import com.fuav.android.R
import com.fuav.android.activities.FlightActivity
import com.fuav.android.activities.WidgetActivity
import com.fuav.android.fragments.widget.TowerWidget
import com.fuav.android.fragments.widget.TowerWidgets
import com.fuav.android.utils.VideoThread

/**
 * Created by Fredia Huya-Kouadio on 7/19/15.
 */
public class MiniWidgetSoloLinkVideo : TowerWidget() {

    override fun getWidgetType()= TowerWidgets.SOLO_VIDEO

    override fun onApiDisconnected() {
    }

    override fun onApiConnected() {
    }

    private val _displayView by lazy(LazyThreadSafetyMode.NONE) {
        view?.findViewById(R.id.sololink_video_view) as DisplayView?
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_mini_widget_solo_video, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        VideoThread(_displayView,activity).start()

        var activity = activity as FlightActivity;
        _displayView?.setOnClickListener({ startActivity(Intent(activity, WidgetActivity::class.java)) })


    }

}