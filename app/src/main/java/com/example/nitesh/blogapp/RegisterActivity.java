package com.example.nitesh.blogapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {
    private EditText reg_email_field, reg_pass_field, reg_conform_field;
    private Button reg_btn, reg_login_btn;
    private ProgressBar reg_progressbar;
    private FirebaseAuth mAuther;
    private String str_reg_email, str_reg_pass, str_reg_confir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuther  = FirebaseAuth.getInstance();
        reg_email_field = findViewById(R.id.regedit_email);
        reg_pass_field = findViewById(R.id.regedit_pass);
        reg_conform_field = findViewById(R.id.regedit_passconform);
        reg_btn = findViewById(R.id.btn_regalredy);
        reg_login_btn = findViewById(R.id.btn_registraion);
        reg_progressbar = findViewById(R.id.regist_progress);
        reg_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        reg_login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                str_reg_email = reg_email_field.getText().toString();
                str_reg_pass = reg_pass_field.getText().toString();
                str_reg_confir = reg_conform_field.getText().toString();
                if (!TextUtils.isEmpty(str_reg_email) && !TextUtils.isEmpty(str_reg_pass) && !TextUtils.isEmpty(str_reg_confir)) {
                    if (str_reg_pass.equals(str_reg_confir)) {
                        reg_progressbar.setVisibility(View.VISIBLE);
                        mAuther.createUserWithEmailAndPassword(str_reg_email, str_reg_pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()){
                                    Intent setupintent = new Intent(RegisterActivity.this,SetupActivity.class);
                                    startActivity(setupintent);
//                                    sendtomain();
                                }else {
                                    String errormesaage = task.getException().getMessage();
                                    Toast.makeText(RegisterActivity.this, "Error:" + errormesaage, Toast.LENGTH_LONG).show();

                                }
                                reg_progressbar.setVisibility(View.INVISIBLE);
                            }
                        });

                    } else {
                        Toast.makeText(RegisterActivity.this, "Confirm password don't match plz try agin", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currantuser = mAuther.getCurrentUser();
        if (currantuser != null) {
            sendtomain();

        }
    }

    private void sendtomain() {
        Intent mainintent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(mainintent);
        finish();
    }
}
