package com.sty.echarts;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

/**
 * 参考API：http://echarts.baidu.com/tutorial.html
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnHistogramByJs;
    private Button btnHistogramByAndroid;
    private WebView wvHistogramByJs;
    private WebView wvHistogramByAndroid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        btnHistogramByJs = findViewById(R.id.btn_histogram_by_js);
        btnHistogramByAndroid = findViewById(R.id.btn_histogram_by_android);
        btnHistogramByJs.setOnClickListener(this);
        btnHistogramByAndroid.setOnClickListener(this);
        wvHistogramByJs = findViewById(R.id.wv_histogram_by_js);
        wvHistogramByAndroid = findViewById(R.id.wv_histogram_by_android);

        //进行web view的一堆设置
        //开启本地文件读取（默认为true， 不设置也可以）
        wvHistogramByJs.getSettings().setAllowFileAccess(true);
        //允许JavaScript--important
        wvHistogramByJs.getSettings().setJavaScriptEnabled(true);
        wvHistogramByJs.loadUrl("file:///android_asset/echarts/myechart.html");

//        wvHistogramByJs.setWebViewClient(new WebViewClient() {
//            @Override
//            public void onPageFinished(WebView view, String url) {
////                runOnUiThread(new Runnable() {
////                    @Override
////                    public void run() {
//                        wvHistogramByJs.loadUrl("javascript:createBarLineChart();");
////                    }
////                });
//                super.onPageFinished(view, url);
//            }

//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                view.loadUrl(url);
//                return true;
//            }
//        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_histogram_by_js:
                Log.i("sty", "button is clicked");
                wvHistogramByJs.loadUrl("javascript:createBarLineChart();");
                break;
            case R.id.btn_histogram_by_android:
                setHistogramEChartData();
                break;
            default:
                break;
        }
    }


    private void setHistogramEChartData(){

    }
}
