package com.vinay.vinplayer.helpers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import java.io.File;

/**
 * Created by vinaysajjanapu on 1/3/17.
 */



    public class Delete extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... params) {

            try {
                File file = new File(params[0]);
                file.delete();
               /* context.sendBroadcast(new Intent(
                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.fromFile(new File(params[0]))));
            */
                return "success";
            }catch (Exception e){
                return "failed";
            }
        }
    }

