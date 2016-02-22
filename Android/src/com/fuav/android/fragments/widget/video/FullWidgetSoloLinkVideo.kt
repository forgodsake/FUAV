package com.fuav.android.fragments.widget.video

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.demo.sdk.DisplayView
import com.fuav.android.R
import com.fuav.android.utils.VideoThread

/**
 * Created by Fredia Huya-Kouadio on 7/19/15.
 */
public class FullWidgetSoloLinkVideo : Fragment() {



    private val _displayView by lazy(LazyThreadSafetyMode.NONE) {
        view?.findViewById(R.id.sololink_video_view) as DisplayView?
    }


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_widget_sololink_video, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

    override fun onResume() {
        super.onResume()
        VideoThread(_displayView,activity).start()
    }
}