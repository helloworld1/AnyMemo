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
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import roboguice.util.Ln;

@Aspect
public class RetryAspect {

    @Pointcut("execution(@org.liberty.android.fantastischmemo.aspect.Retry * org.liberty.android.fantastischmemo.*.*(..))")
    public void retryMethodPointcut(){}

    @Around(value="retryMethodPointcut()")
    public Object retryMethod(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Retry retryAnnotation = signature.getMethod().getAnnotation(Retry.class);
        final int times = retryAnnotation.times();

        for (int i = 1; i <= times - 1; i++) {
            try {
                return joinPoint.proceed();
            } catch (Throwable e) {
                Ln.e(e, "Executing " + signature.getName() + " failed " + " on attempt " + i + "/" + times + ".");
            }
        }

        // Handle the last time of the execution.
        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
                Ln.e(e, "Executing " + signature.getName() + " failed " + " on attempt " + times + "/" + times + ".");
            throw e;
        }
    }
}



