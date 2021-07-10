package com.example.project1mobile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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

public class RegisterActivity extends AppCompatActivity {
    Button registerbtn;
    Button back_btn;
    TextView name;
    TextView email;
    TextView psw;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        registerbtn = findViewById(R.id.register);
        back_btn = findViewById(R.id.back);
        name = findViewById(R.id.username);
        email = findViewById(R.id.email);
        psw = findViewById(R.id.psw);

        back_btn.setOnClickListener(v ->{
            finish();
        });

        registerbtn.setOnClickListener(v -> {

            if(!name.getText().toString().equals("") && !email.getText().toString().equals("") && !psw.getText().toString().equals("")) {
                if(psw.getText().toString().length() > 8) {
                    String url = "https://lam21.modron.network/users";
                    OkHttpClient client = new OkHttpClient();
                    MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                    JSONObject data = new JSONObject();
                    //body of the request
                    try {
                        data.put("username", name.getText());
                        data.put("email", email.getText());
                        data.put("password", psw.getText());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //building body
                    RequestBody body = RequestBody.create(JSON, data.toString());
                    Request newReq = new Request.Builder()
                            .url(url)
                            .post(body)
                            .build();

                    //sending request
                    client.newCall(newReq).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            RegisterActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(RegisterActivity.this, "Register Error!", Toast.LENGTH_SHORT).show();
                                }
                            });
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (response.isSuccessful()) {
                                RegisterActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(RegisterActivity.this, "Registered:" + name.getText(), Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                });
                            }
                        }
                    });
                }else{
                    Toast.makeText(RegisterActivity.this, "Password must be longer then 8 characters", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(RegisterActivity.this, "All fields required", Toast.LENGTH_SHORT).show();
            }
        });
    }
}