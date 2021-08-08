package com.example.fallguardian;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

public class StatsActivity extends AppCompatActivity {
    TextView tvEmergency, tvFalseAlarm, tvDetectedFall, tvAccuracy;
    PieChart pieChart;


    DatabaseHelper fall_db;

    int fn=0,fp=0,tp=0;

    double accuracy;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        setTitle("Statistics");

        tvEmergency = findViewById(R.id.tvEmergency);
        tvFalseAlarm= findViewById(R.id.tvFalseAlarm);
        tvDetectedFall = findViewById(R.id.tvDetectedFall);
        tvAccuracy = findViewById(R.id.tvAccuracy);
        pieChart = findViewById(R.id.piechart);

        fall_db = new DatabaseHelper(this);

        getAndSetData();
    }

    private void getAndSetData() {

            Cursor res = fall_db.getAllData();

            StringBuffer stringBuffer = new StringBuffer();


            if(res.getCount()!=0){
                while (res.moveToNext()){
                    fn+=new Integer(res.getString(2));
                    fp+=new Integer(res.getString(3));
                    tp+=new Integer(res.getString(4));
                }
            }
            else{
                Log.i("STATS"," --------------------> Empty");
            }

            accuracy = fn+tp+fp;

            if(accuracy!=0){
                accuracy = (tp/accuracy)*100;
            }
            else{
                accuracy = 100.00;
            }
            setData();

    }

    private void setData(){
        tvEmergency.setText(Integer.toString(fn));
        tvFalseAlarm.setText(Integer.toString(fp));
        tvDetectedFall.setText(Integer.toString(tp));

        accuracy = (double) Math.round(accuracy * 100) / 100;
        tvAccuracy.setText(Double.toString(accuracy)+"%");

        pieChart.addPieSlice(
                new PieModel(
                        "Emerygency Alert",
                        Integer.parseInt(tvEmergency.getText().toString()),
                        Color.parseColor("#C30606")));
        pieChart.addPieSlice(
                new PieModel(
                        "False Alarm",
                        Integer.parseInt(tvFalseAlarm.getText().toString()),
                        Color.parseColor("#0000FF")));
        pieChart.addPieSlice(
                new PieModel(
                        "Detected Fall",
                        Integer.parseInt(tvDetectedFall.getText().toString()),
                        Color.parseColor("#32CD32")));
    }
}