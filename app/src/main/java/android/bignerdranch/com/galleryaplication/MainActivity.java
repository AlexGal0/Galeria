package android.bignerdranch.com.galleryaplication;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EventListener;

public class MainActivity extends Activity {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_FULL_IMAGE = 2;
    static final int REQUEST_VIDEO_CAPTURE = 3;
    private GridLayout grid;
    private ImageButton imageButton;
    private ImageButton videoButton;
    private int width;
    private int height;

    private ArrayList<Uri> dir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dir = new ArrayList<>();

        grid = findViewById(R.id.grid);
        imageButton = findViewById(R.id.imageButton);
        videoButton = findViewById(R.id.video_button);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels-50;

        Log.i("MARLON", width + "");
        //grid.setLayoutParams(new LinearLayout.LayoutParams(width/4, width/4));
        grid.setAlignmentMode(GridLayout.ALIGN_MARGINS);
        grid.setUseDefaultMargins(true);

        getAllSaveImages();
        imageButton.setOnClickListener((v) ->{
            dispatchTakePictureIntent();
        });
        videoButton.setOnClickListener(v ->{
            dispatchTakeVideoIntent();
        });


    }

    private void getAllSaveImages() {
        File folder = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath());
        File[] list = folder.listFiles();

        for(File file: list){
            dir.add(FileProvider.getUriForFile(this, "android.bignerdranch.com.galleryaplication", file));
            int index = dir.size()-1;
            addImage(index);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            addImage(dir.size() - 1);
        }
        else if(requestCode == REQUEST_FULL_IMAGE && resultCode == RESULT_OK){
            Log.i("MARLON", "FULL OK");
            if(data.hasExtra("index")){
                Log.i("MARLON", "Eliminated");

                int index = data.getIntExtra("index", -1);
                dir.remove(index);
                grid.removeViewAt(index);
            }
        }
        else if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            addVideo(dir.size()-1, data);
        }

    }

    private void addVideo(int index, Intent data){
        Uri videoUri = data.getData();
        dir.add(videoUri);
        VideoView video = new VideoView(this);

        video.setVideoURI(videoUri);
        video.pause();
        LinearLayout.LayoutParams options = new LinearLayout.LayoutParams(width/3, width/3);
        options.width = LinearLayout.LayoutParams.MATCH_PARENT;
        options.height = LinearLayout.LayoutParams.MATCH_PARENT;
        video.setLayoutParams(new LinearLayout.LayoutParams(width/3, width/3));

        grid.addView(video);
        grid.setColumnCount(3);
    }
    private void addImage(int index) {
        ImageView image = new ImageView(this);
        grabImage(image, index);
        grid.addView(image);
        image.setOnClickListener(v -> {
            Intent intent = new Intent(this, FullImage.class);
            int i = grid.indexOfChild(v);
            intent.putExtra(FullImage.URI, dir.get(i).toString());
            intent.putExtra("index", index);
            startActivityForResult(intent, REQUEST_FULL_IMAGE);
        });
        grid.setColumnCount(3);
    }


    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.i("MARLON", ex.getMessage());
                return;
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "android.bignerdranch.com.galleryaplication",
                        photoFile);
                dir.add(photoURI);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    String currentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void grabImage(ImageView imageView, int index)
    {
        this.getContentResolver().notifyChange(dir.get(index), null);
        ContentResolver cr = this.getContentResolver();
        Bitmap bitmap;
        try
        {
            bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, dir.get(index));
            bitmap = fixSquare(bitmap);
            bitmap = Bitmap.createScaledBitmap(bitmap, width/3, width/ 3, false);
            imageView.setImageBitmap(bitmap);
        }
        catch (Exception e)
        {
            Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show();
            Log.d("MARLON", "Failed to load", e);
            dir.remove(dir.size()-1);
        }
    }

    private Bitmap fixSquare(Bitmap bitmap) {
        int x = bitmap.getWidth();
        int y = bitmap.getHeight();
        int dif = Math.abs(x-y) / 2;

        if(x > y)
            bitmap = Bitmap.createBitmap(bitmap, dif, 0, y, y);
        else if(y > x){
            bitmap = Bitmap.createBitmap(bitmap, 0, dif, x, x);
        }
        return bitmap;
    }
}


