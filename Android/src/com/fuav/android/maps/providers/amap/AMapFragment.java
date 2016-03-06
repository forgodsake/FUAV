package com.fuav.android.maps.providers.amap;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.Projection;
import com.amap.api.maps2d.SupportMapFragment;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.LatLngBounds;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.maps2d.model.Polygon;
import com.amap.api.maps2d.model.PolygonOptions;
import com.amap.api.maps2d.model.Polyline;
import com.amap.api.maps2d.model.PolylineOptions;
import com.amap.api.maps2d.model.VisibleRegion;
import com.fuav.android.DroidPlannerApp;
import com.fuav.android.R;
import com.fuav.android.maps.DPMap;
import com.fuav.android.maps.MarkerInfo;
import com.fuav.android.maps.providers.DPMapProvider;
import com.fuav.android.utils.DroneHelper;
import com.fuav.android.utils.collection.HashBiMap;
import com.fuav.android.utils.prefs.AutoPanMode;
import com.fuav.android.utils.prefs.DroidPlannerPrefs;
import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.FootPrint;
import com.o3dr.services.android.lib.drone.property.Gps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class AMapFragment extends SupportMapFragment implements DPMap, LocationSource, AMapLocationListener {

    private static final String TAG = AMapFragment.class.getSimpleName();
    private AMap mMap;
    private OnLocationChangedListener mListener;
    //声明AMapLocationClient类对象
    public AMapLocationClient mlocationClient = null;
    //声明mLocationOption对象
    public AMapLocationClientOption mLocationOption = null;


    private DPMap.OnMapClickListener mMapClickListener;
    private DPMap.OnMapLongClickListener mMapLongClickListener;
    private DPMap.OnMarkerClickListener mMarkerClickListener;
    private DPMap.OnMarkerDragListener mMarkerDragListener;
    private android.location.LocationListener mLocationListener;
    protected boolean useMarkerClickAsMapClick = false;

    private List<Polygon> polygonsPaths = new ArrayList<Polygon>();


    private final AtomicReference<AutoPanMode> mPanMode = new AtomicReference<AutoPanMode>(
            AutoPanMode.DISABLED);

    protected DroidPlannerApp dpApp;
    private Polygon footprintPoly;

    private final HashBiMap<MarkerInfo, Marker> mBiMarkersMap = new HashBiMap<MarkerInfo, Marker>();
    private DroidPlannerPrefs mAppPrefs;

    private Polyline flightPath;
    private Polyline missionPath;
    private Polyline mDroneLeashPath;
    private int maxFlightPathSize;
    private LatLng latLng;

    private static final float GO_TO_MY_LOCATION_ZOOM = 18.6f;

    private static final IntentFilter eventFilter = new IntentFilter(AttributeEvent.GPS_POSITION);

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case AttributeEvent.GPS_POSITION:
                    if (mPanMode.get() == AutoPanMode.DRONE) {
                        final Drone drone = getDroneApi();
                        if (!drone.isConnected())
                            return;

                        final Gps droneGps = drone.getAttribute(AttributeType.GPS);
                        if (droneGps != null && droneGps.isValid()) {
                            final LatLong droneLocation = droneGps.getPosition();
                            updateCamera(droneLocation);
                        }
                    }
                    break;
            }
        }
    };


    private void setUpMapIfNeeded() {
        if (mMap != null) {
            mMap.setMapType(AMap.MAP_TYPE_SATELLITE);

            MyLocationStyle myLocationStyle = new MyLocationStyle();
            myLocationStyle.myLocationIcon(BitmapDescriptorFactory
                    .fromResource(R.drawable.location_marker));// 设置小蓝点的图标
            myLocationStyle.strokeColor(Color.BLACK);// 设置圆形的边框颜色
            myLocationStyle.radiusFillColor(Color.argb(100, 0, 0, 180));// 设置圆形的填充颜色
            // myLocationStyle.anchor(int,int)//设置小蓝点的锚点
            myLocationStyle.strokeWidth(1.0f);// 设置圆形的边框粗细
            mMap.setMyLocationStyle(myLocationStyle);
            mMap.setLocationSource(this);// 设置定位监听
            mMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        dpApp = (DroidPlannerApp) activity.getApplication();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final FragmentActivity activity = getActivity();
        final Context context = activity.getApplicationContext();

        final View view = super.onCreateView(inflater, container, savedInstanceState);

        mAppPrefs = new DroidPlannerPrefs(context);

        final Bundle args = getArguments();
        if (args != null) {
            maxFlightPathSize = args.getInt(EXTRA_MAX_FLIGHT_PATH_SIZE);
        }

        mMap = getMap();
        mMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
        mMap.getUiSettings().setZoomControlsEnabled(false);

        final AMap.OnMapClickListener onMapClickListener = (new AMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                if (mMapClickListener != null) {
                    mMapClickListener.onMapClick(DroneHelper.GaodeLatLngToCoord(point));
                }
            }
        });

        mMap.setOnMapClickListener(onMapClickListener);
        mMap.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (useMarkerClickAsMapClick) {
                    onMapClickListener.onMapClick(marker.getPosition());
                    return true;
                }
                if (mMarkerClickListener != null) {
                    return mMarkerClickListener.onMarkerClick(mBiMarkersMap.getKey(marker));
                }
                return false;
            }
        });

        mMap.setOnMapLongClickListener(new AMap.OnMapLongClickListener() {
            public void onMapLongClick(LatLng point) {
                if (mMapLongClickListener != null) {
                    mMapLongClickListener.onMapLongClick((DroneHelper.GaodeLatLngToCoord(point)));
                }
            }
        });

        mMap.setOnMarkerDragListener(new AMap.OnMarkerDragListener() {
            public void onMarkerDrag(Marker marker) {
                if (mMarkerDragListener != null) {
                    final MarkerInfo markerInfo = mBiMarkersMap.getKey(marker);
                    markerInfo.setPosition((DroneHelper.GaodeLatLngToCoord(marker.getPosition())));
                    mMarkerDragListener.onMarkerDrag(markerInfo);
                }
            }

            public void onMarkerDragStart(Marker marker) {
                if (mMarkerDragListener != null) {
                    final MarkerInfo markerInfo = mBiMarkersMap.getKey(marker);
                    markerInfo.setPosition((DroneHelper.GaodeLatLngToCoord(marker.getPosition())));
                    mMarkerDragListener.onMarkerDragStart(markerInfo);
                }
            }

            public void onMarkerDragEnd(Marker marker) {
                if (mMarkerDragListener != null) {
                    final MarkerInfo markerInfo = mBiMarkersMap.getKey(marker);
                    markerInfo.setPosition((DroneHelper.GaodeLatLngToCoord(marker.getPosition())));
                    mMarkerDragListener.onMarkerDragEnd(markerInfo);
                }
            }

        });

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }


    @Override
    public void onStart() {
        super.onStart();
        if (mPanMode.get() == AutoPanMode.DRONE) {
            LocalBroadcastManager.getInstance(getActivity().getApplicationContext())
                    .registerReceiver(eventReceiver, eventFilter);
        }
        setUpMapIfNeeded();
    }


    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext())
                .unregisterReceiver(eventReceiver);

    }

    @Override
    public void addFlightPathPoint(LatLong coord) {
        final LatLng position = DroneHelper.CoordToGaodeLatLang(coord);

        if (maxFlightPathSize > 0) {
            if (flightPath == null) {
                PolylineOptions flightPathOptions = new PolylineOptions();
                flightPathOptions.color(FLIGHT_PATH_DEFAULT_COLOR)
                        .width(FLIGHT_PATH_DEFAULT_WIDTH).zIndex(1);
                flightPath = mMap.addPolyline(flightPathOptions);
            }

            List<LatLng> oldFlightPath = flightPath.getPoints();
            if (oldFlightPath.size() > maxFlightPathSize) {
                oldFlightPath.remove(0);
            }
            oldFlightPath.add(position);
            flightPath.setPoints(oldFlightPath);
        }
    }

    @Override
    public List<LatLong> projectPathIntoMap(List<LatLong> path) {
        List<LatLong> coords = new ArrayList<LatLong>();
        Projection projection = mMap.getProjection();

        for (LatLong point : path) {
            LatLng coord = projection.fromScreenLocation(new Point((int) point
                    .getLatitude(), (int) point.getLongitude()));
            coords.add(DroneHelper.GaodeLatLngToCoord(coord));
        }

        return coords;
    }

    private LatLngBounds getBounds(List<LatLng> pointsList) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : pointsList) {
            builder.include(point);
        }
        return builder.build();
    }

    @Override
    public void zoomToFit(List<LatLong> coords) {
        if (!coords.isEmpty()) {
            final List<LatLng> points = new ArrayList<LatLng>();
            for (LatLong coord : coords)
                points.add(DroneHelper.CoordToGaodeLatLang(coord));
            final LatLngBounds bounds = getBounds(points);
            final Activity activity = getActivity();
            if (activity == null)
                return;

            final View rootView = ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
            if (rootView == null)
                return;

            final int height = rootView.getHeight();
            final int width = rootView.getWidth();
            CameraUpdate animation = CameraUpdateFactory.newLatLngBounds(bounds, width, height, 100);
            if (height > 0 && width > 0) {
                if (mMap != null)
                    mMap.animateCamera(animation);
            }
        }
    }

    @Override
    public void clearMarkers() {
        for (Marker marker : mBiMarkersMap.valueSet()) {
            marker.remove();
        }
        mBiMarkersMap.clear();
    }

    @Override
    public void clearFlightPath() {
        if (flightPath != null) {
            List<LatLng> oldFlightPath = flightPath.getPoints();
            oldFlightPath.clear();
            flightPath.setPoints(oldFlightPath);
        }
    }

    @Override
    public LatLong getMapCenter() {
        return DroneHelper.GaodeLatLngToCoord(mMap.getCameraPosition().target);
    }

    @Override
    public float getMapZoomLevel() {
        return mMap.getMaxZoomLevel();
    }

    @Override
    public Set<MarkerInfo> getMarkerInfoList() {
        return new HashSet<MarkerInfo>(mBiMarkersMap.keySet());
    }

    @Override
    public float getMaxZoomLevel() {
        return mMap.getMaxZoomLevel();
    }

    @Override
    public float getMinZoomLevel() {
        return mMap.getMinZoomLevel();
    }

    @Override
    public DPMapProvider getProvider() {
        return DPMapProvider.高德地图;
    }

    @Override
    public VisibleMapArea getVisibleMapArea() {
        final AMap map = getMap();
        if (map == null)
            return null;

        final VisibleRegion mapRegion = map.getProjection().getVisibleRegion();
        return new VisibleMapArea(DroneHelper.GaodeLatLngToCoord(mapRegion.farLeft),
                DroneHelper.GaodeLatLngToCoord(mapRegion.nearLeft),
                DroneHelper.GaodeLatLngToCoord(mapRegion.nearRight),
                DroneHelper.GaodeLatLngToCoord(mapRegion.farRight));
    }

    @Override
    public void goToDroneLocation() {
        Drone dpApi = getDroneApi();
        if (!dpApi.isConnected())
            return;

        Gps gps = dpApi.getAttribute(AttributeType.GPS);
        if (!gps.isValid()) {
            Toast.makeText(getActivity().getApplicationContext(), R.string.drone_no_location, Toast.LENGTH_SHORT).show();
            return;

        }

        final float currentZoomLevel = mMap.getCameraPosition().zoom;
        final LatLong droneLocation = gps.getPosition();
        updateCamera(droneLocation, (int) currentZoomLevel);
    }

    @Override
    public void goToMyLocation() {
        if (latLng != null)
            updateCamera(DroneHelper.GaodeLatLngToCoord(latLng), GO_TO_MY_LOCATION_ZOOM);
    }

    @Override
    public void loadCameraPosition() {
        final SharedPreferences settings = mAppPrefs.prefs;

        CameraPosition.Builder camera = new CameraPosition.Builder();
        camera.bearing(settings.getFloat(PREF_BEA, DEFAULT_BEARING));
        camera.tilt(settings.getFloat(PREF_TILT, DEFAULT_TILT));
        camera.zoom(settings.getFloat(PREF_ZOOM, DEFAULT_ZOOM_LEVEL));
        camera.target(new LatLng(settings.getFloat(PREF_LAT, DEFAULT_LATITUDE),
                settings.getFloat(PREF_LNG, DEFAULT_LONGITUDE)));

        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(camera.build()));
    }


    @Override
    public void removeMarkers(Collection<MarkerInfo> markerInfoList) {

        if (markerInfoList == null || markerInfoList.isEmpty()) {
            return;
        }

        for (MarkerInfo markerInfo : markerInfoList) {
            Marker marker = mBiMarkersMap.getValue(markerInfo);
            if (marker != null) {
                marker.remove();
                mBiMarkersMap.removeKey(markerInfo);
            }
        }
        mMap.invalidate();


    }

    @Override
    public void saveCameraPosition() {
        final AMap aMap = getMap();
        if (aMap == null)
            return;

        CameraPosition camera = aMap.getCameraPosition();
        mAppPrefs.prefs.edit()
                .putFloat(PREF_LAT, (float) camera.target.latitude)
                .putFloat(PREF_LNG, (float) camera.target.longitude)
                .putFloat(PREF_BEA, camera.bearing)
                .putFloat(PREF_TILT, camera.tilt)
                .putFloat(PREF_ZOOM, camera.zoom).apply();
    }

    private Drone getDroneApi() {
        return dpApp.getDrone();
    }


    @Override
    public void selectAutoPanMode(AutoPanMode target) {
        final AutoPanMode currentMode = mPanMode.get();
        if (currentMode == target)
            return;
        setAutoPanMode(currentMode, target);
    }

    private void setAutoPanMode(AutoPanMode current, AutoPanMode update) {
        if (mPanMode.compareAndSet(current, update)) {
            switch (current) {
                case DRONE:
                    LocalBroadcastManager.getInstance(getActivity().getApplicationContext())
                            .unregisterReceiver(eventReceiver);
                    break;

                case USER:

                    break;

                case DISABLED:
                default:
                    break;
            }

            switch (update) {
                case DRONE:
                    LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver
                            (eventReceiver, eventFilter);
                    break;

                case USER:

                    break;

                case DISABLED:
                default:
                    break;
            }
        }

    }

    @Override
    public void setMapPadding(int left, int top, int right, int bottom) {
//         mMap.setPadding(left, top, right, bottom);
    }

    @Override
    public void setOnMapClickListener(OnMapClickListener listener) {

        mMapClickListener = listener;
    }

    @Override
    public void setOnMapLongClickListener(OnMapLongClickListener listener) {
        mMapLongClickListener = listener;
    }

    @Override
    public void setOnMarkerClickListener(OnMarkerClickListener listener) {
        mMarkerClickListener = listener;
    }

    @Override
    public void setOnMarkerDragListener(OnMarkerDragListener listener) {
        mMarkerDragListener = listener;
    }

    private void updateCamera(final LatLong coord) {
        if (coord != null) {
            final float zoomLevel = mMap.getCameraPosition().zoom;
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(DroneHelper.CoordToGaodeLatLang(coord),
                    zoomLevel));
        }
    }

    @Override
    public void updateCamera(final LatLong coord, final float zoomLevel) {
        if (coord != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(DroneHelper.CoordToGaodeLatLang(coord), zoomLevel));
        }
    }

    @Override
    public void updateCameraBearing(float bearing) {
        final CameraPosition cameraPosition = new CameraPosition(DroneHelper.CoordToGaodeLatLang(getMapCenter()), getMapZoomLevel(), 0, bearing);
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }

    @Override
    public void updateDroneLeashPath(PathSource pathSource) {
        List<LatLong> pathCoords = pathSource.getPathPoints();
        final List<LatLng> pathPoints = new ArrayList<LatLng>(pathCoords.size());
        for (LatLong coord : pathCoords) {
            pathPoints.add(DroneHelper.CoordToGaodeLatLang(coord));
        }

        if (mDroneLeashPath == null) {
            PolylineOptions flightPath = new PolylineOptions();
            flightPath.color(DRONE_LEASH_DEFAULT_COLOR).width(
                    DroneHelper.scaleDpToPixels(DRONE_LEASH_DEFAULT_WIDTH,
                            getResources()));
            mDroneLeashPath = mMap.addPolyline(flightPath);
        }

        mDroneLeashPath.setPoints(pathPoints);
    }

    @Override
    public void updateMarker(MarkerInfo markerInfo) {
        updateMarker(markerInfo, markerInfo.isDraggable());
    }

    @Override
    public void updateMarker(MarkerInfo markerInfo, boolean isDraggable) {
        // if the drone hasn't received a gps signal yet
        final LatLong coord = markerInfo.getPosition();
        if (coord == null) {
            return;
        }

        final LatLng position = DroneHelper.CoordToGaodeLatLang(coord);
        Marker marker = mBiMarkersMap.getValue(markerInfo);
        if (marker == null) {
            // Generate the marker
            generateMarker(markerInfo, position, isDraggable);
        } else {
            // Update the marker
            updateMarker(marker, markerInfo, position, isDraggable);
        }
    }

    private void generateMarker(MarkerInfo markerInfo, LatLng position, boolean isDraggable) {
        Log.v(TAG, "generateMarker");
        final MarkerOptions markerOptions = new MarkerOptions()
                .position(position)
                .draggable(isDraggable)
                .anchor(markerInfo.getAnchorU(), markerInfo.getAnchorV())
                .snippet(markerInfo.getSnippet()).title(markerInfo.getTitle());

        final Bitmap markerIcon = markerInfo.getIcon(getResources());
        if (markerIcon != null) {
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(markerIcon));
        }

        Marker marker = mMap.addMarker(markerOptions);
        mBiMarkersMap.put(markerInfo, marker);
    }

    private void updateMarker(Marker marker, MarkerInfo markerInfo, LatLng position,
                              boolean isDraggable) {
        final Bitmap markerIcon = markerInfo.getIcon(getResources());
        if (markerIcon != null) {
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(markerIcon));
        }


        marker.setAnchor(markerInfo.getAnchorU(), markerInfo.getAnchorV());
        marker.setPosition(position);
        marker.setRotateAngle(markerInfo.getRotation());
        marker.setSnippet(markerInfo.getSnippet());
        marker.setTitle(markerInfo.getTitle());
        marker.setDraggable(isDraggable);
        marker.setVisible(markerInfo.isVisible());
    }


    @Override
    public void updateMarkers(List<MarkerInfo> markersInfos) {
        for (MarkerInfo info : markersInfos) {
            updateMarker(info);
        }
    }

    @Override
    public void updateMarkers(List<MarkerInfo> markersInfos, boolean isDraggable) {
        for (MarkerInfo info : markersInfos) {
            updateMarker(info, isDraggable);
        }
    }

    @Override
    public void updateMissionPath(PathSource pathSource) {
        List<LatLong> pathCoords = pathSource.getPathPoints();
        final List<LatLng> pathPoints = new ArrayList<LatLng>(pathCoords.size());
        for (LatLong coord : pathCoords) {
            pathPoints.add(DroneHelper.CoordToGaodeLatLang(coord));
        }

        if (missionPath == null) {
            PolylineOptions pathOptions = new PolylineOptions();
            pathOptions.color(MISSION_PATH_DEFAULT_COLOR).width(
                    MISSION_PATH_DEFAULT_WIDTH);
            missionPath = mMap.addPolyline(pathOptions);
        }

        missionPath.setPoints(pathPoints);
    }

    @Override
    public void updateRealTimeFootprint(FootPrint footprint) {
        if (footprintPoly == null) {
            PolygonOptions pathOptions = new PolygonOptions();
            pathOptions.strokeColor(FOOTPRINT_DEFAULT_COLOR).strokeWidth(FOOTPRINT_DEFAULT_WIDTH);
            pathOptions.fillColor(FOOTPRINT_FILL_COLOR);

            for (LatLong vertex : footprint.getVertexInGlobalFrame()) {
                pathOptions.add(DroneHelper.CoordToGaodeLatLang(vertex));
            }
            footprintPoly = mMap.addPolygon(pathOptions);
        } else {
            List<LatLng> list = new ArrayList<LatLng>();
            for (LatLong vertex : footprint.getVertexInGlobalFrame()) {
                list.add(DroneHelper.CoordToGaodeLatLang(vertex));
            }
            footprintPoly.setPoints(list);
        }
    }

    @Override
    public void skipMarkerClickEvents(boolean skip) {
        useMarkerClickAsMapClick = skip;
    }

    @Override
    public void zoomToFitMyLocation(final List<LatLong> coords) {


    }

    @Override
    public void updatePolygonsPaths(List<List<LatLong>> paths) {
        for (Polygon poly : polygonsPaths) {
            poly.remove();
        }

        for (List<LatLong> contour : paths) {
            PolygonOptions pathOptions = new PolygonOptions();
            pathOptions.strokeColor(POLYGONS_PATH_DEFAULT_COLOR).strokeWidth(
                    POLYGONS_PATH_DEFAULT_WIDTH);
            final List<LatLng> pathPoints = new ArrayList<LatLng>(contour.size());
            for (LatLong coord : contour) {
                pathPoints.add(DroneHelper.CoordToGaodeLatLang(coord));
            }
            pathOptions.addAll(pathPoints);
            polygonsPaths.add(mMap.addPolygon(pathOptions));
        }
    }

    @Override
    public void setLocationListener(android.location.LocationListener receiver) {
        mLocationListener = receiver;
        //Update the listener with the last received location
        if (mLocationListener != null && isResumed()) {

        }
    }

    @Override
    public void addCameraFootprint(FootPrint footprintToBeDraw) {
        PolygonOptions pathOptions = new PolygonOptions();
        pathOptions.strokeColor(FOOTPRINT_DEFAULT_COLOR).strokeWidth(FOOTPRINT_DEFAULT_WIDTH);
        pathOptions.fillColor(FOOTPRINT_FILL_COLOR);

        for (LatLong vertex : footprintToBeDraw.getVertexInGlobalFrame()) {
            pathOptions.add(DroneHelper.CoordToGaodeLatLang(vertex));
        }
        mMap.addPolygon(pathOptions);


    }

    @Override
    public void onLocationChanged(AMapLocation aLocation) {
        if (mListener != null && aLocation != null) {
            if (aLocation.getErrorCode() == 0) {
                latLng = new LatLng(aLocation.getLatitude(), aLocation.getLongitude());
                mListener.onLocationChanged(aLocation);//
            }
            LatLong latlong = DroneHelper.GaodeLatLngToCoord(latLng);
            if (mPanMode.get() == AutoPanMode.USER) {
                updateCamera(latlong, (float) (getMap().getMaxZoomLevel() - 0.4));
            }
        }
    }

    /**
     * 激活定位
     */
    @Override
    public void activate(OnLocationChangedListener listener) {
        Log.v("123", "active");
        mListener = listener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(getContext());
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();
        }
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }


}
