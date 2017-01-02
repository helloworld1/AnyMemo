package org.liberty.android.fantastischmemo.common;

import android.support.v4.app.Fragment;

import org.liberty.android.fantastischmemo.modules.ActivityComponents;
import org.liberty.android.fantastischmemo.modules.AppComponents;
import org.liberty.android.fantastischmemo.modules.DaggerFragmentComponents;
import org.liberty.android.fantastischmemo.modules.FragmentComponents;
import org.liberty.android.fantastischmemo.modules.FragmentModules;

public class BaseFragment extends Fragment {
    private FragmentComponents fragmentComponents;

    public AppComponents appComponents() {
        return ((AMApplication) getActivity().getApplication()).appComponents();
    }

    public ActivityComponents activityComponents() {
        return ((BaseActivity) getActivity()).activityComponents();
    }

    public FragmentComponents fragmentComponents() {
        if (fragmentComponents == null) {
            fragmentComponents = DaggerFragmentComponents.builder()
                    .activityComponents(activityComponents())
                    .fragmentModules(new FragmentModules(this))
                    .build();
        }
        return fragmentComponents;
    }
}
