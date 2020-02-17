package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMapClickListener, PermissionsListener {

    MapView mapView;
    MapboxMap mapboxMap;
    LocationComponent locationComponent;
    PermissionsManager permissionsManager;
    DirectionsRoute curentRoute;
    NavigationMapRoute navigationMapRoute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.token));
        setContentView(R.layout.activity_main);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {
        if(granted)
        {
            enableLocationComponent(mapboxMap.getStyle());
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Permission not granted",Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        mapboxMap.addMarker(new MarkerOptions().position(point));
        Log.d("ok",""+point.getLatitude());
        Point destinationPoint = Point.fromLngLat(point.getLongitude(), point.getLatitude());
        Point originPoint = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),
                locationComponent.getLastKnownLocation().getLatitude());

        GeoJsonSource source = mapboxMap.getStyle().getSourceAs("destination-source-id");

        if(source != null)
        {
            source.setGeoJson(Feature.fromGeometry(destinationPoint));
        }

        Log.d("long",""+point.getLatitude());

        getRoute(originPoint, destinationPoint);

        return  true;
    }

    private void getRoute(Point originPoint, Point destinationPoint) {
        NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken())
                .origin(originPoint)
                .destination(destinationPoint)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        if(response.body() == null)
                        {
                            return ;
                        }
                        else if(response.body().routes().size() == 0)
                        {
                            return ;
                        }

                        curentRoute = response.body().routes().get(0);
                        if(navigationMapRoute != null)
                        {
                            navigationMapRoute.removeRoute();
                        }
                        else{
                            navigationMapRoute = new NavigationMapRoute(null, mapView,mapboxMap);
                        }


                        navigationMapRoute.addRoute(curentRoute);
                        Toast.makeText(getApplicationContext(), ""+curentRoute.distance(),Toast.LENGTH_LONG).show();

                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {

                    }
                });
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        Log.d("long","hello");
        this.mapboxMap = mapboxMap;
        //this.mapboxMap.addOnMapClickListener(this);
        this.mapboxMap.setMinZoomPreference(15);
        mapboxMap.setStyle(getString(R.string.navigation_guidance_day), new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                Log.d("long","hello");
                enableLocationComponent(style);
                MainActivity.this.mapboxMap.addOnMapClickListener(MainActivity.this);
            }
        });

    }


//    private  void addDestinationIconLayer(Style style)
//    {
//        style.addImage("destination-icon-id",
//                BitmapFactory.decodeResource(this.getResources(), R.drawable.mapbox_marker_icon_default));
//
//        GeoJsonSource geoJsonSource = new GeoJsonSource("destination-source-id");
//        style.addSource(geoJsonSource);
//
//        SymbolLayer symbolLayer = new SymbolLayer("destinatin-symbol-layer-id","destination-source-id");
//        symbolLayer.withProperties(iconImage())
//    }

    public  void startNavigation(View view)
    {
        Boolean simulate_route = true;

    }

    private void enableLocationComponent(Style loadedMapStyle) {
        if(PermissionsManager.areLocationPermissionsGranted(this))
        {
            locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(this,loadedMapStyle).build());
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(CameraMode.TRACKING);
        }
        else{
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this) ;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
       permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
