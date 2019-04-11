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

public class LoginActivity extends AppCompatActivity {
    private EditText edit_email, edt_pass;
    private Button btn_login, btn_reg;
    private FirebaseAuth mAuth;
    private ProgressBar mprogressbas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        edit_email = findViewById(R.id.edit_email);
        edt_pass = findViewById(R.id.edit_pass);
        btn_login = findViewById(R.id.btn_login);
        btn_reg = findViewById(R.id.btn_reg);
        mprogressbas = findViewById(R.id.login_progress);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mprogressbas.setVisibility(View.VISIBLE);
                String loginmail = edit_email.getText().toString();
                String logpass = edt_pass.getText().toString();
                if (!TextUtils.isEmpty(loginmail) && !TextUtils.isEmpty(logpass)) {
                    mprogressbas.setVisibility(View.VISIBLE);
                    mAuth.signInWithEmailAndPassword(loginmail, logpass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                sendtomain();
                            } else {
                                String errormessage = task.getException().getMessage();
                                Toast.makeText(LoginActivity.this, "error :" + errormessage, Toast.LENGTH_LONG).show();
                            }
                            mprogressbas.setVisibility(View.INVISIBLE);
                        }

                    });

                }
            }
        });

        btn_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent regesterintent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(regesterintent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currantuser = mAuth.getCurrentUser();
        if (currantuser != null) {
            sendtomain();
        }
    }

    private void sendtomain() {
        Intent mainintent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(mainintent);
        finish();
    }


}
