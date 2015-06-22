package yachen.ntust.finddog;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class btnHelpActivity extends Activity {

    private Context context;
    private EditText txtDogName, txtDogFT, txtUserName, txtUserPhone, txtAddress, txtReward;
    private RadioButton sexF, sexM, rewardF, rewardT;
    private Spinner spinBreed, spinCity;
    private DatePickerDialog lostDateDialog;
    private ImageView imgDogImg;
    private RadioGroup radioGroup;
    // Progress Dialog
    private ProgressDialog pDialog;
    JSONParser jsonParser = new JSONParser();
    private Button btnUpload, btnLostDate, btnDogImg;

    private String imagepath = null, tempfileName = "";
    // url to create new data
    private static String url_create_data = "http://140.118.37.220/android_connect/create_product.php";
    // upload photo
    private static String upLoadServerUri = "http://140.118.37.220/UploadToServer.php";
    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private String tempSpin, strLostDate;
    private Calendar calendar;
    private int mYear, mMonth, mDay;
    private int serverResponseCode = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btnhelp);

        context = this;

        //findViewById
        findViewById();

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rewardFalse:

                        txtReward.setVisibility(View.INVISIBLE);
                        //Toast.makeText(context,"F",Toast.LENGTH_SHORT).show();
                        break;


                    case R.id.rewardTrue:

                        txtReward.setVisibility(View.VISIBLE);
                        //Toast.makeText(context,"T",Toast.LENGTH_SHORT).show();
                        break;

                }
            }
        });

        //btn
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //欄位未填偵錯
                if ("".equals(txtDogName.getText().toString().trim()) ||
                        "".equals(txtDogFT.getText().toString().trim()) ||
                        "".equals(txtUserName.getText().toString().trim()) ||
                        "".equals(txtUserPhone.getText().toString().trim()) ||
                        "".equals(txtAddress.getText().toString().trim()) ||
                        "選擇照片".equals(btnDogImg.getText().toString().trim())
                        ) {
                    Toast.makeText(context, "輸入資料不得空白", Toast.LENGTH_SHORT).show();

                }
                //creat a new data
                else {
                    new CreateNewData().execute();
                }

            }
        });

        btnLostDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(0);
                lostDateDialog.updateDate(mYear, mMonth, mDay);

            }
        });

        btnDogImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), 1);
            }
        });
        //

        //
        calendar = Calendar.getInstance();
        mYear = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH);
        mDay = calendar.get(Calendar.DAY_OF_MONTH);

    }

    private void findViewById() {


        imgDogImg = (ImageView) findViewById(R.id.imgDogImg);
        //
        txtDogName = (EditText) findViewById(R.id.txtDogName);
        txtDogFT = (EditText) findViewById(R.id.txtDogFT);
        txtUserName = (EditText) findViewById(R.id.txtUserName);
        txtUserPhone = (EditText) findViewById(R.id.txtUserPhone);
        txtAddress = (EditText) findViewById(R.id.txtAddress);
        txtReward = (EditText) findViewById(R.id.txtReward);
        txtReward.setVisibility(View.INVISIBLE);
        //
        sexF = (RadioButton) findViewById(R.id.sexF);
        sexM = (RadioButton) findViewById(R.id.sexM);
        rewardF = (RadioButton) findViewById(R.id.rewardFalse);
        rewardT = (RadioButton) findViewById(R.id.rewardTrue);

//        if (rewardT.isChecked()) {
//            txtReward.setEnabled(true);
//        }
        radioGroup = (RadioGroup) findViewById(R.id.radiogroup);
        //
        spinBreed = (Spinner) findViewById(R.id.spinBreed);
        spinCity = (Spinner) findViewById(R.id.spinCity);
        //
        btnDogImg = (Button) findViewById(R.id.btnDogImg);
        btnUpload = (Button) findViewById(R.id.btnUpload);
        btnLostDate = (Button) findViewById(R.id.btnLostDate);


    }

    /**
     * Background Async Task to Create new data
     */
    class CreateNewData extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(context);
            pDialog.setMessage("資料上傳中...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        //新增資料並轉成json
        protected String doInBackground(String... args) {

            //將圖檔上傳至server
            uploadFile(imagepath);

            //抓取欄位資料
            String strDogName = txtDogName.getText().toString().trim();
            String strDogFT = txtDogFT.getText().toString().trim();
            String strUserName = txtUserName.getText().toString().trim();
            String strUserPhone = txtUserPhone.getText().toString().trim();
            String strAddress = txtAddress.getText().toString().trim();
            String strReward = txtReward.getText().toString().trim();
            String strDogSex = "";
            String isReward = "";
            String strBreed = "";
            String strCity = "";
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
            strCity = spinCity.getSelectedItem().toString();

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("DogName", strDogName));
            params.add(new BasicNameValuePair("DogSex", strDogSex));
            params.add(new BasicNameValuePair("DogFT", strDogFT));
            params.add(new BasicNameValuePair("DogBreed", strBreed));
            params.add(new BasicNameValuePair("LostCity", strCity));
            params.add(new BasicNameValuePair("Address", strCity + strAddress));
            params.add(new BasicNameValuePair("Reward", strReward));
            params.add(new BasicNameValuePair("isReward", isReward));
            //
            params.add(new BasicNameValuePair("DogImg", tempfileName));
            //
            params.add(new BasicNameValuePair("UserName", strUserName));
            params.add(new BasicNameValuePair("UserPhone", strUserPhone));
            params.add(new BasicNameValuePair("LostDate", strLostDate));
            // getting JSON Object

            //上傳
            // Note that create data url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_create_data,
                    "POST", params);

            // check log cat fro response
            Log.d("Create Response", json.toString());

            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {

                    // successfully created data
                    Intent i = new Intent(getApplicationContext(), ResultActivity.class);
                    startActivity(i);

                    // closing this screen
                    finish();
                } else {
                    // failed to create data
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

    //狗狗圖檔上傳
    public int uploadFile(String sourceFileUri) {


        String fileName = sourceFileUri;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);
        tempfileName = sourceFile.getName();

        if (!sourceFile.isFile()) {

            pDialog.dismiss();

            Log.e("uploadFile", "Source File not exist :" + imagepath);

            runOnUiThread(new Runnable() {
                public void run() {
                    btnDogImg.setText("檔案不存在");
                }
            });

            return 0;

        } else {
            try {

                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(upLoadServerUri);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + fileName + "\"" + lineEnd);

                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

                if (serverResponseCode == 200) {

                    runOnUiThread(new Runnable() {
                        public void run() {
                            String msg = "選擇照片";
                            btnDogImg.setText(msg);
                            //Toast.makeText(context, "照片上傳成功", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {

                pDialog.dismiss();
                ex.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                        // messageText.setText("MalformedURLException Exception : check script url.");
                        Toast.makeText(context, "MalformedURLException", Toast.LENGTH_SHORT).show();
                    }
                });

                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {

                pDialog.dismiss();
                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                        //messageText.setText("Got Exception : see logcat ");
                        Toast.makeText(context, "Got Exception : see logcat ", Toast.LENGTH_SHORT).show();
                    }
                });
                //Log.e("Upload file to server Exception", "Exception : "  + e.getMessage(), e);
            }
            pDialog.dismiss();
            return serverResponseCode;

        } // End else block
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    //日期dialog
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

    //日期格式
    private String setDateFormat(int year, int monthOfYear, int dayOfMonth) {
        return String.valueOf(year) + "-"
                + String.valueOf(monthOfYear + 1) + "-"
                + String.valueOf(dayOfMonth);
    }

    //選擇圖檔成功
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1 && resultCode == RESULT_OK) {

            Uri selectedImageUri = data.getData();
            imagepath = getPath(selectedImageUri);
            Bitmap bitmap = BitmapFactory.decodeFile(imagepath);
            imgDogImg.setImageBitmap(bitmap);
            btnDogImg.setText("重新選擇照片");

        }
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
