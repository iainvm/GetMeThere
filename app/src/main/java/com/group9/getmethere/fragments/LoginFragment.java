package com.group9.getmethere.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.group9.getmethere.MainActivity;
import com.group9.getmethere.R;
import com.group9.getmethere.backend.SQLiteHandler;
import com.group9.getmethere.backend.UserFunctions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class LoginFragment extends Fragment {

    private Button btnLogin;
    private Button btnRegister;
    private EditText inputEmail;
    private EditText inputPassword;
    private View rootView;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_news, container, false);

        getActivity().getActionBar().hide();
        getActivity().setContentView(R.layout.fragment_login);
        inputEmail = (EditText) rootView.findViewById(R.id.email);
        inputPassword = (EditText) rootView.findViewById(R.id.password);
        btnLogin = (Button) rootView.findViewById(R.id.button);
        btnRegister = (Button) rootView.findViewById(R.id.button2);


        return rootView;
    }

    public void onButtonClick(View v){
        Intent myIntent = new Intent(v.getContext(), RegisterFragment.class);
        startActivity(myIntent);
        getActivity().finish();

    }

    public void onButtonClickHome(View v){
        if ( ( !inputEmail.getText().toString().equals("")) && ( !inputPassword.getText().toString().equals("")) ){

            NetAsync(v);
        }
        else {

            Toast.makeText(getActivity().getApplicationContext(),
                    "Email and Password field are empty", Toast.LENGTH_SHORT).show();
        }

    }

    private class NetCheck extends AsyncTask <String, Void, Boolean> {

        private ProgressDialog nDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(getActivity().getBaseContext());
            nDialog.setTitle("Checking Network");
            nDialog.setMessage("Loading..");
            nDialog.setIndeterminate(false);
            nDialog.setCancelable(true);
            nDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... args) {

            //checks for working internet connection

            ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()) {
                try {
                    URL url = new URL("http://www.google.com");
                    HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                    urlc.setConnectTimeout(3000);
                    urlc.connect();
                    if (urlc.getResponseCode() == 200) {
                        return true;
                    }
                } catch (MalformedURLException e1) {

                    e1.printStackTrace();
                } catch (IOException e) {

                    e.printStackTrace();
                }
            }

            return false;
        }


        protected void onPostExecute(Boolean connect) {
            if (connect == true) {
                nDialog.dismiss();
                new ProcessLogin().execute();
            } else {
                nDialog.dismiss();
                Toast.makeText(getActivity().getApplicationContext(),
                        "Error connecting to the network", Toast.LENGTH_SHORT).show();

            }
        }
    }


    private class ProcessLogin extends AsyncTask<String, Void, JSONObject> {
        private ProgressDialog pDialog;
        private String email;
        private String password;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            inputEmail = (EditText) rootView.findViewById(R.id.email);
            inputPassword = (EditText) rootView.findViewById(R.id.password);
            email = inputEmail.getText().toString();
            password = inputPassword.getText().toString();
            pDialog = new ProgressDialog(getActivity().getBaseContext());
            pDialog.setTitle("Contacting Servers");
            pDialog.setMessage("Logging in ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }
        @Override
        protected JSONObject doInBackground(String... args) {
            UserFunctions userFunction = new UserFunctions();
            JSONObject json = userFunction.loginUser(email, password);

            return json;

        }
        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                if (json.getString("success") != null) {
                    String success = json.getString("success");
                    if(Integer.parseInt(success) == 1){
                        pDialog.setMessage("Loading User Space");
                        pDialog.setTitle("Getting Data");
                        SQLiteHandler db = new SQLiteHandler(getActivity().getApplicationContext());
                        JSONObject json_user = json.getJSONObject("user");


                        db.addUser(json_user.getString("email"));
                        //load home page
                        Intent home = new Intent(getActivity().getApplicationContext(), MainActivity.class);

                        pDialog.dismiss();
                        startActivity(home);

                        getActivity().finish();
                    }else{
                        pDialog.dismiss();

                        Toast.makeText(getActivity().getApplicationContext(),
                                "Incorrect username/password", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    public void NetAsync(View view){
        new NetCheck().execute();
    }

}