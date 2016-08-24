package com.gibstudio.crosswalkembedded;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import org.xwalk.core.XWalkActivity;
import org.xwalk.core.XWalkGetBitmapCallback;
import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkUIClient;
import org.xwalk.core.XWalkView;
import org.xwalk.core.XWalkWebResourceRequest;
import org.xwalk.core.XWalkWebResourceResponse;

public class MainActivity extends XWalkActivity {

    public static final String TAG = "XWalkViewCallbacks";

    private XWalkView mXWalkView;

    @Override
    protected void onXWalkReady() {
        // preferences
//        XWalkPreferences.setValue(XWalkPreferences.ANIMATABLE_XWALK_VIEW, true);

        mXWalkView = (XWalkView) findViewById(R.id.xwalkview);

        // touch listener
        mXWalkView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.w(TAG, "onTouch: " + motionEvent);
                return false;
            }
        });

        // listeners
        mXWalkView.setResourceClient(new MyResourceClient(mXWalkView));
        mXWalkView.setUIClient(new MyUIClient(mXWalkView));

        // load url
        mXWalkView.load("http://stars.chromeexperiments.com/", null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // layout
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mXWalkView != null) {
            mXWalkView.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (mXWalkView != null) {
            mXWalkView.onNewIntent(intent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mXWalkView != null) {
            mXWalkView.pauseTimers();
            mXWalkView.onHide();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mXWalkView != null) {
            mXWalkView.resumeTimers();
            mXWalkView.onShow();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mXWalkView != null) {
            mXWalkView.onDestroy();
        }
    }

    @Override
    public void onBackPressed() {
        Log.w(TAG, "onBackPressed");

        super.onBackPressed();
    }

    /**
     * Example of XWalkResourceClient implementation
     */
    class MyResourceClient extends XWalkResourceClient {

        MyResourceClient(XWalkView view) {
            super(view);
        }

        @Override
        public void onLoadStarted(XWalkView view, String url) {
            Log.w(TAG, "onLoadStarted: " + url);
        }

        @Override
        public void onLoadFinished(XWalkView view, String url) {
            Log.w(TAG, "onLoadFinished: " + url);
        }

        @Override
        public void onProgressChanged(XWalkView view, int newProgress) {
            Log.w(TAG, "onProgressChanged: " + newProgress);
        }

        @Override
        public boolean shouldOverrideUrlLoading(XWalkView view, String url) {
            Log.w(TAG, "shouldOverrideUrlLoading: " + url);
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public XWalkWebResourceResponse shouldInterceptLoadRequest(XWalkView view,
                                                                   XWalkWebResourceRequest request) {
            Log.w(TAG, "shouldInterceptLoadRequest: url: " + request.getUrl()
                    + ", method: " + request.getMethod());
            return super.shouldInterceptLoadRequest(view, request);
        }
    }

    /**
     * Example of XWalkUIClient implementation
     */
    class MyUIClient extends XWalkUIClient {

        MyUIClient(XWalkView view) {
            super(view);
        }

        @Override
        public void onPageLoadStarted(XWalkView view, java.lang.String url) {
            Log.w(TAG, "onPageLoadStarted: " + url);
        }

        @Override
        public void onPageLoadStopped(XWalkView view, String url, LoadStatus status) {
            Log.w(TAG, "onPageLoadStopped: " + url + ", status: " + status);

            if (status == LoadStatus.FINISHED) {
                view.captureBitmapAsync(new XWalkGetBitmapCallback() {
                    @Override
                    public void onFinishGetBitmap(Bitmap bitmap, int i) {
                        Log.w(TAG, "onFinishGetBitmap: " + bitmap);
                    }
                });
            }
        }
    }
}
