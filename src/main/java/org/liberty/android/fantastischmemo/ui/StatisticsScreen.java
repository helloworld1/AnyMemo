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

import java.util.Calendar;
import java.util.Date;

import org.achartengine.GraphicalView;

import org.achartengine.chart.AbstractChart;
import org.achartengine.chart.BarChart;
import org.achartengine.chart.PieChart;

import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;

import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.R;

import org.liberty.android.fantastischmemo.dao.CardDao;

import org.liberty.android.fantastischmemo.ui.widgets.AMSpinner;

import android.app.ProgressDialog;

import android.graphics.Color;

import android.os.AsyncTask;
import android.os.Bundle;

import android.view.View;
import android.widget.AdapterView;

import android.widget.FrameLayout;

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
                    ChartTask task = new CardToReviewTask();
                    task.execute((Void)null);

                } else if(selectedValue.equals("GRADE_STATISTICS")) {
                    ChartTask task = new GradeStatisticsTask();
                    task.execute((Void)null);
                } else if(selectedValue.equals("ACCUMULATIVE_CARDS_TO_REVIEW")) {
                    ChartTask task = new AccumulativeCardsToReviewTask();
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

    private BarChart generateBarGraph(XYSeries series) {
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();

        dataset.addSeries(series);

        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();

        SimpleSeriesRenderer r1 = new SimpleSeriesRenderer();
        r1.setColor(Color.CYAN);
        r1.setDisplayChartValues(true);

        renderer.addSeriesRenderer(r1);

        BarChart chart = new BarChart(dataset, renderer, BarChart.Type.DEFAULT);

        return chart;
    }

    private PieChart generateGradePieChart(CategorySeries series) {
        int[] colors = new int[] { Color.BLUE, Color.GREEN, Color.MAGENTA, Color.YELLOW, Color.CYAN, Color.RED};
        DefaultRenderer renderer = new DefaultRenderer();
        renderer.setLabelsTextSize(15);
        renderer.setLegendTextSize(15);
        renderer.setMargins(new int[] { 20, 30, 15, 0 });
        for (int color : colors) {
            SimpleSeriesRenderer r = new SimpleSeriesRenderer();
            r.setColor(color);
            renderer.addSeriesRenderer(r);
        }
        renderer.setZoomButtonsVisible(true);
        renderer.setZoomEnabled(true);
        renderer.setChartTitleTextSize(20);

        return new PieChart(series, renderer);

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

    private class CardToReviewTask extends ChartTask {
        @Override
        public AbstractChart doInBackground(Void... params) {

            cardDao = dbOpenHelper.getCardDao();

            XYSeries series = new XYSeries((getString(R.string.number_of_cards_scheduled_in_a_day_text)));
            Date now = new Date();
            for (int i = 0; i < 30; i++) {
                Date startDate = truncateDate(new Date(now.getTime() + i * 60 * 60 * 24 * 1000));
                Date endDate = new Date(startDate.getTime() + 1 * 60 * 60 * 24 * 1000);
                series.add(i, (int)cardDao.getScheduledCardCount(null, startDate, endDate));

            }
            return generateBarGraph(series);

        }
    }

    private class AccumulativeCardsToReviewTask extends ChartTask {
        @Override
        public AbstractChart doInBackground(Void... params) {

            cardDao = dbOpenHelper.getCardDao();

            XYSeries series = new XYSeries((getString(R.string.accumulative_cards_scheduled_text)));
            Date now = new Date();
            Date startDate = new Date(0);
            for (int i = 0; i < 30; i++) {
                Date endDate = new Date(now.getTime() + i * 60 * 60 * 24 * 1000);
                series.add(i, (int)cardDao.getScheduledCardCount(null, startDate, endDate));

            }
            return generateBarGraph(series);

        }
    }

    private class GradeStatisticsTask extends ChartTask {
        @Override
        public AbstractChart doInBackground(Void... params) {

            cardDao = dbOpenHelper.getCardDao();

            CategorySeries series = new CategorySeries(getString(R.string.grade_statistics_text));
            for (int i = 0; i < 6; i++) {
                long n = cardDao.getNumberOfCardsWithGrade(i);
                series.add("Grade" + i + ": " + n, n);
            }
            return generateGradePieChart(series);

        }
    }


    private abstract class ChartTask extends AsyncTask<Void, Void, AbstractChart> {
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

        @Override
        public void onPostExecute(AbstractChart result){
            GraphicalView gv = new GraphicalView(StatisticsScreen.this, result);
            statisticsGraphFrame.removeAllViews();
            statisticsGraphFrame.addView(gv);
            progressDialog.dismiss();
        }
    }
}
