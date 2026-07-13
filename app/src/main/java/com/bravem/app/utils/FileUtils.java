package com.bravem.app.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;

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
     */
    public static long downloadFile(Context context, String fileUrl, String fileName) {
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
