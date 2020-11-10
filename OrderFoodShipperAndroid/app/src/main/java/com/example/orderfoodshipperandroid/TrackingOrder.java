package com.example.orderfoodshipperandroid;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.orderfoodshipperandroid.Common.Common;
import com.example.orderfoodshipperandroid.Helper.DirectionJSONParser;
import com.example.orderfoodshipperandroid.Model.Request;
import com.example.orderfoodshipperandroid.Remote.IGeoCoodinates;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrackingOrder extends FragmentActivity implements OnMapReadyCallback {
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;
    LocationRequest locationRequest;
    Location mLastLocation;
    private GoogleMap mMap;
    Marker mCurrentMarker;
    Polyline polyline;

    IGeoCoodinates mService;
    Button btn_call, btn_shipped;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_order);
        mService = Common.getIGeoCoodinates();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btn_call = (Button) findViewById(R.id.btn_call);
        btn_shipped = (Button) findViewById(R.id.btn_shipped);
        btn_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + Common.currentRequest.getPhone()));
                if (ActivityCompat.checkSelfPermission(TrackingOrder.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                startActivity(intent);
            }
        });


        btn_shipped.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shippedOrder();
            }
        });
        buildLocationRequest();
        buildLocationCallBack();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    private void shippedOrder() {
        //delete  order in table and update of order to shipped
        FirebaseDatabase.getInstance().getReference(Common.ORDER_NEED_SHIP_TABLE)
                .child(Common.currentShipper.getPhone())
                .child(Common.currentKey)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //update status on Request table
                        Map<String, Object> update_status = new HashMap<>();
                        update_status.put("status", "03");
                        FirebaseDatabase.getInstance()
                                .getReference("Requests")
                                .child(Common.currentKey).
                                updateChildren(update_status)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //delete from Shipping Order
                                        FirebaseDatabase.getInstance().getReference(Common.SHIPPER_INFO_TABLE)
                                                .child(Common.currentKey).removeValue()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(TrackingOrder.this, "Đã Giao", Toast.LENGTH_SHORT).show();
                                                        finish();
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }

    private void buildLocationCallBack() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                mLastLocation = locationResult.getLastLocation();
                if (mCurrentMarker != null) {
                    mCurrentMarker.setPosition(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                }
                //update location onfirebase
                Common.updateShippingInformation(Common.currentKey, mLastLocation);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(16.0f));
    /*Because in Client App have 2 option
     1.ship to this addres:get location of client when as soon as Place Order
     2.ship to address:ship to address  where Client picked from App

     So in drawRoute,i will get routes base on this option:
     for this op1:i will  base on Lat and Lng from Order
     for this op2:i will base on Adress on Order
                */
                drawRoute(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), Common.currentRequest);

            }

        };

    }

    private void drawRoute(final LatLng yourLocation, Request request) {
//        clear all Polyline
        if (polyline != null) {
            polyline.remove();
        }
        if (request.getAddress() != null && !request.getAddress().isEmpty()) {
            mService.getGeoCode(request.getAddress(), "AIzaSyAUzgyy7EjWgKnEzwB1Dj9rTZOASNDlG_I1").enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        String lat = ((JSONArray) jsonObject.get("results"))
                                .getJSONObject(0)
                                .getJSONObject("geometry")
                                .getJSONObject("location")
                                .get("lat").toString();
                        String lng = ((JSONArray) jsonObject.get("results"))
                                .getJSONObject(0)
                                .getJSONObject("geometry")
                                .getJSONObject("location")
                                .get("lng").toString();

                        LatLng orderLocation = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.box);
                        bitmap = Common.scaleBitmap(bitmap, 70, 70);
                        MarkerOptions markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                                .title("Đơn Hàng  " + Common.currentRequest.getPhone())
                                .position(orderLocation);
                        mMap.addMarker(markerOptions);
                        //vẽ tuyến đường
                        mService.getDirections(yourLocation.latitude + "," + yourLocation.longitude,
                                orderLocation.latitude + "," + orderLocation.longitude, "AIzaSyAUzgyy7EjWgKnEzwB1Dj9rTZOASNDlG_I1")
                                .enqueue(new Callback<String>() {
                                    @Override
                                    public void onResponse(Call<String> call, Response<String> response) {
                                        new ParserTask().execute(response.body().toString());
                                    }

                                    @Override
                                    public void onFailure(Call<String> call, Throwable t) {

                                    }
                                });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {

                }
            });
        }
    }


    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(10f);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
    }

    @Override
    protected void onStop() {
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // Add a marker in Sydney and move the camera
                LatLng yourLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                mCurrentMarker = mMap.addMarker(new MarkerOptions().position(yourLocation).title("Vị Trí Của Bạn"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(yourLocation));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(16.0f));
            }
        });
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        ProgressDialog mDialog = new ProgressDialog(TrackingOrder.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.setMessage("Vui Lòng Đợi....");
            mDialog.show();

        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject;
            List<List<HashMap<String, String>>> route = null;
            try {
                jsonObject = new JSONObject(strings[0]);
                DirectionJSONParser parser = new DirectionJSONParser();
                route = parser.parse(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return route;

        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            mDialog.dismiss();

            ArrayList points = null;

            PolylineOptions polylineOptions = null;

            for (int i = 0; i < lists.size(); i++) {
                points = new ArrayList();
                polylineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = lists.get(i);
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);

                }
                polylineOptions.addAll(points);
                polylineOptions.width(12);
                polylineOptions.color(Color.BLUE);
                polylineOptions.geodesic(true);
            }
            mMap.addPolyline(polylineOptions);
        }
    }

}