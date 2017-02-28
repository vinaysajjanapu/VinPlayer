package com.vinay.vinplayer.helpers;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.vinay.vinplayer.R;
import com.vinay.vinplayer.adapters.ArtistSongAdapter;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by vinaysajjanapu on 28/2/17.
 */

public class ArtistImageCacheService extends Service {


    WebView browser;
    byte[] Image;
    ArrayList<HashMap<String,String>> artists;
    static int counter=0;

    static String filename = "";

    static Intent intent;
    static int flags;
    static int startId;
    static int index=0;
    String LOGTAG = "Cache Service";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        artists = VinMediaLists.getInstance().getArtistsList(this);
        counter = 0;
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        this.intent = intent;
        this.flags = flags;
        this.startId = startId;

        Log.d("cache service","started");
        browser = new WebView(this);
        //browser = (WebView)findViewById(R.id.browser);
        browser.getSettings().setJavaScriptEnabled(true);
        browser.getSettings().setLoadWithOverviewMode(true);
        browser.getSettings().setUseWideViewPort(true);

        browser.getSettings().setSupportZoom(true);
        browser.getSettings().setBuiltInZoomControls(true);
        browser.getSettings().setDisplayZoomControls(false);
        browser.getSettings().setDomStorageEnabled(true);

        browser.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        browser.setScrollbarFadingEnabled(false);
        browser.setWebViewClient(new MyWebViewClient());
        browser.setWebChromeClient(new MyWebChromeClient());

        checkAvailabilityAndStart(true);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                int counter = ArtistImageCacheService.counter;
                while (true){
                    try {
                        Thread.sleep(10);
                    }catch (Exception e){

                    }
                    if (ArtistImageCacheService.counter==counter) {
                        checkAvailabilityAndStart(false);
                        Log.d("thread","detected timedout");
                    }

                }
            }
        };
        Thread thread = new Thread(runnable);
      //  thread.start();
        return super.onStartCommand(intent, flags, startId);
    }



    private String makeFilename(String artist) {
        String subdir;
        String externalRootDir = Environment.getExternalStorageDirectory().getPath();
        if (!externalRootDir.endsWith("/")) {
            externalRootDir += "/";
        }
        subdir = "VinPlayer/.thumb/.artistcache/";
        String parentdir = externalRootDir + subdir;

        File parentDirFile = new File(parentdir);
        parentDirFile.mkdirs();

        if (!parentDirFile.isDirectory()) {
//            parentdir = externalRootDir;
            Toast.makeText(this,"Unable to access Storage",Toast.LENGTH_SHORT).show();
        }
        return parentdir+artist+".jpg";
    }

    private boolean saveImage(Bitmap bitmap){
        String outPath = makeFilename(filename);
        if (outPath == null) {
            return false;
        }
        File outFile = new File(outPath);
        try {
            OutputStream fOut = new FileOutputStream(outFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();
            Log.d(LOGTAG, filename + "  Success: Image downloaded");
            EventBus.getDefault().post("artistImageDownloaded");
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }


    private void checkAvailabilityAndStart(boolean errorOccurred) {
        if(!errorOccurred) {
            ++counter;
            if (counter < artists.size()) {
                index = 0;
                String search = artists.get(counter).get("artist");
                filename = search;
                search = search.replace(" ", "-");
                String[] split = search.split(",");
                search = search.split(",")[index];
                try {
                    RandomAccessFile f = new RandomAccessFile(new File(makeFilename(filename)), "r");
                    f.close();
                    checkAvailabilityAndStart(false);
                } catch (Exception e) {
                    browser.loadUrl("https://www.google.co.in/search?q=" + search);
                }
            } else {
                stopService(new Intent(this, ArtistImageCacheService.class));
            }
        }else {
                index++;
                if (counter < artists.size()) {
                    String search = artists.get(counter).get("artist");
                    filename =  search;
                    search = search.replace(" ", "-");
                    String[] split = search.split(",");
                    if (index<3 && index<split.length) {
                        search =split[index]+"-music";
                        try {
                            RandomAccessFile f = new RandomAccessFile(new File(makeFilename(filename)), "r");
                            f.close();
                            checkAvailabilityAndStart(false);
                        } catch (Exception e) {
                            browser.loadUrl("https://www.google.co.in/search?q=" + search);
                        }
                    }else {
                        checkAvailabilityAndStart(false);
                    }
                } else {
                    stopService(new Intent(this, ArtistImageCacheService.class));
                }
        }
    }

    private boolean decodeMessage(String message) {
        String substring = message.substring(message.indexOf(",")  + 1);
        Image = Base64.decode(substring, Base64.DEFAULT);
        Bitmap decodedBitmap = BitmapFactory.decodeByteArray(Image, 0, Image.length);
        return saveImage(decodedBitmap);
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            ++counter;
            onStartCommand(intent, flags, startId);
        }

        @Override
        public void onPageFinished (WebView view, String url){
            try {
                view.loadUrl("javascript:alert(document.getElementsByClassName('iuth')[1].getElementsByTagName('img')[0].src)");
            }catch (Exception e){
                checkAvailabilityAndStart(true);
            }
        }
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return true;
        }
    }

    private class MyWebChromeClient extends WebChromeClient {
        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            Log.d("console", consoleMessage.message());
            if (consoleMessage.message().contains("Uncaught TypeError")||
                    consoleMessage.message().contains("undefined")){
                Log.e(LOGTAG, filename + "  ERROR: image not found");
                checkAvailabilityAndStart(true);
            }
            return super.onConsoleMessage(consoleMessage);
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            result.confirm();
            Log.d("image",message);
            decodeMessage(message);
            checkAvailabilityAndStart(false);
            return true;
        }
    }


}
