package com.ydw.flippertextview;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.util.ArrayList;

/**
 * 文本上下切换动画
 * Created by yaodawei on 2016/5/26.
 */
public class FlipperTextView extends TextSwitcher {
    private ArrayList<String> mData;
    private int currentPosition = 0;
    private static final String TAG = "ViewFlipper";
    private static final boolean LOGD = false;

    private static final int DEFAULT_INTERVAL = 3000;

    private int mFlipInterval = DEFAULT_INTERVAL;
    private boolean mAutoStart = false;

    private boolean mRunning = false;
    private boolean mStarted = false;
    private boolean mVisible = false;
    private boolean mUserPresent = true;


    public FlipperTextView(Context context) {
        this(context,null);
    }

    public FlipperTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFactory(new ViewFactory() {
            @Override
            public View makeView() {
                TextView textView = new TextView(getContext());
                textView.setTextSize(14);
                textView.setLines(1);
                textView.setLayoutParams(new LayoutParams(
                        android.widget.AbsListView.LayoutParams.MATCH_PARENT, android.widget.AbsListView.LayoutParams.MATCH_PARENT));
                textView.setGravity(Gravity.CENTER_VERTICAL);
                textView.setEllipsize(TextUtils.TruncateAt.END);
                textView.setTextColor(Color.parseColor("#333333"));
                return textView;
            }
        });
        setInAnimation(AnimationUtils.loadAnimation(getContext(),
                R.anim.notification_slide_in_bottom));
        setOutAnimation(AnimationUtils.loadAnimation(getContext(),
                R.anim.notification_slide_out_top));
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                mUserPresent = false;
                updateRunning();
            } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                mUserPresent = true;
                updateRunning(false);
            }
        }
    };

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        // Listen for broadcasts related to user-presence
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);

        // OK, this is gross but needed. This class is supported by the
        // remote views machanism and as a part of that the remote views
        // can be inflated by a context for another user without the app
        // having interact users permission - just for loading resources.
        // For exmaple, when adding widgets from a user profile to the
        // home screen. Therefore, we register the receiver as the current
        // user not the one the context is for.
        /*getContext().registerReceiverAsUser(mReceiver, android.os.Process.myUserHandle(),
                filter, null, mHandler);*/
        getContext().registerReceiver(mReceiver,filter);
        if (mAutoStart) {
            // Automatically start when requested
            startFlipping();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mVisible = false;

        getContext().unregisterReceiver(mReceiver);
        updateRunning();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        mVisible = visibility == VISIBLE;
        updateRunning(false);
    }

    /**
     * How long to wait before flipping to the next view
     *
     * @param milliseconds
     *            time in milliseconds
     */
    public void setFlipInterval(int milliseconds) {
        mFlipInterval = milliseconds;
    }

    /**
     * Start a timer to cycle through child views
     */
    public void startFlipping() {
        mStarted = true;
        updateRunning();
    }

    /**
     * No more flips
     */
    public void stopFlipping() {
        mStarted = false;
        updateRunning();
    }

    @Override
    public CharSequence getAccessibilityClassName() {
        return ViewFlipper.class.getName();
    }

    /**
     * Internal method to start or stop dispatching flip {@link Message} based
     * on {@link #mRunning} and {@link #mVisible} state.
     */
    private void updateRunning() {
        updateRunning(true);
    }

    /**
     * Internal method to start or stop dispatching flip {@link Message} based
     * on {@link #mRunning} and {@link #mVisible} state.
     *
     * @param flipNow Determines whether or not to execute the animation now, in
     *            addition to queuing future flips. If omitted, defaults to
     *            true.
     */
    private void updateRunning(boolean flipNow) {
        boolean running = mVisible && mStarted && mUserPresent;
        if (running != mRunning) {
            if (running) {
                if(mData != null && mData.size() > 0)
                    setText(mData.get(currentPosition));
                Message msg = mHandler.obtainMessage(FLIP_MSG);
                mHandler.sendMessageDelayed(msg, mFlipInterval);
            } else {
                mHandler.removeMessages(FLIP_MSG);
            }
            mRunning = running;
        }
        if (LOGD) {
            Log.d(TAG, "updateRunning() mVisible=" + mVisible + ", mStarted=" + mStarted
                    + ", mUserPresent=" + mUserPresent + ", mRunning=" + mRunning);
        }
    }

    /**
     * Returns true if the child views are flipping.
     */
    public boolean isFlipping() {
        return mStarted;
    }

    /**
     * Set if this view automatically calls {@link #startFlipping()} when it
     * becomes attached to a window.
     */
    public void setAutoStart(boolean autoStart) {
        mAutoStart = autoStart;
    }

    /**
     * Returns true if this view automatically calls {@link #startFlipping()}
     * when it becomes attached to a window.
     */
    public boolean isAutoStart() {
        return mAutoStart;
    }

    private final int FLIP_MSG = 1;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == FLIP_MSG) {
                if (mRunning) {
                    currentPosition++;
                    if(currentPosition >= mData.size())
                        currentPosition = 0;
                    if(mData != null && mData.size() > 0)
                        setText(mData.get(currentPosition));
                    msg = obtainMessage(FLIP_MSG);
                    sendMessageDelayed(msg, mFlipInterval);
                }
            }
        }
    };

    public void setData(ArrayList<String> data){
        this.mData = data;
    }

    public int getCurrentPosition(){
        return  currentPosition;
    }
}
