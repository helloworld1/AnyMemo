package org.liberty.android.fantastischmemo.widget;

import android.widget.RemoteViewsService;
import android.content.Intent;
import android.annotation.TargetApi;


@TargetApi(11)
public class UpdateWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WidgetRemoteViewsFactory(this.getApplicationContext(), intent);
    }

}