package org.liberty.android.fantastischmemo.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;

import org.apache.commons.io.FilenameUtils;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.entity.Card;

import java.io.File;

import javax.inject.Inject;

public class ShareUtil {

    private Activity mContext;

    @Inject
    public ShareUtil(Activity context) {
        mContext = context;
    }

    public void shareDb(String dbPath) {
        Intent sendIntent= new Intent(Intent.ACTION_SEND);
        sendIntent.setType("application/x-sqlite3");

        Uri uri = FileProvider.getUriForFile(
                mContext,
                mContext.getPackageName() + ".fileprovider", new File(dbPath));

        sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, FilenameUtils.getName(dbPath));
        mContext.startActivity(Intent.createChooser(sendIntent, mContext.getString(R.string.share_text)));
    }

    public void shareCard(Card card) {
        Intent sendIntent= new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, card.getQuestion());
        sendIntent.putExtra(Intent.EXTRA_TEXT, card.getAnswer());
        mContext.startActivity(Intent.createChooser(sendIntent, mContext.getString(R.string.share_text)));
    }
}
