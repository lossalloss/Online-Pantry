package com.example.project1mobile;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ComponentActivity;

import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Post_Product extends AppCompatActivity {

    TextView name;
    TextView des;
    TextView barcode;
    Button postbtn;
    Button back_btn;
    String sessionToken = WorkActivity.megaSessionToken;
    String accessToken = MainActivity.megaToken;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post__product);

        name = findViewById(R.id.text_name);
        des = findViewById(R.id.text_description);
        barcode = findViewById(R.id.text_barcode);
        postbtn = findViewById(R.id.post);
        back_btn = findViewById(R.id.back_post);

        String code = getIntent().getStringExtra("barcode");
        barcode.setText(code);

        back_btn.setOnClickListener(v ->{
            finish();
        });

        postbtn.setOnClickListener(v -> {
            if(!name.getText().toString().equals("") && !des.getText().toString().equals("") && !barcode.getText().toString().equals("")) {
                String url = "https://lam21.modron.network/products";
                OkHttpClient client = new OkHttpClient();
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                JSONObject data = new JSONObject();

                //body of the request
                try {
                    data.put("token", sessionToken);
                    data.put("name", name.getText());
                    data.put("description", des.getText());
                    data.put("barcode", barcode.getText());
                    data.put("test", false);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //building header and body
                RequestBody body = RequestBody.create(JSON, data.toString());
                Request newReq = new Request.Builder()
                        .addHeader("authorization", "Bearer " + accessToken)
                        .post(body)
                        .url(url)
                        .build();
                //sending request
                client.newCall(newReq).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        makeToast("Error!");
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            Post_Product.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    makeToast("Item added!");
                                    DatabaseActivity.dbHelper.addData(name.getText().toString(), des.getText().toString());
                                    finish();
                                }
                            });
                        } else {
                            Log.d("HTTP3", "response not successful!");
                        }
                    }
                });
            }else{
                makeToast("All fields required");
            }
        });
    }
    void makeToast(String text){
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }
}