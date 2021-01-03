package com.trionictuning.combilogger;

import android.content.Context;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.trionictuning.combilogger.logging.RealtimeManager;
import com.trionictuning.combilogger.logging.RealtimeValue;

public class MainActivity extends AppCompatActivity {
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            for(int i = 0; i < RealtimeManager.getInstance().getRTValueCount(); i++) {
                RealtimeValue value = RealtimeManager.getInstance().getRTValue(i);

                TextView tv = mRealtimeGrid.findViewWithTag(value.getName());

                if (tv != null) {
                    tv.setText(value.getValue());
                }
            }
        }
    };

    GridView mRealtimeGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RealtimeManager.getInstance().setHandler(mHandler);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mRealtimeGrid = findViewById(R.id.realtime_grid);

        mRealtimeGrid.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return RealtimeManager.getInstance().getNames().length;
            }

            @Override
            public Object getItem(int i) {
                return RealtimeManager.getInstance().getNames()[i];
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(int i, View convertView, ViewGroup parent) {
                LayoutInflater inflater = (LayoutInflater) MainActivity.this
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                View gridView = inflater.inflate(R.layout.realtime_view, null);

                TextView nameTv = gridView.findViewById(R.id.realtime_value_name);
                TextView valueTv = gridView.findViewById(R.id.realtime_value_text);

                nameTv.setText(RealtimeManager.getInstance().getNames()[i]);
                valueTv.setText("-----");
                valueTv.setTag(RealtimeManager.getInstance().getNames()[i]);

                return gridView;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_connect) {
            if (CombiLoggerApp.getInstance().startKWPSession()) {
                Toast.makeText(this, "Connected", Toast.LENGTH_SHORT);
            }
            return true;
        }

        if (id == R.id.action_disconnect) {
            CombiLoggerApp.getInstance().stopKWPSession();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}