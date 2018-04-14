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

package org.liberty.android.fantastischmemo.common;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.v4.app.Fragment;

import org.liberty.android.fantastischmemo.modules.ActivityComponents;
import org.liberty.android.fantastischmemo.modules.ActivityModules;
import org.liberty.android.fantastischmemo.modules.AppComponents;
import org.liberty.android.fantastischmemo.modules.DaggerAppComponents;
import org.liberty.android.fantastischmemo.modules.FragmentComponents;

import javax.inject.Inject;
import javax.inject.Provider;

public class AMApplication extends Application {

    @Inject Provider<ActivityComponents.Builder> activityComponentsBuilder;

    private static final String TAG = "AMApplication";

    private static Context currentApplicationContext = null;

    private AppComponents appComponents;

    public static Context getCurrentApplicationContext() {
        if (currentApplicationContext == null) {
            throw new NullPointerException("Null application context");
        }

        return currentApplicationContext;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        currentApplicationContext = this;

        appComponents = DaggerAppComponents.builder()
                .application(this)
                .build();
        appComponents.inject(this);
    }


    public AppComponents appComponents() {
        return appComponents;
    }

    public ActivityComponents activityComponents(BaseActivity activity) {
        return activityComponentsBuilder.get()
                .activity(activity)
                .build();
    }

    public FragmentComponents fragmentComponents(ActivityComponents activityComponents, Fragment fragment) {
        return activityComponents.fragmentsComponentsBuilder().get()
                .fragment(fragment)
                .build();

    }
}
