package com.fuav.android.utils.video;

/**
 * Created by fhuya on 12/4/14.
 */
public interface DecoderListener {

    void onDecodingStarted();

    void onDecodingError();

    void onDecodingEnded();

}
