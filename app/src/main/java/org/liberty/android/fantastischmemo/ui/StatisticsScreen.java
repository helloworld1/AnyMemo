/*
CYAxisopyright (C) 2012 Haowen Ning

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
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;

import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.common.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.common.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.common.BaseActivity;
import org.liberty.android.fantastischmemo.dao.CardDao;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StatisticsScreen extends BaseActivity {
    public static final String EXTRA_DBPATH = "dbpath";

    private FrameLayout statisticsGraphFrame;

    private CardDao cardDao;

    private AnyMemoDBOpenHelper dbOpenHelper;

    private DrawerLayout drawerLayout;

    private NavigationView navigationView;

    private static final ValueFormatter valueFormatter = new ChartValueFormatter();

    private static final YAxisValueFormatter yAxisValueFormatter = new ChartYAxisValueFormatter();

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.statistics_screen);
        setTitle(R.string.statistics_text);

        Bundle extras = getIntent().getExtras();
        assert extras != null : "Open StatisticsScreen without extras";

        String dbPath = extras.getString(EXTRA_DBPATH);
        assert dbPath != null : "dbPath shouldn't be null";

        dbOpenHelper = AnyMemoDBOpenHelperManager.getHelper(this, dbPath);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        statisticsGraphFrame = (FrameLayout) findViewById(R.id.statistics_graph_frame);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initDrawer();

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // For the first execution to display default statistics info
        setTitle(R.string.number_of_cards_scheduled_in_a_day_text);
        new CardToReviewTask().execute((Void)null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initDrawer() {
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                 @Override
                 public boolean onNavigationItemSelected(MenuItem menuItem) {
                 switch (menuItem.getItemId()) {
                     case R.id.cards_scheduled_in_a_month_menu:
                         setTitle(R.string.number_of_cards_scheduled_in_a_day_text);
                         new CardToReviewTask()
                                 .execute((Void)null);
                         break;
                     case R.id.accumulative_cards_scheduled_menu:
                         setTitle(R.string.accumulative_cards_scheduled_text);
                         new AccumulativeCardsToReviewTask()
                                 .execute((Void) null);
                         break;
                     case R.id.new_cards_learned_in_the_past_month_menu:
                         setTitle(R.string.number_of_new_cards_learned_in_a_day_text);
                         new NewCardTask().execute((Void) null);
                         break;
                     case R.id.grade_statistics_menu:
                         setTitle(R.string.grade_statistics_text);
                         new GradeStatisticsTask()
                                 .execute((Void)null);
                         break;
                 }

                 menuItem.setChecked(true);
                 drawerLayout.closeDrawers();

                 return true;
                 }
             }
        );

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AnyMemoDBOpenHelperManager.releaseHelper(dbOpenHelper);
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

    private class NewCardTask extends ChartTask<Void, Void, BarData> {

        private static final int INITIAL_CAPACITY = 30;
        private static final int MILLISECONDS_PER_DAY = 60 * 60 * 24 * 1000;

        @Override
        public BarData doInBackground(Void... params) {
            cardDao = dbOpenHelper.getCardDao();
            List<String> xVals = new ArrayList<String>(INITIAL_CAPACITY);
            List<BarEntry> yVals = new ArrayList<BarEntry>(INITIAL_CAPACITY);
            Date now = new Date();
            for (int i = -INITIAL_CAPACITY; i < 1; i++) {
                Date endDate = new Date(now.getTime() + i * MILLISECONDS_PER_DAY);
                Date startDate = new Date(endDate.getTime() - MILLISECONDS_PER_DAY);
                xVals.add("" + i);
                yVals.add(new BarEntry((float)cardDao.getNewLearnedCardCount(null, startDate, endDate),
                                       INITIAL_CAPACITY + i)); // the order has to be nonnegative

            }

            BarDataSet dataSet = new BarDataSet(yVals, getString(R.string.number_of_new_cards_learned_in_a_day_text));
            BarData data = new BarData(xVals, dataSet);
            data.setValueTextColor(Color.WHITE);
            data.setValueFormatter(valueFormatter);

            return data;

        }

        @Override
        public Chart generateChart(BarData data) {
            BarChart chart = new BarChart(StatisticsScreen.this);
            chart.setDrawGridBackground(false);
            chart.getLegend().setTextColor(Color.WHITE);
            chart.getXAxis().setTextColor(Color.WHITE);
            chart.getAxisLeft().setTextColor(Color.WHITE);
            chart.getAxisRight().setTextColor(Color.WHITE);
            chart.getAxisLeft().setValueFormatter(yAxisValueFormatter);
            chart.getAxisRight().setValueFormatter(yAxisValueFormatter);

            chart.setData(data);
            chart.setDescription("");
            return chart;
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
                Date startDate = new Date(now.getTime() + i * 60 * 60 * 24 * 1000);
                Date endDate = new Date(startDate.getTime() + 1 * 60 * 60 * 24 * 1000);
                xVals.add("" + i);
                yVals.add(new BarEntry((float)cardDao.getScheduledCardCount(null, startDate, endDate), i));

            }

            BarDataSet dataSet = new BarDataSet(yVals, getString(R.string.number_of_cards_scheduled_in_a_day_text));
            BarData data = new BarData(xVals, dataSet);
            data.setValueTextColor(Color.WHITE);
            data.setValueFormatter(valueFormatter);

            return data;

        }

        @Override
        public Chart generateChart(BarData data) {
            BarChart chart = new BarChart(StatisticsScreen.this);
            chart.setDrawGridBackground(false);
            chart.getLegend().setTextColor(Color.WHITE);
            chart.getXAxis().setTextColor(Color.WHITE);
            chart.getAxisLeft().setTextColor(Color.WHITE);
            chart.getAxisRight().setTextColor(Color.WHITE);
            chart.getAxisLeft().setValueFormatter(yAxisValueFormatter);
            chart.getAxisRight().setValueFormatter(yAxisValueFormatter);

            chart.setData(data);
            chart.setDescription("");
            return chart;
        }
    }

    private class AccumulativeCardsToReviewTask extends ChartTask<Void, Void, BarData> {
        @Override
        public BarData doInBackground(Void... params) {
            cardDao = dbOpenHelper.getCardDao();
            List<String> xVals = new ArrayList<String>(30);
            List<BarEntry> yVals = new ArrayList<BarEntry>(30);

            Date now = new Date();
            Date startDate = new Date(0);
            for (int i = 0; i < 30; i++) {
                Date endDate = new Date(now.getTime() + (i + 1) * 60 * 60 * 24 * 1000);
                xVals.add("" + i);
                yVals.add(new BarEntry((float)cardDao.getScheduledCardCount(null, startDate, endDate), i));
            }

            BarDataSet dataSet = new BarDataSet(yVals, getString(R.string.accumulative_cards_scheduled_text));
            BarData data = new BarData(xVals, dataSet);
            data.setValueTextColor(Color.WHITE);
            data.setValueFormatter(valueFormatter);

            return data;
        }

        @Override
        public Chart generateChart(BarData data) {
            BarChart chart = new BarChart(StatisticsScreen.this);
            chart.setDrawGridBackground(false);
            chart.getLegend().setTextColor(Color.WHITE);
            chart.getXAxis().setTextColor(Color.WHITE);
            chart.getAxisLeft().setTextColor(Color.WHITE);
            chart.getAxisRight().setTextColor(Color.WHITE);
            chart.getAxisLeft().setValueFormatter(yAxisValueFormatter);
            chart.getAxisRight().setValueFormatter(yAxisValueFormatter);

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
            data.setValueFormatter(valueFormatter);

            return data;
        }

        @Override
        public Chart generateChart(PieData data) {
            PieChart chart = new PieChart(StatisticsScreen.this);
            chart.getLegend().setTextColor(Color.WHITE);
            chart.setData(data);
            chart.setDescription("");

            return chart;
        }
    }

    private static final class ChartValueFormatter implements ValueFormatter {
        private static final DecimalFormat formatter = new DecimalFormat("###,###,##0");

        public String getFormattedValue(float value, Entry entry, int dataSetIndex,
                                        ViewPortHandler viewPortHandler) {
            return formatter.format(value);
        }
    }

    private static final class ChartYAxisValueFormatter implements YAxisValueFormatter {
        private static final DecimalFormat formatter = new DecimalFormat("###,###,##0");

        public String getFormattedValue(float value, YAxis yAxis) {
            return formatter.format(value);
        }
    }

}
