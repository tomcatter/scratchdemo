package com.scratch.example;

import android.app.Activity;
import android.os.Bundle;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ScratchView scratchView = (ScratchView) findViewById(R.id.customeView);
        scratchView.setText("刮刮卡");
        
    }
}
