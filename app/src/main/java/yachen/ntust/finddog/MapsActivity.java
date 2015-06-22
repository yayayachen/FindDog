package yachen.ntust.finddog;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class MapsActivity extends FragmentActivity {

    private Context context;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private String address, dn, dft, dbr, un, up, ld, rw, img;
    private static final String TAG_PID = "ID";
    private Button btnSMS;
    private static final String TAG_NAME = "DogName";
    private static final String TAG_DOGBREED = "DogBreed";
    private static final String TAG_USERNAME = "UserName";
    private static final String TAG_USERPHONE = "UserPhone";
    private static final String TAG_ADDRESS = "Address";
    private static final String TAG_DOGFT = "DogFT";
    private static final String TAG_REWARD = "Reward";
    private static final String TAG_LOSTDATE = "LostDate";
    private static final String TAG_DOGIMG = "DogImg";
    //    private ImageView dogImg;
    private Bitmap bitmap;
    private Marker markerShowingInfoWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        context = this;
        btnSMS = (Button) findViewById(R.id.btnSMS);

        Toast.makeText(context, "點擊地圖上紅色標記可顯示走失寵物資訊", Toast.LENGTH_LONG).show();


        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String msg = bundle.getString(TAG_ADDRESS);
            dn = bundle.getString(TAG_NAME);
            dft = bundle.getString(TAG_DOGFT);
            dbr = bundle.getString(TAG_DOGBREED);
            un = bundle.getString(TAG_USERNAME);
            up = bundle.getString(TAG_USERPHONE);
            ld = bundle.getString(TAG_LOSTDATE);
            rw = bundle.getString(TAG_REWARD);
            img = bundle.getString(TAG_DOGIMG);

            if ("".equals(rw) || "0".equals(rw)) {
                rw = "無提供賞金";
            }
            if (msg != null) {
                address = msg;
                locationNameToMarker(address);
            }
        }

        btnSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent it = new Intent(Intent.ACTION_VIEW);
                it.putExtra("address", up);
                it.putExtra("sms_body", un + "您好：\n" + "我有看到您走失的" + dn + "，" + "\n若看到此封訊息請與我聯繫，\n謝謝。");
                it.setType("vnd.android-dir/mms-sms");
                startActivity(it);
            }
        });


    }

    class mapInfoWindow implements GoogleMap.InfoWindowAdapter {
        @Override
        public View getInfoWindow(Marker marker) {

            markerShowingInfoWindow = marker;

            View infoWindow = getLayoutInflater().inflate(R.layout.map_window, null);

            ImageView dogImg = (ImageView) infoWindow.findViewById(R.id.wimgDog);
            TextView dogName = (TextView) infoWindow.findViewById(R.id.wDogName);
            TextView dogFT = (TextView) infoWindow.findViewById(R.id.wDogFT);
            TextView dogBreed = (TextView) infoWindow.findViewById(R.id.wDogBreed);
            TextView userName = (TextView) infoWindow.findViewById(R.id.wUserName);
            TextView userPhone = (TextView) infoWindow.findViewById(R.id.wUserPhone);
            TextView lostDate = (TextView) infoWindow.findViewById(R.id.wLostDate);
            TextView txtaddress = (TextView) infoWindow.findViewById(R.id.wAddress);
            TextView reward = (TextView) infoWindow.findViewById(R.id.wReward);


            /*
            * 抓取MySQL中的Img檔名(99.jpg)，然後使用HTTP url(http://140.118.37.220/dogimg/)
            * 再利用url+SQL中的檔名去讀取網路圖片
            * */
            //Picasso.with(context).load(img).into(dogImg);
            if (img != null) {
                Picasso.with(context)
                        .load(img)
                        .placeholder(R.mipmap.tra)
                        .into(dogImg, new MarkerCallback(marker));
            }
            dogName.setText(dn);
            dogFT.setText(dft);
            dogBreed.setText(dbr);
            userName.setText(un);
            userPhone.setText(up);
            lostDate.setText(ld);
            txtaddress.setText("走失地點：\n" + address);
            reward.setText(rw);
//            Toast.makeText(context, img, Toast.LENGTH_SHORT).show();
            return infoWindow;
        }

        @Override
        public View getInfoContents(Marker marker) {

            return null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("走失寵物資訊"));
        mMap.setInfoWindowAdapter(new mapInfoWindow());


    }

    public class MarkerCallback implements Callback {
        Marker marker = null;

        MarkerCallback(Marker marker) {
            this.marker = marker;
        }

        @Override
        public void onError() {
            Log.e(getClass().getSimpleName(), "Error loading thumbnail!");
        }

        @Override
        public void onSuccess() {
            if (marker != null && marker.isInfoWindowShown()) {
                marker.hideInfoWindow();
                marker.showInfoWindow();
            }
        }
    }

    private void locationNameToMarker(String locationName) {

        mMap.clear();
        Geocoder mapsActivity = new Geocoder(MapsActivity.this);
        List<Address> addressList = null;
        int maxResults = 1;
        try {

            addressList = mapsActivity
                    .getFromLocationName(locationName, maxResults);
        } catch (IOException e) {

        }

        if (addressList == null || addressList.isEmpty()) {
            Toast.makeText(context, "無地址資料", Toast.LENGTH_SHORT).show();

        } else {

            Address address = addressList.get(0);

            LatLng position = new LatLng(address.getLatitude(),
                    address.getLongitude());


            String snippet = address.getAddressLine(0);

            mMap.addMarker(new MarkerOptions().position(position)
                    .title(locationName).snippet(snippet));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(position).zoom(19).build();
            mMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));
        }
    }


}
