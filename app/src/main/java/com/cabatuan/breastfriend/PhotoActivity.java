package com.cabatuan.breastfriend;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by cobalt on 12/17/15.
 */
public class PhotoActivity extends AppCompatActivity {

    public static final String PHOTO_FILE_EXTENSION = ".png";
    public static final String PHOTO_MIME_TYPE = "image/png";
    public static final String EXTRA_PHOTO_URI =
            "com.cabatuan.breastfriend.PhotoActivity.extra.PHOTO_URI";
    public static final String EXTRA_PHOTO_DATA_PATH =
            "com.cabatuan.breastfriend.PhotoActivity.extra.PHOTO_DATA_PATH";
    private Uri mUri;
    private String mDataPath;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        mUri = intent.getParcelableExtra(EXTRA_PHOTO_URI);
        // Log.d("PhotoActivity","mUri = " + mUri);

        mDataPath = intent.getStringExtra(EXTRA_PHOTO_DATA_PATH);

        final ImageView imageView = new ImageView(this);
        imageView.setImageURI(mUri);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        setContentView(imageView);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.activity_photo, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete:
                deletePhoto();
                return true;
             case R.id.menu_share:
                sharePhoto();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }




    private void deletePhoto() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(
                PhotoActivity.this);

        alert.setTitle(getString(R.string.delete_alert_title));
        alert.setMessage(getString(R.string.photo_delete_prompt_message));
        alert.setCancelable(false);
        alert.setPositiveButton(getString(R.string.delete),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog,
                                        final int which) {
                        getContentResolver().delete(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                MediaStore.MediaColumns.DATA + "=?",
                                new String[] { mDataPath });
                        showToast("Photo successfully deleted!");
                        finish();
                    }
                });
        alert.setNegativeButton(android.R.string.cancel, null);
        alert.show();
    }

/*
* Show a chooser so that the user may pick an app for sending
* the photo.
*/
    private void sharePhoto() {
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(PHOTO_MIME_TYPE);

        intent.putExtra(Intent.EXTRA_STREAM, mUri);
        intent.putExtra(Intent.EXTRA_SUBJECT,
                getString(R.string.photo_send_extra_subject));
        intent.putExtra(Intent.EXTRA_TEXT,
                getString(R.string.photo_send_extra_text));
        startActivity(Intent.createChooser(intent,
                getString(R.string.photo_send_chooser_title)));
    }


    private void showToast(String message) {

        // Inflate the Layout
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.mytoast,
                (ViewGroup) findViewById(R.id.custom_toast_layout));

        // Retrieve the ImageView and TextView
        ImageView iv = (ImageView) layout.findViewById(R.id.toastImageView);
        TextView text = (TextView) layout.findViewById(R.id.textToShow);

        // Set the image
        iv.setImageResource(R.mipmap.ic_video);

        // Set the Text to show in TextView
        text.setText(message);
        text.setBackgroundColor(Color.BLACK);

        final Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}