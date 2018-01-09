package partha.firebasechatdemo.utils;

import android.os.StrictMode;
import android.support.multidex.MultiDexApplication;

/**
 * Created by Partha Chatterjee on 4/21/2017.
 */




public class TrackAppliation extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        //Allowing Strict mode policy for Nougat support
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

//        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

}
