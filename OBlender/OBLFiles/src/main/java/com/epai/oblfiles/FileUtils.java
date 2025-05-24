package com.epai.oblfiles;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static android.os.Environment.MEDIA_MOUNTED;

public final class FileUtils {

    private static final String TAG = "文件辅助类";

    public static String getFilePathByUri(Context context, Uri uri) {
        Log.i(TAG, "Uri转path 1");
        String path = null;
        // 以 file:// 开头的
        if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            path = uri.getPath();
            return path;
        }
        Log.i(TAG, "Uri转path 2");
        // 以 content:// 开头的，比如 content://media/extenral/images/media/17766
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            try (Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.Media.DATA},
                    null, null, null)) {
                if (cursor != null) {
                    Log.i(TAG, "Uri转path 3");
                    if (cursor.moveToFirst()) {
                        Log.i(TAG, "Uri转path 3 1");
                        int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                        Log.i(TAG, "Uri转path 3 2");
                        if (columnIndex > -1) {
                            Log.i(TAG, "Uri转path 3 3");
                            path = cursor.getString(columnIndex);
                            Log.i(TAG, "Uri转path 3 4");
                        }
                    }
                    Log.i(TAG, "Uri转path 4");
                    cursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.i(TAG, "Uri转path 5");
            if (path != null) {
                return path;
            }
        }
        Log.i(TAG, "Uri转path 6");
        // 4.4及之后的 是以 content:// 开头的，比如 content://com.android.providers.media.documents/document/image%3A235700
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                Log.i(TAG, "Uri转path 7");
                if (isExternalStorageDocument(uri)) {
                    Log.i(TAG, "Uri转path 8");
                    // ExternalStorageProvider
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    if ("primary".equalsIgnoreCase(type)) {
                        path = Environment.getExternalStorageDirectory() + "/" + split[1];
                        return path;
                    }
                } else if (isDownloadsDocument(uri)) {
                    Log.i(TAG, "Uri转path 9");
                    // DownloadsProvider
                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                            Long.parseLong(id));
                    path = getDataColumn(context, contentUri, null, null);
                    return path;
                } else if (isMediaDocument(uri)) {
                    Log.i(TAG, "Uri转path 10");
                    // MediaProvider
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    Log.i(TAG, "Uri转path 11");
                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{split[1]};
                    path = getDataColumn(context, contentUri, selection, selectionArgs);
                    Log.i(TAG, "Uri转path 12");
                    return path;
                }
                Log.i(TAG, "Uri转path 13");
            }
            Log.i(TAG, "Uri转path 14");
        }
        Log.i(TAG, "Uri转path 15");
        String filePath = uri.getPath();
        Log.i(TAG, "Uri转path 16");
        if (filePath != null) {
            boolean finduritype = false;
            if (filePath.contains("/root/storage/emulated/0")) {
                Log.i(TAG, "Uri转path 17");
                filePath = filePath.replace("/root/storage/emulated/0", "");
                finduritype = true;
            }
            if (filePath.contains("/storage/emulated/0")) {
                Log.i(TAG, "Uri转path 18");
                filePath = filePath.replace("/storage/emulated/0", "");
                finduritype = true;
            }
            Log.i(TAG, "Uri转path 19");
            if (finduritype) {
                Log.i(TAG, "Uri转path 20");
                String fileTruePath = "/sdcard" + filePath;
                Log.i(TAG, "Uri转path 21");
                return fileTruePath;
            }
        }
        return null;
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static Uri UrigetImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Images.Media._ID}, MediaStore.Images.Media.DATA + "=? ", new String[]{filePath}, null);
        Uri uri = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                Uri baseUri = Uri.parse("content://media/external/images/media");
                uri = Uri.withAppendedPath(baseUri, "" + id);
            }
            cursor.close();
        }
        if (uri == null) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATA, filePath);
            uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        }
        return uri;
    }

    public static boolean exist(String stringFilePathTo) {
        File f = new File(stringFilePathTo);
        return f.exists();
    }

    public static void deleteDirWihtFile(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return;
        for (File file : dir.listFiles()) {
            if (file.isFile())
                file.delete(); // 删除所有文件
            else if (file.isDirectory())
                deleteDirWihtFile(file); // 递规的方式删除文件夹
        }
        dir.delete();// 删除目录本身
    }

    public static String copyFileFromUriToFolder(Context context,
                                                 Uri uri, String cadModelPath) {
        //把文件复制到沙盒目录
        ContentResolver contentResolver = context.getContentResolver();
        String displayName = System.currentTimeMillis() + Math.round((Math.random() + 1) * 1000)
                + "." + MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri));
        try {
            InputStream is = contentResolver.openInputStream(uri);
            File cache = new File(cadModelPath, displayName);
            FileOutputStream fos = new FileOutputStream(cache);
            copyStreamContent(is, fos);
            fos.close();
            is.close();
            return cache.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    //! Copy single file
    public static void copyStreamContent(InputStream theIn,
                                         OutputStream theOut) throws IOException {
        byte[] aBuffer = new byte[1024];
        int aNbReadBytes = 0;
        while ((aNbReadBytes = theIn.read(aBuffer)) != -1) {
            theOut.write(aBuffer, 0, aNbReadBytes);
        }
    }
    public static String getAppProcessName(Context context) {
        //当前应用pid
        return "com.epai.oblender";
    }
    public static String getExternStorageDir(Context context, String strSubDir) {
        String directoryPath;
        if (MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {//判断外部存储是否可用
            String stringpackagename = getAppProcessName(context);
            String strdir = stringpackagename + File.separator + strSubDir;
            directoryPath = Environment.getExternalStoragePublicDirectory(strdir).getAbsolutePath();
        } else {//没外部存储就使用内部存储
            directoryPath = context.getFilesDir() + File.separator + strSubDir;
        }
        File file = new File(directoryPath);
        if (!file.exists()) {//判断文件目录是否存在
            boolean mkdirs = file.mkdirs();
            if (!mkdirs) {
                File fileexternal = context.getExternalFilesDir(strSubDir);
                if (fileexternal != null) {
                    return file.getAbsolutePath();
                } else {
                    return null;
                }
            } else {
                return directoryPath;
            }
        } else {
            return directoryPath;
        }
    }
}