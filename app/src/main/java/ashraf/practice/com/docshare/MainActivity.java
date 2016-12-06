package ashraf.practice.com.docshare;

import android.app.ActivityOptions;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Explode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;

import ashraf.practice.com.docshare.Models.DocInfoModel;
import ashraf.practice.com.docshare.Models.User;

public class MainActivity extends AppCompatActivity {
    FloatingActionButton fab;

    private DatabaseReference mFilesReference;
    private DatabaseReference mDatabaseUsers;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private RecyclerView mRecyclerView;

    private ProgressDialog mProgressDialog;

    private File mCurrentFile;
    private final String TAG = MainActivity.class.getSimpleName();
    private LinearLayoutManager mManager;
    private FirebaseRecyclerAdapter<DocInfoModel, FileViewHolder> mRecyclerViewAdapter;
    private StorageReference mStorageReference;

    private long enqueue;
    private DownloadManager dm;
    private User mUser;
    private BroadcastReceiver mReceiver;
    private ValueEventListener mConnectedListener;
    private ActionBar toolBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {


            getWindow().setExitTransition(new Explode().excludeChildren(getSupportActionBar().getCustomView(),true));

        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        createDir();
        attachDownloadManager();


        mFilesReference = FirebaseDatabase.getInstance().getReference("files");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference("users");


        mFilesReference.keepSynced(true);
        mDatabaseUsers.keepSynced(true);

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
                    Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                    loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }
            }
        };


        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fab.setTransitionName("fab");
        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent uploadIntent = new Intent(MainActivity.this, UploadActivity.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this, fab, fab.getTransitionName()).toBundle();
                    startActivity(uploadIntent, bundle);
                } else
                    startActivity(uploadIntent);
//                overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_enter);
            }
        });

        Log.i(TAG, " " + fab.getX() + fab.getY());

        mRecyclerView = (RecyclerView) findViewById(R.id.availableDocsRecyclerView);

        mManager = new LinearLayoutManager(this);

        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(mManager);


        checkUserExist();

        mAuth.addAuthStateListener(mAuthListener);

        attachRecyclerView();

    }

    private void attachDownloadManager() {

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    long downloadId = intent.getLongExtra(
                            DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                    Query query = new Query();
                    query.setFilterById(enqueue);
                    Cursor c = dm.query(query);
                    if (c.moveToFirst()) {
                        int columnIndex = c
                                .getColumnIndex(DownloadManager.COLUMN_STATUS);
                        if (DownloadManager.STATUS_SUCCESSFUL == c
                                .getInt(columnIndex)) {
                            Intent i = new Intent();
                            i.setAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
                            startActivity(i);
                        }
                    }
                }
            }
        };

        registerReceiver(mReceiver, new IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    public void onStart() {
        super.onStart();
//
//        checkUserExist();
//
//        mAuth.addAuthStateListener(mAuthListener);
//
//        attachRecyclerView();

        mConnectedListener = mFilesReference.getRoot().child(".info/connected").addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = (Boolean) dataSnapshot.getValue();

                final Snackbar snackbar = Snackbar.make(findViewById(R.id.availableDocsRecyclerView),"No Internet",Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction("dismiss", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                    }
                });

                if (connected) {

                    snackbar.dismiss();
                    Log.d(TAG,"connected");

                } else {

                    Log.d(TAG,"DisConnected");
                    snackbar.show();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }

    private void attachRecyclerView() {

      mRecyclerViewAdapter = new FirebaseRecyclerAdapter<DocInfoModel, FileViewHolder>(
                DocInfoModel.class,
                R.layout.recycler_card,
                FileViewHolder.class,
                mFilesReference
        ) {
            @Override
            protected void populateViewHolder(final FileViewHolder viewHolder, final DocInfoModel model, final int position) {

                viewHolder.setTitle(model.getTitle());
                viewHolder.setDesc(model.getDesc());
                viewHolder.setTime(model.getCreated_at());
                viewHolder.setmFileExtension(model.getExtension());
                viewHolder.setUser(model.getUser());
                viewHolder.setAuthorPhoto(model.getUser());
                viewHolder.setFileName(model.getFile_name());
                viewHolder.setFileSize(model.getSize());
                viewHolder.setupDownloadBtnText();

                viewHolder.mDownloadBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        File file = new File(Environment.getExternalStorageDirectory() + "/DocShare", viewHolder.getFullFileName());
                        if (!fileExists(file)) {

                            Toast.makeText(MainActivity.this, "Downloading file...", Toast.LENGTH_LONG).show();
                            downloadFile(model.getFile_url(), viewHolder.getFullFileName());

                        } else {

                            Toast.makeText(MainActivity.this, file.getPath() + "Opening File", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            MimeTypeMap mime = MimeTypeMap.getSingleton();
                            String fileExtension = viewHolder.mFileExtension;
                            String type = mime.getMimeTypeFromExtension(fileExtension);
                            intent.setDataAndType(Uri.fromFile(file), type);
                            startActivity(intent);
                        }
                    }
                });

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    viewHolder.mPostAuthorIcon.setTransitionName("prof" + position);
                }

                viewHolder.mPostAuthorIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent userDetailIntent = new Intent(v.getContext(), UserDetailActivity.class);
                        userDetailIntent.putExtra("user", new User(viewHolder.mAuthorName, viewHolder.mAuthorPhoto));
                        userDetailIntent.putExtra("position", position);

                        startActivity(userDetailIntent);

                    }
                });


            }

        };

//        // Scroll to bottom on new messages
//        mRecyclerViewAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
//            @Override
//            public void onItemRangeInserted(int positionStart, int itemCount) {
//                mManager.smoothScrollToPosition(mRecyclerView, null, mRecyclerViewAdapter.getItemCount());
//            }
//        });
        mRecyclerView.setAdapter(mRecyclerViewAdapter);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) {
                    // Scroll Down
                    if (fab.isShown()) {
                        fab.hide();
                    }
                } else if (dy < 0) {
                    // Scroll Up
                    if (!fab.isShown()) {
                        fab.show();
                    }
                }
            }
        });
    }

    private boolean fileExists(File file) {
        if (file.exists())
            return true;
        else
            return false;

    }

    private void downloadFile(String url, String file_name) {
        dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        Request request = new Request(Uri.parse(url));
        request.setDestinationInExternalPublicDir("/DocShare", file_name);

        enqueue = dm.enqueue(request);
    }


    @Override
    public void onStop() {
        super.onStop();
        if (mRecyclerViewAdapter != null) {
            mRecyclerViewAdapter.cleanup();
        }
        mFilesReference.removeEventListener(mConnectedListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAuth != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        if (mReceiver != null)
            unregisterReceiver(mReceiver);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
        }
        if (item.getItemId() == R.id.action_profile) {
            Intent profileIntent = new Intent(MainActivity.this, SetupActivity.class);
            profileIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(profileIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        mAuth.signOut();
    }


    private void checkUserExist() {
        if (mAuth.getCurrentUser() != null) {
            final String user_id = mAuth.getCurrentUser().getUid();

            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (!dataSnapshot.hasChild(user_id)) {

                        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                        setupIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(setupIntent);

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }


    //Creating Directory DocShare
    private void createDir() {
        Boolean success = null;
        File dir = new File(Environment.getExternalStorageDirectory() + "/DocShare");
        if (!dir.exists()) {
            success = dir.mkdir();
        } else
            Log.d("Main Activity", "Creating Directory result" + success);
    }


}