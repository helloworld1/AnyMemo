/*
Copyright (C) 2013 Haowen Ning

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
package org.liberty.android.fantastischmemo.ui.loader;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;

import roboguice.content.RoboAsyncTaskLoader;
import android.content.Context;

public abstract class DBLoader<T> extends RoboAsyncTaskLoader<T> {

    protected String dbPath;

    protected AnyMemoDBOpenHelper dbOpenHelper;

    protected abstract T dbLoadInBackground();

    public DBLoader(Context context, String dbPath) {
        super(context);
        this.dbPath = dbPath;
    }

    @Override
    public T loadInBackground() {
        dbOpenHelper = AnyMemoDBOpenHelperManager.getHelper(getContext(),
                dbPath);
        try {
            return dbLoadInBackground();
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(dbOpenHelper);
        }
    }
}
