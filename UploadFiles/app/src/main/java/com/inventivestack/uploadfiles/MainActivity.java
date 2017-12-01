package com.inventivestack.uploadfiles;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    public static final int RequestPermissionCode = 1;
    public static final int RequestSelectFileCode = 961;
    private static final String TAG = "MainActivity";

    ImageView imageview;
    String imagepath;
    Button btnSelectFile, btnUploadFile;
    ProgressBar progressBar;
    TextView percentage;
    TextView serverResponse;
    long fileSize = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        askForPermission(Manifest.permission.READ_EXTERNAL_STORAGE, RequestPermissionCode);

        imageview = (ImageView) findViewById(R.id.imageView);
        btnSelectFile = findViewById(R.id.btn_selectFile);
        btnUploadFile = findViewById(R.id.btn_uploadFile);
        progressBar = findViewById(R.id.progressBar);
        percentage = findViewById(R.id.tv_percentage);
        serverResponse = findViewById(R.id.tv_serverResponse);

        btnSelectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (askForPermission(Manifest.permission.READ_EXTERNAL_STORAGE, RequestPermissionCode)) {
                    Intent intent = new Intent();
                    intent.setType("image/*"); // intent.setType("video/*"); to select videos to upload
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), RequestSelectFileCode);
                }
            }
        });

        btnUploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new UploadFile().execute();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == RequestSelectFileCode && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            imagepath = getPath(selectedImageUri);
            Log.e("imagepath", "" + imagepath);
            BitmapFactory.Options options = new BitmapFactory.Options();
            // down sizing image as it throws OutOfMemory Exception for larger images
            // options.inSampleSize = 10;
            final Bitmap bitmap = BitmapFactory.decodeFile(imagepath, options);
            imageview.setImageBitmap(bitmap);

        }
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private boolean askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)) {

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);

            } else {

                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
            }
        } else {

            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int RC, String per[], int[] PResult) {

        switch (RC) {

            case RequestPermissionCode:

                if (PResult.length > 0 && PResult[0] == PackageManager.PERMISSION_GRANTED) {
                    findViewById(R.id.layout_mainView).setVisibility(View.VISIBLE);
                    Toast.makeText(MainActivity.this, "Permission Granted, Application can access External Storage.", Toast.LENGTH_LONG).show();
                    //readContacts();
                } else {
                    findViewById(R.id.layout_mainView).setVisibility(View.GONE);
                    findViewById(R.id.tv_permissionError).setVisibility(View.VISIBLE);
                    ((TextView) findViewById(R.id.tv_permissionError)).setText("Permission Canceled, Application cannot access External Storage.");
                    Toast.makeText(MainActivity.this, "Permission Canceled,Application cannot access External Storage.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private class UploadFile extends AsyncTask<Void, Integer, String> {
        @Override
        protected void onPreExecute() {
            // setting progress bar to zero
            progressBar.setProgress(0);
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            //Making serverResponse textView gone
            serverResponse.setVisibility(View.GONE);
            //Making percentage textView visible
            percentage.setVisibility(View.VISIBLE);
            // Making progress bar visible
            progressBar.setVisibility(View.VISIBLE);
            // updating progress bar value
            progressBar.setProgress(progress[0]);
            // updating percentage value
            percentage.setText(String.valueOf(progress[0]) + "%");
        }

        @Override
        protected String doInBackground(Void... params) {
            return uploadFile();
        }

        @SuppressWarnings("deprecation")
        private String uploadFile() {
            String responseString = null;

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://10.10.99.83:8080/uploadFile/uploadFile.php");

            try {

                MultiPartEntity entity = new MultiPartEntity(new UploadProgressListener() {
                    /**
                     * This method updated how much data size uploaded to server
                     *
                     * @param num
                     */
                    @Override
                    public void transferred(long num) {
                        publishProgress((int) ((num / (float) fileSize) * 100));
                    }
                });

                File sourceFile = new File(imagepath);

                // Adding file data to http body
                entity.addPart("image", new FileBody(sourceFile));

                // Extra parameters if you want to pass to server
                entity.addPart("website",
                        new StringBody("www.inventivestack.com"));
                entity.addPart("email", new StringBody("abc@gmail.com"));

                fileSize = entity.getContentLength();
                httppost.setEntity(entity);

                // Making server call
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity r_entity = response.getEntity();

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    // Server response
                    responseString = EntityUtils.toString(r_entity);
                } else {
                    responseString = "Error occurred! Http Status Code: "
                            + statusCode;
                }

            } catch (ClientProtocolException e) {
                responseString = e.toString();
            } catch (IOException e) {
                responseString = e.toString();
            }

            return responseString;

        }

        @Override
        protected void onPostExecute(String result) {
            Log.e(TAG, "Server Response : " + result);

            percentage.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            // updating progress bar value
            progressBar.setProgress(0);
            //Making serverResponse textView visible
            serverResponse.setVisibility(View.VISIBLE);
            // showing the server response in an textview
            serverResponse.setText(result);
            super.onPostExecute(result);
        }

    }


}
