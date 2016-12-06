package ashraf.practice.com.docshare.Fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import ashraf.practice.com.docshare.Models.DocInfoModel;

/**
 * Created by Ashraf_Patel on 10/28/2016.
 */

public class NewUploadTaskFragment extends Fragment {

    private static final String TAG = "NewUploadTaskFragment";

    public interface TaskCallbacks {

        void onUploaded(String error);

    }

    private Context mApplicationContext;
    private TaskCallbacks mCallbacks;


    public NewUploadTaskFragment() {

    }

    public static NewUploadTaskFragment newInstance() {
        return new NewUploadTaskFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retain this fragment across config changes.
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof TaskCallbacks) {
            mCallbacks = (TaskCallbacks) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement TaskCallbacks");
        }
        mApplicationContext = context.getApplicationContext();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }


    public void uploadFile(Uri inUri, String inTitle, String inDesc, String inUser) {

        new UploadFileTask(inUri, inTitle, inDesc, inUser).execute();

    }

    class UploadFileTask extends AsyncTask<Void, Void, Void> {

        private Uri mUri;
        private String title;
        private String desc;
        private String user;


        public UploadFileTask(Uri inUri, String inTitle, String inDesc, String inUser) {

            mUri = inUri;
            title = inTitle;
            desc = inDesc;
            user = inUser;

        }

        @Override
        protected Void doInBackground(Void... params) {
            if (mUri == null || title == null || desc == null || user == null)
                return null;
            StorageReference mStorageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://docshare-84937.appspot.com");
            final StorageReference filepath = mStorageReference.child("files").child(mUri.getLastPathSegment());

            filepath.putFile(mUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.i(TAG,"Successfully uploaded file");
                    StorageMetadata meta = taskSnapshot.getMetadata();

                    DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("files");
                    DatabaseReference newEntry = mDatabaseReference.push();

                    if (user == null) {
                        mCallbacks.onUploaded("Error: user is not signed in");
                        return;
                    }

                    DocInfoModel newFile = new DocInfoModel(
                            title,
                            desc,
                            user,
                            meta.getDownloadUrl().toString(),
                            meta.getName(),
                            convertTime(meta.getCreationTimeMillis()),
                            convertSize(meta.getSizeBytes()),
                            getMimeType(mApplicationContext, mUri));

                    newEntry.setValue(newFile).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i(TAG,"Suceessfully pushed Database entry");
                            mCallbacks.onUploaded(null);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            filepath.delete();
                            Log.e(TAG,"error in pushing file details " + e.toString());
                            mCallbacks.onUploaded("Sorry! Server problem Please Try Again");
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG,"error in uploadig file " + e.toString());
                    mCallbacks.onUploaded("Error: Please try again.Check file Again");
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                    long totalBytes = taskSnapshot.getTotalByteCount();
                    long byteTransferred = taskSnapshot.getBytesTransferred();
                    Log.i(TAG,"" + (int) (byteTransferred * 100 / (float) totalBytes) + "%");
                }
            });
            return null;
        }
    }

    private String convertSize(long sizeBytes) {
        return String.format("%.2f", (float) (sizeBytes) / 1024 / 1024);
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

}
