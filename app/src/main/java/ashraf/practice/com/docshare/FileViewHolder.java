package ashraf.practice.com.docshare;

import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;

import ashraf.practice.com.docshare.Models.User;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Ashraf_Patel on 10/30/2016.
 */

public class FileViewHolder extends RecyclerView.ViewHolder {
    private final TextView filesizetv;
    private TextView fileTime;
    private TextView fileTitle;
    private TextView fileDesc;
    private LinearLayout mExpandableView;
    View mView;
    Button mDownloadBtn;
    private ImageButton mExpandBtn;
    private String mUid;
    private String mFileName;
    String mFileExtension;
    private CardView mCardView;
    String mAuthorName;
    String mAuthorPhoto;
    TextView fileAuthor;
    CircleImageView mPostAuthorIcon;


    public FileViewHolder(View itemView) {
        super(itemView);

        mView = itemView;
        mDownloadBtn = (Button) mView.findViewById(R.id.btnDownload);
        mExpandBtn = (ImageButton) mView.findViewById(R.id.expandbtn);
        mExpandableView = (LinearLayout) mView.findViewById(R.id.expandable_view);
        mCardView = (CardView) mView.findViewById(R.id.card);
        mPostAuthorIcon = (CircleImageView) mView.findViewById(R.id.post_author_icon);
        fileTitle = (TextView) mView.findViewById(R.id.tvtitle);
        fileDesc = (TextView) mView.findViewById(R.id.tvdesc);
        fileTime = (TextView) mView.findViewById(R.id.tvcreatedAt);
        filesizetv = (TextView) mView.findViewById(R.id.file_size);

        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visibilityBehaviour();
            }
        });

    }


    public void setTitle(String title) {
        fileTitle.setText(title);
    }

    public void setDesc(String desc) {
        fileDesc.setText(desc);

    }

    @NonNull
    public void setUser(String uid) {

        fileAuthor = (TextView) mView.findViewById(R.id.tvauthor);
        this.mUid = uid;
        DatabaseReference userDbRef = FirebaseDatabase.getInstance().getReference("users").child(mUid);

        userDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                User user = dataSnapshot.getValue(User.class);
                mAuthorName = user.getName();
                mAuthorPhoto = user.getImage();
                fileAuthor.setText(mAuthorName);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Main Activity", "loadname:onCancelled", databaseError.toException());
            }
        });

    }

    @NonNull
    public void setAuthorPhoto(String uid) {

        fileAuthor = (TextView) mView.findViewById(R.id.tvauthor);
        this.mUid = uid;
        DatabaseReference userDbRef = FirebaseDatabase.getInstance().getReference("users").child(mUid);

        userDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                User user = dataSnapshot.getValue(User.class);
                ColorDrawable cd = new ColorDrawable(ContextCompat.getColor(mPostAuthorIcon.getContext(), R.color.blue_grey_500));
                Glide.with(mPostAuthorIcon.getContext())
                        .load(user.getImage())
                        .placeholder(cd)
                        .crossFade()
                        .centerCrop()
                        .into(mPostAuthorIcon);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Main Activity", "loadname:onCancelled", databaseError.toException());
            }
        });

//        mPostAuthorIcon.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                Intent userDetailIntent = new Intent(v.getContext(), UserDetailActivity.class);
//                userDetailIntent.putExtra("user", new User(mAuthorName, mAuthorPhoto));
//                mView.getContext().startActivity(userDetailIntent);
//
//            }
//        });
    }


    public void setTime(String time) {

        fileTime.setText(time);

    }

    @NonNull
    public void setmFileExtension(String mFileExtension) {
        this.mFileExtension = mFileExtension;
        ImageButton extnsionBtn = (ImageButton) mView.findViewById(R.id.extensionBtn);
    }

    public void setFileName(String filename) {
        mFileName = filename;
        TextView filenameTv = (TextView) mView.findViewById(R.id.file_name);
        filenameTv.setText(getFullFileName());
    }

    public String getFullFileName() {
        return mFileName + "." + mFileExtension;
    }

    public void setFileSize(String fileSize) {
        filesizetv.setText(fileSize);
    }

    public void setupDownloadBtnText() {

        File file = new File(Environment.getExternalStorageDirectory() + "/DocShare", getFullFileName());
        if (file.exists()) {
            mDownloadBtn.setText("Open");

        } else {

            mDownloadBtn.setText("Downlaod");

        }
    }


    public void visibilityBehaviour() {
        if (mExpandableView.getVisibility() == View.GONE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                TransitionManager.beginDelayedTransition(mCardView);
            }
            mExpandableView.setVisibility(View.VISIBLE);
            mExpandBtn.setBackgroundResource(R.drawable.ic_keyboard_arrow_up_black_24dp);

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                TransitionManager.beginDelayedTransition(mCardView);
            }
            mExpandableView.setVisibility(View.GONE);
            mExpandBtn.setBackgroundResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
        }
    }

}
