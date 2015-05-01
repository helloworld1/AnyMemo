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
package org.liberty.android.fantastischmemo.ui;

// The gesture name for known gestures
public enum GestureName {
    LEFT_SWIPE("left-swipe"),
    RIGHT_SWIPE("right-swipe"),
    S_SHAPE("s-shape"),
    O_SHAPE("o-shape");

    private String gestureName;

    private GestureName(String name) {
        this.gestureName = name;
    }

    public String getName() {
        return gestureName;
    }

    public static GestureName parse(String name) {
        for (GestureName gn : GestureName.values()) {
            if (name.equals(gn.getName())) {
                return gn;
            }
        }
        throw new IllegalArgumentException("The input gesture name is invalid");
    }
}
