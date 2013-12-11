/*
Copyright (C) 2012 Haowen Ning

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/

package org.liberty.android.fantastischmemo;

import roboguice.RoboGuice;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.util.Log;

//import org.acra.ACRA;

//import org.acra.annotation.ReportsCrashes;


//@ReportsCrashes(formKey = "dGhuTDRGUVY1WjA3RG9NNEFmSTFEaXc6MQ")
public class AMApplication extends Application {

    private static final String TAG = "AMApplication";

    private static Context currentApplicationContext = null;

    public static Context getCurrentApplicationContext() {
        assert currentApplicationContext != null : "Null application context!";

        return currentApplicationContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        currentApplicationContext = this;

        // The following line triggers the initialization of ACRA
        //ACRA.init(this);

        // HTTP connection reuse which was buggy pre-froyo

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            Log.w(TAG, "Using version less than 2.2, disable urlconnection connection pool");
            System.setProperty("http.keepAlive", "false");
        }

        // Handle customized module binding
        RoboGuice.setBaseApplicationInjector(this, RoboGuice.DEFAULT_STAGE,
                RoboGuice.newDefaultRoboModule(this), new AMModules());
    }
}
