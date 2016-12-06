package ashraf.practice.com.docshare;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Ashraf_Patel on 10/22/2016.
 */

public class DocshareApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        /* Initialize Firebase */
        if (!FirebaseApp.getApps(this).isEmpty())
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
