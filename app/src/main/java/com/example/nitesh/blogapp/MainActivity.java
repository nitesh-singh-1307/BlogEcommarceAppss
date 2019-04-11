package com.example.nitesh.blogapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;


public class MainActivity extends AppCompatActivity {
    private Toolbar mTopToolbar;
    private FirebaseAuth mAuther;
    private FloatingActionButton addPostBtn;
    private FirebaseFirestore firebasefirestore;
    private String current_user_id;
    private BottomNavigationView mainbottomNav;
    private HomeFragment homeFragment;
    private NotificationFragment notificationFragment;
    private AccountFragment accountFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTopToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mTopToolbar);
        getSupportActionBar().setTitle("Photo Blog");
        mAuther = FirebaseAuth.getInstance();
        firebasefirestore = FirebaseFirestore.getInstance();

        if (mAuther.getCurrentUser() != null) {
            mainbottomNav = findViewById(R.id.mainBottomNav);
// FRAGMENTS
            homeFragment = new HomeFragment();
            notificationFragment = new NotificationFragment();
            accountFragment = new AccountFragment();
            replaceFragment(homeFragment);
            initializeFragment();
            mainbottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.main_container);
                    switch (item.getItemId()) {
                        case R.id.bottom_action_home:
                            replaceFragment(homeFragment);
                            return true;

                        case R.id.bottom_action_account:

                            replaceFragment(accountFragment);
                            return true;

                        case R.id.bottom_action_notif:

                            replaceFragment(notificationFragment);
                            return true;

                        default:
                            return false;
                    }

                }
            });
        }

        addPostBtn = findViewById(R.id.add_post_btn);
        addPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newpostintent = new Intent(MainActivity.this, NewPostActivity.class);
                startActivity(newpostintent);
                finish();
            }
        });

    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (fragment == homeFragment) {
            fragmentTransaction.hide(accountFragment);
            fragmentTransaction.hide(notificationFragment);
        }
        if (fragment == accountFragment) {
            fragmentTransaction.hide(homeFragment);
            fragmentTransaction.hide(notificationFragment);
        }
        if (fragment == notificationFragment) {
            fragmentTransaction.hide(homeFragment);
            fragmentTransaction.hide(accountFragment);
        }
        fragmentTransaction.show(fragment);
        //fragmentTransaction.replace(R.id.main_container, fragment);
        fragmentTransaction.commit();
    }

    private void initializeFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.main_container, homeFragment);
        fragmentTransaction.add(R.id.main_container, notificationFragment);
        fragmentTransaction.add(R.id.main_container, accountFragment);
        fragmentTransaction.hide(notificationFragment);
        fragmentTransaction.hide(accountFragment);
        fragmentTransaction.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currntuser = FirebaseAuth.getInstance().getCurrentUser();
        if (currntuser == null) {
            sendtologinpage();
        } else {
            current_user_id = mAuther.getCurrentUser().getUid();
            firebasefirestore.collection("Users").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        if (!task.getResult().exists()) {
                            Intent setupintent = new Intent(MainActivity.this, SetupActivity.class);
                            startActivity(setupintent);
                            finish();
                        }

                    } else {
                        String errorMessage = task.getException().getMessage();
                        Toast.makeText(MainActivity.this, "Error : " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void sendtologinpage() {
        Intent loginintent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginintent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout_btn:
                logout();
                return true;
            case R.id.action_settings_btn:
                setupragistration();
            default:
                return false;
        }


    }

    private void setupragistration() {
        Intent loginintent = new Intent(MainActivity.this, SetupActivity.class);
        startActivity(loginintent);
        finish();
    }

    private void logout() {
        mAuther.signOut();
        sendtologinpage();
    }
}



