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

import java.io.Serializable;

// The implementation should be serializable so it can be
// passed to Activity as strategy.
public interface AbstractConverter extends Serializable {

    /* Convert the src to dest */
    void convert(String src, String dest) throws Exception;

    /* Get the source's file extension */
    String getSrcExtension();

    /* Get the destination's fle extension */
    String getDestExtension();
}
