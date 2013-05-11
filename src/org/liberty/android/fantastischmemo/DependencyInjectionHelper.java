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

package org.liberty.android.fantastischmemo;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.WeakHashMap;

import javax.inject.Inject;

import android.app.Application;
import android.content.Context;

import com.google.inject.Guice;
import com.google.inject.Injector;

/*
 * Helper class for dependency injection into classes not managed by Guice.
 */
public class DependencyInjectionHelper {

    /* Cache for the injectors for each application. */
    protected static WeakHashMap<Application, Injector> injectors = new WeakHashMap<Application, Injector>();

    /* Get the injector for the current context. The injector is cached for each application. */
    public static Injector getInjector(Context context) {
        Application application = (Application) context.getApplicationContext();

        if (injectors.get(application) == null) {
            Injector injector = Guice.createInjector(new Modules(application
                    .getApplicationContext()));
            injectors.put(application, injector);
        }

        return injectors.get(application);
    }

    /* Inject the dependencies into the object using injector that is not created by Guice. */
    public static void injectDependencies(Object object, Injector injector) {
        Field[] allFields = object.getClass().getDeclaredFields();

        // For field dependency injection
        for (Field field : allFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                try {
                    field.setAccessible(true);
                    field.set(object, injector.getInstance(field.getType()));
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        // For method (setter) dependency injection
        Method[] allMethods = object.getClass().getDeclaredMethods();
        for (Method method : allMethods) {
            if (method.isAnnotationPresent(Inject.class)) {
                Type[] types = method.getGenericParameterTypes();
                Object[] parameters = new Object[types.length];

                for (int i = 0; i < types.length; i++) {
                    // Assume the type must be a class instead of primitives.
                    parameters[i] = injector.getInstance((Class<?>)types[i]);
                }

                try {
                    method.invoke(object, parameters);
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                }

            }
        }
    }
}
