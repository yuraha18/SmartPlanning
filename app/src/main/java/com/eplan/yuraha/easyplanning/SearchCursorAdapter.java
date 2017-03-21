package com.eplan.yuraha.easyplanning;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eplan.yuraha.easyplanning.DBClasses.DBHelper;
import com.eplan.yuraha.easyplanning.DBClasses.SPDatabase;

/**
 * Created by yuraha18 on 3/18/2017.
 */

public class SearchCursorAdapter extends CursorAdapter {
    private LayoutInflater cursorInflater;

    // Default constructor
    public SearchCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
        cursorInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

    }

    public void bindView(final View view, Context context, final Cursor cursor) {

        TextView textViewTitle = (TextView) view.findViewById(R.id.textView1);
        final String title = cursor.getString(cursor.getColumnIndex("TASK_TEXT"));

            textViewTitle.setText(title);
            textViewTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cursor.close();
                    Intent intent = new Intent(view.getContext(), MainActivity.class);
                    intent.putExtra("searchQuery", title);
                    view.getContext().startActivity(intent);
                }
            });



    }

    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // R.layout.list_row is your xml layout for each row
        return cursorInflater.inflate( R.layout.item_layout, parent, false);
    }
}
