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

package org.liberty.android.fantastischmemo.utils;

import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * This is a Queue implementation that prevent duplicated elements added into
 * the queue.
 *
 * The implementation is synchronized.
 */
public class SynchronizedLinkedHashQueue<E> extends LinkedHashSet<E> implements
        Queue<E> {

    private static final long serialVersionUID = -3093175758945433571L;

    @Override
    public synchronized boolean offer(E e) {
        return super.add(e);
    }

    @Override
    public synchronized E remove() {
        E returnVal = element();
        super.remove(returnVal);
        return returnVal;

    }

    @Override
    public synchronized E poll() {
        try {
            return remove();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    @Override
    public synchronized E element() {
        if (super.isEmpty()) {
            throw new NoSuchElementException();
        }
        E returnVal = super.iterator().next();
        super.remove(returnVal);
        return returnVal;
    }

    @Override
    public synchronized E peek() {
        try {
            return element();
        } catch (NoSuchElementException e) {
            return null;
        }
    }
}
