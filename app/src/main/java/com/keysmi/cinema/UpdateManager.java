package com.keysmi.cinema;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateManager {

    public static void checkForUpdates(final Activity activity, final String baseUrl) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(baseUrl + "/api/app/version");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    conn.setRequestMethod("GET");

                    if (conn.getResponseCode() == 200) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }
                        reader.close();

                        JSONObject json = new JSONObject(sb.toString());
                        final int remoteVersionCode = json.optInt("versionCode", 0);
                        final String remoteVersionName = json.optString("version", "2.2.0");
                        final String releaseNotes = json.optString("releaseNotes", "New performance and feature updates.");
                        final String downloadUrl = baseUrl + json.optString("downloadUrl", "/download/keysmi-cinema.apk");

                        PackageInfo pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
                        long currentVersionCode = 220;
                        try {
                            if (Build.VERSION.SDK_INT >= 28) {
                                currentVersionCode = pInfo.getLongVersionCode();
                            } else {
                                currentVersionCode = pInfo.versionCode;
                            }
                        } catch (Throwable t) {
                            currentVersionCode = pInfo.versionCode;
                        }

                        if (remoteVersionCode > currentVersionCode) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showUpdateDialog(activity, remoteVersionName, releaseNotes, downloadUrl);
                                }
                            });
                        }
                    }
                } catch (Exception ignored) {}
            }
        }).start();
    }

    private static void showUpdateDialog(final Activity activity, String version, String notes, final String downloadUrl) {
        new AlertDialog.Builder(activity)
                .setTitle("New Update Available (" + version + ")")
                .setMessage(notes + "\n\nWould you like to install the update now?")
                .setPositiveButton("Update Now", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        downloadAndInstallApk(activity, downloadUrl);
                    }
                })
                .setNegativeButton("Later", null)
                .setCancelable(true)
                .show();
    }

    private static void downloadAndInstallApk(final Activity activity, String apkUrl) {
        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apkUrl));
            request.setTitle("KeySmi Cinema Update");
            request.setDescription("Downloading latest version...");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalFilesDir(activity, Environment.DIRECTORY_DOWNLOADS, "keysmi-cinema-update.apk");

            final DownloadManager manager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
            final long downloadId = manager.enqueue(request);

            activity.registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                    if (id == downloadId) {
                        File file = new File(activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "keysmi-cinema-update.apk");
                        installApk(activity, file);
                    }
                }
            }, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        } catch (Exception e) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(apkUrl));
            activity.startActivity(browserIntent);
        }
    }

    private static void installApk(Context context, File apkFile) {
        if (!apkFile.exists()) return;
        try {
            if (Build.VERSION.SDK_INT >= 24) {
                try {
                    Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                    m.invoke(null);
                } catch (Exception ignored) {}
            }
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            context.startActivity(intent);
        } catch (Exception e) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            context.startActivity(intent);
        }
    }
}
