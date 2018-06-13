package com.sty.echarts.view;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.github.abel533.echarts.Option;
import com.google.gson.Gson;

/**
 * 用百度的ECharts(http://echarts.baidu.com)显示图表的WebView,需要显示怎样的图表可以根据百度
 * <a>http://echarts.baidu.com/option.html#title</a>来配置Option的参数。
 *
 * 正常使用，请先完成以下三步：
 *      1、引入gradle: com.github.abel533:ECharts:<version> 、com.google.code.gson:gson:<version>
 *      2、在http://echarts.baidu.com下载echarts的js文件，
 *         放入.assets/js/echarts.js(如果文件名有变，请自行更新html的<script src="./js/echarts.js"></script>)
 *      3、必须在assets的文件夹下添加名为index.html、内容为以下的文件
 *==============index.html文件 start=====================
     <!DOCTYPE html>
     <html>
     <head>
         <meta charset="utf-8">
         <title>ECharts</title>
         <style>
             html,body{
                 width:100%;
                 height:100%;
                 overflow:hidden;
                 scroll:no;
                 padding:0;
                 margin:0;
             }
             #main{
                 padding:0;
                 margin:0;
                 width:100%;height:100%;overflow:hidden;scroll:no;
            }
         </style>
        <!-- ECharts单文件引入 -->
        <script src="./js/echarts.js"></script>
     </head>
     <body>
         <!-- 为ECharts准备一个具备大小（宽高）的Dom -->
         <div id="main"></div>
         <script type="text/javascript">

             var worldMapContainer = document.getElementById('main');

             //用于使chart自适应高度和宽度,通过窗体高宽计算容器高宽
             var resizeWorldMapContainer = function () {
                 worldMapContainer.style.width = window.innerWidth+'px';
                 worldMapContainer.style.height = window.innerHeight+'px';
             };
             //设置容器高宽
             resizeWorldMapContainer();

             var myChart = echarts.init(document.getElementById('main'));

             function setOption(option){
                 myChart.setOption(option);
                 //用于使chart自适应高度和宽度
                 window.onresize = function () {
                     //重置容器高宽
                     resizeWorldMapContainer();
                     myChart.resize();
                };
             }

             function clear(){
                myChart.clear();
             }
         </script>
     </body>
     </html>
     * ==============index.html文件 end=====================
 * @Auther Steven.S
 * @Date 2018/6/13
 * @Email styzf@qq.com
 */

public class EChartsWebView extends WebView {
    private String jsonOption;
    private OnWebViewClickListener mListener;

    public EChartsWebView(Context context) {
        super(context);
        init();
    }

    public EChartsWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EChartsWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public EChartsWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    protected void init(){
        getSettings().setAllowFileAccess(true);
        getSettings().setJavaScriptEnabled(true); //important

        ClickEvent clickEvent = new ClickEvent() {
            @Override
            public void onClick(String componentType, String seriesIndes, int dataIndex, Object data, String color) {

            }

            @Override
            public void onDoubleClick(String componentType, String seriesType, int seriesIndex, int dataIndex, Object data, String color) {

            }

            @Override
            public void mouseover(String componentType, String seriesType, int seriesIndex, int dataIndex, Object data, String color) {

            }

            @Override
            public void mouseout(String componentType, String seriesType, int seriesIndex, int dataIndex, Object data, String color) {

            }
        };

        addJavascriptInterface(clickEvent, "clickEvent");
        loadUrl("file:///android_asset/index.html");

        setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url){
                super.onPageFinished(view, url);

                if(!TextUtils.isEmpty(jsonOption)){
                    setOption(jsonOption);
                }

                //todo EventBus
//                ChartWebViewPageFinishedEvent event = new ChartWebViewPageFinishedEvent();
//                if(EventBus.getDefault().hasSubscriberForEvent(ChartWebViewPageFinishedEvent.class)){
//                    EventBus.getDefault().post(event);
//                }
            }
        });
    }

    public void setOnWvClickListener(OnWebViewClickListener listener){
        this.mListener = listener;
    }

    public abstract static class OnWebViewClickListener{
        public void onClick(EventData data){};

        public void onSeriesClick(EventData data, int position){};
    }

    public void setOption(Option option){
        if(option == null){
            option = new Option();
        }
        Gson gson = new Gson();
        String jsonOption = gson.toJson(option);
        setOption(jsonOption);
    }

    public void setOption(String jsonOption){
        this.jsonOption = jsonOption;
        loadUrl("javascript:setOption(" + jsonOption + ")");
    }

    public void select(int current, int select){
        loadUrl("javascript:select(" + current + "," + select + ")");
    }

    public void clear(){
        loadUrl("javascript:clear()");
    }

    private abstract static class ClickEvent{
        /**
         *  点击事件
         */
        @JavascriptInterface
        public abstract void onClick(String componentType, String seriesIndes, int dataIndex, Object data, String color);

        /**
         *  双击事件
         */
        @JavascriptInterface
        public abstract void onDoubleClick(String componentType, String seriesType, int seriesIndex, int dataIndex, Object data, String color);

        /**
         *  移入事件
         */
        @JavascriptInterface
        public abstract void mouseover(String componentType, String seriesType, int seriesIndex, int dataIndex, Object data, String color);

        /**
         *  移出事件
         */
        @JavascriptInterface
        public abstract void mouseout(String componentType, String seriesType, int seriesIndex, int dataIndex, Object data, String color);
    }

    public class EventData{
        // 当前点击的图形元素所属的组件名称
        // 其值如'series'，'markLine'，'markPoint'，'timeLine'等
        private String componentType;

        // 系列类型。
        // 其值如'line'，'bar'，'pie'等，当componentType 为'series'时有意义
        private String seriesType;

        // 系列在传入当option.series中的index，当componentType 为'series'时有意义
        private int seriesIndex;

        // 数据在传入的data数组中的index
        private int dataIndex;

        // 传入的原始数据项
        private Object data;

        // 数据图形的颜色，当componentType为'series'时有意义
        private String color;

        public String getComponentType() {
            return componentType;
        }

        public void setComponentType(String componentType) {
            this.componentType = componentType;
        }

        public String getSeriesType() {
            return seriesType;
        }

        public void setSeriesType(String seriesType) {
            this.seriesType = seriesType;
        }

        public int getSeriesIndex() {
            return seriesIndex;
        }

        public void setSeriesIndex(int seriesIndex) {
            this.seriesIndex = seriesIndex;
        }

        public int getDataIndex() {
            return dataIndex;
        }

        public void setDataIndex(int dataIndex) {
            this.dataIndex = dataIndex;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }
    }
}
