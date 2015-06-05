package yachen.ntust.finddog;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
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

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity {

    private Context context;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private String address, dn, dft, dbr, un, up, ld, rw;
    private static final String TAG_PID = "ID";

    private static final String TAG_NAME = "DogName";
    private static final String TAG_DOGBREED = "DogBreed";
    private static final String TAG_USERNAME = "UserName";
    private static final String TAG_USERPHONE = "UserPhone";
    private static final String TAG_ADDRESS = "Address";
    private static final String TAG_DOGFT = "DogFT";
    private static final String TAG_REWARD = "Reward";
    private static final String TAG_LOSTDATE = "LostDate";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        context = this;

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

            if ("".equals(rw)) {
                rw = "無提供賞金";
            }
            if (msg != null) {
                address = msg;
                locationNameToMarker(address);
            }
        }

        mMap.setInfoWindowAdapter(new mapInfoWindow());
    }

    class mapInfoWindow implements GoogleMap.InfoWindowAdapter {
        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
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
            *
            * */


            dogName.setText(dn);
            dogFT.setText(dft);
            dogBreed.setText(dbr);
            userName.setText(un);
            userPhone.setText(dn);
            lostDate.setText(ld);
            txtaddress.setText(address);
            reward.setText(rw);

            return infoWindow;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
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

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("走失寵物資訊"));
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
