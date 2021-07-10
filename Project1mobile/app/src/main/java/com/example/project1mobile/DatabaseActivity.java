package com.example.project1mobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Time;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class DatabaseActivity extends AppCompatActivity {
    //main views
    ListView localList;
    TextView searchText;
    ImageButton search;
    Button goto_webS;
    ArrayList<String> localArray;
    ArrayList<String> desArray;
    ArrayList<String> showArray;
    ArrayAdapter adapter;
    public static DatabaseHelper dbHelper;
    Spinner sortSpinner;
    ArrayList<String> spinnerList;

    //edit popup views
    AlertDialog.Builder dialogBuilder;
    AlertDialog dialog;
    TextView nameView;
    TextView newName;
    TextView newDes;
    TextView desView;
    Button delBtn;
    Button editBtn;
    Spinner typeSpinner;
    ArrayList<String> typeSpinnerList;
    ImageView iconType;

    @Override
    protected void onPostResume() {
        super.onPostResume();
        populateLocalList();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database);
        goto_webS = findViewById(R.id.goto_web);
        localList = findViewById(R.id.localList);
        searchText = findViewById(R.id.searchQuery);
        search = findViewById(R.id.searchButton);
        dbHelper = new DatabaseHelper(this);
        sortSpinner = findViewById(R.id.spinner1);
        spinnerList = new ArrayList<>();
        spinnerList.add("Filter");
        spinnerList.add("A->Z");
        spinnerList.add("Z->A");
        spinnerList.add("Time");
        spinnerList.add("Sweet");
        spinnerList.add("Salty");
        spinnerList.add("Beverage");
        spinnerList.add("Alcool");
        spinnerList.add("Pasta");
        spinnerList.add("Vegetable");
        spinnerList.add("Fruit");
        ArrayAdapter spinnerAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, spinnerList);
        sortSpinner.setAdapter(spinnerAdapter);
        populateLocalList();
        createNotificationChannel();

        PeriodicWorkRequest notification = new PeriodicWorkRequest.Builder(notificationWorker.class, 1, TimeUnit.DAYS)
                .setInitialDelay(1, TimeUnit.DAYS)
                .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork( "dailify", ExistingPeriodicWorkPolicy.REPLACE, notification);

        //dropdown menu for filters
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Cursor data;
                switch(position){
                    default:
                        data = dbHelper.getData();
                        break;
                    case 1:
                        data = dbHelper.sortASCbyName();
                        break;
                    case 2:
                        data = dbHelper.sortDESCbyName();
                        break;
                    case 3:
                        data = dbHelper.sortTime();
                        break;
                }
                showArray = new ArrayList<>();
                localArray = new ArrayList<>();
                desArray = new ArrayList<>();
                int i = 1;
                if(position > 3){
                    while (data.moveToNext()) {
                        if(data.getInt(3) == (position - 3)) {
                            showArray.add(i + ". " + data.getString(1));
                            localArray.add(data.getString(1));
                            desArray.add(data.getString(2));
                            i++;
                        }
                    }
                }else {
                    while (data.moveToNext()) {
                        showArray.add(i + ". " + data.getString(1));
                        localArray.add(data.getString(1));
                        desArray.add(data.getString(2));
                        i++;
                    }
                }
                adapter = new ArrayAdapter<String>(DatabaseActivity.this, android.R.layout.simple_list_item_1, showArray);
                localList.setAdapter(adapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        search.setOnClickListener(v ->{
            Cursor data = dbHelper.getData();
            showArray = new ArrayList<>();
            localArray = new ArrayList<>();
            desArray = new ArrayList<>();
            int i = 1;
            while  (data.moveToNext()){
                if(data.getString(1).toString().contains(searchText.getText().toString())){
                    showArray.add(i+". "+data.getString(1));
                    localArray.add(data.getString(1));
                    desArray.add(data.getString(2));
                    i++;
                }
            }
            adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, showArray);
            localList.setAdapter(adapter);
        });

        localList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialogBuilder = new AlertDialog.Builder(DatabaseActivity.this);
                final View edit_view = getLayoutInflater().inflate(R.layout.edit_database, null);
                nameView = edit_view.findViewById(R.id.itemName2);
                desView = edit_view.findViewById(R.id.desName);
                newName = edit_view.findViewById(R.id.newName);
                newDes = edit_view.findViewById(R.id.newDes);
                delBtn = edit_view.findViewById(R.id.delete);
                editBtn = edit_view.findViewById(R.id.edit);
                typeSpinner = edit_view.findViewById(R.id.spinnerType);
                iconType = edit_view.findViewById(R.id.iconType);
                typeSpinnerList = new ArrayList<>();
                typeSpinnerList.add("Category");
                typeSpinnerList.add("Sweet");
                typeSpinnerList.add("Salty");
                typeSpinnerList.add("Beverage");
                typeSpinnerList.add("Alcool");
                typeSpinnerList.add("Pasta");
                typeSpinnerList.add("Vegetable");
                typeSpinnerList.add("Fruit");
                ArrayAdapter typeSpinnerAdapter = new ArrayAdapter(edit_view.getContext(), android.R.layout.simple_spinner_dropdown_item, typeSpinnerList);
                typeSpinner.setAdapter(typeSpinnerAdapter);

                dialogBuilder.setView(edit_view);
                dialog = dialogBuilder.create();
                dialog.show();

                nameView.setText("Name: "+localArray.get(position));
                desView.setText("Description: "+desArray.get(position));

                Cursor spinnerData = dbHelper.getData();
                while(spinnerData.moveToNext()){
                    if(spinnerData.getString(1).equals(localArray.get(position))){
                        if(spinnerData.getString(3) != null) {
                            if (spinnerData.getInt(3) == 1) {
                                iconType.setImageResource(R.drawable.sweet_v);
                            }
                            if (spinnerData.getInt(3) == 2) {
                                iconType.setImageResource(R.drawable.salt_v);
                            }
                            if (spinnerData.getInt(3) == 3) {
                                iconType.setImageResource(R.drawable.drink_v);
                            }
                            if (spinnerData.getInt(3) == 4) {
                                iconType.setImageResource(R.drawable.alcool_v);
                            }
                            if (spinnerData.getInt(3) == 5) {
                                iconType.setImageResource(R.drawable.pasta_v);
                            }
                            if (spinnerData.getInt(3) == 6) {
                                iconType.setImageResource(R.drawable.vegetable_v);
                            }
                            if (spinnerData.getInt(3) == 7) {
                                iconType.setImageResource(R.drawable.fruit_v);
                            }
                        }
                    }
                }

                typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int spinnerPosition, long id) {
                        String name = localArray.get(position);
                        Cursor data = dbHelper.getItemID(name); //get the id associated with that name
                        int itemID = -1;
                        while(data.moveToNext()){
                            itemID = data.getInt(0);
                        }
                        if(itemID>-1) {
                            if(spinnerPosition>0) {
                                dbHelper.addType(spinnerPosition, itemID);
                                populateLocalList();
                                Toast.makeText(DatabaseActivity.this, "Category updated!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });




                delBtn.setOnClickListener(v -> {
                    String name = localArray.get(position);
                    Cursor data = dbHelper.getItemID(name); //get the id associated with that name

                    int itemID = -1;
                    while(data.moveToNext()){
                        itemID = data.getInt(0);
                    }

                     if(itemID > -1) {
                         dbHelper.deleteName(itemID, name);
                         dialog.dismiss();
                         populateLocalList();
                     }

                });

                editBtn.setOnClickListener(v -> {
                    String name = localArray.get(position);
                    Cursor data = dbHelper.getItemID(name); //get the id associated with that name
                    int itemID = -1;
                    while(data.moveToNext()){
                        itemID = data.getInt(0);
                    }

                    if(itemID > -1) {
                        String new_name = newName.getText().toString();
                        String new_des = newDes.getText().toString();
                        Boolean flag = false;
                        if (!new_name.equals("")) {
                            dbHelper.updateName(new_name, itemID);
                            flag = true;
                        }
                        if(!new_des.equals("")){
                            dbHelper.updateDescription(new_des, itemID);
                            flag = true;
                        }
                        if(flag == true) {
                            dialog.dismiss();
                            populateLocalList();
                        }
                    }
                });
            }
        });

        goto_webS.setOnClickListener(v -> {
            Intent intent = new Intent(this, WorkActivity.class);
            startActivity(intent);
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("channel_id", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void populateLocalList() {
        Cursor data = dbHelper.getData();
        localArray = new ArrayList<>();
        showArray = new ArrayList<>();
        desArray = new ArrayList<>();
        int i = 1;
        while  (data.moveToNext()){
            localArray.add(data.getString(1));
            if(data.getInt(3) == 0){
                showArray.add(i+". "+data.getString(1)+" Add category");
            }else{
                showArray.add(i+". "+data.getString(1));
            }
            desArray.add(data.getString(2));
            i++;
        }
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, showArray);
        localList.setAdapter(adapter);
    }
}
