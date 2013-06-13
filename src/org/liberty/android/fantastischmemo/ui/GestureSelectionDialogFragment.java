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

package org.liberty.android.fantastischmemo.ui;

import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;

import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.domain.Option;
import org.liberty.android.fantastischmemo.utils.AMUiUtil;

import roboguice.fragment.RoboDialogFragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

public class GestureSelectionDialogFragment extends RoboDialogFragment {

    public static final String EXTRA_GESTURE_NAME_DESCRIPTION_MAP = "gesture_name_description_map";

    private AMActivity mActivity;

    private ListView gestureList;

    private CheckBox enableGestureCheckbox;

    private GesturesAdapter gestureAdapter;

    private AMUiUtil amUiUtil;

    private Option option;

    private boolean isOptionChanged = false;

    private Map<String, String> gestureNameDescriptionMap = Collections.emptyMap();

    @Inject
    public void setAmUiUtil(AMUiUtil amUiUtil) {
        this.amUiUtil = amUiUtil;
    }

    @Inject
    public void setOption(Option option) {
        this.option = option;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (AMActivity) activity;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Bundle args = getArguments();
        assert args != null : "The gesture_name_description_map must be passed in";
        gestureNameDescriptionMap = (Map<String, String>) args.getSerializable(EXTRA_GESTURE_NAME_DESCRIPTION_MAP);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.gesture_selection_dialog, container,
                false);
        gestureList = (ListView) v.findViewById(R.id.gesture_list);
        enableGestureCheckbox = (CheckBox) v.findViewById(R.id.enable_gestures);
        enableGestureCheckbox.setChecked(option.getGestureEnabled());
        enableGestureCheckbox.setOnCheckedChangeListener(enableGestureCheckboxChangeListener);

        gestureAdapter= new GesturesAdapter(mActivity);
        gestureList.setAdapter(gestureAdapter);

        GestureLibrary gestureLibrary = GestureLibraries.fromRawResource(
                mActivity, R.raw.gestures);
        gestureLibrary.load();

        for (String gestureEntry : gestureLibrary.getGestureEntries()) {
            for (Gesture gesture : gestureLibrary.getGestures(gestureEntry)) {
                NamedGesture namedGesture = new NamedGesture();
                namedGesture.name = gestureEntry;
                namedGesture.gesture = gesture;
                // Only add the gestures that has description
                // passed from the activity.
                // This can essentially prevent gestures the activity
                // do not want to use.
                if (gestureNameDescriptionMap.containsKey(gestureEntry)) {
                    gestureAdapter.add(namedGesture);
                }
            }
        }

        setStyle(0, STYLE_NO_TITLE);
        Rect displayRectangle = new Rect();
        Window window = mActivity.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);

        v.setMinimumWidth((int)(displayRectangle.width() * 0.9f));

        return v;
    }

    private class GesturesAdapter extends ArrayAdapter<NamedGesture> {
        private final LayoutInflater mInflater;
        private int mThumbnailSize;
        private int mThumbnailInset;
        private int mPathColor;

        public GesturesAdapter(Context context) {
            super(context, 0);
            mInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            // Reference here:
            // https://android.googlesource.com/platform/packages/apps/Launcher/+/1dd3a077a293bf3ac4e61e91dcc3dfc99487acd4/res/values/dimens.xml
            // and here:
            // https://android.googlesource.com/platform/packages/apps/Launcher/+/1dd3a077a293bf3ac4e61e91dcc3dfc99487acd4/res/values/colors.xml
            mPathColor = 0xff0563c1;
            mThumbnailInset = amUiUtil.convertDpToPx(8);
            mThumbnailSize = amUiUtil.convertDpToPx(64);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(
                        R.layout.gesture_selection_list_item, parent, false);
            }

            final NamedGesture gesture = getItem(position);
            final TextView label = (TextView) convertView;

            final Bitmap bitmap = gesture.gesture.toBitmap(mThumbnailSize,
                    mThumbnailSize, mThumbnailInset, mPathColor);
            final BitmapDrawable bitmapDrawable = new BitmapDrawable(
                    mActivity.getResources(), bitmap);

            label.setTag(gesture);
            // label.setText(gesture.name + "hello");
            label.setText(Html.fromHtml("<b>" + gesture.name + "</b><br />"
                        + "<small>" + gestureNameDescriptionMap.get(gesture.name) + "</small>"));
            label.setCompoundDrawablesWithIntrinsicBounds(bitmapDrawable, null,
                    null, null);

            return convertView;
        }
    }

    @Override
    public void onDismiss(DialogInterface di) {
        if (isOptionChanged) {
            mActivity.restartActivity();
        }
    }

    static class NamedGesture {
        String name;
        Gesture gesture;
    }

    private CheckBox.OnCheckedChangeListener enableGestureCheckboxChangeListener = new CheckBox.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                boolean isChecked) {
            option.setGestureEnabled(isChecked);
            isOptionChanged = true;
        }
    };


}
