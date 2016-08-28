package org.liberty.android.fantastischmemo.modules;

import android.support.v4.app.Fragment;

import dagger.Module;
import dagger.Provides;

@Module
public class FragmentModules {
    private final Fragment fragment;
    public FragmentModules(Fragment fragment) {
        this.fragment = fragment;
    }

    @Provides
    @PerFragment
    Fragment providesFragment() {
        return fragment;
    }
}
