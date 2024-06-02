package com.example.tobiya_books;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
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
                try {
                    URL url = new URL(pdfUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    InputStream inputStream = connection.getInputStream();
                    File file = new File(context.getFilesDir(), fileName); // Save to internal storage
                    FileOutputStream outputStream = new FileOutputStream(file);

                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, len);
                    }

                    outputStream.close();
                    inputStream.close();
                    connection.disconnect();

                    Log.d(TAG, "PDF downloaded successfully.");

                    // Notify that the download is complete
                    callback.onDownloadComplete(file.getAbsolutePath());
                } catch (Exception e) {
                    Log.e(TAG, "Error downloading PDF: ", e);
                    // Notify that an error occurred during download
                    callback.onDownloadError(e);
                }
            }
        }).start();
    }

    public static boolean isBookDownloaded(Context context, String fileName) {
        File file = new File(context.getFilesDir(), fileName);
        return file.exists();
    }
}
