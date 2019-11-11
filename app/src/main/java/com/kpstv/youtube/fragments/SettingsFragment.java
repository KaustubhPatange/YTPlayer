package com.kpstv.youtube.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.jakewharton.processphoenix.ProcessPhoenix;
import com.kpstv.youtube.MainActivity;
import com.kpstv.youtube.R;
import com.kpstv.youtube.utils.YTutils;

import java.io.File;
import java.util.ArrayList;

public class SettingsFragment extends PreferenceFragment {

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName("appSettings");
        addPreferencesFromResource(R.xml.settings);

        Preference update = findPreference("pref_update");
        update.setOnPreferenceClickListener(preference -> {
            new YTutils.CheckForUpdates(getActivity(),false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            return false;
        });

        Preference clear = findPreference("pref_delete");
        clear.setOnPreferenceClickListener(preference -> {
            if (getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        300);
                return false;
            } else removeBackups();
            return false;
        });

        Preference backup = findPreference("pref_backup");
        backup.setOnPreferenceClickListener(preference -> {
            if (getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        100);
                return false;
            } else backupData();
            return false;
        });

        Preference restore = findPreference("pref_restore");
        restore.setOnPreferenceClickListener(preference -> {
            if (getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        200);
                return false;
            } else restoreData();
            return false;
        });
    }

    void removeBackups() {
        File files = YTutils.getFile("YTPlayer/backups"); files.mkdirs();
        File[] allfiles = files.listFiles();
        if (allfiles.length>0) {
            for (File f : allfiles) f.delete();
            Toast.makeText(getActivity(), "All backups are removed!", Toast.LENGTH_LONG).show();
        } else Toast.makeText(getActivity(), "No backups were found!", Toast.LENGTH_LONG).show();
    }

    void restoreData() {
        File file = YTutils.getFile("YTPlayer/backups");
        file.mkdirs();
        File[] allfiles = file.listFiles();
        if (allfiles.length>0) {
            ArrayList<File> fileList = new ArrayList<>();
            ArrayList<String> fileName = new ArrayList<>();
            for(File f : allfiles) {
                fileList.add(f);
                fileName.add(f.getName());
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Select session");
            builder.setItems(fileName.toArray(new String[fileName.size()]),(dialog, which) -> {

                File files = getActivity().getFilesDir();
                File[] tmpFiles = files.listFiles();
                if (tmpFiles.length>0) {
                    for (File f : tmpFiles) f.delete();
                }

                File torestore = fileList.get(which);
                Log.e("FileToExtract", torestore.toString());
                YTutils.extractZip(torestore.toString(),getActivity().getFilesDir().getParent());

                Intent intent = new Intent(getActivity(),MainActivity.class);
                ProcessPhoenix.triggerRebirth(getActivity(),intent);
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else Toast.makeText(getActivity(), "No backups were found!", Toast.LENGTH_LONG).show();
    }

    void backupData() {
        String postFix = YTutils.getTodayDate_Time();
        SharedPreferences preferences = getActivity().getSharedPreferences("history",
                Context.MODE_PRIVATE);
        String urls = preferences.getString("urls","");
        if (!urls.isEmpty()) {
            YTutils.writeContent(getActivity(),"History",urls);
        }
        YTutils.getFile("YTPlayer/backups").mkdirs();
        File location = YTutils.getFile("YTPlayer/backups/backup-"+postFix+".zip");
        YTutils.zipFileAtPath(getActivity().getFilesDir().toString(),
                location.toString());
        if (location.exists())
            Toast.makeText(getActivity(), "Created local backup-"+postFix+".zip", Toast.LENGTH_LONG).show();
        else {
            Toast.makeText(getActivity(), "Failed creating a local backup", Toast.LENGTH_LONG).show();
            Log.e("FileName_To_Save",location.toString());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 100: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                   backupData();
                } else {
                    Toast.makeText(getActivity(), "Permission denied!",
                            Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case 200:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    restoreData();
                } else {
                    Toast.makeText(getActivity(), "Permission denied!",
                            Toast.LENGTH_SHORT).show();
                }
                return;
            case 300:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    removeBackups();
                } else {
                    Toast.makeText(getActivity(), "Permission denied!",
                            Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }
}
