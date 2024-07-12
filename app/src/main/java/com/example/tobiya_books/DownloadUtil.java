package com.example.tobiya_books;

import android.content.Context;
import android.os.AsyncTask;
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
        void onProgressUpdate(int progress);
    }

    public static void downloadPdf(final Context context, final String pdfUrl, final String fileName, final DownloadCallback callback) {
        AsyncTask<Void, Integer, String> task = new AsyncTask<Void, Integer, String>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Log.d(TAG, "Starting download...");
            }

            @Override
            protected String doInBackground(Void... voids) {
                HttpURLConnection connection = null;
                InputStream inputStream = null;
                FileOutputStream outputStream = null;
                int totalSize;

                try {
                    URL url = new URL(pdfUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        throw new IOException("HTTP error code: " + connection.getResponseCode());
                    }

                    // Determine the total file size
                    totalSize = connection.getContentLength();

                    inputStream = connection.getInputStream();
                    File file = new File(context.getFilesDir(), fileName);
                    outputStream = new FileOutputStream(file);

                    byte[] buffer = new byte[1024];
                    int len;
                    int downloadedSize = 0;
                    while ((len = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, len);
                        downloadedSize += len;
                        // Calculate progress percentage
                        int progress = (int) ((downloadedSize / (float) totalSize) * 100);
                        publishProgress(progress);
                    }

                    Log.d(TAG, "PDF downloaded successfully.");

                    return file.getAbsolutePath();
                } catch (Exception e) {
                    Log.e(TAG, "Error downloading PDF: ", e);
                    return null;
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

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                callback.onProgressUpdate(values[0]);
            }

            @Override
            protected void onPostExecute(String filePath) {
                super.onPostExecute(filePath);
                if (filePath != null) {
                    callback.onDownloadComplete(filePath);
                } else {
                    callback.onDownloadError(new Exception("Download failed"));
                }
            }
        };

        task.execute();
    }

    public static boolean isBookDownloaded(Context context, String fileName) {
        File file = new File(context.getFilesDir(), fileName);
        return file.exists();
    }
}
