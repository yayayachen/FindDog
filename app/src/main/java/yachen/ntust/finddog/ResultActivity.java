package yachen.ntust.finddog;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class ResultActivity extends ListActivity {


    private Context context;
    // Progress Dialog
    private ProgressDialog pDialog;

    private GetWebImg ImgCache = new GetWebImg(this);
    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();

    ArrayList<HashMap<String, String>> productsList;

    // url to get all products list
    private static String url_all_products = "http://140.118.37.220/android_connect/get_all_products.php";
    private static String url_img = "http://140.118.37.220/dogimg/";
    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_PRODUCTS = "lostdata";
    private static final String TAG_PID = "ID";

    private static final String TAG_NAME = "DogName";
    private static final String TAG_DOGBREED = "DogBreed";
    private static final String TAG_USERNAME = "UserName";
    private static final String TAG_USERPHONE = "UserPhone";
    private static final String TAG_ADDRESS = "Address";
    private static final String TAG_ISREWARD = "isReward";
    private static final String TAG_REWARD = "Reward";
    private static final String TAG_DOGFT = "DogFT";
    private static final String TAG_LOSTDATE = "LostDate";
    private static final String TAG_DOGIMG = "DogImg";

    private ListView lv;
    private String imgArray[] = new String[500], dnArray[] = new String[500], dbArray[] = new String[500], unArray[] = new String[500], rwArray[] = new String[500];
    // products JSONArray
    JSONArray products = null;
    private String tempAdd;
    private ImageLoader imageLoader = ImageLoader.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        context = this;
        //

        // Hashmap for ListView
        productsList = new ArrayList<HashMap<String, String>>();

        // Loading products in Background Thread
        new LoadAllProducts().execute();

        // Get listview
        lv = getListView();

        // on seleting single product
        // launching Edit Product Screen
        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // getting values from selected ListItem

//                String pid = ((TextView) view.findViewById(R.id.dogID)).getText().toString();

                //抓取資料傳送到地圖
                String add = productsList.get(position).get(TAG_ADDRESS);
                String dogbreed = productsList.get(position).get(TAG_DOGBREED);
                String dogname = productsList.get(position).get(TAG_NAME);
                String dogft = productsList.get(position).get(TAG_DOGFT);
                String username = productsList.get(position).get(TAG_USERNAME);
                String userphone = productsList.get(position).get(TAG_USERPHONE);
                String reward = productsList.get(position).get(TAG_REWARD);
                String date = productsList.get(position).get(TAG_LOSTDATE);
                String dogimg = productsList.get(position).get(TAG_DOGIMG);

                //Toast.makeText(context, productsList.get(position).get(TAG_DOGIMG), Toast.LENGTH_SHORT).show();

                // Starting new intent
                Intent in = new Intent(getApplicationContext(),
                        MapsActivity.class);

                Bundle bundle = new Bundle();
                // sending pid to next activity
                //bundle.putString(TAG_PID, pid);

                bundle.putString(TAG_ADDRESS, add);
                bundle.putString(TAG_DOGBREED, dogbreed);
                bundle.putString(TAG_NAME, dogname);
                bundle.putString(TAG_DOGFT, dogft);
                bundle.putString(TAG_USERNAME, username);
                bundle.putString(TAG_USERPHONE, userphone);
                bundle.putString(TAG_REWARD, reward);
                bundle.putString(TAG_LOSTDATE, date);
                bundle.putString(TAG_DOGIMG, dogimg);


                in.putExtras(bundle);
                //starting new activity and expecting some response back
                startActivityForResult(in, 100);

            }
        });

    }

    // Response from Edit Product Activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // if result code 100
        if (resultCode == 100) {
            // if result code 100 is received
            // means user edited/deleted product
            // reload this screen again
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }

    }

    /**
     * Background Async Task to Load all product by making HTTP Request
     */
    class LoadAllProducts extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(context);
            pDialog.setMessage("資料載入中，請稍後...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting All products from url
         */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_all_products, "GET", params);

            // Check your log cat for JSON reponse
            Log.d("All Products: ", json.toString());

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // products found
                    // Getting Array of Products
                    products = json.getJSONArray(TAG_PRODUCTS);

                    // looping through All Products
                    for (int i = 0; i < products.length(); i++) {
                        JSONObject c = products.getJSONObject(i);

                        // Storing each json item in variable
                        String id = c.getString(TAG_PID);
                        String name = c.getString(TAG_NAME);
                        String breed = c.getString(TAG_DOGBREED);
                        String uname = c.getString(TAG_USERNAME);
                        String uphone = c.getString(TAG_USERPHONE);
                        String lostadd = c.getString(TAG_ADDRESS);
                        String isreward = c.getString(TAG_ISREWARD);
                        String dft = c.getString(TAG_DOGFT);
                        String lostdate = c.getString(TAG_LOSTDATE);
                        String reward = c.getString(TAG_REWARD);
                        String img = c.getString(TAG_DOGIMG);
                        //tempImg = img;
                        //

                        //

                        tempAdd = lostadd;
                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        map.put(TAG_PID, id);
                        map.put(TAG_NAME, name);
                        map.put(TAG_DOGBREED, breed);
                        map.put(TAG_USERNAME, uname);
                        map.put(TAG_USERPHONE, uphone);
                        map.put(TAG_ADDRESS, lostadd);
                        map.put(TAG_ISREWARD, isreward);
                        map.put(TAG_DOGFT, dft);
                        map.put(TAG_LOSTDATE, lostdate);
                        map.put(TAG_REWARD, reward);
                        map.put(TAG_DOGIMG, url_img + img);


                        //傳遞給adapter
                        dnArray[i] = name;//狗名
                        dbArray[i] = breed;//狗種
                        unArray[i] = uname;//主名
                        rwArray[i] = isreward;//是否賞金
                        imgArray[i] = url_img + img;//圖片網址
                        // adding HashList to ArrayList

                        productsList.add(map);

                    }
                } else {
                    // no products found
                    // Launch Add New product Activity
                    Intent i = new Intent(getApplicationContext(),
                            btnHelpActivity.class);
                    // Closing all previous activities
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * *
         */
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
//
                    //無圖片adapter
//                    SimpleAdapter simpleAdapter = new SimpleAdapter(
//                            context, productsList,
//                            R.layout.list_item, new String[]{TAG_PID, TAG_NAME, TAG_DOGBREED, TAG_USERNAME, TAG_ISREWARD},
//                            new int[]{R.id.dogID, R.id.dogN, R.id.dogB, R.id.userN, R.id.rewardFT});
//
//
//                    ListAdapter adapter = simpleAdapter;
//                    // updating listview
//
//
//                    setListAdapter(adapter);

                    //傳遞陣列內容給adapter，送給listView列出
                    ArrayList<String[]> alldata = new ArrayList<String[]>();
                    for (int i = 0; i < productsList.size(); i++) {
                        alldata.add(createData(dnArray[i], dbArray[i], unArray[i], rwArray[i], imgArray[i]));
                    }

                    setListAdapter(new MydataAdapter(ResultActivity.this, alldata, ImgCache));

                }
            });

        }


    }

    private String[] createData(String dn, String db, String un, String rw, String imgurl) {
        String temp[] = {dn, db, un, rw, imgurl};
        return temp;
    }
}