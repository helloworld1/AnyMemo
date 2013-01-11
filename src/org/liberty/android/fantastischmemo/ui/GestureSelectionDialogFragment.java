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

import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.domain.Option;
import org.liberty.android.fantastischmemo.utils.AMUiUtil;

import android.app.Activity;
import android.content.Context;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

public class GestureSelectionDialogFragment extends DialogFragment {

    private static final String TAG = "GestureSelectionDialogFragment";

    private AMActivity mActivity;

    private ListView gestureList;

    private CheckBox enableGestureCheckbox;

    private GesturesAdapter gestureAdapter;

    private AMUiUtil amUiUtil;

    private Option option;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (AMActivity) activity;
        amUiUtil = new AMUiUtil(mActivity);
        option = new Option(mActivity);
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

        GesturesAdapter adapter = new GesturesAdapter(mActivity);
        gestureList.setAdapter(adapter);

        GestureLibrary gestureLibrary = GestureLibraries.fromRawResource(
                mActivity, R.raw.gestures);
        gestureLibrary.load();

        for (String gestureEntry : gestureLibrary.getGestureEntries()) {
            for (Gesture gesture : gestureLibrary.getGestures(gestureEntry)) {
                NamedGesture namedGesture = new NamedGesture();
                namedGesture.name = gestureEntry;
                namedGesture.gesture = gesture;
                adapter.add(namedGesture);
            }
        }

        setStyle(0, STYLE_NO_TITLE);

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
            label.setText(gesture.name);
            label.setCompoundDrawablesWithIntrinsicBounds(bitmapDrawable, null,
                    null, null);

            return convertView;
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
            option.setGestureEnabled(true);
        }
    };
}
