package com.shi1d.viewtrick_project;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.shi1d.viewtrick.ViewTrick;
import com.shie1d.viewtrick.annos.BindView;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tv)
    public TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewTrick.bind(this);
        Log.e("MainActivity", tv == null ? "don't init" : tv.toString());
    }
}