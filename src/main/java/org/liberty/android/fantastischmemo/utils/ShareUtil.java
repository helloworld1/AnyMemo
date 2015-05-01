package org.liberty.android.fantastischmemo.utils;

import javax.inject.Inject;

import org.apache.commons.io.FilenameUtils;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.domain.Card;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class ShareUtil {

    private Context mContext;

    @Inject
    public ShareUtil(Context context) {
        mContext = context;
    }

    public void shareDb(String dbPath) {
        Intent sendIntent= new Intent(Intent.ACTION_SEND);
        sendIntent.setType("application/x-sqlite3");
        sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + dbPath));
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
