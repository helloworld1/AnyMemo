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
package org.liberty.android.fantastischmemo.utils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import roboguice.util.Ln;

public class AnyMemoExecutor {
    private static ExecutorService executor = Executors.newSingleThreadExecutor();
    private static List<Future<?>> futures = new LinkedList<Future<?>>();

    public static synchronized Future<?> submit(Runnable runnable) {
        Future<?> f = executor.submit(runnable);
        futures.add(f);
        return f;
    }

    public static synchronized void waitTask(Future<?> f) {
        try {
            f.get();
        } catch (InterruptedException e){
            Ln.e(e);
            assert false : "Encounter exception when waiting task";
        } catch (ExecutionException e) {
            Ln.e(e);
            assert false : "Encounter exception when waiting task";
        } finally {
            futures.remove(f);
        }
    }

    public static synchronized void waitAllTasks() {
        Iterator<Future<?>> fi = futures.iterator();
        while (fi.hasNext()) {
            Future<?> f = fi.next();
            try {
                f.get();
            } catch (InterruptedException e) {
                Ln.e(e);
                assert false : "Encounter exception when waiting task";
            } catch (ExecutionException e) {
                Ln.e(e);
                assert false : "Encounter exception when waiting task";
            } finally {
                fi.remove();
            }
        }
        assert futures.isEmpty() == true : "After waiting all futures, the future list shoudl be empty";
    }

}
