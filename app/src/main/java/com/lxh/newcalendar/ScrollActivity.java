package com.lxh.newcalendar;

import android.app.Activity;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by liuxiaohui on 4/8/16.
 */
public class ScrollActivity extends Activity {
    private ImageView mImageView;
    private CustomScrollView mCustomView;
    private CalendarLifterView mLifterView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scroll);
        mImageView = (ImageView) findViewById(R.id.image);
        mCustomView = (CustomScrollView) findViewById(R.id.custom_view);
        mLifterView = (CalendarLifterView) findViewById(R.id.lifter_view);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.up_btn:
                mImageView.scrollBy(0, -20);
                mCustomView.scrollBy(0, 20);
                mLifterView.scrollBy(0, 20);
                break;
            case R.id.down_btn:
                mImageView.scrollBy(0, 20);
                mCustomView.scrollBy(0, -20);
                mLifterView.scrollBy(0, -20);
                break;
        }
    }
}
