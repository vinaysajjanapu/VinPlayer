package com.vinay.vinplayer.activities;

import android.database.Cursor;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.vinay.vinplayer.R;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class test extends AppCompatActivity {
    private File file;
    private List<String> myList;
    private ListView listView;
    private TextView pathTextView;
    private String mediapath = new String(Environment.getExternalStorageDirectory().getAbsolutePath());

    private final static String[] acceptedExtensions= {"mp3", "mp2",    "wav", "flac", "ogg", "au" , "snd", "mid", "midi", "kar"
            , "mga", "aif", "aiff", "aifc", "m3u", "oga", "spx"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test2);
        listView=(ListView) findViewById(R.id.pathlist);
        pathTextView=(TextView) findViewById(R.id.path);

        myList = new ArrayList<String>();

        String root_sd = Environment.getExternalStorageDirectory().toString();
        Log.d("Root",root_sd);

        String state = Environment.getExternalStorageState();
        File list[] = null ;
    /* if ( Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state) ) {  // we can read the External Storage...
        list=getAllFilesOfDir(Environment.getExternalStorageDirectory());
    }*/

        pathTextView.setText(root_sd);

        file = new File( root_sd ) ;
        list = file.listFiles(new AudioFilter());
        Log.d("Size of list ","" +list.length);
        //LoadDirectory(root_sd);

        for( int i=0; i< list.length; i++)
        {

            String name=list[i].getName();
            int count =     getAudioFileCount(list[i].getAbsolutePath());
            Log.d("Count : "+count, list[i].getAbsolutePath());
            if(count!=0)
                myList.add(name);
        /*int count=getAllFilesOfDir(list[i]);
        Log.e("Songs count ",""+count);

        */

        }


        listView.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, myList ));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                    long arg3) {
                File temp_file = new File( file, myList.get( position ) );

                if( !temp_file.isFile())
                {
                    //LoadDirectory(myList.get( position ));

                    file = new File( file, myList.get( position ));
                    File list[] = file.listFiles(new AudioFilter());

                    myList.clear();

                    for( int i=0; i< list.length; i++)
                    {
                        String name=list[i].getName();

                        int count =     getAudioFileCount(list[i].getAbsolutePath());
                        Log.e("Count : "+count, list[i].getAbsolutePath());
                        if(count!=0)
                            myList.add(name);
                    /*int count=getAllFilesOfDir(list[i]);
                    Log.e("Songs count ",""+count);
                    if(count!=0)
                        myList.add(name);*/
                    }

                    pathTextView.setText( file.toString());
                    //Toast.makeText(getApplicationContext(), file.toString(), Toast.LENGTH_LONG).show();
                    listView.setAdapter(new ArrayAdapter<String>(test.this, android.R.layout.simple_list_item_1, myList ));

                }


            }
        });



    }

    private int getAudioFileCount(String dirPath) {

        String selection = MediaStore.Audio.Media.DATA +" like ?";
        String[] projection = {MediaStore.Audio.Media.DATA};
        String[] selectionArgs={dirPath+"%"};
        Cursor cursor = this.managedQuery(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null);
        return cursor.getCount();
    }


    @Override
    public void onBackPressed() {
        try {
            String parent = file.getParent().toString();
            file = new File( parent ) ;
            File list[] = file.listFiles(new AudioFilter());

            myList.clear();

            for( int i=0; i< list.length; i++)
            {
                String name=list[i].getName();
                int count =     getAudioFileCount(list[i].getAbsolutePath());
                Log.e("Count : "+count, list[i].getAbsolutePath());
                if(count!=0)
                    myList.add(name);
            /*int count=getAllFilesOfDir(list[i]);
            Log.e("Songs count ",""+count);
            if(count!=0)*/

            }
            pathTextView.setText(parent);
            //  Toast.makeText(getApplicationContext(), parent,          Toast.LENGTH_LONG).show();
            listView.setAdapter(new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, myList ));
        } catch (Exception e) {
            finish();
        }


    }



    // class to limit the choices shown when browsing to SD card to media files
    public class AudioFilter implements FileFilter {

        // only want to see the following audio file types
        private String[] extension = {".aac", ".mp3", ".wav", ".ogg", ".midi", ".3gp", ".mp4", ".m4a", ".amr", ".flac"};

        @Override
        public boolean accept(File pathname) {

            // if we are looking at a directory/file that's not hidden we want to see it so return TRUE
            if ((pathname.isDirectory() || pathname.isFile()) && !pathname.isHidden()){
                return true;
            }

            // loops through and determines the extension of all files in the directory
            // returns TRUE to only show the audio files defined in the String[] extension array
            for (String ext : extension) {
                if (pathname.getName().toLowerCase().endsWith(ext)) {
                    return true;
                }
            }

            return false;
        }
    }
}
