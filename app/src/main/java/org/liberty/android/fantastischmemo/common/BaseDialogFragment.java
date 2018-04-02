package org.liberty.android.fantastischmemo.common;

import android.support.v4.app.DialogFragment;

import org.liberty.android.fantastischmemo.modules.ActivityComponents;
import org.liberty.android.fantastischmemo.modules.AppComponents;
import org.liberty.android.fantastischmemo.modules.FragmentComponents;

public class BaseDialogFragment extends DialogFragment {
    private FragmentComponents fragmentComponents;

    public AppComponents appComponents() {
        return ((AMApplication) getActivity().getApplication()).appComponents();
    }

    public ActivityComponents activityComponents() {
        if (getActivity() == null) {
            return null;
        }

        return ((BaseActivity) getActivity()).activityComponents();
    }

    public FragmentComponents fragmentComponents() {
        if (getActivity() == null) {
            return null;
        }

        if (fragmentComponents == null) {
            fragmentComponents = ((AMApplication) getActivity().getApplication()).fragmentComponents(activityComponents(), this);
        }

        return fragmentComponents;
    }
}
