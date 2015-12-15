package com.mobilecomputing.alarmanlage2015.alarmanlageapp;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;

/**
 * Created by Donskelle-PC on 15.12.2015.
 */
public class SettingsActivity extends Activity {

    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radio_group);

        editText = new EditText(getApplicationContext());
        editText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));


        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.emailRadio:
                        group.removeView(editText);
                        group.addView(editText, 2);
                        editText.setText("");
                        editText.setHint(R.string.hintemail);
                        editText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                        editText.setError(editText.getHint() + " wird benötigt.");
                        break;
                    case R.id.smsRadio:
                        group.removeView(editText);
                        group.addView(editText, 3);
                        editText.setText("");
                        editText.setHint(R.string.hintsms);
                        editText.setInputType(InputType.TYPE_NUMBER_VARIATION_NORMAL);
                        editText.setError(editText.getHint() + " wird benötigt.");
                        break;
                }
            }

            ;
        });
    }
}
