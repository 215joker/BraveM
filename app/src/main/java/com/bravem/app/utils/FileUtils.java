package com.bravem.app.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;
import com.bravem.app.data.NotificationRepository;
import java.util.Locale;

/**
 * Helper methods for resolving picked-file metadata (name, size) and
 * triggering downloads of remote paper files via the system DownloadManager.
 */
public final class FileUtils {

    private FileUtils() {
        // no instances
    }

    public static String getFileName(Context context, Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception ignored) {
                // fall through to default below
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result != null ? result : "file";
    }

    public static long getFileSize(Context context, Uri uri) {
        long size = 0;
        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                    size = cursor.getLong(sizeIndex);
                }
            }
        } catch (Exception ignored) {
            // size unknown
        }
        return size;
    }

    public static String detectFileType(String fileName) {
        if (fileName == null) return "unknown";
        String lower = fileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".pdf")) return "pdf";
        if (lower.endsWith(".doc") || lower.endsWith(".docx")) return "docx";
        return "unknown";
    }

    public static String humanReadableSize(long bytes) {
        if (bytes <= 0) return "—";
        final String[] units = {"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        digitGroups = Math.min(digitGroups, units.length - 1);
        return String.format(Locale.getDefault(), "%.1f %s",
                bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    /**
     * Copies a file from a Uri to internal app storage.
     */
    public static String copyFileToInternalStorage(Context context, Uri uri, String fileName) {
        java.io.File storageDir = new java.io.File(context.getFilesDir(), "papers");
        if (!storageDir.exists()) storageDir.mkdirs();

        java.io.File destFile = new java.io.File(storageDir, java.util.UUID.randomUUID().toString() + "_" + fileName);
        try (java.io.InputStream is = context.getContentResolver().openInputStream(uri);
             java.io.OutputStream os = new java.io.FileOutputStream(destFile)) {
            byte[] buffer = new byte[4096];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            return destFile.getAbsolutePath();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Starts a system DownloadManager download of a remote past paper file
     * into the device's public Downloads folder, returning the download ID.
     * If the file is local, it copies it to the public Downloads folder.
     */
    public static long downloadFile(Context context, String fileUrl, String fileName) {
        if (fileUrl == null) return -1;

        // If it's a local file path
        if (fileUrl.startsWith("/") || fileUrl.startsWith("file://")) {
            return exportLocalFile(context, fileUrl, fileName);
        }

        try {
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            Uri uri = Uri.parse(fileUrl);

            String mimeType = guessMimeType(fileName);

            DownloadManager.Request request = new DownloadManager.Request(uri)
                    .setTitle(fileName)
                    .setDescription("Downloading from BraveM")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true);

            if (mimeType != null) {
                request.setMimeType(mimeType);
            }

            return downloadManager.enqueue(request);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static long exportLocalFile(Context context, String localPath, String fileName) {
        String path = localPath.startsWith("file://") ? localPath.substring(7) : localPath;
        java.io.File source = new java.io.File(path);
        if (!source.exists()) return -1;

        // Ensure filename has extension if missing
        String finalFileName = fileName;
        if (!finalFileName.contains(".")) {
            String type = detectFileType(localPath);
            if (!"unknown".equals(type)) {
                finalFileName += "." + type;
            }
        }

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                android.content.ContentValues values = new android.content.ContentValues();
                values.put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, finalFileName);
                values.put(android.provider.MediaStore.MediaColumns.MIME_TYPE, guessMimeType(finalFileName));
                values.put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                Uri externalUri = android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI;
                Uri downloadUri = context.getContentResolver().insert(externalUri, values);

                if (downloadUri != null) {
                    try (java.io.InputStream is = new java.io.FileInputStream(source);
                         java.io.OutputStream os = context.getContentResolver().openOutputStream(downloadUri)) {
                        if (os != null) {
                            byte[] buffer = new byte[4096];
                            int length;
                            while ((length = is.read(buffer)) > 0) {
                                os.write(buffer, 0, length);
                            }
                            showDownloadNotification(context, finalFileName);
                            return 1;
                        }
                    }
                }
            } else {
                java.io.File dest = new java.io.File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), finalFileName);
                if (dest.exists()) {
                    dest = new java.io.File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                            System.currentTimeMillis() + "_" + finalFileName);
                }

                try (java.io.InputStream is = new java.io.FileInputStream(source);
                     java.io.OutputStream os = new java.io.FileOutputStream(dest)) {
                    byte[] buffer = new byte[4096];
                    int length;
                    while ((length = is.read(buffer)) > 0) {
                        os.write(buffer, 0, length);
                    }
                    android.media.MediaScannerConnection.scanFile(context, new String[]{dest.getAbsolutePath()}, null, null);
                    showDownloadNotification(context, finalFileName);
                    return 1;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private static void showDownloadNotification(Context context, String fileName) {
        String currentUid = new SessionManager(context).getUid();
        new NotificationRepository(context).addNotification(
                "Download Complete",
                "File '" + fileName + "' has been downloaded successfully.",
                "download",
                currentUid,
                null
        );

        androidx.core.app.NotificationCompat.Builder builder = new androidx.core.app.NotificationCompat.Builder(context, "downloads_channel")
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle("Download Complete")
                .setContentText(fileName)
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        androidx.core.app.NotificationManagerCompat notificationManager = androidx.core.app.NotificationManagerCompat.from(context);
        try {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private static String guessMimeType(String fileName) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(fileName);
        if (extension == null || extension.isEmpty()) {
            String lower = fileName.toLowerCase(Locale.ROOT);
            if (lower.endsWith(".pdf")) return "application/pdf";
            if (lower.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            if (lower.endsWith(".doc")) return "application/msword";
            return null;
        }
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase(Locale.ROOT));
    }
}
