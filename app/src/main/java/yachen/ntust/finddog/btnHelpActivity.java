package yachen.ntust.finddog;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class btnHelpActivity extends Activity {

    private Context context;
    private EditText txtDogName, txtDogFT, txtUserName, txtUserPhone, txtAddress,txtReward;
    private RadioButton sexF, sexM,rewardF,rewardT;
    private Spinner spinBreed;
    private DatePickerDialog lostDateDialog;
    // Progress Dialog
    private ProgressDialog pDialog;
    JSONParser jsonParser = new JSONParser();
    private Button btnUpload, btnLostDate;
    // url to create new product
    private static String url_create_product = "http://140.118.37.220/android_connect/create_product.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";

    private String tempSpin, strLostDate;
    private Calendar calendar;
    private int mYear, mMonth, mDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btnhelp);

        context = this;

        //findViewById
        findViewById();

        //
//        if("".equals(txtDogName.getText().toString().trim())||
//                "".equals(txtDogFT.getText().toString().trim())||
//                "".equals(txtUserName.getText().toString().trim())||
//                "".equals(txtUserPhone.getText().toString().trim())||
//                "".equals(txtAddress.getText().toString().trim())){
//            Toast.makeText(context,"輸入資料有誤",Toast.LENGTH_SHORT).show();
//
//        }

        calendar = Calendar.getInstance();
        mYear = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH);
        mDay = calendar.get(Calendar.DAY_OF_MONTH);

    }

    private void findViewById() {


        txtDogName = (EditText) findViewById(R.id.txtDogName);
        txtDogFT = (EditText) findViewById(R.id.txtDogFT);
        txtUserName = (EditText) findViewById(R.id.txtUserName);
        txtUserPhone = (EditText) findViewById(R.id.txtUserPhone);
        txtAddress = (EditText) findViewById(R.id.txtAddress);
        txtReward = (EditText) findViewById(R.id.txtReward);


        sexF = (RadioButton) findViewById(R.id.sexF);
        sexM = (RadioButton) findViewById(R.id.sexM);
        rewardF = (RadioButton) findViewById(R.id.rewardFalse);
        rewardT = (RadioButton) findViewById(R.id.rewardTrue);

        if(rewardT.isChecked()){
            txtReward.setEnabled(true);
        }

        spinBreed = (Spinner) findViewById(R.id.spinBreed);

        btnUpload = (Button) findViewById(R.id.btnUpload);
        btnLostDate = (Button) findViewById(R.id.btnLostDate);
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //creat a new data
                new CreateNewProduct().execute();

            }
        });

        btnLostDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(0);
                lostDateDialog.updateDate(mYear, mMonth, mDay);

            }
        });


    }

    /**
     * Background Async Task to Create new product
     */
    class CreateNewProduct extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(context);
            pDialog.setMessage("Creating Product..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Creating product
         */
        protected String doInBackground(String... args) {


            String strDogName = txtDogName.getText().toString().trim();
            String strDogFT = txtDogFT.getText().toString().trim();
            String strUserName = txtUserName.getText().toString().trim();
            String strUserPhone = txtUserPhone.getText().toString().trim();
            String strAddress = txtAddress.getText().toString().trim();
            String strReward = txtReward.getText().toString().trim();
            String strDogSex = "";
            String isReward="";
            String strBreed = "";
            if (sexF.isChecked()) {
                strDogSex = sexF.getText().toString();
            } else {
                strDogSex = sexM.getText().toString();
            }

            if (rewardF.isChecked()) {
                isReward = rewardF.getText().toString();
            } else {
                isReward = rewardT.getText().toString();
            }



            strBreed = spinBreed.getSelectedItem().toString();




            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("DogName", strDogName));
            params.add(new BasicNameValuePair("DogSex", strDogSex));
            params.add(new BasicNameValuePair("DogFT", strDogFT));
            params.add(new BasicNameValuePair("DogBreed", strBreed));
            params.add(new BasicNameValuePair("Address", strAddress));
            params.add(new BasicNameValuePair("Reward", strReward));
            params.add(new BasicNameValuePair("isReward", isReward));
            //
            params.add(new BasicNameValuePair("UserName", strUserName));
            params.add(new BasicNameValuePair("UserPhone", strUserPhone));
            params.add(new BasicNameValuePair("LostDate", strLostDate));
            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_create_product,
                    "POST", params);

            // check log cat fro response
            Log.d("Create Response", json.toString());

            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // successfully created product
                    Intent i = new Intent(getApplicationContext(), ResultActivity.class);
                    startActivity(i);

                    // closing this screen
                    finish();
                } else {
                    // failed to create product
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
            // dismiss the dialog once done
            pDialog.dismiss();
        }

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        lostDateDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month,
                                  int day) {
                mYear = year;
                mMonth = month;
                mDay = day;
                strLostDate = setDateFormat(year, month, day);
                btnLostDate.setText(strLostDate);
            }

        }, mYear, mMonth, mDay);

        return lostDateDialog;
    }


    private String setDateFormat(int year, int monthOfYear, int dayOfMonth) {
        return String.valueOf(year) + "-"
                + String.valueOf(monthOfYear + 1) + "-"
                + String.valueOf(dayOfMonth);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_btn_help, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
