package ashraf.practice.com.docshare;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import ashraf.practice.com.docshare.Fragments.NewUploadTaskFragment;

public class UploadActivity extends AppCompatActivity implements NewUploadTaskFragment.TaskCallbacks {
    private static final String TAG = UploadActivity.class.getSimpleName();
    private static final int PICK_IMAGE_REQUEST = 1;
    private EditText mTitleField, mDescField;
    private Button mUploadBtn;
    private ProgressDialog mProgressDialog;
    private Uri mUri;
    private StorageReference mStorageReference;
    private DatabaseReference mDatabaseReference;
    private FirebaseAuth mAuth;

    private CardView cardView;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Uri mSessionUri;
    private boolean saved;
    private NewUploadTaskFragment mTaskFragment;
    private String TAG_TASK_FRAGMENT = "newUploadTaskFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            getWindow().setEnterTransition(new Slide(Gravity.RIGHT));
            getWindow().setAllowEnterTransitionOverlap(false);

        }

        mStorageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://docshare-84937.appspot.com");
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("files");


        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Intent loginIntent = new Intent(UploadActivity.this, LoginActivity.class);
                    loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }
            }
        };

        mProgressDialog = new ProgressDialog(this);

        cardView = (CardView) findViewById(R.id.cardview);

        mTitleField = (EditText) findViewById(R.id.titleField);
        mDescField = (EditText) findViewById(R.id.descField);
        mUploadBtn = (Button) findViewById(R.id.uploadBtn);


        mUploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFileChooser();
            }
        });

        // find the retained fragment on activity restarts
        FragmentManager fm = getSupportFragmentManager();
        mTaskFragment = (NewUploadTaskFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);

        // create the fragment and data the first time
        if (mTaskFragment == null) {
            // add the fragment
            mTaskFragment = new NewUploadTaskFragment();
            fm.beginTransaction().add(mTaskFragment, TAG_TASK_FRAGMENT).commit();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAfterTransition();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAuth.removeAuthStateListener(mAuthListener);

    }

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
//            mUri = data.getData();
            Log.d(TAG, "File Path" + data.getData().toString());

            firebaseUpload(data.getData());
        }
    }

    private void firebaseUpload(Uri uri) {
        mUri = uri;
        Log.d(TAG, "Entered into the firebaseUpload() method");

        final String title_val = mTitleField.getText().toString().trim();
        final String desc_val = mDescField.getText().toString().trim();
        if (!TextUtils.isEmpty(title_val) && !TextUtils.isEmpty(desc_val) && mUri != null) {

//            showProgressDialog(null,"uploading File");
//            StorageReference filepath = mStorageReference.child("files").child(mUri.getLastPathSegment());
//            filepath.putFile(mUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                    if (mAuth.getCurrentUser().getUid() != null) {
//                        StorageMetadata meta = taskSnapshot.getMetadata();
//
//
//                        Log.i(TAG, "getExtension =" + getMimeType(UploadActivity.this, mUri));
//
//                        DatabaseReference newEntry = mDatabaseReference.push();
//                        newEntry.child("title").setValue(title_val);
//                        newEntry.child("desc").setValue(desc_val);
//                        newEntry.child("user").setValue(mAuth.getCurrentUser().getUid());
//                        newEntry.child("file_url").setValue(meta.getDownloadUrl().toString());
//                        newEntry.child("file_name").setValue(meta.getName());
//                        newEntry.child("created_at").setValue(convertTime(meta.getCreationTimeMillis()));
//                        newEntry.child("size").setValue(String.format("%.2f", (float) (meta.getSizeBytes()) / 1024 / 1024));
//                        newEntry.child("extension").setValue(getMimeType(UploadActivity.this, mUri));
//
//                        mProgressDialog.dismiss();
//                        Toast.makeText(UploadActivity.this, "File uploaded", Toast.LENGTH_LONG).show();
//
//                        Intent mainIntent = new Intent(UploadActivity.this, MainActivity.class);
//                        mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        startActivity(mainIntent);
//
//                    }
//                }
//            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
//                    mSessionUri = taskSnapshot.getUploadSessionUri();
//                    if (mSessionUri != null && !saved) {
//                        saved = true;
//                        Log.i(TAG, "onProgress  " + "Upload Resued");
//                    }
//                    long totalBytes = taskSnapshot.getTotalByteCount();
//                    long byteTransferred = taskSnapshot.getBytesTransferred();
//                    mProgressDialog.setMessage("" + (int) (byteTransferred * 100 / (float) totalBytes) + "%");
//                }
//            });
            showProgressDialog("Please Wait","Uploading File");
            mTaskFragment.uploadFile(mUri,title_val,desc_val,mAuth.getCurrentUser().getUid());
        }
        else {
            if (title_val.isEmpty()) {
                mTitleField.setError("this cannot be empty");
            }
            if (desc_val.isEmpty()) {
                mDescField.setError("this cannot be empty");
            }
            if (mUri == null) {
                Toast.makeText(this, "Invalid File Location:", Toast.LENGTH_LONG);
            }
        }
    }

    public String convertTime(long time) {
        Date date = new Date(time);
        Format format = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
        return format.format(date);
    }

    public static String getMimeType(Context context, Uri uri) {
        String extension;
        //Check uri format to avoid null
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //If scheme is a content
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());

        }

        return extension;
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mSessionUri != null && saved)
            firebaseUpload(mSessionUri);
    }

    @Override
    public void onUploaded(final String error) {
        UploadActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mUploadBtn.setEnabled(true);
                dismissProgressDialog();
                if (error == null) {
                    Toast.makeText(UploadActivity.this, "File Uploaded", Toast.LENGTH_SHORT).show();
                    finish();
                    Intent mainIntent = new Intent(UploadActivity.this, MainActivity.class);
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);
                } else {
                    Toast.makeText(UploadActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void dismissProgressDialog() {
        mProgressDialog.dismiss();
    }

    private void showProgressDialog(String title, String message) {
        mProgressDialog.setTitle(title);
        mProgressDialog.setMessage(message);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
