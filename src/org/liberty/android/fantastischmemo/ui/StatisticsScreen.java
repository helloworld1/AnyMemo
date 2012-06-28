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

import org.achartengine.GraphicalView;

import org.achartengine.chart.BarChart;

import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;

import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import org.achartengine.renderer.XYMultipleSeriesRenderer.Orientation;

import org.liberty.android.fantastischmemo.AMActivity;

import android.graphics.Color;

import android.os.Bundle;

public class StatisticsScreen extends AMActivity {
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        XYSeries series = new XYSeries("Title");
        series.add(1,2);
        series.add(2,3);
        series.add(3,4);
        series.add(4,3);
        XYSeries series2 = new XYSeries("motto");
        series2.add(1,8);
        series2.add(2,4);
        series2.add(3,7);
        series2.add(4,2);
        dataset.addSeries(series);
        dataset.addSeries(series2);
        
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();

        //renderer.setOrientation(Orientation.VERTICAL);
        SimpleSeriesRenderer r1 = new SimpleSeriesRenderer();
        r1.setColor(Color.RED);
        r1.setDisplayChartValues(true);
        SimpleSeriesRenderer r2 = new SimpleSeriesRenderer();
        r2.setColor(Color.GREEN);
        r2.setDisplayChartValues(true);
        renderer.addSeriesRenderer(r1);
        renderer.addSeriesRenderer(r2);

        BarChart chart = new BarChart(dataset, renderer, BarChart.Type.STACKED);

        GraphicalView gv = new GraphicalView(this, chart);
        setContentView(gv);

    }
}
