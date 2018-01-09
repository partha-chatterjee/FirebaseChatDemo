package partha.firebasechatdemo.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import partha.firebasechatdemo.R;

/**
 * This Class is used for selecting images from camera and from gallery.<br>
 * USAGE - Extend this class and implement all the methods.<br>
 * It provides Bitmap Image and the bitmap file path.<br>
 * Do mention (android:largeHeap="true") in your manifest application tag.<br><br>
 */

public abstract class CameraManagerActivity extends AppCompatActivity {

    public static final int MEDIA_TYPE_IMAGE = 10;
    public static final int GALLERY_INTENT_CALLED = 11;
    public static final int CAPTURE_INTENT_CALLED = 13;
    /*Variables used for call permission*/
    private static final int CAMERA_REQUEST_CODE = 300;
    private Uri fileUri = null;
    private String mPath = null;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    public void onCallCameraButton() {
//        L.d("onCallCameraButton");
        checkCallpermission();
    }


    /*
    * Checks for App Permission
    * (Camera Permission, Write External Storage)
    * */
    private boolean checkCallpermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            askCallPermission();
        } else {
            uploadImageDialog();
        }

        return false;
    }


    /*
     * Ask for App Permission
     * (Camera Permission, Write External Storage)
     * */
    private void askCallPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_REQUEST_CODE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    uploadImageDialog();
                } else {

                    if (!ActivityCompat.shouldShowRequestPermissionRationale(CameraManagerActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) && !ActivityCompat.shouldShowRequestPermissionRationale(CameraManagerActivity.this,
                            Manifest.permission.CAMERA)) {

                        // Show an explanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.
                        Toast.makeText(this, "Go to app settings to enable storage and camera permission.", Toast.LENGTH_LONG).show();
                    } else {
                        askCallPermission();
                    }
                }
                break;
        }
    }


    /*
     * Dialog options to select camera and gallery
     * */
    public void uploadImageDialog() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(CameraManagerActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE, getFileName());
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                    startActivityForResult(intent, CAPTURE_INTENT_CALLED);
                } else if (options[item].equals("Choose from Gallery")) {
                    if (Build.VERSION.SDK_INT < 19) {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_INTENT_CALLED);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("image/*");
                        startActivityForResult(intent, GALLERY_INTENT_CALLED);
                    }

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }


    /*
    * Receives results from Camera/Gallery
    * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            //Toast.makeText(CameraManagerActivity.this, "You have canceled", Toast.LENGTH_LONG).show();
        } else {
            Uri selectedImageUri;
            switch (requestCode) {
                case CAPTURE_INTENT_CALLED:

                    selectedImageUri = fileUri;

                    Bitmap mBitmap;
                    try {
                        InputStream input = getContentResolver().openInputStream(selectedImageUri);
                        mBitmap = getBitmapExifInterface(selectedImageUri, decodeBitmap(input));
//                        L.d("GALLERY_INTENT_CALLED " + mPath);
                        onBitmapReceivedFromCamera(mBitmap, mPath, selectedImageUri);
                        if (input != null) {
                            input.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

//                    L.d("CAPTURE_INTENT_CALLED" + " Uri " + selectedImageUri);
                    break;

                case GALLERY_INTENT_CALLED:
                    selectedImageUri = data.getData();
//                    L.d("GALLERY_INTENT_CALLED" + " Uri " + selectedImageUri);
                    try {
                        InputStream input = getContentResolver().openInputStream(selectedImageUri);

                        mBitmap = getBitmapExifInterface(selectedImageUri, decodeBitmap(input));
                        onBitmapReceivedFromGallery(mBitmap, mPath, selectedImageUri);
                        if (input != null) {
                            input.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

            }

        }
    }


    /**
     * Check & Returns the correct oriented Bitmap Image
     */
    private Bitmap getBitmapExifInterface(Uri selectedImageUri, Bitmap bmp) {
        /////for rotation of image
        ExifInterface exif;
        int rotation;
        try {
            mPath = ImageFilePath.getPath(this, selectedImageUri);
//            mPath = selectedImageUri.getPath();// "file:///mnt/sdcard/FileName.mp3"
//            L.d("PARENT- " + " Path: " + mPath);
            exif = new ExifInterface(mPath);
            rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

            bmp = getRotatedBitmap(rotation, bmp);

//            L.d("PARENT- " + " Exif Rotation: " + rotation);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmp;
    }


    /**
     * For checking bitmap image orientation and returns the rotated bitmap.
     */
    public Bitmap getRotatedBitmap(int rotation, Bitmap bmp) {

        Matrix matrix = new Matrix();

        switch (rotation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bmp;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(270);
                break;
            case ExifInterface.ORIENTATION_UNDEFINED:
                return bmp;
            default:
                return bmp;
        }

        try {
            Bitmap bmRotated = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
            bmp.recycle();
            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }

    }


    /**
     * Returns the scaled bitmap (by calculating the sample size)
     */
    private Bitmap decodeBitmap(InputStream inputStream) {

        Bitmap bitmap = null;
        InputStream is1 = null, is2 = null;

        try {
//            L.d("PARENT- " + "BITMAP: InputStream- " + inputStream.toString());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // Fake code simulating the copy
            // You can generally do better with nio if you need...
            // And please, unlike me, do something about the Exceptions :D
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();

            // Open new InputStreams using the recorded bytes
            // Can be repeated as many times as you wish
            is1 = new ByteArrayInputStream(baos.toByteArray());
            is2 = new ByteArrayInputStream(baos.toByteArray());

                /*DecodeBitmap before sampling*/
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            //noinspection deprecation
            options.inDither = true;
            BitmapFactory.decodeStream(is1, null, options);
            int imageHeight = options.outHeight;
            int imageWidth = options.outWidth;


                /*Calculate screen height & width*/
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int reqHeight = displayMetrics.heightPixels / 2;
            int reqwidth = displayMetrics.widthPixels / 2;

//            L.d("PURCHASED_ACTIVITY " + "BITMAP: width- " + imageWidth + " height- " + imageHeight + " reqheight- " + reqHeight + " reqWidth- " + reqwidth);

            options.inSampleSize = calculateInSampleSize(options, reqwidth, reqHeight);

//            L.d("PURCHASED_ACTIVITY " + "BITMAP: SampleSize- " + options.inSampleSize + " InputStream- " + inputStream.toString());
            // Decode bitmap with inSampleSize set (DecodeSampledBitmap)
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            //noinspection deprecation
            options.inDither = true;
            bitmap = BitmapFactory.decodeStream(is2, null, options);

            if (bitmap != null) {
//                L.d("PURCHASED_ACTIVITY " + "BITMAP: height- " + bitmap.getHeight() + " width- " + bitmap.getWidth());
            } else {
//                L.d("PURCHASED_ACTIVITY " + "BITMAP: is Null.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (is1 != null) {
                    is1.close();
                }
                if (is2 != null) {
                    is2.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return bitmap;
    }


    /*
    * Calculating sample size of the bitmap
    * */
    public int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


    /**
     * Abstract method to pass the bitmap and path<br>
     * when image captured from camera
     */
    protected abstract void onBitmapReceivedFromCamera(Bitmap bitmap, String path, Uri imageUri);

    /**
     * Abstract method to pass the bitmap and path<br>
     * when image selected from gallery
     */
    protected abstract void onBitmapReceivedFromGallery(Bitmap bitmap, String path, Uri imageUri);


    /*---------------------------------------------------------------------------------------------*/
    /*-- Below are the methods for creating Image file, file name when Image capture from camera --*/
    /*---------------------------------------------------------------------------------------------*/

    /*
    * Creates file name for the image to be captured
    * */
    public String getFileName() {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMyyyy_hhmmss");
        return simpleDateFormat.format(new Date());
    }


    /**
     * Create a file Uri for saving an image or video
     */
    public Uri getOutputMediaFileUri(int type, String name) {
        return Uri.fromFile(getOutputMediaFile(type, name));
    }


    /**
     * Create a File for saving an image or video
     */
    public File getOutputMediaFile(int type, String fName) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        /*File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Stock_Up");*/
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), getResources().getString(R.string.app_name));
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
//                L.d("MyCameraApp" + "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + fName + ".jpg");
        } /*else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        }*/ else {
            return null;
        }

        return mediaFile;
    }

    /*------------------------------------- Ends here --------------------------------------------*/


    /**
     * - Class is used to get Absolute Path from file -<br>
     * Irrespective of any android build version.
     */
    private static class ImageFilePath {

        /**
         * Method for return file path of Gallery image
         *
         * @param context
         * @param uri
         * @return path of the selected image file from gallery
         */
        static String nopath = "Select Video Only";

        @SuppressLint("NewApi")
        static String getPath(final Context context, final Uri uri) {

            // check here to KITKAT or new version
            final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

            // DocumentProvider
            if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {

                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/"
                                + split[1];
                    }
                }
                // DownloadsProvider
                else if (isDownloadsDocument(uri)) {

                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"),
                            Long.valueOf(id));

                    return getDataColumn(context, contentUri, null, null);
                }
                // MediaProvider
                else if (isMediaDocument(uri)) {
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

                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{split[1]};

                    return getDataColumn(context, contentUri, selection,
                            selectionArgs);
                }
            }
            // MediaStore (and general)
            else if ("content".equalsIgnoreCase(uri.getScheme())) {

                // Return the remote address
                if (isGooglePhotosUri(uri))
                    return uri.getLastPathSegment();

                return getDataColumn(context, uri, null, null);
            }
            // File
            else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }

            return nopath;
        }

        /**
         * Get the value of the data column for this Uri. This is <span id="IL_AD2"
         * class="IL_AD">useful</span> for MediaStore Uris, and other file-based
         * ContentProviders.
         *
         * @param context       The context.
         * @param uri           The Uri to query.
         * @param selection     (Optional) Filter used in the query.
         * @param selectionArgs (Optional) Selection arguments used in the query.
         * @return The value of the _data column, which is typically a file path.
         */
        static String getDataColumn(Context context, Uri uri,
                                    String selection, String[] selectionArgs) {

            Cursor cursor = null;
            final String column = "_data";
            final String[] projection = {column};

            try {
                cursor = context.getContentResolver().query(uri, projection,
                        selection, selectionArgs, null);
                if (cursor != null && cursor.moveToFirst()) {
                    final int index = cursor.getColumnIndexOrThrow(column);
                    return cursor.getString(index);
                }
            } finally {
                if (cursor != null)
                    cursor.close();
            }
            return nopath;
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is ExternalStorageProvider.
         */
        static boolean isExternalStorageDocument(Uri uri) {
            return "com.android.externalstorage.documents".equals(uri
                    .getAuthority());
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is DownloadsProvider.
         */
        static boolean isDownloadsDocument(Uri uri) {
            return "com.android.providers.downloads.documents".equals(uri
                    .getAuthority());
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is MediaProvider.
         */
        static boolean isMediaDocument(Uri uri) {
            return "com.android.providers.media.documents".equals(uri
                    .getAuthority());
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is Google Photos.
         */
        static boolean isGooglePhotosUri(Uri uri) {
            return "com.google.android.apps.photos.content".equals(uri
                    .getAuthority());
        }
    }
}
