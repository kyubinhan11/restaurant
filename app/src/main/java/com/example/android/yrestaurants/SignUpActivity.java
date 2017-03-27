package com.example.android.yrestaurants;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";
    private static final String NULL_INPUT = "Please make sure filling in all the black";
    private static final String SHORT_LENGTH = "the letter has to be at least 7 characters";
    private static final String NO_MATCH = "Password does not match";
    private static final int GET_FROM_GALLERY = 1;

    private EditText emailInput;
    private EditText nameInput;
    private EditText passwordInput;
    private EditText passwordComfirmInput;
    private Button signUpButton;
    private TextView statusTextView;
    private TextView backToSignIn;
    private ImageView profileImageView;

    private FirebaseAuth mAuth;
    private ProgressDialog signUpProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize user interfaces
        emailInput = (EditText) findViewById(R.id.input_email_sign_up);
        nameInput = (EditText) findViewById(R.id.input_name_sign_up);
        passwordInput = (EditText)findViewById(R.id.input_password_sign_up);
        passwordComfirmInput = (EditText) findViewById(R.id.input_password_check_sign_up);
        passwordComfirmInput.setEnabled(false); // Initially disabled
        statusTextView = (TextView) findViewById(R.id.status_text_sign_up);
        signUpButton = (Button) findViewById(R.id.button_sign_up);
        backToSignIn = (TextView) findViewById(R.id.go_back_to_sign_in);
        profileImageView = (ImageView) findViewById(R.id.profile_picture_sign_up);
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
            }
        });

        backToSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // Back to SignInActivity
            }
        });


        passwordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                String password = passwordInput.getText().toString();

                // Make sure the password is at least 7 characters long
                if(password.length() <= 6){
                    statusTextView.setText(SHORT_LENGTH);
                } else {
                    passwordComfirmInput.setEnabled(true);
                    statusTextView.setText("");
                }
            }
        });

        passwordComfirmInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                // Tell users that whether the passwords match
                String password = passwordInput.getText().toString();
                String passwordConfirm = passwordComfirmInput.getText().toString();

                if(!password.equals(passwordConfirm)){
                    statusTextView.setText(NO_MATCH);
                } else {
                    statusTextView.setText("");
                }
            }
        });



        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailInput.getText().toString();
                String password = passwordInput.getText().toString();
                String passwordConfirm = passwordComfirmInput.getText().toString();
                String userName = nameInput.getText().toString();
                String photoUrl = "";

                if (TextUtils.isEmpty(photoUrl)){
                    photoUrl = "/images/profile_placeholder.png";
                }

                // Check the email, password, and user nam are not empty
                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)|| TextUtils.isEmpty(userName)
                       || TextUtils.isEmpty(passwordConfirm)){
                    statusTextView.setText(NULL_INPUT);
                    return;
                }
                // Check the length of the password
                if(password.length() <= 6){
                    statusTextView.setText(SHORT_LENGTH);
                    return;
                }

                // Check that the passwords match
                if(!password.equals(passwordConfirm)){
                    statusTextView.setText(NO_MATCH);
                    return;
                }

                // Finally start creating an user account
                createAccount(email, password, userName, photoUrl);
            }
        });
    }


    private void createAccount(String email, String password, final String userName, final String photoUrl) {
        signUpProgressDialog = ProgressDialog.show(this,
                "", "Signing Up...");

        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            String authFailed = "Sign up was unsuccessful";
                            statusTextView.setText(authFailed);
                            signUpProgressDialog.dismiss();
                        } else {
                            // sign-up is successful!

                            // upload the image to the Firebase storage and
                            // update user's display name and profile photo
                            uploadImageFromDataInMemory(userName);

                            // Set result and finish this Activity
                            setResult(Activity.RESULT_OK, null);
                            signUpProgressDialog.dismiss();
                            finish();
                        }
                    }
                });
        // [END create_user_with_email]
    }

    // https://firebase.google.com/docs/storage/android/upload-files?authuser=0
    private void uploadImageFromDataInMemory(final String userName){
        // Get the current user's information reference
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Get the current user's storage reference
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        // Set an unique reference for the profile image of each user
        StorageReference currUserStorageRef = storageRef.child(user.getUid() + "/profile_image");

        // Get the data from an ImageView as bytes
        profileImageView.setDrawingCacheEnabled(true);
        profileImageView.buildDrawingCache();
        Bitmap bitmap = profileImageView.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = currUserStorageRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                // http://stackoverflow.com/questions/41105586/android-firebase-tasksnapshot-method-should-only-be-accessed-within-privat
                @SuppressWarnings("VisibleForTests") Uri userPhotoUrl = taskSnapshot.getDownloadUrl();
                updateUserPictureAndName(userPhotoUrl, userName);
            }
        });
    }

    // https://firebase.google.com/docs/auth/android/manage-users?authuser=0#update_a_users_profile
    private void updateUserPictureAndName(Uri picUrl, String userName){
        // get the current user's information reference
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(userName)
                .setPhotoUri(picUrl)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated.");
                        }
                    }
                });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Detects request codes
        if(requestCode==GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                profileImageView.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "FileNotFoundException " + e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, "IOException " + e.getMessage());
            }
        }
    }
}
