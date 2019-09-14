package android.bignerdranch.com.galleryaplication;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.time.Duration;

public class FullImage extends Activity{

    public static final String URI = "URI_CODE_NAME";
    public static final int PERMISSION_WRITE = 1;
    private Uri uri;
    private int index;
    private Toolbar toolbar;
    private String filepath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);

        toolbar = findViewById(R.id.my_toolbar);
        toolbar.inflateMenu(R.menu.menu);
        index = getIntent().getIntExtra("index", -1);
        uri = Uri.parse(getIntent().getStringExtra(URI));
        filepath = getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() + uri.getPath().substring(10);
        ImageView imageView = findViewById(R.id.full_image_view);
        grabImage(imageView);
        Log.i("MARLON", index + "");


        toolbar.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()){
                case R.id.delete_image:
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("¿Desea eliminar la imagen?");
                    builder.setPositiveButton("Si", (dialog, which) -> {
                        File file = new File(filepath);
                        if(file.exists())
                            file.delete();
                        Toast.makeText(FullImage.this, "Imagen Eliminada Correctamente", Toast.LENGTH_SHORT).show();
                        Intent result = new Intent();
                        result.putExtra("index", index);
                        setResult(Activity.RESULT_OK, result);
                        finish();
                    });
                    builder.setNegativeButton("No", (dialog, which) -> {});

                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return true;
                case R.id.save_image:
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_WRITE);
                        return true;
                    }
                    addImageToGallery(filepath, this);
                    return true;
                    default:
                        return false;
            }
        });

    }

    public void grabImage(ImageView imageView)
    {
        this.getContentResolver().notifyChange(uri, null);
        ContentResolver cr = this.getContentResolver();
        Bitmap bitmap;
        try
        {
            bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, uri);
            imageView.setImageBitmap(bitmap);
        }
        catch (Exception e)
        {
            Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show();
            Log.d("MARLON", "Failed to load", e);
        }
    }


    public void addImageToGallery(final String filePath, final Context context) {

        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, filePath);

        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Toast.makeText(this, "Imagen guardada en la galeria correctamente", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_WRITE && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            addImageToGallery(filepath, this);
    }
}

