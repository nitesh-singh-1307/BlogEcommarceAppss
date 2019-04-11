package com.example.nitesh.blogapp;


import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment {

    EditText edt_name;
    EditText edt_email;
    Button btn_update;
    CircleImageView img_view;
    private Uri mainImageURI = null;
    private String user_uid;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private boolean isChanged = false;
    public String emailid;

    public AccountFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        edt_name = view.findViewById(R.id.edt_name);
        edt_email = view.findViewById(R.id.edt_email);
        btn_update = view.findViewById(R.id.btn_update);
        img_view = view.findViewById(R.id.circal_image);
        firebaseAuth = FirebaseAuth.getInstance();
        user_uid = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        btn_update.setEnabled(false);
        FirebaseUser currantuser = firebaseAuth.getCurrentUser();
        if (currantuser != null) {
            for (UserInfo profile : currantuser.getProviderData()) {
                emailid = profile.getEmail();
            }
        }

        firebaseFirestore.collection("Users").document(user_uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");
                        mainImageURI = Uri.parse(image);
                        edt_name.setText(name);
                        edt_email.setText(emailid);
                        RequestOptions palaceholder = new RequestOptions();
                        palaceholder.placeholder(R.drawable.default_image);
                        Glide.with(getActivity()).setDefaultRequestOptions(palaceholder).load(image).into(img_view);
                        Toast.makeText(getActivity(), "Dos Exit", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getActivity(), "Dosn't Exit", Toast.LENGTH_LONG).show();
                    }
                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(getActivity(), "FIRESTROREGE error :" + error, Toast.LENGTH_LONG).show();
                }
                btn_update.setEnabled(true);
            }
        });

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = edt_name.getText().toString();
                final String email = edt_email.getText().toString();
                if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(email) && mainImageURI != null) {
                    if (isChanged) {
                        user_uid = firebaseAuth.getCurrentUser().getUid();
                        StorageReference image_path = storageReference.child("profile_images").child(user_uid + ".jpg");
                        image_path.putFile(mainImageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    storeFileStrores(task, username,email);
                                } else {
                                    String error = task.getException().getMessage();
                                    Toast.makeText(getActivity(), "Image error :" + error, Toast.LENGTH_LONG).show();
//                                    update_progress.setVisibility(View.INVISIBLE);
                                }

                            }
                        });
                    }
                }
            }
        });
        return view;
    }

    private void storeFileStrores(Task<UploadTask.TaskSnapshot> task, String username,String emailname) {
        Uri download_uri;
        if (task != null) {
            download_uri = task.getResult().getDownloadUrl();
        } else {
            download_uri = mainImageURI;
        }
        Map<String, String> userMap = new HashMap<>();
        userMap.put("name", username);
        userMap.put("emailid", emailname);
        userMap.put("image", download_uri.toString());
    }

}
