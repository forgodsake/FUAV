package com.fuav.android.communication.connection;

import android.content.Context;

import com.fuav.android.core.MAVLink.connection.MavLinkConnection;
import com.fuav.android.core.model.Logger;
import com.fuav.android.utils.AndroidLogger;

public abstract class AndroidMavLinkConnection extends MavLinkConnection {

    private static final String TAG = AndroidMavLinkConnection.class.getSimpleName();

    protected final Context mContext;

    public AndroidMavLinkConnection(Context applicationContext) {
        this.mContext = applicationContext;
    }

    @Override
    protected final Logger initLogger() {
        return AndroidLogger.getLogger();
    }
}
