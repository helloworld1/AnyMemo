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

import java.sql.SQLException;

import java.util.Calendar;
import java.util.Date;

import org.achartengine.GraphicalView;

import org.achartengine.chart.BarChart;

import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;

import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import org.apache.mycommons.lang3.time.DateUtils;

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

import android.widget.FrameLayout;

public class StatisticsScreen extends AMActivity {
    private static final String TAG = "StatisticsScreen";

    public static final String EXTRA_DBPATH = "dbpath";

    private AMSpinner typeSelectSpinner;
    private FrameLayout statisticsGraphFrame;
    private InitTask initTask;
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

        initTask = new InitTask();
        initTask.execute((Void)null);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AnyMemoDBOpenHelperManager.releaseHelper(dbOpenHelper);
    }

    private GraphicalView generateBarGraph(String title, int[] x_values, int[] y_values) {
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        XYSeries series = new XYSeries(title);
        assert x_values.length == y_values.length : "The number of x values should match the number of y values";
        for(int i = 0; i < x_values.length; i++) {
            series.add(x_values[i], y_values[i]);
        }
        dataset.addSeries(series);
        
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();

        //renderer.setOrientation(Orientation.VERTICAL);
        SimpleSeriesRenderer r1 = new SimpleSeriesRenderer();
        r1.setColor(Color.RED);
        r1.setDisplayChartValues(true);
        renderer.addSeriesRenderer(r1);

        BarChart chart = new BarChart(dataset, renderer, BarChart.Type.DEFAULT);

        GraphicalView gv = new GraphicalView(this, chart);
        return gv;
    }

    private class InitTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;
        private int[] x_values;
        private int[] y_values;

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
        public Void doInBackground(Void... params) {
            try {
                cardDao = dbOpenHelper.getCardDao();
            } catch(SQLException e) {
                throw new RuntimeException(e);
            }
            x_values = new int[30];
            y_values = new int[30];
            Date now = new Date();
            for (int i = 0; i < 30; i++) {
                Date startDate = DateUtils.addDays(now, i);
                DateUtils.truncate(startDate, Calendar.DAY_OF_MONTH);
                Date endDate = DateUtils.addDays(startDate, 1);
                x_values[i] = i;
                y_values[i] = (int)cardDao.getScheduledCardCount(null, startDate, endDate);
            }
            return null;

        }

        @Override
        public void onPostExecute(Void result){
            GraphicalView gv = generateBarGraph(getString(R.string.number_of_cards_scheduled_in_a_day_text), x_values, y_values);
            statisticsGraphFrame.removeAllViews();
            statisticsGraphFrame.addView(gv);
            progressDialog.dismiss();
        }
    }
}
