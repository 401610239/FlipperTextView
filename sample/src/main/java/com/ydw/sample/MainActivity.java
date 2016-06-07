package com.ydw.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ydw.flippertextview.FlipperTextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ArrayList<String> mData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initDate();

        FlipperTextView mFlipper; mFlipper = ((FlipperTextView) this.findViewById(R.id.flipper));
        mFlipper.setData(mData);
        mFlipper.startFlipping();
    }

    private void initDate(){
        mData = new ArrayList();
        mData.add("第一条");
        mData.add("第二条");
        mData.add("第三条");
        mData.add("第四条");
        mData.add("第五条");
    }
}
