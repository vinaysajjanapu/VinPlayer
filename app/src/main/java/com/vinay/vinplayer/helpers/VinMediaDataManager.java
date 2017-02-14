package com.vinay.vinplayer.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.util.Log;

import com.vinay.vinplayer.R;

import org.cmc.music.common.ID3WriteException;
import org.cmc.music.metadata.IMusicMetadata;
import org.cmc.music.metadata.ImageData;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

/**
 * Created by vinaysajjanapu on 14/2/17.
 */

public class VinMediaDataManager {

    public boolean setMetaData(Context context, String sourceAudioUrl, String songTitle,
                               String album, String artist, String genre, Bitmap albumArt) {

        File src = new File(sourceAudioUrl);
        MusicMetadataSet src_set = null;
        try {
            src_set = new MyID3().read(src);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        if (src_set == null)
        {
            Log.i("NULL", "NULL");
        } else {

            MusicMetadata meta = new MusicMetadata("vinplayer_edit");

            if (songTitle!=null)meta.setSongTitle(songTitle);
            if (album!=null)meta.setAlbum(album);
            if (artist!=null)meta.setArtist(artist);
            if (genre!=null)meta.setGenre(genre);
            if (albumArt!=null){
                Vector<ImageData> fileList = new Vector<ImageData>();
                ImageData imageData = new ImageData(getBytesFromBitmap(albumArt), "", "", 3);
                fileList.add(imageData);
                meta.setPictureList(fileList);
                Log.d("metadata","albumart set");
            }

            File dst = new File(sourceAudioUrl);
            try {
                new MyID3().update(src, src_set, meta);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return false;
            } catch (ID3WriteException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

        }
        ////////////////////////////////////////////////////////refresh mediastore
        MediaScannerConnection.scanFile(
                context,
                new String[]{sourceAudioUrl},
                null,
                new MediaScannerConnection.MediaScannerConnectionClient() {
                    public void onMediaScannerConnected() {

                    }

                    public void onScanCompleted(String path, Uri uri) {
                    }
                });

        return  true;
    }

    private byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, stream);
        return stream.toByteArray();
    }



}
