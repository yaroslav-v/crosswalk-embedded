package com.gibstudio.crosswalkembedded;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceResponse;

import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkUIClient;
import org.xwalk.core.XWalkView;
import org.xwalk.core.internal.XWalkCookieManager;
import org.xwalk.core.internal.XWalkSettings;
import org.xwalk.core.internal.XWalkViewBridge;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "XWalkViewCallbacks";

    private XWalkView mXWalkView;
    private XWalkCookieManager mXCookieManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // preferences
        XWalkPreferences.setValue(XWalkPreferences.ANIMATABLE_XWALK_VIEW, true);

        // layout
        setContentView(R.layout.activity_main);
        mXWalkView = (XWalkView) findViewById(R.id.xwalkview);

        // listeners
        mXWalkView.setResourceClient(new MyResourceClient(mXWalkView));
        mXWalkView.setUIClient(new MyUIClient(mXWalkView));

        // cookie manager
        mXCookieManager = new XWalkCookieManager();
        mXCookieManager.setAcceptCookie(true);
        mXCookieManager.setAcceptFileSchemeCookies(true);
        mXCookieManager.setCookie("http://.vk.com/", "cookie=hi");

        // load url
        mXWalkView.load("http://stars.chromeexperiments.com/", null);
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
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP &&
                event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            Log.i(TAG, "dispatchKeyEvent: " + event.getKeyCode());
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                Log.i(TAG, "dispatchTouchEvent: " + event.getAction());
                break;
        }

        return super.dispatchTouchEvent(event);
    }

    /**
     * Example of default CookieManager and XWalkCookieManager sync
     *
     * @param cookieManager
     */
    private void syncCookieStores(CookieManager cookieManager) {
        List<HttpCookie> cookies = cookieManager.getCookieStore().getCookies();
        if (cookies.size() > 0) {
            mXCookieManager.removeExpiredCookie();

            for (HttpCookie cookie : cookies) {
                StringBuilder builder = new StringBuilder();
                if (cookie.getSecure()) {
                    builder.append("https://");
                } else {
                    builder.append("http://");
                }
                builder.append(cookie.getDomain());
                builder.append(cookie.getPath());

                mXCookieManager.setCookie(builder.toString(), cookie.toString());
            }

            cookieManager.getCookieStore().removeAll();
            mXCookieManager.flushCookieStore();
        }
    }

    /**
     * Example of XWalkSettings obtaining through reflection
     *
     * @param webView
     * @return XWalkView default User-Agent
     */
    private String getXWalkViewUserAgent(XWalkView webView) {
        try {
            Method ___getBridge = XWalkView.class.getDeclaredMethod("getBridge");
            ___getBridge.setAccessible(true);
            XWalkViewBridge xWalkViewBridge = null;
            xWalkViewBridge = (XWalkViewBridge) ___getBridge.invoke(webView);
            XWalkSettings xWalkSettings = xWalkViewBridge.getSettings();
            return xWalkSettings.getUserAgentString();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Returns TextureView which is used in XWalkView
     *
     * @param group
     * @return
     */
    private TextureView findXWalkTextureView(ViewGroup group) {
        int childCount = group.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = group.getChildAt(i);
            if (child instanceof TextureView) {
                String parentClassName = child.getParent().getClass().toString();
                boolean isRightKindOfParent = (parentClassName.contains("XWalk"));
                if (isRightKindOfParent) {
                    return (TextureView) child;
                }
            } else if (child instanceof ViewGroup) {
                TextureView textureView = findXWalkTextureView((ViewGroup) child);
                if (textureView != null) {
                    return textureView;
                }
            }
        }

        return null;
    }

    /**
     * Example of capturing image from XWalkView based on TextureView
     *
     * @return
     */
    public Bitmap captureImage() {
        if (mXWalkView != null) {
            Bitmap bitmap = null;

            boolean isCrosswalk = false;
            try {
                Class.forName("org.xwalk.core.XWalkView");
                isCrosswalk = true;
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (isCrosswalk) {
                try {
                    TextureView textureView = findXWalkTextureView(mXWalkView);
                    bitmap = textureView.getBitmap();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                bitmap = Bitmap.createBitmap(mXWalkView.getWidth(), mXWalkView.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas c = new Canvas(bitmap);
                mXWalkView.draw(c);

//                View view = mXWalkView.getRootView();
//                view.setDrawingCacheEnabled(true);
//                bitmap = Bitmap.createBitmap(view.getDrawingCache());
//                view.setDrawingCacheEnabled(false);
            }

            return bitmap;
        } else {
            return null;
        }
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
            Log.i(TAG, "onLoadStarted: " + url);
        }

        @Override
        public void onLoadFinished(XWalkView view, String url) {
            Log.i(TAG, "onLoadFinished: " + url);
        }

        @Override
        public void onProgressChanged(XWalkView view, int newProgress) {
            Log.i(TAG, "onProgressChanged: " + newProgress);
        }

        @Override
        public boolean shouldOverrideUrlLoading(XWalkView view, String url) {
            Log.i(TAG, "shouldOverrideUrlLoading: " + url);
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public WebResourceResponse shouldInterceptLoadRequest(XWalkView view, String url) {
            Log.i(TAG, "shouldInterceptLoadRequest: " + url);
            return super.shouldInterceptLoadRequest(view, url);
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
            Log.i(TAG, "onPageLoadStarted: " + url);
        }

        @Override
        public void onPageLoadStopped(XWalkView view, String url, LoadStatus status) {
            Log.i(TAG, "onPageLoadStopped: " + url + ", status: " + status);
        }
    }

}
