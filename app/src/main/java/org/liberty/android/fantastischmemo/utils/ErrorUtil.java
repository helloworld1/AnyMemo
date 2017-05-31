package org.liberty.android.fantastischmemo.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.common.base.Throwables;

import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.common.BaseActivity;
import org.liberty.android.fantastischmemo.modules.PerActivity;

import javax.inject.Inject;

@PerActivity
public class ErrorUtil {

    private final Activity activity;

    private final String tag;

    @Inject
    public ErrorUtil(BaseActivity activity) {
        this.activity = activity;
        tag = activity.getClass().getSimpleName();
    }


    // Display unrecoverable exception and exit the activity
    public void showFatalError(@Nullable final String text, @Nullable final Throwable e){
        Log.e(tag, text, e);

        new AlertDialog.Builder(activity)
                .setTitle(R.string.error_text)
                .setMessage(getErrorString(text, e))
                .setPositiveButton(activity.getString(R.string.back_menu_text), new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface arg0, int arg1){
                        activity.finish();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener(){
                    public void onCancel(DialogInterface dialog){
                        activity.finish();
                    }
                })
                .show();
    }

    // Display recoverable exception
    public void showNonFatalError(@Nullable final String text, @Nullable final Throwable e){
        Log.e(tag, text, e);

        new AlertDialog.Builder(activity)
                .setTitle(R.string.error_text)
                .setMessage(getErrorString(text, e))
                .setNeutralButton(R.string.back_menu_text, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .show();
    }

    private String getErrorString(@Nullable String text, @Nullable Throwable exception) {
        StringBuilder errorBuilder = new StringBuilder();

        if (text != null) {
            errorBuilder.append(text);
        }

        if (exception != null) {
            if (text != null) {
                errorBuilder.append("\n");

            }

            errorBuilder.append(activity.getString(R.string.exception_text))
                    .append(": ")
                    .append(Throwables.getRootCause(exception))
                    .append("\n")
                    .append(Throwables.getStackTraceAsString(exception));
        }

        if (errorBuilder.length() == 0) {
            errorBuilder.append(activity.getString(R.string.error_text));
        }

        return errorBuilder.toString();
    }
}
