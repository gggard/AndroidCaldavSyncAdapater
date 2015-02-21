package de.jdevel.acaldav;

import android.app.Application;
import android.content.Context;

/**
 * @author Joseph Weigl
 */
public class App extends Application {

    private static Context mContext;

    public App() {
        super();
        App.mContext = this;
    }

    public static Context getContext() {
        return mContext;
    }

    public static void setContext(Context context) {
        mContext = context;
    }
}
