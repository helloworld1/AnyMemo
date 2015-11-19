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

package org.liberty.android.fantastischmemo.queue;

import org.liberty.android.fantastischmemo.entity.Card;

/*
 * This interface will be used to fetch the card to learn
 */
public interface QueueManager {
    /* Flush the database */
    void release();

    /* Update one card */
    void update(Card card);

    /* de-queue one card*/
    Card dequeue();

    /* Remove one card from the queue. */
    void remove(Card card);

    /* Set the head of the queue to card. */
    Card dequeuePosition(int cardId);
}
