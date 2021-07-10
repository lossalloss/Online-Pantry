package com.example.project1mobile;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WorkActivity extends AppCompatActivity {
    TextView productNum;
    TextView TextProductInfo;
    TextView no_res;
    ImageButton getbtn;
    Button votebtn;
    Button voteNo;
    Button goto_local_btn;
    ListView listresponse;
    FloatingActionButton goto_postbtn;
    ArrayList<String> stringArray;
    ArrayList<String> nameArray;
    ArrayList<String> desArray;
    ArrayList<String> idArray;
    ArrayAdapter<String> adapter;
    ImageButton camerabtn;
    public static String megaSessionToken;
    AlertDialog.Builder dialogBuilder;
    AlertDialog dialog;

    @Override
    protected void onPostResume() {
        super.onPostResume();
        String code = ScannerActivity.textCode;
        productNum.setText(code);
        if(!productNum.getText().toString().equals("")){
            getbtn.performClick();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work);
        goto_local_btn = findViewById(R.id.goto_local);
        productNum = findViewById(R.id.ProductNumber);
        getbtn = findViewById(R.id.get);
        listresponse = (ListView)findViewById(R.id.list);
        goto_postbtn = findViewById(R.id.goto_post);
        stringArray = new ArrayList<String>();
        nameArray = new ArrayList<String>();
        desArray = new ArrayList<String>();
        idArray = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(WorkActivity.this, android.R.layout.simple_list_item_1, stringArray);
        listresponse.setAdapter(adapter);
        camerabtn = findViewById(R.id.cameraScanner);
        no_res = findViewById(R.id.no_results);

        camerabtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScannerActivity.class);
            startActivity(intent);
        });

        listresponse.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialogBuilder = new AlertDialog.Builder(WorkActivity.this);
                final View popup_view = getLayoutInflater().inflate(R.layout.popup_vote, null);

                TextProductInfo = popup_view.findViewById(R.id.productInfo);
                votebtn = popup_view.findViewById(R.id.vote);
                voteNo = popup_view.findViewById(R.id.voteNo);
                dialogBuilder.setView(popup_view);
                dialog = dialogBuilder.create();
                dialog.show();
                TextProductInfo.setText("Name: "+nameArray.get(position)+"\nDescription: "+desArray.get(position));
                voteNo.setOnClickListener(v -> {
                    dialog.dismiss();
                });
                votebtn.setOnClickListener(v -> {

                    String url = "https://lam21.modron.network/votes";
                    OkHttpClient client = new OkHttpClient();
                    MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                    JSONObject data = new JSONObject();

                    //body of the request
                    try {
                        data.put("token", megaSessionToken);
                        data.put("rating", 1);
                        data.put("productId", idArray.get(position));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //building header and body
                    RequestBody body = RequestBody.create(JSON, data.toString());
                    Request newReq = new Request.Builder()
                            .addHeader("authorization", "Bearer " + MainActivity.megaToken)
                            .post(body)
                            .url(url)
                            .build();
                    //sending request
                    client.newCall(newReq).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.d("HTTP3", "response failed!");
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (response.isSuccessful()) {
                                WorkActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //aggiungi elemento al database locale
                                        DatabaseHelper dataB = DatabaseActivity.dbHelper;
                                        dataB.addData(nameArray.get(position), desArray.get(position));
                                        makeToast("Server Notified,\nItem added!");
                                    }
                                });
                            } else {
                                WorkActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        DatabaseHelper dataB = DatabaseActivity.dbHelper;
                                        dataB.addData(nameArray.get(position), desArray.get(position));
                                        makeToast("Item added!");
                                    }
                                });
                            }
                        }
                    });
                    dialog.dismiss();
                });
            }
        });

        getbtn.setOnClickListener(v -> {
            getbtn.setEnabled(false);
            if(!productNum.getText().toString().equals("")) {
                stringArray.clear();
                nameArray.clear();
                idArray.clear();
                desArray.clear();
                String accessToken = MainActivity.megaToken;
                String url = "https://lam21.modron.network/products?barcode=" + productNum.getText();
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .header("authorization", "Bearer " + accessToken)
                        .url(url)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d("HTTP3", "GET failed!");
                        WorkActivity.this.runOnUiThread(() -> getbtn.setEnabled(true));
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String myResponse = response.body().string();
                            JsonObject convertedObj = new Gson().fromJson(myResponse, JsonObject.class);
                            JsonElement products = convertedObj.get("products");
                            JsonElement session = convertedObj.get("token");
                            megaSessionToken = session.getAsString();
                            JsonArray productsArray = products.getAsJsonArray();
                            for (int i = 0; i < productsArray.size(); i++) {
                                JsonObject convName = new Gson().fromJson(productsArray.get(i).toString(), JsonObject.class);
                                JsonElement eleName = convName.get("name");
                                JsonElement eleDes = convName.get("description");
                                JsonElement eleid = convName.get("id");
                                String name = eleName.getAsString();
                                String des = eleDes.getAsString();
                                String id = eleid.getAsString();
                                idArray.add(id);
                                nameArray.add(name);
                                desArray.add(des);
                                stringArray.add(i + 1 + ". " + name);
                            }

                            WorkActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                    goto_postbtn.setEnabled(true);
                                    if (stringArray.size() == 0) {
                                        no_res.setVisibility(View.VISIBLE);
                                    } else {
                                        no_res.setVisibility(View.INVISIBLE);
                                    }
                                    getbtn.setEnabled(true);
                                }
                            });
                        } else {
                            Log.d("HTTP3", " GET response not successful");
                            WorkActivity.this.runOnUiThread(() -> getbtn.setEnabled(true));
                        }
                    }
                });
            }else{
                getbtn.setEnabled(true);
            }
        });
        goto_local_btn.setOnClickListener(v -> {
            finish();
        });
        goto_postbtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, Post_Product.class);
            intent.putExtra("barcode", productNum.getText().toString());
            startActivity(intent);
        });
    }
    void makeToast(String text){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}