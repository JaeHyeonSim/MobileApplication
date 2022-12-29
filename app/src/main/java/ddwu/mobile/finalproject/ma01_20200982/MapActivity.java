package ddwu.mobile.finalproject.ma01_20200982;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.ApiException;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceTypes;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import ddwu.mobile.place.placebasic.OnPlaceBasicResult;
import ddwu.mobile.place.placebasic.PlaceBasicManager;
import ddwu.mobile.place.placebasic.pojo.PlaceBasic;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MapActivity extends AppCompatActivity {

    final String TAG = "MapActivity";

    final static int REQ_PERMISSION_CODE = 100;

    FusedLocationProviderClient flpClient;
    Location mLastLocation;

    private GoogleMap mGoogleMap;
    private PlaceBasicManager placeBasicManager;
    private PlacesClient placesClient;

    private Marker marker;
    private List<Marker> basicMarkerList;
//    private Polyline mPolyline;

    private LayoutInflater inflater;
    private ConstraintLayout layout_place;
    private EditText etKeyword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        basicMarkerList = new ArrayList<Marker>();

        etKeyword = findViewById(R.id.etKeyword);
        inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        flpClient = LocationServices.getFusedLocationProviderClient(this);

        checkPermission();
        mapLoad();

        // 마지막 위치
        getLastLocation();
        if (mLastLocation != null) {
            Log.d(TAG, mLastLocation.toString());
            LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            try {
                executeGeocoding(latLng);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // 현재 위치
        flpClient.requestLocationUpdates(
                getLocationRequest(),
                mLocCallback,
                Looper.getMainLooper()
        );
        flpClient.removeLocationUpdates(mLocCallback);

        // poly line
//        PolylineOptions polylineOptions = new PolylineOptions()
//                .color(Color.RED)
//                .width(5);
//        mPolyline = mGoogleMap.addPolyline(polylineOptions);
//        mPolyline.remove();

        // Google Place API
        placeBasicManager = new PlaceBasicManager(getString(R.string.google_api_key));
        placeBasicManager.setOnPlaceBasicResult(onPlaceBasicResult);
        Places.initialize(getApplicationContext(), getString(R.string.google_api_key));
        placesClient = Places.createClient(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        flpClient.removeLocationUpdates(mLocCallback);
    }

    public void onClick(View v) throws IOException {
        switch (v.getId()) {
            case R.id.btnCurrent:
                checkPermission();
                if (marker != null) {
                    marker.remove();
                }

                flpClient.requestLocationUpdates(
                        getLocationRequest(),
                        mLocCallback,
                        Looper.getMainLooper()
                );
                Log.d(TAG, "btnCurrent");
                break;
            case R.id.btnSearch:
                for (Marker aMarker : basicMarkerList) {
                    aMarker.remove();
                }
                LatLng keywordLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                if (marker != null) {
                    keywordLatLng = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
                }
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(keywordLatLng, 16));

                if (etKeyword.getText().toString().equals("카페")) {
                    searchStart(keywordLatLng.latitude, keywordLatLng.longitude,
                            200, PlaceTypes.CAFE);
                } else if (etKeyword.getText().toString().equals("식당")) {
                    searchStart(keywordLatLng.latitude, keywordLatLng.longitude,
                            200, PlaceTypes.RESTAURANT);
                } else {
                    if (marker != null) {
                        marker.remove();
                    }
                    LatLng latLng = null;
                    try {
                        latLng = executeReverseGeocoding(etKeyword.getText().toString());
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (latLng != null) {
                        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                        MarkerOptions options = new MarkerOptions()
                                .title(etKeyword.getText().toString())
                                .position(latLng)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
                        marker = mGoogleMap.addMarker(options);
                    }
                }
                break;
        }
    }

    private void mapLoad() {
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(mapReadyCallback);
    }

    private void searchStart(double lat, double lng, int radius, String type) {
        placeBasicManager.searchPlaceBasic(lat, lng, radius, type);
    }

    OnMapReadyCallback mapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(@NonNull GoogleMap googleMap) {
            checkPermission();

            mGoogleMap = googleMap;
            mGoogleMap.setMyLocationEnabled(true);

//            지도 초기 위치 이동
            LatLng latLng = new LatLng(Double.parseDouble(getString(R.string.init_lat)), Double.parseDouble(getString(R.string.init_lng)));
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));

            mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(@NonNull LatLng latLng) {
                    flpClient.removeLocationUpdates(mLocCallback);
                    if (marker != null) {
                        marker.remove();
                    }
                    if (layout_place != null) {
                        layout_place.removeAllViews();
                    }
                    List<Address> addressList = new ArrayList<Address>();
                    try {
                        addressList = executeGeocoding(latLng);
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (addressList.size() != 0) {
                        MarkerOptions options = new MarkerOptions()
                                .title(addressList.get(0).getAddressLine(0))
                                .position(new LatLng(addressList.get(0).getLatitude(), addressList.get(0).getLongitude()))
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
                        marker = mGoogleMap.addMarker(options);
                        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
                    }
                }
            });

//            mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
//                @Override
//                public void onMapLongClick(@NonNull LatLng latLng) {
//                    try {
//                        executeGeocoding(latLng);
//                    } catch (ExecutionException e) {
//                        e.printStackTrace();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            });

            mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(@NonNull Marker marker) {
                    if (marker.getTag() != null) {
                        String placeId = marker.getTag().toString();
                        getPlaceDetail(placeId);

                    }
                }
            });

        }
    };

    LocationCallback mLocCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            for (Location loc : locationResult.getLocations()) {
                double lat = loc.getLatitude();
                double lng = loc.getLongitude();

//                지도 위치 이동
                mLastLocation = loc;
                LatLng currentLoc = new LatLng(lat, lng);
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 17));

//                지도 선을 그리기 위한 지점(위도/경도) 추가
//                List<LatLng> latLngs = mPolyline.getPoints();
//                latLngs.add(currentLoc);
//                mPolyline.setPoints(latLngs);
            }
        }
    };

    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_PERMISSION_CODE:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "위치권한 획득 완료", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "위치권한 미획득", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void checkPermission() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
        } else {
            // 권한 요청
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, REQ_PERMISSION_CODE);
        }
    }

    private void getLastLocation() {
        checkPermission();

        flpClient.getLastLocation().addOnSuccessListener(
                new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            mLastLocation = location;
                        } else {
                            Toast.makeText(MapActivity.this, "No location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        flpClient.getLastLocation().addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Unknown");
                    }
                }
        );

    }


    OnPlaceBasicResult onPlaceBasicResult = new OnPlaceBasicResult() {
        @Override
        public void onPlaceBasicResult(List<PlaceBasic> list) {
            if (list.size() == 0) {
                Toast.makeText(MapActivity.this, "주변에 " + etKeyword.getText() + "가 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
            }
            for (PlaceBasic place : list) {
                MarkerOptions options = new MarkerOptions()
                        .title(place.getName())
                        .position(new LatLng(place.getLatitude(), place.getLongitude()))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                Marker marker = mGoogleMap.addMarker(options);
                basicMarkerList.add(marker);
                /*현재 장소의 place_id 를 각각의 마커에 보관*/
                marker.setTag(place.getPlaceId());
            }
        }
    };

    private Place getPlaceDetail(String placeId) {
        List<Place.Field> placeFields       // 상세정보로 요청할 정보의 유형 지정
                = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.PHONE_NUMBER, Place.Field.ADDRESS);

        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields).build();    // 요청 생성
        // 요청 처리 및 요청 성공/실패 리스너 지정
        placesClient.fetchPlace(request).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
            @Override                    // 요청 성공 시 처리 리스너 연결
            public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {  // 요청 성공 시
                Place place = fetchPlaceResponse.getPlace();
                Log.i(TAG, "Place found: " + place.getName());  // 장소 명 확인 등
                Log.i(TAG, "Phone: " + place.getPhoneNumber());
                Log.i(TAG, "Address: " + place.getAddress());
                Log.i(TAG, "ID: " + place.getId());

                inflatePlaceLayout(place);
            }
        }).addOnFailureListener(new OnFailureListener() {   // 요청 실패 시 처리 리스너 연결
            @Override
            public void onFailure(@NonNull Exception exception) {   // 요청 실패 시
                if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    int statusCode = apiException.getStatusCode();  // 필요 시 확인
                    Log.e(TAG, "Place not found: " + exception.getMessage());
                }
            }
        });
        return null;
    }

    private void inflatePlaceLayout(Place place) {
        if (layout_place != null) {
            layout_place.removeAllViews();
        }
        layout_place = (ConstraintLayout) findViewById(R.id.layout_place);
        inflater.inflate(R.layout.custom_place_view, layout_place, true);

        TextView tvPlaceTitle = findViewById(R.id.tvPlaceTitle);
        TextView tvPlaceAddr = findViewById(R.id.tvPlaceAddr);
        TextView tvPlacePhone = findViewById(R.id.tvPlacePhone);
        ImageView ivPlaceIcon = findViewById(R.id.ivPlaceIcon);

        String phone = place.getPhoneNumber();
        if (phone == null) {
            phone = "연락처가 없습니다.";
        }
        tvPlaceTitle.setText(place.getName());
        tvPlaceAddr.setText(place.getAddress());
        tvPlacePhone.setText(place.getPhoneNumber());
        String url = place.getIconUrl();
//        if (url != null) {
//            Glide.with(MapActivity.this)
//                    .load(url)
//                    .into(ivPlaceIcon);
//        }
        if (etKeyword.getText().toString().equals("카페")) {
            ivPlaceIcon.setImageResource(R.drawable.logo_cafe);
        }

        layout_place.bringToFront();

        layout_place.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
                builder.setTitle(R.string.dialog_add_title)
                        .setMessage("위 장소로 " + getString(R.string.dialog_add_message))
                        .setPositiveButton(R.string.dialog_add_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(MapActivity.this, InsertScheduleActivity.class);
                                intent.putExtra("name", place.getName());
//                                intent.putExtra("address", place.getAddress());
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(R.string.dialog_cancel, null)
                        .setCancelable(false)
                        .show();
            }
        });
    }

    private List<Address> executeGeocoding(LatLng latLng) throws ExecutionException, InterruptedException {
        if (Geocoder.isPresent()) {
            if (latLng != null) {
                return new GeoTask().execute(latLng).get();
            }
        } else {
            Toast.makeText(this, "No Geocoder", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    class GeoTask extends AsyncTask<LatLng, Void, List<Address>> {
        Geocoder geocoder = new Geocoder(MapActivity.this, Locale.getDefault());
        @Override
        protected List<Address> doInBackground(LatLng... latLngs) {
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(latLngs[0].latitude,
                        latLngs[0].longitude, 5);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return addresses;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {
            if (addresses != null) {
                Address address = addresses.get(0);
            }
        }
    }

    private LatLng executeReverseGeocoding(String string) throws ExecutionException, InterruptedException {
        if (Geocoder.isPresent()) {
            if (string != null) {
                return new ReverseGeoTask().execute(string).get();
            }
        } else {
            Toast.makeText(this, "No Geocoder", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    class ReverseGeoTask extends AsyncTask<String, Void, LatLng> {
        Geocoder geocoder = new Geocoder(MapActivity.this, Locale.getDefault());
        @Override
        protected LatLng doInBackground(String... strings) {
            LatLng latLng = null;
            try {
                Address address = geocoder.getFromLocationName(strings[0], 5).get(0);
                latLng = new LatLng(address.getLatitude(), address.getLongitude());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return latLng;
        }

        @Override
        protected void onPostExecute(LatLng latLng) {
            return;
        }
    }




}
