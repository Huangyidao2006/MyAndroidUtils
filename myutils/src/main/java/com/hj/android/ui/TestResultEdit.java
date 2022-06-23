package com.hj.android.ui;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by hj at 2022/5/20 16:52
 */
public class TestResultEdit extends androidx.appcompat.widget.AppCompatEditText {
    private int mLineLimit = 1000;

    public TestResultEdit(Context context) {
        super(context);
    }

    public TestResultEdit(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TestResultEdit(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setLineLimit(int limit) {
        mLineLimit = limit;
    }

    public void append(String title, String text) {
        if (getLineCount() > mLineLimit) {
            setText("");
        }

        StringBuilder sb = new StringBuilder();
        sb.append(title).append(":\n").append(text).append("\n\n");

        append(text);
    }
}
