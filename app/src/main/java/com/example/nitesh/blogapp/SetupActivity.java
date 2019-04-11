package com.example.nitesh.blogapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SetupActivity extends AppCompatActivity {
    private Toolbar mTopToolbar;
    private EditText setup_name;
    private Button setup_btn;
    private ProgressBar update_progress;

    private CircleImageView setupImage;
    private Uri mainImageURI = null;
    private String user_uid;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private Bitmap compressedImageFile;
    private boolean isChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        mTopToolbar = (Toolbar) findViewById(R.id.setupToolbar);
        setSupportActionBar(mTopToolbar);
        getSupportActionBar().setTitle("Photo Blog");
        firebaseAuth = FirebaseAuth.getInstance();
        user_uid = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        setup_name = findViewById(R.id.setup_name);
        setupImage = findViewById(R.id.setup_image);
        setup_btn = findViewById(R.id.setup_btn);
        update_progress = findViewById(R.id.setup_progress);
        update_progress.setVisibility(View.VISIBLE);
        setup_btn.setEnabled(false);
        firebaseFirestore.collection("Users").document(user_uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");
                        mainImageURI = Uri.parse(image);
                        setup_name.setText(name);
                        RequestOptions palaceholder = new RequestOptions();
                        palaceholder.placeholder(R.drawable.default_image);
                        Glide.with(SetupActivity.this).setDefaultRequestOptions(palaceholder).load(image).into(setupImage);
                        Toast.makeText(SetupActivity.this, "Dos Exit", Toast.LENGTH_LONG).show();

                    } else {
                        Toast.makeText(SetupActivity.this, "Dosn't Exit", Toast.LENGTH_LONG).show();

                    }

                } else {

                    String error = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "FIRESTROREGE error :" + error, Toast.LENGTH_LONG).show();

                }
                update_progress.setVisibility(View.INVISIBLE);
                setup_btn.setEnabled(true);
            }
        });
        setup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = setup_name.getText().toString();
                if (!TextUtils.isEmpty(username) && mainImageURI != null) {
                    update_progress.setVisibility(View.VISIBLE);
                    if (isChanged) {
                        user_uid = firebaseAuth.getCurrentUser().getUid();
                        StorageReference image_path = storageReference.child("profile_images").child(user_uid + ".jpg");
                        image_path.putFile(mainImageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    storeFileStrores(task, username);
                                } else {
                                    String error = task.getException().getMessage();
                                    Toast.makeText(SetupActivity.this, "Image error :" + error, Toast.LENGTH_LONG).show();
                                    update_progress.setVisibility(View.INVISIBLE);
                                }

                            }
                        });
                    } else {
                        storeFileStrores(null, username);
                    }
                }
            }
        });
        setupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(SetupActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(SetupActivity.this, "permission Denied", Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(SetupActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else {
                        bringImagepicker();

                    }
                } else {
                    bringImagepicker();
                }
            }
        });
    }

    private void storeFileStrores(@NonNull Task<UploadTask.TaskSnapshot> task, String username) {
        Uri download_uri;
        if (task != null) {
            download_uri = task.getResult().getDownloadUrl();
        } else {
            download_uri = mainImageURI;
        }

        Map<String, String> userMap = new HashMap<>();
        userMap.put("name", username);
        userMap.put("image", download_uri.toString());
        firebaseFirestore.collection("Users").document(user_uid).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(SetupActivity.this, "uSER sEtting ARE uPDATE", Toast.LENGTH_LONG).show();

                    Intent mainintent = new Intent(SetupActivity.this, MainActivity.class);
                    startActivity(mainintent);
                    finish();
                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "FIRESTROREGE error :" + error, Toast.LENGTH_LONG).show();

                }
                update_progress.setVisibility(View.INVISIBLE);
            }
        });

    }

    private void bringImagepicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(SetupActivity.this);
    }

   /* @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }*/

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mainImageURI = result.getUri();
                setupImage.setImageURI(mainImageURI);
                isChanged = true;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
