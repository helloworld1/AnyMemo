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
package org.liberty.android.fantastischmemo.downloader;

import java.util.HashMap;

public class DownloadItem{
    public static final int TYPE_CATEGORY = 1;
    public static final int TYPE_DATABASE = 2;
    public static final int TYPE_UP= 3;
    private int type;
    private String title = "";
    private String description = "";
    private String address = "";
    private HashMap<String, String> extras;

    public DownloadItem(){
        extras = new HashMap<String, String>();
    }

    public DownloadItem(int type, String title, String description, String address){
        this.type = type;
        this.title = title;
        this.description = description;
        this.address = address;
        extras = new HashMap<String, String>();
    }

    public DownloadItem clone(){
        DownloadItem newItem = new DownloadItem(this.type, this.title, this.description, this.address);
        newItem.extras = (HashMap<String, String>)this.extras.clone();
        return newItem;
    }

    public void setType(int type){
        this.type = type;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public void setAddress(String address){
        this.address = address;
    }

    public void setExtras(String key, String item){
        this.extras.put(key, item);
    }

    public int getType(){
        return type;
    }

    public String getTitle(){
        return title;
    }

    public String getDescription(){
        return description;
    }

    public String getAddress(){
        return address;
    }

    public String getExtras(String key){
        return this.extras.get(key);
    }


}
