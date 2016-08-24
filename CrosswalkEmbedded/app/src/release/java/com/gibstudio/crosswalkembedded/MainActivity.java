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

import org.xwalk.core.XWalkCookieManager;
import org.xwalk.core.XWalkGetBitmapCallback;
import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkUIClient;
import org.xwalk.core.XWalkView;
import org.xwalk.core.XWalkWebResourceRequest;
import org.xwalk.core.XWalkWebResourceResponse;

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
    public void onBackPressed() {
        Log.w(TAG, "onBackPressed");

        super.onBackPressed();
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
     * Returns default User-Agent
     *
     * @param webView
     * @return XWalkView default User-Agent
     */
    private String getXWalkViewUserAgent(XWalkView webView) {
//        return webView.getSettings().getUserAgentString();
        return webView.getUserAgentString();
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
     * <br/><br/>
     * Use XWalkView.captureBitmapAsync(XWalkGetBitmapCallback callback) instead of this method
     *
     * @return Image of view's content
     */
    @Deprecated
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
                bitmap = Bitmap.createBitmap(mXWalkView.getWidth(), mXWalkView.getHeight(),
                        Bitmap.Config.ARGB_8888);
                Canvas c = new Canvas(bitmap);
                mXWalkView.draw(c);
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
