package com.example.project1mobile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.project1mobile.R.layout.activity_main;
public class MainActivity extends AppCompatActivity {
    Button uploadbtn;
    Button goto_register_btn;
    TextView email;
    TextView psw;
    public static String megaToken;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_main);

        uploadbtn = findViewById(R.id.Upload);
        goto_register_btn = findViewById(R.id.goto_register);
        email = findViewById(R.id.loginEmail);
        psw = findViewById(R.id.loginPsw);

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);

        email.setText(sharedPref.getString("emailText", ""));
        psw.setText(sharedPref.getString("pswText", ""));

        goto_register_btn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        uploadbtn.setOnClickListener(v -> {
            uploadbtn.setEnabled(false);
            if(!email.getText().toString().equals("") & !psw.getText().toString().equals("")) {
                String url = "https://lam21.modron.network/auth/login";
                OkHttpClient client = new OkHttpClient();
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                JSONObject data = new JSONObject();
                //body of the request
                try {
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
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Login Error!", Toast.LENGTH_SHORT).show();
                                uploadbtn.setEnabled(true);
                            }
                        });
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String myResponse = response.body().string();
                            JsonObject convertedObj = new Gson().fromJson(myResponse, JsonObject.class);
                            JsonElement token = convertedObj.get("accessToken");
                            String stringToken = token.getAsString();
                            megaToken = stringToken;
                            SharedPreferences.Editor editorEmail = sharedPref.edit();
                            SharedPreferences.Editor editorPsw = sharedPref.edit();
                            editorEmail.putString("emailText", email.getText().toString());
                            editorPsw.putString("pswText", psw.getText().toString());
                            editorEmail.apply();
                            editorPsw.apply();
                            
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "Login: "+email.getText().toString(), Toast.LENGTH_SHORT).show();
                                }
                            });
                            Intent intent = new Intent(MainActivity.this, DatabaseActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
            }else{
                Toast.makeText(MainActivity.this, "Insert credentials", Toast.LENGTH_SHORT).show();
                uploadbtn.setEnabled(true);
            }
        });

    }
}