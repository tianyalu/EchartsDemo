package com.sty.echarts;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.github.abel533.echarts.Option;
import com.github.abel533.echarts.axis.CategoryAxis;
import com.github.abel533.echarts.axis.ValueAxis;
import com.github.abel533.echarts.code.Orient;
import com.github.abel533.echarts.code.Position;
import com.github.abel533.echarts.code.Trigger;
import com.github.abel533.echarts.code.X;
import com.github.abel533.echarts.data.PieData;
import com.github.abel533.echarts.series.Bar;
import com.github.abel533.echarts.series.Line;
import com.github.abel533.echarts.series.Pie;
import com.sty.echarts.view.EChartsWebView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 参考API：http://echarts.baidu.com/tutorial.html
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnHistogramByJs;
    private Button btnLineByJs;
    private Button btnPieByJs;
    private Button btnHistogramByAndroid;
    private Button btnLineByAndroid;
    private Button btnPieByAndroid;
    private WebView wvHistogramByJs;
    private WebView wvLineByJs;
    private WebView wvPieByJs;
    private EChartsWebView wvHistogramByAndroid;
    private EChartsWebView wvLineByAndroid;
    private EChartsWebView wvPieByAndroid;


    private List<String> xAxisData = new ArrayList<>(
            Arrays.asList("1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月")
    );

    private List<String> phoneExpensesData = new ArrayList<>(
            Arrays.asList("100.5", "123", "86.5", "58.9", "90", "92.4", "88.7", "69.2", "98", "75", "97.8", "109")
    );
    private List<String> phoneflowData = new ArrayList<>(
            Arrays.asList("50", "48", "55", "99", "100", "98.9", "87.3", "55", "83", "59", "68", "88")
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        btnHistogramByJs = findViewById(R.id.btn_histogram_by_js);
        btnLineByJs = findViewById(R.id.btn_line_by_js);
        btnPieByJs = findViewById(R.id.btn_pie_by_js);
        btnHistogramByAndroid = findViewById(R.id.btn_histogram_by_android);
        btnLineByAndroid = findViewById(R.id.btn_line_by_android);
        btnPieByAndroid = findViewById(R.id.btn_pie_by_android);
        btnHistogramByJs.setOnClickListener(this);
        btnHistogramByAndroid.setOnClickListener(this);
        btnLineByJs.setOnClickListener(this);
        btnLineByAndroid.setOnClickListener(this);
        btnPieByJs.setOnClickListener(this);
        btnPieByAndroid.setOnClickListener(this);
        wvHistogramByJs = findViewById(R.id.wv_histogram_by_js);
        wvHistogramByAndroid = findViewById(R.id.wv_histogram_by_android);
        wvLineByJs = findViewById(R.id.wv_line_by_js);
        wvLineByAndroid = findViewById(R.id.wv_line_by_android);
        wvPieByJs = findViewById(R.id.wv_pie_by_js);
        wvPieByAndroid = findViewById(R.id.wv_pie_by_android);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_histogram_by_js:
                Log.i("sty", "button is clicked");
                setHistogramByJs();
                break;
            case R.id.btn_histogram_by_android:
                setHistogramEChartDataByAndroid();
                break;
            case R.id.btn_line_by_js:
                setLineByJs();
                break;
            case R.id.btn_line_by_android:
                setLineEChartDataByAndroid();
                break;
            case R.id.btn_pie_by_js:
                setPieByJs();
                break;
            case R.id.btn_pie_by_android:
                setPieEChartDataByAndroid();
                break;
            default:
                break;
        }
    }


    private void setHistogramByJs(){
        //开启本地文件读取（默认为true， 不设置也可以）
        wvHistogramByJs.getSettings().setAllowFileAccess(true);
        //允许JavaScript--important
        wvHistogramByJs.getSettings().setJavaScriptEnabled(true);
        wvHistogramByJs.loadUrl("file:///android_asset/echarts/myechart.html");

        wvHistogramByJs.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                wvHistogramByJs.loadUrl("javascript:createBarChart();");
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

    }

    private void setHistogramEChartDataByAndroid(){
        Option option = new Option();
        option.title().text("");
        option.tooltip().trigger(Trigger.axis); //触发类型-触发指示器
        option.legend().data("话费","流量");
        option.grid().containLabel(true); //防止标签溢出
        option.grid().show(false).left(6).right(6).bottom(30);

        Bar barPhoneExpenses = new Bar("话费");
        barPhoneExpenses.setData(phoneExpensesData);
        barPhoneExpenses.barWidth(50);
        barPhoneExpenses.label().normal().show(true).position(Position.inside).color("white"); //label样式
        barPhoneExpenses.stack("管控"); //柱状图叠加

        Bar barPhoneflow = new Bar("流量");
        barPhoneflow.setData(phoneflowData);
        barPhoneflow.barWidth(50);
        barPhoneflow.label().normal().show(true).setPosition(Position.top); //label样式
        barPhoneflow.stack("管控"); //柱状图叠加

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.boundaryGap(true); //值是在中间还是在原点
        xAxis.setData(xAxisData);
        xAxis.axisTick().show(false); //是否显示坐标轴刻度
        xAxis.axisLabel().interval(0); //x轴标签显示间隔数
        option.xAxis(xAxis);

        ValueAxis yAxis = new ValueAxis();
        option.yAxis(yAxis);

        option.series(barPhoneExpenses, barPhoneflow);

        wvHistogramByAndroid.setOption(option);
    }

    private void setLineByJs(){
        //开启本地文件读取（默认为true， 不设置也可以）
        wvLineByJs.getSettings().setAllowFileAccess(true);
        //允许JavaScript--important
        wvLineByJs.getSettings().setJavaScriptEnabled(true);
        wvLineByJs.loadUrl("file:///android_asset/echarts/myechart.html");

        wvLineByJs.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                wvLineByJs.loadUrl("javascript:createLineChart();");
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
    }

    private void setLineEChartDataByAndroid(){
        Option option = new Option();
        option.title().text("");
        option.tooltip().trigger(Trigger.axis); //触发类型-触发指示器
        option.legend().data("话费","流量");
        option.grid().containLabel(true); //防止标签溢出
        option.grid().show(false).left(6).right(6).bottom(30);

        Line linePhoneExpenses = new Line("话费");
        linePhoneExpenses.setData(phoneExpensesData);
        linePhoneExpenses.setSmooth(true);
        linePhoneExpenses.label().normal().show(true).setPosition(Position.top); //label样式

        Line linePhoneflow = new Line("流量");
        linePhoneflow.setData(phoneflowData);
        linePhoneflow.setSmooth(true);
        linePhoneflow.label().normal().show(true).setPosition(Position.bottom); //label样式

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.boundaryGap(true); //值是在中间还是在原点
        xAxis.setData(xAxisData);
        xAxis.axisTick().show(false); //是否显示坐标轴刻度
        xAxis.axisLabel().interval(0); //x轴标签显示间隔数
        option.xAxis(xAxis);

        ValueAxis yAxis = new ValueAxis();
        option.yAxis(yAxis);

        option.series(linePhoneExpenses, linePhoneflow);

        wvLineByAndroid.setOption(option);
    }

    private void setPieByJs(){
        //开启本地文件读取（默认为true， 不设置也可以）
        wvPieByJs.getSettings().setAllowFileAccess(true);
        //允许JavaScript--important
        wvPieByJs.getSettings().setJavaScriptEnabled(true);
        wvPieByJs.loadUrl("file:///android_asset/echarts/myechart.html");

        wvPieByJs.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                wvPieByJs.loadUrl("javascript:createPieChart();");
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
    }

    private List<PieData> genPieData(){
        List<PieData> list = new ArrayList<>();
        for(int i = 0; i < 12; i++){
            PieData pieData = new PieData(xAxisData.get(i), phoneExpensesData.get(i));
            list.add(pieData);
        }
        return list;
    }

    private void setPieEChartDataByAndroid(){
        Option option = new Option();
        option.title().text("12个月手机话费占比图").top(20).left(X.center);
        option.tooltip().trigger(Trigger.item) //触发类型-触发指示器
                .formatter("{a} <br/>{b} : {c} ({d}%)"); //饼图、雷达图 : a（系列名称），b（数据项名称），c（数值）, d（百分比）
        option.legend().orient(Orient.vertical)          //https://blog.csdn.net/mxdmojingqing/article/details/77340245
                .right(10).top(20).bottom(20).data(xAxisData);

        Pie piePhoneExpenses = new Pie("话费");
        piePhoneExpenses.setData(genPieData());
        piePhoneExpenses.radius("50%"); // 半径，相对于容器窄边一半的百分比
        piePhoneExpenses.center(new String[]{ "40%", "50%"}); //圆心位置相对于容器的百分比

        option.series(piePhoneExpenses);

        wvPieByAndroid.setOption(option);
    }
}
