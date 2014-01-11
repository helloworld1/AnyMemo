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
package org.liberty.android.fantastischmemo.ui;

import org.liberty.android.fantastischmemo.R;

import roboguice.fragment.RoboFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * This fragment is for a card with mulitple sides and can be flipped to different sides
 */
public class FlipableCardFragment extends RoboFragment {

    /**
     * The key for the input a list of CardFragemnt.Builder that is used to build a fragment.
     * Input type is CardFragment.Builder[]
     */
    public static final String EXTRA_CARD_FRAGMENT_BUILDERS = "cardFragmentBuilders";

    /**
     * The initial position of the side in a multi-sided card.
     * Input type is int.
     */
    public static final String EXTRA_INITIAL_POSITION = "initialPosition";

    private CardFragment.Builder[] cardFragmentBuilders;

    private int initialPosition = 0;

    private ViewPager cardPager;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        // Need to convert the array type to Serializable to CardFragment.Builder
        Object[] array1 = (Object[]) getArguments().getSerializable(EXTRA_CARD_FRAGMENT_BUILDERS);
        cardFragmentBuilders = new CardFragment.Builder[array1.length];
        for (int i = 0; i < array1.length;  i++) {
            cardFragmentBuilders[i] = (CardFragment.Builder) array1[i];
        }

        initialPosition = getArguments().getInt(EXTRA_INITIAL_POSITION, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.flipable_card, container, false);
        cardPager = (ViewPager) v.findViewById(R.id.card_pager);
        cardPager.setAdapter(new FragmentStatePagerAdapter(
                getChildFragmentManager()) {

            @Override
            public Fragment getItem(int position) {
                return cardFragmentBuilders[position].build();
            }

            @Override
            public int getCount() {
                return cardFragmentBuilders.length;
            }
        });
        cardPager.setCurrentItem(initialPosition);

        return v;
    }

    public void flipTo(int position) {
        cardPager.setCurrentItem(position);
    }
}
