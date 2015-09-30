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

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.common.collect.Iterables;
import com.google.common.collect.ObjectArrays;
import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.ui.widgets.AMSpinner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class StatisticsScreen extends AMActivity {
    public static final String EXTRA_DBPATH = "dbpath";

    private AMSpinner typeSelectSpinner;
    private FrameLayout statisticsGraphFrame;
    private String dbPath;

    private CardDao cardDao;

    private AnyMemoDBOpenHelper dbOpenHelper;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.statistics_screen);
        typeSelectSpinner = (AMSpinner)findViewById(R.id.statistics_type_spinner);
        statisticsGraphFrame = (FrameLayout)findViewById(R.id.statistics_graph_frame);

        Bundle extras = getIntent().getExtras();
        assert extras != null : "Open StatisticsScreen without extras";

        dbPath = extras.getString(EXTRA_DBPATH);

        assert dbPath != null : "dbPath shouldn't be null";

        dbOpenHelper = AnyMemoDBOpenHelperManager.getHelper(this, dbPath);
        typeSelectSpinner.setOnItemSelectedListener(typeSelectSpinnerListener);

        //typeSelectSpinnerListener.onItemSelected(null, null, 0, 0);

    }

    private AdapterView.OnItemSelectedListener typeSelectSpinnerListener =
        new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent , View view,
					int position, long id) {
                String selectedValue = typeSelectSpinner.getItemValueForPosition(position);
                if(selectedValue.equals("CARDS_TO_REVIEW")) {
                    CardToReviewTask task = new CardToReviewTask();
                    task.execute((Void)null);

                } else if(selectedValue.equals("GRADE_STATISTICS")) {
                    GradeStatisticsTask task = new GradeStatisticsTask();
                    task.execute((Void)null);
                } else if(selectedValue.equals("ACCUMULATIVE_CARDS_TO_REVIEW")) {
                    AccumulativeCardsToReviewTask task = new AccumulativeCardsToReviewTask();
                    task.execute((Void)null);
                }
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
                // Do nothing
			}
        };

    @Override
    public void onDestroy() {
        super.onDestroy();
        AnyMemoDBOpenHelperManager.releaseHelper(dbOpenHelper);
    }

    private Date truncateDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new Date(cal.getTimeInMillis());
    }

    private abstract class ChartTask<T, K, ResultT>  extends AsyncTask<T, K, ResultT> {
        private ProgressDialog progressDialog;

        @Override
        public void onPreExecute() {
            progressDialog = new ProgressDialog(StatisticsScreen.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle(getString(R.string.loading_please_wait));
            progressDialog.setMessage(getString(R.string.loading_database));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        public abstract Chart generateChart(ResultT result);

        @Override
        public void onPostExecute(ResultT result) {
            Chart chart = generateChart(result);

            statisticsGraphFrame.removeAllViews();
            statisticsGraphFrame.addView(chart);
            progressDialog.dismiss();
        }
    }

    private class CardToReviewTask extends ChartTask<Void, Void, BarData> {
        @Override
        public BarData doInBackground(Void... params) {
            cardDao = dbOpenHelper.getCardDao();
            List<String> xVals = new ArrayList<String>(30);
            List<BarEntry> yVals = new ArrayList<BarEntry>(30);
            Date now = new Date();
            for (int i = 0; i < 30; i++) {
                Date startDate = truncateDate(new Date(now.getTime() + i * 60 * 60 * 24 * 1000));
                Date endDate = new Date(startDate.getTime() + 1 * 60 * 60 * 24 * 1000);
                xVals.add("" + i);
                yVals.add(new BarEntry((float)cardDao.getScheduledCardCount(null, startDate, endDate), i));

            }

            BarDataSet dataSet = new BarDataSet(yVals, getString(R.string.number_of_cards_scheduled_in_a_day_text));
            BarData data = new BarData(xVals, dataSet);
            return data;
        }

        @Override
        public Chart generateChart(BarData data) {
            BarChart chart = new BarChart(StatisticsScreen.this);
            chart.setData(data);
            chart.setDescription("");
            return chart;
        }
    }

    private class AccumulativeCardsToReviewTask extends ChartTask<Void, Void, BarData> {
        @Override public BarData doInBackground(Void... params) {
            cardDao = dbOpenHelper.getCardDao();
            List<String> xVals = new ArrayList<String>(30);
            List<BarEntry> yVals = new ArrayList<BarEntry>(30);

            Date now = new Date();
            Date startDate = new Date(0);
            for (int i = 0; i < 30; i++) {
                Date endDate = new Date(now.getTime() + i * 60 * 60 * 24 * 1000);
                xVals.add("" + i);
                yVals.add(new BarEntry((float)cardDao.getScheduledCardCount(null, startDate, endDate), i));
            }

            BarDataSet dataSet = new BarDataSet(yVals, getString(R.string.accumulative_cards_scheduled_text));
            BarData data = new BarData(xVals, dataSet);
            return data;
        }

        @Override
        public Chart generateChart(BarData data) {
            BarChart chart = new BarChart(StatisticsScreen.this);
            chart.setData(data);
            chart.setDescription("");
            return chart;
        }
    }

    private class GradeStatisticsTask extends ChartTask<Void, Void, PieData> {
        @Override
        public PieData doInBackground(Void... params) {

            cardDao = dbOpenHelper.getCardDao();

            List<String> xVals = new ArrayList<String>(6);
            List<Entry> yVals = new ArrayList<Entry>(6);

            for (int i = 0; i < 6; i++) {
                long n = cardDao.getNumberOfCardsWithGrade(i);
                xVals.add("" + i);
                yVals.add(new Entry((float)cardDao.getNumberOfCardsWithGrade(i), i));
            }

            PieDataSet dataSet = new PieDataSet(yVals, getString(R.string.grade_statistics_text));

            List<Integer> colors = new ArrayList<Integer>();

            for (int c : ColorTemplate.COLORFUL_COLORS) {
                colors.add(c);
            }
            colors.add(ColorTemplate.JOYFUL_COLORS[0]);
            dataSet.setColors(colors);
            dataSet.setSliceSpace(3f);
            dataSet.setSelectionShift(5f);

            PieData data = new PieData(xVals, dataSet);

            return data;
        }

        @Override
        public Chart generateChart(PieData data) {
            PieChart chart = new PieChart(StatisticsScreen.this);
            chart.setData(data);
            chart.setDescription("");

            return chart;
        }
    }
}
