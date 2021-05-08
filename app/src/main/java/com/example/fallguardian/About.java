package com.example.fallguardian;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

public class About extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle("Terms & Conditions");
        setContentView(R.layout.activity_about);
        TextView foo = (TextView)findViewById(R.id.about_textID);
        foo.setText(Html.fromHtml(getString(R.string.agreement_text_html)));
    }
}