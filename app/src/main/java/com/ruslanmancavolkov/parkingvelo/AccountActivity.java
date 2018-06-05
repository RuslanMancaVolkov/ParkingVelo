package com.ruslanmancavolkov.parkingvelo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class AccountActivity extends AppCompatActivity {

    private Button btnChangeEmail, btnChangePassword, btnSendResetEmail, btnRemoveUser,
            changeEmail, changePassword, sendEmail, btnPersistAnonymousAccount, remove, signOut, persistAnonymousAccount;

    private SignInButton btnPersistGoogle;

    private EditText oldEmail, newEmail, password, newPasswordConfirm, newPassword;
    private ProgressBar progressBar;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;

    private GoogleSignInClient mGoogleSignInClient;
    private int RC_SIGN_IN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));
        setSupportActionBar(toolbar);

        //get firebase auth instance
        auth = FirebaseAuth.getInstance();

        //get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(AccountActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };

        btnChangeEmail = findViewById(R.id.change_email_button);
        btnChangePassword = findViewById(R.id.change_password_button);
        btnSendResetEmail = findViewById(R.id.sending_pass_reset_button);
        btnRemoveUser = findViewById(R.id.remove_user_button);
        changeEmail = findViewById(R.id.changeEmail);
        changePassword = findViewById(R.id.changePass);
        sendEmail = findViewById(R.id.send);
        remove = findViewById(R.id.remove);
        signOut = findViewById(R.id.sign_out);
        persistAnonymousAccount = findViewById(R.id.persist_anonymous_account);
        btnPersistAnonymousAccount = findViewById(R.id.btnPersistAnonymousAccount);
        btnPersistGoogle = findViewById(R.id.btn_persist_google);

        oldEmail = findViewById(R.id.old_email);
        newEmail = findViewById(R.id.new_email);
        password = findViewById(R.id.password);
        newPasswordConfirm = findViewById(R.id.newPasswordConfirm);
        newPassword = findViewById(R.id.newPassword);

        oldEmail.setVisibility(View.GONE);
        newEmail.setVisibility(View.GONE);
        password.setVisibility(View.GONE);
        newPasswordConfirm.setVisibility(View.GONE);
        newPassword.setVisibility(View.GONE);
        changeEmail.setVisibility(View.GONE);
        changePassword.setVisibility(View.GONE);
        sendEmail.setVisibility(View.GONE);
        remove.setVisibility(View.GONE);
        btnPersistAnonymousAccount.setVisibility(View.GONE);
        persistAnonymousAccount.setVisibility(View.GONE);
        btnPersistGoogle.setVisibility(View.GONE);

        progressBar = findViewById(R.id.progressBar);

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        if (user.isAnonymous()){
            signOut.setVisibility(View.GONE);
            persistAnonymousAccount.setVisibility(View.VISIBLE);
            btnChangeEmail.setVisibility(View.GONE);
            btnChangePassword.setVisibility(View.GONE);
            btnSendResetEmail.setVisibility(View.GONE);
        }

        persistAnonymousAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oldEmail.setVisibility(View.GONE);
                newEmail.setVisibility(View.VISIBLE);
                password.setVisibility(View.VISIBLE);
                newPasswordConfirm.setVisibility(View.GONE);
                newPassword.setVisibility(View.GONE);
                changeEmail.setVisibility(View.GONE);
                changePassword.setVisibility(View.GONE);
                sendEmail.setVisibility(View.GONE);
                remove.setVisibility(View.GONE);
                btnPersistAnonymousAccount.setVisibility(View.VISIBLE);
                btnPersistGoogle.setVisibility(View.VISIBLE);
            }
        });

        btnPersistAnonymousAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                if (user != null && !newEmail.getText().toString().trim().equals("") && !password.getText().toString().trim().equals("")) {
                    if (password.getText().toString().trim().length() < 6) {
                        password.setError(getString(R.string.minimum_password));
                        progressBar.setVisibility(View.GONE);
                    }

                    AuthCredential credential = EmailAuthProvider.getCredential(newEmail.getText().toString().trim(), password.getText().toString().trim());
                    auth.getCurrentUser().linkWithCredential(credential)
                            .addOnCompleteListener(AccountActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(AccountActivity.this, getString(R.string.persist_anonymous_success), Toast.LENGTH_LONG).show();
                                        Log.d("TAG", "linkWithCredential:success");
                                        signOut();
                                        progressBar.setVisibility(View.GONE);
                                    } else {
                                        Log.w("TAG", "linkWithCredential:failure", task.getException());
                                        Toast.makeText(AccountActivity.this, getString(R.string.persist_anonymous_fail), Toast.LENGTH_LONG).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });

                } else if (newEmail.getText().toString().trim().equals("")) {
                    newEmail.setError(getString(R.string.enter_email));
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        btnChangeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oldEmail.setVisibility(View.GONE);
                newEmail.setVisibility(View.VISIBLE);
                password.setVisibility(View.GONE);
                newPasswordConfirm.setVisibility(View.GONE);
                newPassword.setVisibility(View.GONE);
                changeEmail.setVisibility(View.VISIBLE);
                changePassword.setVisibility(View.GONE);
                sendEmail.setVisibility(View.GONE);
                remove.setVisibility(View.GONE);
            }
        });

        changeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                if (user != null && !newEmail.getText().toString().trim().equals("")) {
                    user.updateEmail(newEmail.getText().toString().trim())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(AccountActivity.this, getString(R.string.email_updated), Toast.LENGTH_LONG).show();
                                        signOut();
                                        progressBar.setVisibility(View.GONE);
                                    } else {
                                        Toast.makeText(AccountActivity.this, getString(R.string.failed_update_email), Toast.LENGTH_LONG).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                } else if (newEmail.getText().toString().trim().equals("")) {
                    newEmail.setError(getString(R.string.enter_email));
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oldEmail.setVisibility(View.GONE);
                newEmail.setVisibility(View.GONE);
                password.setVisibility(View.GONE);
                newPasswordConfirm.setVisibility(View.VISIBLE);
                newPassword.setVisibility(View.VISIBLE);
                changeEmail.setVisibility(View.GONE);
                changePassword.setVisibility(View.VISIBLE);
                sendEmail.setVisibility(View.GONE);
                remove.setVisibility(View.GONE);
            }
        });

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                if (user != null && !newPassword.getText().toString().trim().equals("")) {
                    if (newPassword.getText().toString().trim().length() < 6) {
                        newPassword.setError(getString(R.string.minimum_password));
                        progressBar.setVisibility(View.GONE);
                    }
                    else if (!newPassword.getText().toString().trim().equals(newPasswordConfirm.getText().toString().trim()))
                    {
                        newPasswordConfirm.setError(getString(R.string.different_passwords));
                        progressBar.setVisibility(View.GONE);
                    }
                    else {
                        user.updatePassword(newPassword.getText().toString().trim())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(AccountActivity.this, getString(R.string.password_updated), Toast.LENGTH_SHORT).show();
                                            signOut();
                                            progressBar.setVisibility(View.GONE);
                                        } else {
                                            Toast.makeText(AccountActivity.this, getString(R.string.failed_update_password), Toast.LENGTH_SHORT).show();
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    }
                                });
                    }
                } else if (newPassword.getText().toString().trim().equals("")) {
                    newPassword.setError(getString(R.string.enter_password));
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        btnSendResetEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oldEmail.setVisibility(View.VISIBLE);
                newEmail.setVisibility(View.GONE);
                password.setVisibility(View.GONE);
                newPassword.setVisibility(View.GONE);
                changeEmail.setVisibility(View.GONE);
                changePassword.setVisibility(View.GONE);
                sendEmail.setVisibility(View.VISIBLE);
                remove.setVisibility(View.GONE);
            }
        });

        sendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                if (!oldEmail.getText().toString().trim().equals("")) {
                    auth.sendPasswordResetEmail(oldEmail.getText().toString().trim())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(AccountActivity.this, getString(R.string.reset_password_mail_sent), Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);
                                    } else {
                                        Toast.makeText(AccountActivity.this, getString(R.string.reset_password_mail_sent_failed), Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                } else {
                    oldEmail.setError(getString(R.string.enter_email));
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        btnRemoveUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //progressBar.setVisibility(View.VISIBLE);
                new AlertDialog.Builder(AccountActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(getString(R.string.delete_account_popup_title))
                        .setMessage(getString(R.string.delete_account_popup_content))
                        .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                progressBar.setVisibility(View.VISIBLE);
                                auth.signOut();
                                if (user != null) {
                                    user.delete()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(AccountActivity.this, getString(R.string.account_deleted), Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(AccountActivity.this, SignupActivity.class));
                                                        finish();
                                                        progressBar.setVisibility(View.GONE);
                                                    } else {
                                                        Toast.makeText(AccountActivity.this, getString(R.string.account_deleted_failed), Toast.LENGTH_SHORT).show();
                                                        progressBar.setVisibility(View.GONE);
                                                    }
                                                }
                                            });
                                }
                                finish();
                            }
                        })
                        .setNegativeButton(getString(R.string.no), null)
                        .show();
            }
        });

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnPersistGoogle.setSize(SignInButton.SIZE_STANDARD);

        btnPersistGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

    }

    // Persistence d'un compte anonyme avec un compte Google
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                auth.getCurrentUser().linkWithCredential(credential)
                        .addOnCompleteListener(AccountActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(AccountActivity.this, getString(R.string.persist_anonymous_success), Toast.LENGTH_LONG).show();
                                    Log.d("TAG", "linkWithCredential:success");
                                    signOut();
                                    progressBar.setVisibility(View.GONE);
                                } else {
                                    Log.w("TAG", "linkWithCredential:failure", task.getException());
                                    Toast.makeText(AccountActivity.this, getString(R.string.persist_anonymous_fail), Toast.LENGTH_LONG).show();
                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                        });
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("TAG", "Google sign in failed", e);
                // ...
            }
        }

    }

    //sign out method
    public void signOut() {
        auth.signOut();
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }

}
