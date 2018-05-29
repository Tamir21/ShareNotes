package com.example.tamir.sharenotes;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.tamir.sharenotes.Common.Common;
import com.example.tamir.sharenotes.Model.MyPlaces;
import com.example.tamir.sharenotes.Model.Results;
import com.example.tamir.sharenotes.Remote.IGoogleAPIService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleMap.OnInfoWindowClickListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private double latitude,longitude;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    IGoogleAPIService mService;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private Marker mMarker;
    private Button findLib,savedLibs;
    MyPlaces currentPlace;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mService = Common.getGoogleAPIService();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            checkLocationPermission();
        }

        findLib = (Button)findViewById(R.id.findLib);
        findLib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nearByLibraries();
            }
        });

        savedLibs = (Button)findViewById(R.id.btnSavedLib);
        savedLibs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retriveLibraries();
            }
        });
    }


    //Finds the local libraries, clears current map, runs the Google API link and retrieves the name,lat and long of the libraries returned
    //Then creates a collection of markers from the values and displays the markers on the map
    private void nearByLibraries() {
        mMap.clear();
        String url = getUrl(latitude,longitude);
        mService.getNearByPlaces(url)
                .enqueue(new Callback<MyPlaces>() {
                    @Override
                    public void onResponse(Call<MyPlaces> call, Response<MyPlaces> response) {
                        currentPlace = response.body();
                        if (response.isSuccessful())
                        {
                            for (int i=0;i<response.body().getResults().length;i++)
                            {
                                MarkerOptions markerOptions = new MarkerOptions();
                                Results googlePlace = response.body().getResults()[i];
                                double lat = Double.parseDouble(googlePlace.getGeometry().getLocation().getLat());
                                double lng = Double.parseDouble(googlePlace.getGeometry().getLocation().getLng());
                                String placeName = googlePlace.getName();
                                LatLng latLng = new LatLng(lat,lng);
                                markerOptions.position(latLng);
                                markerOptions.title(placeName);
                                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                                markerOptions.snippet(String.valueOf(i));
                                mMap.addMarker(markerOptions);
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<MyPlaces> call, Throwable t) {

                    }
                });
    }

    //Creates the URL for nearby places API
    private String getUrl(double latitude, double longitude) {
        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location="+latitude+","+longitude);
        googlePlacesUrl.append("&radius="+10000);
        googlePlacesUrl.append("&type=library");
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=AIzaSyADFinplcZe8HHHzmZMdYnd7U05SbUul0s");
        Log.d("STATE","THE LINK TO TEST::::::::::"+googlePlacesUrl);
        return googlePlacesUrl.toString();
    }

    //Checks if location permission enabled in Manifest then requests user permisssion
    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                new AlertDialog.Builder(this).setTitle("Location").setMessage("Allow Permission?").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},MY_PERMISSIONS_REQUEST_LOCATION);
                    }
                }).create().show();
            }else{
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else{
            return false;
        }
    }

    //Specifies the Permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case MY_PERMISSIONS_REQUEST_LOCATION:
                {
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    {
                        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
                        {
                            if (mGoogleApiClient == null)
                                buildGoogleApiClient();
                            mMap.setMyLocationEnabled(true);
                        }
                    }
                    else
                        Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    //Loads the map
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
            }

        }
        else
        {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(false);
        }
        mMap.setOnInfoWindowClickListener(this);
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        //Sets the marker of the current location
        mLastLocation = location;
        if (mMarker != null)
            mMarker.remove();

        latitude = location.getLatitude();
        longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude,longitude);
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Your Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        mMarker = mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        if (mGoogleApiClient != null)
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
    }

    @Override
    public void onInfoWindowClick(final Marker marker) {
        //Deletes the marker from firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("Libraries");
        if(marker.getSnippet().equals("saved"))
        {
            Query query = databaseReference.orderByChild("Name").equalTo(marker.getTitle());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                        snapshot.getRef().removeValue();
                        Toast.makeText(MapsActivity.this, ""+marker.getTitle()+" deleted from saved Libraries", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        else
        {
            //Saves the marker to firebase
            Common.currentResult = currentPlace.getResults()[Integer.parseInt(marker.getSnippet())];
            String saveName = Common.currentResult.getName();
            double saveLat = Double.parseDouble(Common.currentResult.getGeometry().getLocation().getLat());
            double saveLong = Double.parseDouble(Common.currentResult.getGeometry().getLocation().getLng());
            Log.d("STATE","THE LAT AND LOG HAS BEEN RETRIEVED ===="+saveLat+saveLong);
            LibraryInformation libraryInformation = new LibraryInformation(saveName,saveLat,saveLong);
            databaseReference.push().setValue(libraryInformation);
            Toast.makeText(this, ""+saveName+" added to saved Libraries", Toast.LENGTH_SHORT).show();
        }
    }

    //Retrieves the locations of libraries from firebase
    //Creates new marker options then displays all
    private void retriveLibraries() {
        mMap.clear();
        databaseReference = FirebaseDatabase.getInstance().getReference("Libraries");
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String sveTitle = dataSnapshot.child("Name").getValue(String.class);
                double sveLat = dataSnapshot.child("lat").getValue(Double.class);
                double sveLng = dataSnapshot.child("lng").getValue(Double.class);
                LatLng sveLocation = new LatLng(sveLat,sveLng);
                MarkerOptions savedlibMarkers = new MarkerOptions();
                savedlibMarkers.position(sveLocation);
                savedlibMarkers.title(sveTitle);
                savedlibMarkers.snippet("saved");
                mMap.addMarker(savedlibMarkers);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
