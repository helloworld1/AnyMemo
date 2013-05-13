/*
Copyright (C) 2010 Haowen Ning

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

@Aspect
public class CheckNullArgsAspect {

    @Pointcut("execution(@org.liberty.android.fantastischmemo.aspect.CheckNullArgs * org.liberty.android.fantastischmemo..*(..))")
    public void checkNull(){}

    @Around("checkNull()")
    public void aroundCheckNullPointcut(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Class<?>[] argTypes = signature.getParameterTypes();
        String[] argNames = signature.getParameterNames();
        Object[] args = point.getArgs();

        boolean hasNull = false;
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Method " + signature.toLongString() + " has null arguments.");
        messageBuilder.append(" " + signature.getName() + "(");
        for (int i = 0; i < args.length; i++) {
            if ((args[i]) == null) {
                hasNull = true;
            }
            messageBuilder.append(argTypes[i].getCanonicalName() + " " + argNames[i] + " = " + args[i] + ", ");
        }
        messageBuilder.append(")");

        if (hasNull == true) {
            System.err.println(messageBuilder.toString());
            CheckNullArgs checkNullAnnotation = signature.getMethod().getAnnotation(CheckNullArgs.class);
            if (checkNullAnnotation.throwException()) {
                throw new NullPointerException(messageBuilder.toString());
            }
        } else {
            point.proceed();
        }
    }

}
