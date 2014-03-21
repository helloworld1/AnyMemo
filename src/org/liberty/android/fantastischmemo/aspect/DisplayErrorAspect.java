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
package org.liberty.android.fantastischmemo.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclarePrecedence;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.liberty.android.fantastischmemo.R;

import roboguice.util.Ln;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;

import com.google.common.base.Throwables;

@Aspect
@DeclarePrecedence("org.liberty.android.roboguiceplayground.DisplayErrorAspect,org.liberty.android.roboguiceplayground.RetryAspect")
public class DisplayErrorAspect {

    @Pointcut("execution(@org.liberty.android.fantastischmemo.aspect.DisplayError * org.liberty.android.fantastischmemo..*(..))")
    public void errorPointcut(){}

    @Around(value="errorPointcut()")
    public Object displayErrorWhenMethodThrowException(ProceedingJoinPoint joinPoint) throws Throwable {
        RuntimeException e = null;

        // Only handle RuntimeException!
        try {
            return joinPoint.proceed();
        } catch (RuntimeException ex) {
            e = ex;
            Ln.e(e, "Error caught in displayError aspect");
        } catch (Throwable t) {
            throw t;
        }

        final Activity activity;
        Ln.i("Displaying error here!");
        if (joinPoint.getThis() instanceof Activity) {
            activity = (Activity) joinPoint.getThis();
        } else if (joinPoint.getThis() instanceof Fragment) {
            activity = ((Fragment) joinPoint.getThis()).getActivity();
        } else {
            Ln.w("Using @DisplayError on a object other than Activity and Fragment");
            // Do not handle this exception here since this aspect should not take
            // any effect if calling context is wrong.
            throw e;
        }

        // Get the finishActivity argument passed from annotation.
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        DisplayError displayErrorAnnotation = signature.getMethod().getAnnotation(DisplayError.class);
        final boolean finishActivity = displayErrorAnnotation.finishActivity();

        new AlertDialog.Builder(activity)
            .setTitle(R.string.exception_text)
            .setMessage(activity.getString(R.string.exception_text) +": " + Throwables.getRootCause(e) + "\n" + Throwables.getStackTraceAsString(e))
            .setPositiveButton(R.string.ok_text, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (finishActivity) {
                        activity.finish();
                    }
                }
            })
            .show();

        return null;

    }
}


