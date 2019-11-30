package com.kpstv.youtube.models;

/**
 * Created by REYANSH on 4/8/2017.
 */

public class SongsModel {

    public String _ID;
    public String mSongsName;
    public String mArtistName;
    public String mDuration;
    public String mPath;
    public String mAlbum;
    public String mFileType;
    public String mAlbumId;

    public SongsModel(String _ID,
                      String songsName,
                      String artistName,
                      String duration,
                      String album,
                      String path,
                      String albumId,
                      String fileType) {
        this._ID = _ID;
        mSongsName = songsName;
        mArtistName = artistName;
        mDuration = duration;
        mPath = path;
        mAlbum = album;
        mAlbumId=albumId;
        mFileType = fileType;
    }

}
