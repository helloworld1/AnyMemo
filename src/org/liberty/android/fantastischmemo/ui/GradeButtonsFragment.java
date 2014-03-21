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


import javax.inject.Inject;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.LearningDataDao;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.LearningData;
import org.liberty.android.fantastischmemo.domain.Option;
import org.liberty.android.fantastischmemo.scheduler.Scheduler;
import org.liberty.android.fantastischmemo.utils.AMDateUtil;

import roboguice.fragment.RoboFragment;
import roboguice.util.Ln;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.common.base.Strings;


/*
 * Display the control bar in CardPlayerActivity. Handle the logic
 * of controlling the CardPlayerService.
 */
public class GradeButtonsFragment extends RoboFragment {

    public static final String EXTRA_DBPATH = "dbpath";

    private QACardActivity activity;

    private AnyMemoDBOpenHelper dbOpenHelper;

    private LinearLayout buttonView;

    private Option option;

    private Button[] gradeButtons = new Button[6];

    private CardDao cardDao;

    private LearningDataDao learningDataDao;

    private Scheduler scheduler = null;

    private OnCardChangedListener onCardChangedListener;

    private AMDateUtil amDateUtil;

    private Handler handler = new Handler();


    // The default button titles from the string
    private CharSequence[] defaultGradeButtonTitles = new CharSequence[6];

    @Inject
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Inject
    public void setAmDateUtil(AMDateUtil amDateUtil) {
        this.amDateUtil = amDateUtil;
    }

    public void setOnCardChangedListener(
            OnCardChangedListener onCardChangedListener) {
        this.onCardChangedListener = onCardChangedListener;
        
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (QACardActivity) activity;

        Bundle args = getArguments();

        String dbPath = args.getString(EXTRA_DBPATH);

        dbOpenHelper = AnyMemoDBOpenHelperManager.getHelper(activity, dbPath);

        cardDao = dbOpenHelper.getCardDao();

        learningDataDao = dbOpenHelper.getLearningDataDao();

        option = this.activity.getOption();

    }

    @Override
    public void onDetach() {
        super.onDetach();
        AnyMemoDBOpenHelperManager.releaseHelper(dbOpenHelper);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        int gradeButtonResource;
        switch (option.getButtonStyle()) {
            case ANKI:
                gradeButtonResource = R.layout.grade_buttons_anki;
                break;
            case MNEMOSYNE:
                gradeButtonResource = R.layout.grade_buttons_mnemosyne;
                break;
            default:
                gradeButtonResource = R.layout.grade_buttons_anymemo;
        }

        if (activity instanceof QuizActivity) {
            gradeButtonResource = R.layout.grade_buttons_quiz;
        }

        buttonView = (LinearLayout) inflater.inflate(gradeButtonResource, null);

        // Make sure touching all areas can reveal the card.
        buttonView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                activity.onClickAnswerView();
            }
        });
        buttonView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                activity.onClickAnswerView();
                return true ;
            }
        });

        gradeButtons[0] = (Button)buttonView.findViewById(R.id.grade_button_0);
        gradeButtons[1] = (Button)buttonView.findViewById(R.id.grade_button_1);
        gradeButtons[2] = (Button)buttonView.findViewById(R.id.grade_button_2);
        gradeButtons[3] = (Button)buttonView.findViewById(R.id.grade_button_3);
        gradeButtons[4] = (Button)buttonView.findViewById(R.id.grade_button_4);
        gradeButtons[5] = (Button)buttonView.findViewById(R.id.grade_button_5);

        for (int i = 0; i < 6; i++) {
            setButtonOnClickListener(gradeButtons[i], i);
            setButtonOnLongClickListener(gradeButtons[i], i);
            defaultGradeButtonTitles[i] = gradeButtons[i].getText();
            gradeButtons[i].setText(Html.fromHtml("<b>" + gradeButtons[i].getText() + "</b>"));
        }

        return buttonView;
    }

    public void gradeCurrentCard(int grade) {
        onGradeButtonClickListener.onGradeButtonClick(grade);
    }

    private void setButtonOnClickListener(final Button button, final int grade) {
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onGradeButtonClickListener.onGradeButtonClick(grade);
            }
        });
    }

    private void setButtonOnLongClickListener(final Button button, final int grade) {
        button.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                int[] helpText = {
                    R.string.memo_btn0_help_text,
                    R.string.memo_btn1_help_text,
                    R.string.memo_btn2_help_text,
                    R.string.memo_btn3_help_text,
                    R.string.memo_btn4_help_text,
                    R.string.memo_btn5_help_text
                };
                Toast.makeText(getActivity(), helpText[grade], Toast.LENGTH_LONG)
                    .show();
                return true;
            }
        });
    }

    private OnGradeButtonClickListener onGradeButtonClickListener =
        new OnGradeButtonClickListener() {
            public void onGradeButtonClick(int grade) {
                GradeTask gradeTask = new GradeTask();
                gradeTask.execute(grade);
            }
        };

    @SuppressWarnings("unused")
    private void setButtonText(int grade, CharSequence title, CharSequence description) {
        if (description != null && !Strings.isNullOrEmpty(description.toString())) {
            gradeButtons[grade].setText(Html.fromHtml("<b>" + title + "</b>" +  "<br />" + "<small>" + description + "</small>"));
        } else {
            gradeButtons[grade].setText(Html.fromHtml("<b>" + title + "</b>"));
        }
    }

    private void setButtonDescription(int grade, CharSequence description) {
        if (description != null && !Strings.isNullOrEmpty(description.toString())) {
            gradeButtons[grade].setText(Html.fromHtml("<b>" + defaultGradeButtonTitles[grade] + "</b>" +  "<br />" + "<small>" + description + "</small>"));
        }
    }

    public static interface OnGradeButtonClickListener {
        void onGradeButtonClick(int grade);
    }

    /*
     * Set the visibility of the buttons view.
     * The visibility is the same as View.GONE / VISIBLE / INVISIBLE.
     */
    public void setVisibility(final int visibility) {
        // This must be called throgh handler because it needs to
        // wait until the init of the fragment done.
        handler.post(new Runnable() {
            public void run() {
                buttonView.setVisibility(visibility);
                setGradeButtonTitle();
            }
        });
    }

    /*
     * Use AsyncTask to update the database and update the statistics
     * information
     */
    private class GradeTask extends AsyncTask<Integer, Void, Void> {

        private Card prevCard;

        private Card updatedCard;

        @Override
        public void onPreExecute() {
            super.onPreExecute();
            activity.setProgressBarIndeterminateVisibility(true);
        }

        @Override
        public Void doInBackground(Integer... grades) {
            assert grades.length == 1 : "Grade more than 1 time";
            int grade = grades[0];

            // Save current card as prev card for undo.
            // This was saved to determine the stat info
            // and the card id for undo
            prevCard = activity.getCurrentCard();

            // Save previous learning for Undo
            // This part is ugly due to mutablity of ORMLite
            
            updatedCard = cardDao.queryForId(activity.getCurrentCard().getId());
            learningDataDao.refresh(updatedCard.getLearningData());

            LearningData newLd = scheduler.schedule(prevCard.getLearningData(), grade, true);

            // Need to clone the data due to ORMLite restriction on "update()" method.
            updatedCard.getLearningData().cloneFromLearningData(newLd);

            return null;
        }

        @Override
        public void onPostExecute(Void result){
            super.onPostExecute(result);
            activity.setProgressBarIndeterminateVisibility(false);

            Ln.v("Prev card: " + prevCard);
            Ln.v("Updated card: " + updatedCard);


            onCardChangedListener.onCardChanged(prevCard, updatedCard);

        }
    }

    public static interface OnCardChangedListener {
        void onCardChanged(Card prevCard, Card updatedCard);
    }

    private void setGradeButtonTitle() {
        // Mnemosyne grade button style won't display the interval.
        // QuizAcivity does not write to db so it does not make sense to display.
        if (option.getButtonStyle() != Option.ButtonStyle.MNEMOSYNE && !(activity instanceof QuizActivity)) {
            setButtonDescription(0, ""+ amDateUtil.convertDayIntervalToDisplayString(scheduler.schedule(activity.getCurrentCard().getLearningData(), 0, false).getInterval()));
            setButtonDescription(1, ""+ amDateUtil.convertDayIntervalToDisplayString(scheduler.schedule(activity.getCurrentCard().getLearningData(), 1, false).getInterval()));
            setButtonDescription(2, ""+ amDateUtil.convertDayIntervalToDisplayString(scheduler.schedule(activity.getCurrentCard().getLearningData(), 2, false).getInterval()));
            setButtonDescription(3, ""+ amDateUtil.convertDayIntervalToDisplayString(scheduler.schedule(activity.getCurrentCard().getLearningData(), 3, false).getInterval()));
            setButtonDescription(4, ""+ amDateUtil.convertDayIntervalToDisplayString(scheduler.schedule(activity.getCurrentCard().getLearningData(), 4, false).getInterval()));
            setButtonDescription(5, ""+ amDateUtil.convertDayIntervalToDisplayString(scheduler.schedule(activity.getCurrentCard().getLearningData(), 5, false).getInterval()));
        }
    }
}
