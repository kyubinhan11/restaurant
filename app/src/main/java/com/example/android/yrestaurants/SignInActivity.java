package com.example.android.yrestaurants;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";
    private CallbackManager mCallbackManager;
    private FirebaseAuth mAuth;
    private EditText emailInput;
    private EditText passwordInput;
    private TextView statusTextView;
    private ProgressDialog signInProgressDialog;
    private TextView signUpTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize user interfaces
        emailInput = (EditText) findViewById(R.id.input_email);
        passwordInput = (EditText) findViewById(R.id.input_password);
        statusTextView = (TextView) findViewById(R.id.status_text_sign_in);
        signUpTextView = (TextView) findViewById(R.id.sign_up_text);
        signUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start signUpActivity

                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        // Initialize email and password button
        Button signInWithEmailAndPasswordLoginButton = (Button) findViewById(R.id.button_login);
        signInWithEmailAndPasswordLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start a sign-in process with given email and password
                signInWithEmailAndPassword(emailInput.getText().toString(),
                        passwordInput.getText().toString());

            }
        });

        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        LoginButton FBLoginButton = (LoginButton) findViewById(R.id.button_facebook_login);
        FBLoginButton.setReadPermissions("email", "public_profile");
        FBLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
//                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());

            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");

            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);

            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    private void signInWithEmailAndPassword(String email, String password) {
        // Make sure the email and password are not empty
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
            String nullInput = "Please make sure to fill the email or password";
            statusTextView.setText(nullInput);
            return;
        }

        signInProgressDialog = ProgressDialog.show(this,
                "", "Signing in...");

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            String authFailed = "Please check your email or password";
                            statusTextView.setText(authFailed);
                            signInProgressDialog.dismiss();
                        }
                        else {
                            // sign in is successful!
                            // Set result and finish this Activity
                            setResult(Activity.RESULT_OK, null);
                            signInProgressDialog.dismiss();
                            finish();
                        }


                    }
                });
        // [END sign_in_with_email]
    }


    private void handleFacebookAccessToken(AccessToken token) {
        signInProgressDialog = ProgressDialog.show(this,
                "", "Signing in...");

//        Log.d(TAG, "handleFacebookAccessToken:" + token);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            String authFailed = "Please check your Facebook account";
                            statusTextView.setText(authFailed);
                            signInProgressDialog.dismiss();
                        } else {
                            //sign in is successful!
                            // Set result and finish this Activity
                            setResult(Activity.RESULT_OK, null);
                            signInProgressDialog.dismiss();
                            finish();
                        }
                    }
                });
    }
}
