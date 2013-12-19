/*
Copyright (C) 2012 Haowen Ning, Xinxin Wang

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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.AMEnv;
import org.liberty.android.fantastischmemo.AMPrefKeys;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.LearningData;
import org.liberty.android.fantastischmemo.domain.SchedulingAlgorithmParameters;
import org.liberty.android.fantastischmemo.scheduler.Scheduler;
import org.liberty.android.fantastischmemo.utils.AMPrefUtil;
import org.liberty.android.fantastischmemo.utils.CardTextUtil;
import org.liberty.android.fantastischmemo.utils.CardTextUtilFactory;

import roboguice.RoboGuice;
import roboguice.inject.ContextScope;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SearchView;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

public class CardListActivity extends AMActivity {
    private String dbPath;

    private CardListAdapter cardListAdapter;

    private AnyMemoDBOpenHelper dbOpenHelper;

    private ListView listView;

    private AMPrefUtil amPrefUtil;

    private CardTextUtilFactory cardTextUtilFactory;

    private CardTextUtil cardTextUtil;

    private Scheduler scheduler;

    private SchedulingAlgorithmParameters schedulingAlgorithmParameters;

    private boolean initialAnswerVisible = true;

    /* Initial position in the list */

    private Drawable defaultBackground;

    public static String EXTRA_DBPATH = "dbpath";

    @Inject
    public void setAmPrefUtil(AMPrefUtil amPrefUtil) {
        this.amPrefUtil = amPrefUtil;
    }

    @Inject
    public void setCardTextUtilFactory(CardTextUtilFactory cardTextUtilFactory) {
        this.cardTextUtilFactory = cardTextUtilFactory;
    }

    @Inject
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Inject
    public void setSchedulingAlgorithmParameters(
            SchedulingAlgorithmParameters schedulingAlgorithmParameters) {
        this.schedulingAlgorithmParameters = schedulingAlgorithmParameters;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_list);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            dbPath = extras.getString(CardListActivity.EXTRA_DBPATH);
        }

        initialAnswerVisible = amPrefUtil.getSavedBoolean(
                AMPrefKeys.LIST_ANSWER_VISIBLE_PREFIX, dbPath, true);

        listView = (ListView) findViewById(R.id.item_list);
        defaultBackground = listView.getBackground();

        InitTask initTask = new InitTask();
        initTask.execute((Void) null);

    }

    @Override
    public void onDestroy() {
        amPrefUtil.putSavedInt(AMPrefKeys.LIST_EDIT_SCREEN_PREFIX, dbPath,
                listView.getFirstVisiblePosition());
        AnyMemoDBOpenHelperManager.releaseHelper(dbOpenHelper);
        super.onDestroy();
    }

    @Override
    public void restartActivity() {
        Intent myIntent = new Intent(this, CardListActivity.class);
        assert dbPath != null : "Use null dbPath to restartAcitivity";
        myIntent.putExtra(EXTRA_DBPATH, dbPath);
        finish();
        startActivity(myIntent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Refresh activity when the data has been changed.
        if (resultCode == Activity.RESULT_CANCELED) {
            return;
        } else {
            restartActivity();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.card_list_activity_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat
                .getActionView(searchItem);
        searchView.setOnQueryTextListener(onQueryTextChangedListener);

        return true;
    }

    private void showListItemPopup(final View childView, final Card card) {
        View view = childView.findViewById(R.id.item_question);
        PopupMenu popup = new PopupMenu(this, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.card_list_popup_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                case R.id.mark_as_learned_menu:
                    markAsLearned(card);
                    break;
                case R.id.mark_as_forgotten_menu:
                    markAsForgotten(card);
                    break;
                case R.id.mark_as_new_menu:
                    markAsNew(card);
                    break;
                case R.id.mark_as_learned_forever_menu:
                    markAsLearnedForever(card);
                    break;
                }
                return true;
            }
        });
        popup.show();
    }

    private void showListItemLongClickPopup(final View childView,
            final Card card) {
        View view = childView.findViewById(R.id.item_question);
        PopupMenu popup = new PopupMenu(this, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.card_list_long_click_popup_menu,
                popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                case R.id.edit:
                    gotoCardEditorActivity(card);
                    break;
                case R.id.detail:
                    gotoDetailActivity(card);
                    break;

                case R.id.preview_edit:
                    gotoPreviewEditActivity(card);
                    break;

                }
                return true;
            }
        });
        popup.show();
    }

    private void gotoCardEditorActivity(Card card) {
        Intent intent = new Intent(this, CardEditor.class);
        intent.putExtra(CardEditor.EXTRA_DBPATH, dbPath);
        intent.putExtra(CardEditor.EXTRA_CARD_ID, card.getId());
        startActivity(intent);
    }

    private void gotoDetailActivity(Card card) {
        Intent intent = new Intent(this, DetailScreen.class);
        intent.putExtra(DetailScreen.EXTRA_DBPATH, dbPath);
        intent.putExtra(DetailScreen.EXTRA_CARD_ID, card.getId());
        startActivity(intent);
    }

    private void gotoPreviewEditActivity(Card card) {
        Intent intent = new Intent(this, PreviewEditActivity.class);
        intent.putExtra(PreviewEditActivity.EXTRA_DBPATH, dbPath);
        intent.putExtra(PreviewEditActivity.EXTRA_CARD_ID, card.getId());
        startActivity(intent);
    }

    private void markAsLearned(Card card) {
        LearningData newLd = scheduler.schedule(card.getLearningData(), 5,
                schedulingAlgorithmParameters.getEnableNoise());
        dbOpenHelper.getLearningDataDao().updateLearningData(newLd);
        card.setLearningData(newLd);
        cardListAdapter.notifyDataSetChanged();
    }

    private void markAsForgotten(Card card) {
        LearningData newLd = scheduler.schedule(card.getLearningData(), 1,
                schedulingAlgorithmParameters.getEnableNoise());
        dbOpenHelper.getLearningDataDao().updateLearningData(newLd);
        card.setLearningData(newLd);
        cardListAdapter.notifyDataSetChanged();
    }

    private void markAsNew(Card card) {
        dbOpenHelper.getLearningDataDao().resetLearningData(
                card.getLearningData());
        cardListAdapter.notifyDataSetChanged();
    }

    private void markAsLearnedForever(Card card) {
        dbOpenHelper.getLearningDataDao().markAsLearnedForever(
                card.getLearningData());
        cardListAdapter.notifyDataSetChanged();
    }

    private void highlightCardViewAsLearned(View view) {
        // Light green color
        view.setBackgroundColor(0x4F00FF00);
    }

    private void highlightCardViewAsForgotten(View view) {
        // Light yellow color
        view.setBackgroundColor(0x4FFFFF00);
    }

    // Need to maintain compatibility with Android 2.3
    @SuppressWarnings("deprecation")
    private void highlightCardViewAsNew(View view) {
        // The default background saved when the activity is created
        view.setBackgroundDrawable(defaultBackground);
    }

    // Toggle the visibility of all the answers
    private void showHideAnswers() {
        if (initialAnswerVisible) {
            for (CardWrapper wrapper : cardListAdapter) {
                wrapper.setVisible(false);
            }
            initialAnswerVisible = false;
        } else {
            for (CardWrapper wrapper : cardListAdapter) {
                wrapper.setVisible(true);
            }
            initialAnswerVisible = true;
        }
        amPrefUtil.putSavedBoolean(AMPrefKeys.LIST_ANSWER_VISIBLE_PREFIX, dbPath, initialAnswerVisible);
        cardListAdapter.notifyDataSetChanged();
    }

    private void showSortListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //items in enum SortMethod and array sort_by_options_values should have the same order
        String defaultItem = getResources().getStringArray(
                R.array.sort_by_options_values)[0];
        String savedMethod = amPrefUtil.getSavedString(
                AMPrefKeys.LIST_SORT_BY_METHOD_PREFIX, dbPath, defaultItem);
        builder.setSingleChoiceItems(R.array.sort_by_options,
                SortMethod.valueOf(savedMethod).ordinal(),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String[] items = getResources().getStringArray(
                                R.array.sort_by_options_values);
                        sortList(SortMethod.valueOf(items[which]));
                        amPrefUtil.putSavedString(
                                AMPrefKeys.LIST_SORT_BY_METHOD_PREFIX, dbPath,
                                items[which]);
                        dialog.dismiss();
                    }
                }).show();
    }

    private void sortList(SortMethod sort) {
        //Handle sort method
        switch (sort) {
        case ORDINAL:
            cardListAdapter.sort(new Comparator<CardWrapper>() {
                @Override
                public int compare(CardWrapper c1, CardWrapper c2) {
                    return c1.getCard().getOrdinal()
                            - c2.getCard().getOrdinal();
                };
            });
            break;
        case QUESTION:
            cardListAdapter.sort(new Comparator<CardWrapper>() {
                @Override
                public int compare(CardWrapper c1, CardWrapper c2) {
                    return c1.getCard().getQuestion()
                            .compareTo(c2.getCard().getQuestion());
                };
            });
            break;
        case ANSWER:
            cardListAdapter.sort(new Comparator<CardWrapper>() {
                @Override
                public int compare(CardWrapper c1, CardWrapper c2) {
                    return c1.getCard().getAnswer()
                            .compareTo(c2.getCard().getAnswer());
                };
            });
            break;
        default:
            throw new AssertionError(
                    "This case will not happen! Or the system has carshed.");
        }
    }

    SearchView.OnQueryTextListener onQueryTextChangedListener = new SearchView.OnQueryTextListener() {

        @Override
        public boolean onQueryTextChange(String text) {
            // This is used to make sure user can clear the search result
            // and show all cards easily.
            if (StringUtils.isEmpty(text)) {
                cardListAdapter.getFilter().filter(text);
            }
            return true;
        }

        @Override
        public boolean onQueryTextSubmit(String text) {
            cardListAdapter.getFilter().filter(text);
            return true;
        }

    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.sort:
            showSortListDialog();
            return true;
        case R.id.show_hide_answers:
            showHideAnswers();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private OnItemClickListener listItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parentView, View childView,
                int position, long id) {
            showListItemPopup(childView, cardListAdapter.getCardItem(position));
            CardWrapper cardWrapper = cardListAdapter.getItem(position);
            if (!cardWrapper.isVisible()) {
                cardWrapper.setVisible(true);
                cardListAdapter.notifyDataSetChanged();
            }

        }
    };

    private AdapterView.OnItemLongClickListener listItemLongClickListener = new AdapterView.OnItemLongClickListener() {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view,
                int position, long id) {
            showListItemLongClickPopup(view,
                    cardListAdapter.getCardItem(position));
            return true;
        }
    };

    private class CardListAdapter extends ArrayAdapter<CardWrapper> implements
            SectionIndexer, Iterable<CardWrapper> {
        /* quick index sections */
        private String[] sections;

        private List<CardWrapper> cardList = null;
        // As soon as filter is used card list this keeps all the origian cards
        private List<CardWrapper> originalCardList = null;

        public CardListAdapter(Context context, List<CardWrapper> cards) {
            super(context, 0, cards);
            cardList = cards;

            int sectionSize = getCount() / 100;
            sections = new String[sectionSize];
            for (int i = 0; i < sectionSize; i++) {
                sections[i] = String.valueOf(i * 100);
            }
        }

        public Card getCardItem(int position) {
            return getItem(position).getCard();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CardWrapper cardWrapper = getItem(position);
            Card card = cardWrapper.getCard();
            if (convertView == null) {
                LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = li.inflate(R.layout.card_list_item, null);
            }
            TextView idView = (TextView) convertView.findViewById(R.id.item_id);
            TextView questionView = (TextView) convertView
                    .findViewById(R.id.item_question);
            TextView answerView = (TextView) convertView
                    .findViewById(R.id.item_answer);

            idView.setText("" + card.getOrdinal());

            // 0 -> question 1-> answer
            List<Spannable> fields = cardTextUtil.getFieldsToDisplay(card);
            questionView.setText(fields.get(0));
            answerView.setText(fields.get(1));

            if (scheduler.isCardNew(card.getLearningData())) {
                highlightCardViewAsNew(convertView);
            } else if (scheduler.isCardForReview(card.getLearningData())) {
                highlightCardViewAsForgotten(convertView);
            } else {
                highlightCardViewAsLearned(convertView);
            }

            if (cardWrapper.isVisible()) {
                answerView.setVisibility(View.VISIBLE);
            } else {
                answerView.setVisibility(View.INVISIBLE);
            }
            return convertView;
        }

        /* Display the quick index when the user is scrolling */
        @Override
        public int getPositionForSection(int section) {
            return section * 100;
        }

        @Override
        public int getSectionForPosition(int position) {
            return 1;
        }

        @Override
        public Object[] getSections() {
            return sections;
        }

        // Used to implement the search function for the card list
        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence searchTerm) {
                    FilterResults results = new FilterResults();

                    // Back up the original card list when called for the first time
                    if (originalCardList == null) {
                        originalCardList = new ArrayList<CardWrapper>(cardList);
                    }

                    // If empty term is gived, restore to the original list
                    if (StringUtils.isEmpty(searchTerm)) {
                        List<CardWrapper> list = new ArrayList<CardWrapper>(
                                originalCardList);
                        results.values = list;
                        results.count = list.size();
                    } else {
                        List<CardWrapper> resultList = new ArrayList<CardWrapper>();

                        for (CardWrapper cardWrapper : cardList) {
                            Card card = cardWrapper.getCard();
                            if (card.getQuestion().toLowerCase().contains(searchTerm.toString().toLowerCase()) || 
                                    card.getAnswer() .toLowerCase().contains(searchTerm.toString().toLowerCase())) {
                                resultList.add(cardWrapper);
                            }
                        }

                        results.values = resultList;
                        results.count = resultList.size();
                    }

                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint,
                        FilterResults results) {
                    //noinspection unchecked
                    cardList.clear();

                    @SuppressWarnings("unchecked")
                    List<CardWrapper> values = (List<CardWrapper>) results.values;

                    cardList.addAll(values);
                    if (results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
        }

        @Override
        public Iterator<CardWrapper> iterator() {
            return cardList.iterator();
        }

    }

    private class InitTask extends AsyncTask<Void, Void, List<CardWrapper>> {
        private ProgressDialog progressDialog;

        @Override
        public void onPreExecute() {
            progressDialog = new ProgressDialog(CardListActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle(getString(R.string.loading_please_wait));
            progressDialog.setMessage(getString(R.string.loading_database));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected List<CardWrapper> doInBackground(Void... params) {
            dbOpenHelper = AnyMemoDBOpenHelperManager.getHelper(
                    CardListActivity.this, dbPath);

            CardDao cardDao = dbOpenHelper.getCardDao();
            List<CardWrapper> cardWrappers = new ArrayList<CardWrapper>((int)cardDao.countOf());

            for (Card card : cardDao.getAllCards(null)) {
                cardWrappers.add(new CardWrapper(card, initialAnswerVisible));
            }

            ContextScope scope = RoboGuice.getInjector(CardListActivity.this).getInstance(ContextScope.class);
            // Make sure the method is running under the context
            // The AsyncTask thread does not have the context, so we need
            // to manually enter the scope.
            synchronized(ContextScope.class) {
                scope.enter(CardListActivity.this);
                try {
                    cardTextUtil = cardTextUtilFactory.create(dbPath);
                } finally {
                    scope.exit(CardListActivity.this);
                }
            }

            return cardWrappers;
        }

        @Override
        public void onPostExecute(List<CardWrapper> result) {
            cardListAdapter = new CardListAdapter(CardListActivity.this, result);
            cardListAdapter.setNotifyOnChange(true);
            int initPosition = amPrefUtil.getSavedInt(AMPrefKeys.LIST_EDIT_SCREEN_PREFIX, dbPath, 0);
            listView.setAdapter(cardListAdapter);
            listView.setSelection(initPosition);
            listView.setFastScrollEnabled(true);
            listView.setOnItemClickListener(listItemClickListener);
            listView.setOnItemLongClickListener(listItemLongClickListener);
            listView.setTextFilterEnabled(true);
            
            //Get the sort method from system database and set this method as origin method
            String defaultItem = getResources().getStringArray(R.array.sort_by_options_values)[0];
            String savedMethod = amPrefUtil.getSavedString(AMPrefKeys.LIST_SORT_BY_METHOD_PREFIX, dbPath, defaultItem);
            sortList(SortMethod.valueOf(savedMethod));
            progressDialog.dismiss();
        }
    }

    private static class CardWrapper {

        private Card card;

        private boolean visible;

        public CardWrapper(Card card, boolean visible) {
            this.card = card;
            this.visible = visible;
        }

        public Card getCard() {
            return card;
        }

        public boolean isVisible() {
            return visible;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }

    }

    private enum SortMethod {
        ORDINAL,
        QUESTION,
        ANSWER};
}


