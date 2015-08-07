package io.kickflip.sdk.helper;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.http.util.ByteArrayBuffer;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Environment;


import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.io.ZipInputStream;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.unzip.UnzipUtil;

import io.kickflip.sdk.KickflipApplication;
import io.kickflip.sdk.R;
import io.kickflip.sdk.utilities.NetworkUtilities;

public class FilesHelper {
    private static final String OUTPUT_FOLDER = "output";
    private static final String FINAL_VIDEO = "final_video.mp4";
    private static final String TEMP_VIDEO = "temp_video.mp4";
    private static final String MERGE_VIDEO = "merge_video.mp4";
    private static final String EDIT_VIDEO = "edit_video.mp4";
    private static final String IMAGE = "output.jpg";
    private static final String TEMP_PICTURE = "picture.jpg";
    private static final String CAMERA_PICTURE = "camera.jpg";
    private static final String AUDIO = "audio.mp3";
    private static final String AAC = "audio.aac";
    private static final String PROMOTE = "output.jpg";
    private static final String TEMP_AUDIO = "tempaudio.mp3";
    private static final String DRAWING = "drawing";

    private static final int BUFF_SIZE = 4096;

    public static boolean isExternalAvailable() {
        return (KickflipApplication.instance().getExternalFilesDir(null) != null);
    }

    public static String getPathFiles() {
        return isExternalAvailable() ? KickflipApplication.instance().getExternalFilesDir(null).getPath() + "/" : null;
    }

    public static String getEnviromentFolder(String folder) {
        File file = new File(Environment.getExternalStoragePublicDirectory(folder), ResourcesHelper.getString(R.string.app_name));
        if (!file.exists()) {
            file.mkdir();
        }
        return file.getAbsolutePath();
    }

    public static String getNewPictureName() {
        String file = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.US).format(new Date()) + ".jpg";
        return new File(getEnviromentFolder(Environment.DIRECTORY_PICTURES), file).getAbsolutePath();
    }

    public static String getNewVideoName() {
        String file = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.US).format(new Date()) + ".mp4";
        return new File(getEnviromentFolder(Environment.DIRECTORY_MOVIES), file).getAbsolutePath();
    }

    public static void refreshGallery(File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        KickflipApplication.instance().sendBroadcast(mediaScanIntent);
    }

    public static String getDestinationVideoFolder() {
        File folder = new File(getPathFiles(), OUTPUT_FOLDER);
        if (!folder.exists()) {
            folder.mkdir();
        }
        return folder.getAbsolutePath();
    }

    public static void clearDestinationVideoFolder(final boolean deleteDrawing) {
        Thread delete = new Thread(new Runnable() {
            @Override
            public void run() {
                File folder = new File(getPathFiles(), OUTPUT_FOLDER);
                if (folder.exists()) {
                    File[] files = folder.listFiles();
                    if (files != null) {
                        for (int i = 0; i < files.length; i++) {
                            if (!files[i].getName().contains(DRAWING)) {
                                files[i].delete();
                            } else if (deleteDrawing) {
                                files[i].delete();
                            }
                        }
                    }
                }
            }
        });
        delete.start();
    }

    public static void purgeCache(final int time) {
        Thread delete = new Thread(new Runnable() {
            @Override
            public void run() {
                long expires = System.currentTimeMillis() - time;
                File folder = new File(getPathFiles());
                File[] files = folder.listFiles();
                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        if (files[i].isFile()) {
                            if (files[i].lastModified() < expires) {
                                files[i].delete();
                            }
                        }
                    }
                }
            }
        });
        delete.start();
    }

    public static String getFinalVideoFile() {
        return getPathFiles() + OUTPUT_FOLDER + "/" + FINAL_VIDEO;
    }

    public static String getTempVideoFile() {
        return getPathFiles() + OUTPUT_FOLDER + "/" + TEMP_VIDEO;
    }

    public static String getMergeVideo() {
        return getPathFiles() + OUTPUT_FOLDER + "/" + MERGE_VIDEO;
    }

    public static String getEditVideoFile() {
        return getPathFiles() + OUTPUT_FOLDER + "/" + EDIT_VIDEO;
    }

    public static String getAudioFile() {
        return getPathFiles() + OUTPUT_FOLDER + "/" + AUDIO;
    }

    public static String getAACFile() {
        return getPathFiles() + OUTPUT_FOLDER + "/" + AAC;
    }

    public static String getTempAudioFile() {
        return getPathFiles() + OUTPUT_FOLDER + "/" + TEMP_AUDIO;
    }

    public static String getTempVideoFile(int video) {
        return getPathFiles() + OUTPUT_FOLDER + "/" + video + ".mp4";
    }

    public static String getTempGifFile(String hash) {
        return getPathFiles() + OUTPUT_FOLDER + "/" + hash + ".mp4";
    }

    public static String getImageFile() {
        return getPathFiles() + OUTPUT_FOLDER + "/" + IMAGE;
    }

    public static String getFirstFrame() {
        return getPathFiles() + OUTPUT_FOLDER + "/0.jpg";
    }

    public static String getSerializedFile(int frameNumber) {
        return getPathFiles() + OUTPUT_FOLDER + "/" + frameNumber + ".dat";
    }

    public static String readFile(String file) {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(KickflipApplication.instance().getAssets().open(file)));
            String line;
            StringBuilder buffer = new StringBuilder();
            while ((line = in.readLine()) != null)
                buffer.append(line).append('\n');
            return buffer.toString();
        } catch (IOException e) {
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public static String readFile(File file) {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            StringBuilder buffer = new StringBuilder();
            while ((line = in.readLine()) != null)
                buffer.append(line).append('\n');
            return buffer.toString();
        } catch (IOException e) {
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public static String downloadFile(String address, String fileName) {
        return downloadFile(address, new File(FilesHelper.getPathFiles(), fileName));
    }

    public static String downloadFile(String address, File file) {
        if (!file.exists() && NetworkUtilities.isOnline()) {
            InputStream is = null;
            OutputStream os = null;
            try {
                is = new URL(address).openStream();
                os = new FileOutputStream(file);
                copyStream(is, os);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return file.getAbsolutePath();
    }

    private static String getPassword() {
        String one = "'6QbR7xI^pT<7Yk9o4hi-eQ5L9D'G76RVd|sX-z3$TYy@TFZ@c";
        String two = getLocation();
        int three = getMultiplier();
        String key = String.format(Locale.US, "%s$%d$erfner334vckjcvdisow:%s", one, three, two);
        return null;
    }

    private static String getLocation() {
        return "0vno?G=`92Ro55nIurk9iBq`Em-oSS07zEh(hNV:3H2=N$lA^D";
    }

    private static int getMultiplier() {
        return 12493;
    }

    public static void unpackZip(String zipname) {

        ZipInputStream is = null;
        OutputStream os = null;

        try {
            // Initiate the ZipFile
            ZipFile zipFile = new ZipFile(FilesHelper.getPathFiles() + zipname);
            String destinationPath = FilesHelper.getPathFiles();

            // If zip file is password protected then set the password
            if (zipFile.isEncrypted()) {
                zipFile.setPassword(getPassword());
            }

            // Get a list of FileHeader. FileHeader is the header information
            // for all the
            // files in the ZipFile
            List<?> fileHeaderList = zipFile.getFileHeaders();

            // Loop through all the fileHeaders
            for (int i = 0; i < fileHeaderList.size(); i++) {
                FileHeader fileHeader = (FileHeader) fileHeaderList.get(i);
                if (fileHeader != null) {

                    // Build the output file
                    String outFilePath = destinationPath + fileHeader.getFileName();
                    File outFile = new File(outFilePath);

                    // Checks if the file is a directory
                    if (fileHeader.isDirectory()) {
                        // This functionality is up to your requirements
                        // For now I create the directory
                        outFile.mkdirs();
                        continue;
                    }

                    // Check if the directories(including parent directories)
                    // in the output file path exists
                    File parentDir = outFile.getParentFile();
                    if (!parentDir.exists()) {
                        parentDir.mkdirs();
                    }

                    // Get the InputStream from the ZipFile
                    is = zipFile.getInputStream(fileHeader);
                    // Initialize the output stream
                    os = new FileOutputStream(outFile);

                    int readLen = -1;
                    byte[] buff = new byte[BUFF_SIZE];

                    // Loop until End of File and write the contents to the
                    // output stream
                    while ((readLen = is.read(buff)) != -1) {
                        os.write(buff, 0, readLen);
                    }
                    // Please have a look into this method for some important
                    // comments
                    closeFileHandlers(is, os);

                    // To restore File attributes (ex: last modified file time,
                    // read only flag, etc) of the extracted file, a utility
                    // class
                    // can be used as shown below
                    UnzipUtil.applyFileAttributes(fileHeader, outFile);

                } else {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                closeFileHandlers(is, os);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void closeFileHandlers(ZipInputStream is, OutputStream os) throws IOException {
        // Close output stream
        if (os != null) {
            os.close();
            os = null;
        }

        if (is != null) {
            is.close();
            is = null;
        }
    }

    public static String getTempPictureFile() {
        return getPathFiles() + TEMP_PICTURE;
    }

    public static Uri getTempPictureUri() {
        return Uri.fromFile(new File(getTempPictureFile()));
    }

    public static String getCameraPicture() {
        return getPathFiles() + CAMERA_PICTURE;
    }

    public static String getPromoteFile() {
        return getDestinationVideoFolder() + "/" + PROMOTE;
    }

    public static Uri getCameraPictureUri() {
        return Uri.fromFile(new File(getCameraPicture()));
    }

    public static boolean copyAsset(String filename) {
        AssetManager assetManager = KickflipApplication.instance().getAssets();
        InputStream in = null;
        OutputStream out = null;
        try {
            java.io.File outFile = new java.io.File(FilesHelper.getPathFiles(), filename);
            if (!outFile.exists()) {
                in = assetManager.open(filename);
                out = new FileOutputStream(outFile);
                copyStream(in, out);
                out.flush();
                out.close();
                in.close();
                in = null;
                out = null;
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public static boolean copyFile(String source, String destination) {
        File sourceFile = new File(source);
        File destinationFile = new File(destination);
        if (sourceFile.exists()) {
            InputStream is = null;
            OutputStream os = null;
            try {
                is = new FileInputStream(sourceFile);
                os = new FileOutputStream(destinationFile);
                FilesHelper.copyStream(is, os);
                FilesHelper.refreshGallery(destinationFile);
                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                NetworkUtilities.close(is);
                NetworkUtilities.close(os);

            }
        }
        return false;
    }

    public static boolean copyFile(InputStream is, String destination) {
        File destinationFile = new File(destination);
        OutputStream os = null;
        try {
            os = new FileOutputStream(destinationFile);
            FilesHelper.copyStream(is, os);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            NetworkUtilities.close(is);
            NetworkUtilities.close(os);
        }
        return false;
    }

    public static void copyStream(InputStream in, OutputStream out) throws IOException {
        BufferedInputStream bufferinstream = new BufferedInputStream(in);
        ByteArrayBuffer baf = new ByteArrayBuffer(5000);
        int current = 0;
        while ((current = bufferinstream.read()) != -1) {
            baf.append((byte) current);
        }
        out.write(baf.toByteArray());
    }

    public static void saveBitmap(Bitmap bitmap, String filename, boolean png) {
        FileOutputStream os = null;
        File imageFile = new File(filename);
        try {
            os = new FileOutputStream(imageFile);
            if (png) {
                bitmap.compress(CompressFormat.PNG, 100, os);
            } else {
                bitmap.compress(CompressFormat.JPEG, 90, os);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                    os = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}