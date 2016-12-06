package ashraf.practice.com.docshare;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class SetupActivity extends AppCompatActivity {

    private static final int GALLERY_REQUEST = 1;
    private EditText mSetupNameField;
    private ImageButton mSetupProfileBtn;
    private Button mFinalSetupBtn;
    private ProgressDialog mProgressDialog;

    private DatabaseReference mUserReferrence;
    private FirebaseAuth mAuth;
    private StorageReference mStorage;

    private Uri mImageCaptureUri;

    private AlertDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mUserReferrence = FirebaseDatabase.getInstance().getReference().child("users");

        mStorage = FirebaseStorage.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();


        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);

        mSetupNameField = (EditText) findViewById(R.id.setupNameField);
        mSetupProfileBtn = (ImageButton) findViewById(R.id.prfilePicBtn);

        mFinalSetupBtn = (Button) findViewById(R.id.finishSetupBtn);
        mFinalSetupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finalSetup();
            }
        });


        mSetupProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);

            }
        });
    }


    private void finalSetup() {
        final String name = mSetupNameField.getText().toString().trim();
        final String user_id = mAuth.getCurrentUser().getUid();

        if (user_id != null) {

            if (!TextUtils.isEmpty(name) && mImageCaptureUri != null) {
                mProgressDialog.setMessage("Finishing Setup...");
                mProgressDialog.show();

                StorageReference profileStorage = mStorage.child("profile_pics");

                profileStorage.child(user_id).putFile(mImageCaptureUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                Log.i("SetupActivity", "Profile Pic Storing successfull");
                                Log.i("SetupActivity", "profile pic url :" + taskSnapshot.getDownloadUrl());
                                DatabaseReference currentUser = mUserReferrence.child(user_id);

                                currentUser.child("name").setValue(name);
                                currentUser.child("image").setValue(taskSnapshot.getMetadata().getDownloadUrl().toString());

                                mProgressDialog.dismiss();

                                Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
                                mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(mainIntent);

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Log.e("SetupActivity", "Profile Pic Storing Failed, Reason" + e.toString());
                                Toast.makeText(SetupActivity.this, "Error Setuping profile photo", Toast.LENGTH_LONG).show();
                                mProgressDialog.dismiss();
                            }
                        });

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mImageCaptureUri = result.getUri();
                mSetupProfileBtn.setImageURI(result.getUri());

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();

                Log.e("SetupActivity", "Profile Pic Storing Failed, Reason" + error
                        .toString());
            }
        }
    }
}
