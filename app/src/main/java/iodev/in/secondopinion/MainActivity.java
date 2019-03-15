package iodev.in.secondopinion;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.PendingIntent.getActivity;
import static android.os.Environment.getExternalStoragePublicDirectory;

public class MainActivity extends AppCompatActivity {

    Button takePicture;
    Button uploadPicture;
    TextView resulttv;
    ImageView displayImage;
    String pathToFile;
    Bitmap bitimage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        takePicture = (Button) findViewById(R.id.button);

        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        }

        displayImage = (ImageView) findViewById(R.id.imageView);
        uploadPicture = (Button) findViewById(R.id.upload_button);
        resulttv = (TextView) findViewById(R.id.textView);
        bitimage = null;

        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                getPictureFromCamera();
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, 1);
            }
        });

        uploadPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadPictureToWeb();
            }
        });

    }

    private void uploadPictureToWeb() {
        if(bitimage!=null) {
            try {
                String image64 = convertBase64(bitimage);
                String URL = "http://13.234.141.65/android";
                RequestQueue requestQueue = Volley.newRequestQueue(this);
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("Image", image64);
                final String requestBody = jsonBody.toString();

                StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("VOLLEY", response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VOLLEY", error.toString());
                    }
                }) {
                    @Override
                    public String getBodyContentType() {
                        return "application/json; charset=utf-8";
                    }

                    @Override
                    public byte[] getBody() throws AuthFailureError {
                        try {
                            return requestBody == null ? null : requestBody.getBytes("utf-8");
                        } catch (UnsupportedEncodingException uee) {
                            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                            return null;
                        }
                    }

                    @Override
                    protected Response<String> parseNetworkResponse(NetworkResponse response) {
                        String responseString = "";
                        String responseData = "";
                        if (response != null) {
                            responseString = String.valueOf(response.statusCode);
                            String parsed;
                            try {
                                parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                            } catch (UnsupportedEncodingException e) {
                                parsed = new String(response.data);
                            }
                            Log.i("Rajat",responseString);
                            Log.i("Rajat",parsed);

                            if(parsed.equals("Parasite")) {
                                resulttv.setText("Result: Infected");
                            }
                            else {
                                resulttv.setText("Result: Not infected");
                            }
                            // can get more details such as response.headers
                        }
                        return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                    }
                };

                requestQueue.add(stringRequest);
//
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else {
            Log.i("Error","No image");
        }
    }

    private String convertBase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            if(requestCode == 1) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                bitimage = bitmap;
                displayImage.setImageBitmap(bitmap);
            }
        }
    }

    private void getPictureFromCamera() {
        Intent takePic =  new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePic.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createPhotoFile();
            }
            catch (Exception e) {

            }

            if(photoFile!=null) {
                pathToFile  = photoFile.getAbsolutePath();
                Uri photoUri = FileProvider.getUriForFile(MainActivity.this,"sadas", photoFile);
                takePic.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePic,1);
            }
        }
    }

    private File createPhotoFile() throws IOException {
        String name = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            image = File.createTempFile(name, ".jpg", storageDir);
        }
        catch (Exception e) {
            Log.d("Picture", "Excep: "+e.toString());
        }
        return image;
    }
}
