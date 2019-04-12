package com.bespoke.davie.etb_contact_tracing_app;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    /*
     * this is the url to our webservice
     * make sure you are using the ip instead of localhost
     * it will not work if you are using localhost
     * */
    public static final String URL_SAVE_NAME = "http://192.168.1.241/SyncData/save.php";

    //database helper object
    private static DatabaseHelper db;

    //View objects
    private Button buttonSave;
    private Button buttonOpenLists;
    private Button buttonOPenPatients;
    private EditText editTextName;
    private EditText editTextName1;
    private EditText editTextName2;
    private EditText editTextName3;
    private EditText editTextPatientId;

    private static ListView listViewNames;

    //List to store all the names
    private static List<Name> names;

    //1 means data is synced and 0 means data is not synced
    public static final int NAME_SYNCED_WITH_SERVER = 1;
    public static final int NAME_NOT_SYNCED_WITH_SERVER = 0;

    //a broadcast to know weather the data is synced or not
    public static final String DATA_SAVED_BROADCAST = "net.simplifiedcoding.datasaved";

    //Broadcast receiver to know the sync status
    private BroadcastReceiver broadcastReceiver;

    //adapterobject for list view
    private static NameAdapter nameAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerReceiver(new NetworkStateChecker(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        //initializing views and objects
        db = new DatabaseHelper(this);
        names = new ArrayList<>();

        buttonSave = (Button) findViewById(R.id.buttonSave);
        buttonOpenLists = (Button) findViewById(R.id.buttonOpenLists);
        buttonOPenPatients = (Button) findViewById(R.id.buttonOPenPatients);
        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextName1 = (EditText) findViewById(R.id.editTextName1);
        editTextName2 = (EditText) findViewById(R.id.editTextName2);
        editTextName3 = (EditText) findViewById(R.id.editTextName3);
        editTextPatientId = (EditText) findViewById(R.id.editTextPatientId);
        listViewNames = (ListView) findViewById(R.id.listViewNames);


        //adding click listener to button
        buttonSave.setOnClickListener(this);
        buttonOpenLists.setOnClickListener(this);
        buttonOPenPatients.setOnClickListener(this);

        buttonOpenLists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openLists();
            }
        });

        buttonOPenPatients.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                OpenPatients();
            }
        });

        //calling the method to load all the stored names
        loadNames();

        //the broadcast receiver to update sync status
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                //loading the names again
                loadNames();
            }
        };

        //registering the broadcast receiver to update sync status
        registerReceiver(broadcastReceiver, new IntentFilter(DATA_SAVED_BROADCAST));

        //getting the patient id from ShowDataActivity class
        editTextPatientId.setText(getIntent().getStringExtra("pIdnt"));

    }



    /*
     * this method will
     * load the names from the database
     * with updated sync status
     * */


    public void loadNames() {
        names.clear();
        Cursor cursor = db.getNames();
        if (cursor.moveToFirst()) {
            do {
                Name name = new Name(
                        cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME)),
                        cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_MIDD)),
                        cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_LAST)),
                        cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_MOBILE)),
                        cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_STATUS))
                );
                names.add(name);
            } while (cursor.moveToNext());
        }

        nameAdapter = new NameAdapter(this, R.layout.names, names);
        // listViewNames.setAdapter(nameAdapter);
    }

    /*
     * this method will simply refresh the list
     * */
    private void refreshList() {
        nameAdapter.notifyDataSetChanged();
    }

    /*
     * this method is saving the name to ther server
     * */
    private void saveNameToServer() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving Name...");
        progressDialog.show();

        final String nam = editTextName.getText().toString().trim();
        final String mid = editTextName1.getText().toString().trim();
        final String las = editTextName2.getText().toString().trim();
        final String mob = editTextName3.getText().toString().trim();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_SAVE_NAME,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (!obj.getBoolean("error")) {
                                //if there is a success
                                //storing the name to sqlite with status synced
                                saveNameToLocalStorage(nam,mid,las , mob, NAME_SYNCED_WITH_SERVER);
                            } else {
                                //if there is some error
                                //saving the name to sqlite with status unsynced
                                saveNameToLocalStorage(nam,mid,las, mob, NAME_NOT_SYNCED_WITH_SERVER);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        //on error storing the name to sqlite with status unsynced
                        saveNameToLocalStorage(nam,mid,las,mob, NAME_NOT_SYNCED_WITH_SERVER);
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("given_name", nam);
                params.put("given_midd", mid);
                params.put("given_last", las);
                params.put("mobile", mob);
                return params;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    //saving the name to local storage
    private void saveNameToLocalStorage(String given_name, String given_midd, String given_last, String mobile,  int status) {
        editTextName.setText("");
        db.addName(given_name, given_midd, given_last,mobile,  status);
        Name n = new Name(given_name, given_midd, given_last , mobile ,status);
        names.add(n);
        refreshList();
    }

    @Override
    public void onClick(View view) {
        saveNameToServer();
    }

    public void openLists() {
        Intent intent = new Intent(this, SyncedNameLists.class);
        startActivity(intent);
    }

    public void OpenPatients(){
        Intent intent = new Intent(this, RetrieveMysqlData.class);
        startActivity(intent);
    }
}
