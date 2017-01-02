package org.liberty.android.fantastischmemo.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.widget.RemoteViewsService;


@TargetApi(11)
public class UpdateWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WidgetRemoteViewsFactory(this.getApplicationContext(), intent);
    }

}