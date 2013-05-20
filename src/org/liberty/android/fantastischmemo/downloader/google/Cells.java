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
package org.liberty.android.fantastischmemo.downloader.google;

import java.util.ArrayList;
import java.util.List;

public class Cells {

    private String worksheetName;

    private List<List<String>> rows;

    public Cells() {
        rows = new ArrayList<List<String>>();
    }

	public int getRowCounts() {
		return rows.size();
	}

    public List<String> getRow(int row) {
        return rows.get(row);
    }

    /* The rowNo and columnNo is starting from 1. The first cell is (1,1) */
	public void addCell(int rowNo, int columnNo, String data) {
        int rowsSize = rows.size();
        for (int i = 0; i < (rowNo - rowsSize); i++) {
            rows.add(new ArrayList<String>());
        }
        List<String> row = rows.get(rowNo - 1);

        int rowSize = row.size();
        for (int i = 0; i < (columnNo - rowSize); i++) {
            row.add("");
        }
        row.set(columnNo - 1, data);
	}

	@Override
	public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Worksheet: " + worksheetName);
        sb.append(" Cells data: ");

        for (int i = 0; i < rows.size(); i++) {
            sb.append(" Row " + i + ": ");
            for (String s : rows.get(i)) {
                sb.append(s + ", ");
            }
        }
        return sb.toString();
	}

	public String getWorksheetName() {
		return worksheetName;
	}

	public void setWorksheetName(String worksheetName) {
		this.worksheetName = worksheetName;
	}
}
