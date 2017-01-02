package org.liberty.android.fantastischmemo.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.modules.PerActivity;

import javax.inject.Inject;

@PerActivity
public class AboutUtil {

    private final Activity mContext;

    @Inject
    public AboutUtil(Activity activityContext) {
        this.mContext = activityContext;
    }

    public void createAboutDialog() {

        // Get the version defined in the AndroidManifest.
        String versionName = "";
        try {
            versionName = this.mContext.getPackageManager()
                    .getPackageInfo(this.mContext.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "1.0";
        }

        View alertView = View.inflate(this.mContext, R.layout.link_alert, null);
        TextView textView = (TextView)alertView.findViewById(R.id.link_alert_message);
        textView.setText(Html.fromHtml(mContext.getString(R.string.about_text)));
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        new AlertDialog.Builder(mContext)
                .setView(alertView)
                .setTitle(mContext.getString(R.string.app_full_name) + " " + versionName)
                .setPositiveButton(mContext.getString(R.string.ok_text), null)
                .setNegativeButton(mContext.getString(R.string.about_version),
                        new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface arg0, int arg1){
                                Intent myIntent = new Intent();
                                myIntent.setAction(Intent.ACTION_VIEW);
                                myIntent.addCategory(Intent.CATEGORY_BROWSABLE);
                                myIntent.setData(Uri.parse(mContext.getString(R.string.website_versions_view)));
                                mContext.startActivity(myIntent);
                            }
                        })
                .show();
    }

}
