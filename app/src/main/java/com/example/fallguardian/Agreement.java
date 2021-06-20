package com.example.fallguardian;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

public class Agreement extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle("Terms");
        setContentView(R.layout.activity_agreement);

        CheckBox checkBox = findViewById(R.id.checkID);
        TextView foo = (TextView)findViewById(R.id.agreement_textID);
        foo.setText(Html.fromHtml(getString(R.string.agreement_text_html)));

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(Agreement.this,SensorActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }
}