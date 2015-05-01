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
package org.liberty.android.fantastischmemo.downloader.quizlet;

import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class QuizletLauncher extends AMActivity implements OnClickListener {
    private Button searchTagButton;

    private Button searchUserButton;

    private Button userPrivateButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quizlet_launcher);

        searchTagButton = (Button) findViewById(R.id.quizlet_search_tag);
        searchUserButton = (Button) findViewById(R.id.quizlet_search_user);
        userPrivateButton = (Button) findViewById(R.id.quizlet_private_cards);
        searchTagButton.setOnClickListener(this);
        searchUserButton.setOnClickListener(this);
        userPrivateButton.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        if (v == searchTagButton) {
            Intent intent = new Intent(this, QuizletSearchByTitleActivity.class);
            startActivity(intent);
        }
        if (v == searchUserButton) {
            Intent intent = new Intent(this,
                    QuizletSearchByUsernameActivity.class);
            startActivity(intent);
        }
        if (v == userPrivateButton) {
            Intent intent = new Intent(this, QuizletUserPrivateActivity.class);
            startActivity(intent);
        }

    }
}
