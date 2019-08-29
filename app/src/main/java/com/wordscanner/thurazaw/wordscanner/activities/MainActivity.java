package com.wordscanner.thurazaw.wordscanner.activities;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.wordscanner.thurazaw.wordscanner.R;
import com.wordscanner.thurazaw.wordscanner.utils.Utils;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    public static final int CAMERA_REQUEST_CODE = 109;
    public static final int GALLERY_REQUEST_CODE = 110;
    public int GALLERY_CLICK_CODE = 109;
    private AppCompatImageView img_main;
    private AppCompatTextView tv_noImage;
    private AppCompatButton btn_scan;
    private boolean doubleBackToExitPressedOnce = false;
    public String mCurrentPhotoPath;
    private AlertDialog alertDialog;
    private AlertDialog.Builder builder;
    private StringBuilder strBuilder;
   // private Bitmap imageBitmap;
    private Bitmap galleryBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        img_main = findViewById(R.id.img_main);
        img_main.setScaleType(ImageView.ScaleType.CENTER_CROP);

        tv_noImage = findViewById(R.id.tv_no_image);
        tv_noImage.setVisibility(View.VISIBLE);

        btn_scan = findViewById(R.id.btn_scan);

        builder = new AlertDialog.Builder(MainActivity.this);


        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(img_main.getDrawable() == null){

                    Toast.makeText(MainActivity.this, "Something Wrong!", Toast.LENGTH_SHORT).show();


                }else {

                    if(GALLERY_CLICK_CODE == 100){

                        //imageBitmap = galleryBitmap;
                        ScanImage(galleryBitmap);
                        removeImageView();

                    }else {

                        //imageBitmap = getImageFromStorageAndSetToImageView();
                        //removeImageViewAndDeleteFile();
                        ScanImage(getImageFromStorageAndSetToImageView());
                        removeImageViewAndDeleteFile();


                    }

                }

            }//btnScan onClick

        });

    }//onCreate

    void ScanImage(Bitmap bitmap){

        TextRecognizer txtRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        if (!txtRecognizer.isOperational())
        {
            // Shows if your Google Play services is not up to date or OCR is not supported for the device
            // txtView.setText("Detector dependencies are not yet available");
            Log.i("main", "Detector dependencies are not yet available");

        }
        else
        {


            // Set the bitmap taken to the frame to perform OCR Operations.
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray items = txtRecognizer.detect(frame);
            strBuilder = new StringBuilder();
            for (int i = 0; i < items.size(); i++)
            {
                TextBlock item = (TextBlock)items.valueAt(i);
                strBuilder.append(item.getValue());
                //strBuilder.append("/");

            }
        }


        if(strBuilder == null){

            builder.setMessage("No words found!");


        }else {


            //show dialog for the results
            builder.setMessage("The results : \n" + strBuilder.toString());
            builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();

                }
            });

            builder.setPositiveButton("Copy", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("text", strBuilder.toString());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(MainActivity.this, "Copied to clipboard", Toast.LENGTH_SHORT).show();

                }
            });


            alertDialog = builder.create();
            alertDialog.show();
            Log.i("main", strBuilder.toString());
        }


    }

    void removeImageViewAndDeleteFile(){

        File file = new File(mCurrentPhotoPath);
        file.delete();
        img_main.setImageDrawable(null);
        tv_noImage.setVisibility(View.VISIBLE);

    }

    void removeImageView(){

        img_main.setImageDrawable(null);
        tv_noImage.setVisibility(View.VISIBLE);

    }

    //////

    private File createImageFile() throws IOException {
        // Create an image file name
        //String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        //String imageFileName = "JPEG_" + timeStamp + "_";
        String imageFileName = "MyPhoto";

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission allowed", Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_LONG).show();
            }

        }

        if (requestCode == GALLERY_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Gallery permission allowed", Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(this, "Gallery permission denied", Toast.LENGTH_LONG).show();
            }

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            tv_noImage.setVisibility(View.INVISIBLE);
            img_main.setImageBitmap(getImageFromStorageAndSetToImageView());

        }else if(requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK){

            GALLERY_CLICK_CODE = 100;

            Uri selectedImage = data.getData();
            try {
                galleryBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                img_main.setImageBitmap(galleryBitmap);

            } catch (IOException e) {
                Log.i("TAG", "Some exception " + e);
            }
        }
    }

    public Bitmap getImageFromStorageAndSetToImageView(){

        int targetW = img_main.getWidth();
        int targetH = img_main.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath,bmOptions);

        //img_main.setImageBitmap(bitmap);

        return bitmap;



    }

    public void onCameraClick(View v) {

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this
                    , new String[]{Manifest.permission.CAMERA}
                    , CAMERA_REQUEST_CODE);


        } else {
            //permission already granded for android 5.1 and lower

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File

                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(this,
                            "com.example.android.fileprovider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
                }
            }



        }


    }

    public void onGalleryClick(View v){

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this
                    , new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}
                    , GALLERY_REQUEST_CODE);


        } else {

            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            //photoPickerIntent.putExtra("galleryClick", GALLERY_CLICK_CODE);
            startActivityForResult(photoPickerIntent, GALLERY_REQUEST_CODE);


        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = getSharedPreferences(Utils.SP_SCANIMAHE, MODE_PRIVATE);
        int value = sharedPreferences.getInt(Utils.SP_KEY_SCAN_ACTIVITY, 0);

        if (value == 122) {
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
            sharedPreferences.edit().remove(Utils.SP_KEY_SCAN_ACTIVITY).apply();

        } else {

            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        }

    }

    @Override
    public void onBackPressed() {

        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press again to exit!", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }
}
