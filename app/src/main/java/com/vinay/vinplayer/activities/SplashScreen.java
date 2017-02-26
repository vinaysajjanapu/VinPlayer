package com.vinay.vinplayer.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.vinay.vinplayer.R;
import com.vinay.vinplayer.VinPlayer;
import com.vinay.vinplayer.database.NextSongDataTable;
import com.vinay.vinplayer.database.RecentAddedTable;
import com.vinay.vinplayer.database.RecommendedTable;
import com.vinay.vinplayer.fragments.FoldersFragment;
import com.vinay.vinplayer.helpers.VinMediaLists;

public class SplashScreen extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().hide();
        sharedPreferences = getSharedPreferences("constants",MODE_PRIVATE);
        editor  = sharedPreferences.edit();

        dialog = new ProgressDialog(this);

        if (isStoragePermissionGranted()) {
            initializations();
        }

        //startActivity(new Intent(this,WifiTest.class));

    }

    private void initializations(){

        new AsyncTask<String, Void, String>() {

            @Override
            protected String doInBackground(String... params) {
                VinMediaLists.getInstance().createAllSongsList(getBaseContext());
                VinMediaLists.getInstance().createAlbumsList(getBaseContext());
                VinMediaLists.getInstance().createArtistsList(getBaseContext());
                VinMediaLists.getInstance().createGenresList(getBaseContext());
                FoldersFragment.getInstance().createFolderFileStructure();
                RecommendedTable.getInstance(getBaseContext()).createRecommendationsList();

                return "Executed";
            }

            @Override
            protected void onPreExecute() {
                dialog.setTitle("Initializing Data");
                dialog.show();
            }

            @Override
            protected void onPostExecute(String s) {
                dialog.dismiss();
                if (sharedPreferences.getBoolean("firstTimeOpened", true)) {
                    editor.putBoolean("firstTimeOpened", false);
                    editor.apply();
                    firstTimeInitialisations();
                } else {
                    finish();
                    startActivity(new Intent(getBaseContext(), MainActivity.class));
                }
            }
        }.execute();
    }


    private void firstTimeInitialisations() {
        final ProgressDialog dialog1 = new ProgressDialog(this);
        new AsyncTask<String,Void,String>(){

            @Override
            protected String doInBackground(String... params) {
                NextSongDataTable.getInstance(getBaseContext()).initializeNextSongData();
                RecentAddedTable.getInstance(getBaseContext()).updateRecentAddedList();
                return "Executed";
            }

            @Override
            protected void onPreExecute() {
                dialog1.setTitle("One Time Initializations");
                dialog1.show();
            }

            @Override
            protected void onPostExecute(String s) {
                dialog1.dismiss();
                if (isStoragePermissionGranted()) {
                    finish();
                    startActivity(new Intent(getBaseContext(), MainActivity.class));
                }
            }
        }.execute();

    }


    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED && checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED ) {
                Log.v("Storage Permisson","granted");
                return true;
            } else {
                Log.v("Storage Permisson","revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 150);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v("Storage Permisson","granted");
            return true;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==150) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Log.v("Storage Permisson", "Permission: " + permissions[0] + "was " + grantResults[0]);
                //resume tasks needing this permission
                initializations();
            }else {
                Toast.makeText(this,"No Permissions",Toast.LENGTH_SHORT).show();
            }
        }
    }
}
