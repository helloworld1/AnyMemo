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
package org.liberty.android.fantastischmemo.converter;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.liberty.android.fantastischmemo.utils.AMZipUtils;

import com.google.inject.BindingAnnotation;

public class ZipExporter implements Converter {

    private static final long serialVersionUID = -7316554160292269944L;

    /*
     * Dest is not used, it is always in [external]/anymemo
     * directory
     */

    public void convert(String src, String dest) throws Exception {
        AMZipUtils.compressFile(new File(src), new File(dest));

    }

    @Override
    public String getSrcExtension() {
        return "db";
    }

    @Override
    public String getDestExtension() {
        return "zip";
    }

    @BindingAnnotation
    @Target({ ElementType. FIELD, ElementType.PARAMETER, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Type {};
}
