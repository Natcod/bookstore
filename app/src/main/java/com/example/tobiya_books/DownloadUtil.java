package com.example.tobiya_books;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadUtil {

    private static final String TAG = "DownloadUtil";

    public interface DownloadCallback {
        void onDownloadComplete(String filePath);
        void onDownloadError(Exception e);
    }

    public static void downloadPdf(final Context context, final String pdfUrl, final String fileName, final DownloadCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                InputStream inputStream = null;
                FileOutputStream outputStream = null;

                try {
                    URL url = new URL(pdfUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        throw new IOException("HTTP error code: " + connection.getResponseCode());
                    }

                    inputStream = connection.getInputStream();
                    File file = new File(context.getFilesDir(), fileName);
                    outputStream = new FileOutputStream(file);

                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, len);
                    }

                    Log.d(TAG, "PDF downloaded successfully.");

                    // Notify that the download is complete
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onDownloadComplete(file.getAbsolutePath());
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error downloading PDF: ", e);
                    // Notify that an error occurred during download
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onDownloadError(e);
                        }
                    });
                } finally {
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            Log.e(TAG, "Error closing outputStream: ", e);
                        }
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            Log.e(TAG, "Error closing inputStream: ", e);
                        }
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    public static boolean isBookDownloaded(Context context, String fileName) {
        File file = new File(context.getFilesDir(), fileName);
        return file.exists();
    }
}
