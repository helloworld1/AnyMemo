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

package org.liberty.android.fantastischmemo.cardscreen;

import org.liberty.android.fantastischmemo.*;

/*
 * This interface will be used to fetch the card to learn
 */
interface QueueManager{
    /* before everything */
    public boolean initQueue();
    /* give the next item according to current one */
    public Item updateAndNext(Item item);
    /* Update the item with the same id */
    public boolean updateQueueItem(Item item);
    /* Insert to the queue based on position, -1 means the end of queue */
    public void insertIntoQueue(Item item, int position);
    /* after everything */
    public void close();
    /* Get statistics info*/
    public int[] getStatInfo();
    /* Get Next card */
    public Item getNext(Item item);
}


