package ashraf.practice.com.docshare;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.transition.Slide;
import android.view.Gravity;
import android.view.MenuItem;

import com.bumptech.glide.Glide;

import ashraf.practice.com.docshare.Models.User;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserDetailActivity extends AppCompatActivity {

    private CircleImageView userPhoto;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int position = getIntent().getIntExtra("position", 0);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

            getWindow().setAllowEnterTransitionOverlap(false);
            Slide slide = new Slide(Gravity.RIGHT);
            getWindow().setReturnTransition(slide);

        }

        setContentView(R.layout.activity_user_detail);

        userPhoto = (CircleImageView) findViewById(R.id.user_detail_photo);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            userPhoto.setTransitionName("prof" + position);

        }

        User user = (User) getIntent().getSerializableExtra("user");

        Glide.with(this).load(user.getImage()).crossFade().fitCenter().into(userPhoto);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAfterTransition();
        }
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
