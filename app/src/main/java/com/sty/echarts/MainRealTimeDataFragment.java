package com.sty.echarts;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.github.abel533.echarts.Option;
import com.github.abel533.echarts.axis.Axis;
import com.github.abel533.echarts.axis.CategoryAxis;
import com.github.abel533.echarts.axis.ValueAxis;
import com.github.abel533.echarts.code.Trigger;
import com.github.abel533.echarts.series.Line;
import com.huaguoshan.steward.R;
import com.huaguoshan.steward.api.ApiCallback;
import com.huaguoshan.steward.api.ApiClient;
import com.huaguoshan.steward.application.AppConfig;
import com.huaguoshan.steward.base.BaseFragment;
import com.huaguoshan.steward.base.BaseResult;
import com.huaguoshan.steward.custom.ContentViewId;
import com.huaguoshan.steward.event.ConvertStoreEvent;
import com.huaguoshan.steward.model.SalesDaily;
import com.huaguoshan.steward.model.SalesDailys;
import com.huaguoshan.steward.model.SalesReal;
import com.huaguoshan.steward.model.UserSet;
import com.huaguoshan.steward.ui.fragment.chart.LineChartFragment;
import com.huaguoshan.steward.ui.view.ATextView;
import com.huaguoshan.steward.ui.view.EChartsLineWebView;
import com.huaguoshan.steward.ui.view.EChartsWebView;
import com.huaguoshan.steward.utils.MathUtils;
import com.huaguoshan.steward.utils.SuperToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.R.string.yes;

/**
 * 实时数据
 *
 * @Author lankang
 * @Date 16/12/23
 */

@ContentViewId(R.layout.fragment_real_time_data)
public class MainRealTimeDataFragment extends BaseFragment {

    @Bind(R.id.login_progress)
    ProgressBar loginProgress;
    @Bind(R.id.tv_each_damage)
    TextView tvDamage;
    @Bind(R.id.tv_retail_amount)
    ATextView tvRetailAmount;
    @Bind(R.id.tv_user_number)
    TextView tvUserNumber;
    @Bind(R.id.tv_profit)
    TextView tvProfit;
    @Bind(R.id.tv_each_income)
    TextView tvEachIncome;
    @Bind(R.id.tv_client_deposit_number)
    TextView tvClientDepositNumber;
    @Bind(R.id.tv_deposit_amount)
    TextView tvDepositAmount;
    @Bind(R.id.rb_retail_amount)
    RadioButton rbRetailAmount;
    @Bind(R.id.rb_retail_profit)
    RadioButton rbRetailProfit;
    @Bind(R.id.rg_retail)
    RadioGroup rgRetail;
    @Bind(R.id.layout_form)
    SwipeRefreshLayout layoutForm;
    @Bind(R.id.lineChart_retail)
    EChartsWebView retailWebView;
    @Bind(R.id.lineChart_user)
    EChartsWebView userWebView;

    private SalesReal salesReal;

    private List<SalesDaily> todaySalesDailyList;
    private List<SalesDaily> yesterdaySalesDailyList;

    private boolean firstInitDataComplete = false;

    public static MainRealTimeDataFragment newInstance() {
        return new MainRealTimeDataFragment();
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        ButterKnife.bind(this, rootView);

        EventBus.getDefault().register(this);

        initData();
        initView();
        addViewListener();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        //防止 ScrollView 滑动到底部,所以让顶部的 TextView 获取焦点
        tvRetailAmount.setFocusable(true);
        tvRetailAmount.setFocusableInTouchMode(true);
        tvRetailAmount.requestFocus();
    }

    private void addViewListener() {

        rgRetail.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (todaySalesDailyList!=null){
                    setRetailFragment();
                }
            }
        });

        layoutForm.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                firstInitDataComplete = false;
                Call<BaseResult<SalesReal>> call1 = ApiClient.getApi().getSalesReal();
                Call<BaseResult<SalesDailys>> call2 = ApiClient.getApi().getSalesDaily();

                call1.enqueue(new ApiCallback<BaseResult<SalesReal>>(getActivity().getClass()) {
                    @Override
                    public void success(Call<BaseResult<SalesReal>> call, Response<BaseResult<SalesReal>> response, BaseResult<SalesReal> result) {
                        if (firstInitDataComplete){
                            layoutForm.setRefreshing(false);
                        }
                        firstInitDataComplete = true;
                        if (result.isSuccess()){
                            salesReal = result.getBody();
                            setSalesRealView(false);
                        }else{
                            SuperToastUtils.showError(result.getMsg());
                        }

                    }

                    @Override
                    public void error(Call<BaseResult<SalesReal>> call, Response<BaseResult<SalesReal>> response) {
                        if (firstInitDataComplete){
                            layoutForm.setRefreshing(false);
                        }
                        firstInitDataComplete = true;
                        SuperToastUtils.showError("加载实时销售数据失败");
                    }

                    @Override
                    public void failure(Call<BaseResult<SalesReal>> call, Throwable t) {
                        if (firstInitDataComplete){
                            layoutForm.setRefreshing(false);
                        }
                        firstInitDataComplete = true;
                        SuperToastUtils.showError("加载实时销售数据失败");
                    }
                });

                call2.enqueue(new ApiCallback<BaseResult<SalesDailys>>(getActivity().getClass()) {
                    @Override
                    public void success(Call<BaseResult<SalesDailys>> call, Response<BaseResult<SalesDailys>> response, BaseResult<SalesDailys> result) {
                        if (firstInitDataComplete){
                            layoutForm.setRefreshing(false);
                        }
                        firstInitDataComplete = true;
                        if (result.isSuccess()){
                            todaySalesDailyList = result.getBody().getToday();
                            yesterdaySalesDailyList = result.getBody().getYesterday();
                            setSalesDailyView();
                        }else{
                            SuperToastUtils.showError(result.getMsg());
                        }
                    }

                    @Override
                    public void error(Call<BaseResult<SalesDailys>> call, Response<BaseResult<SalesDailys>> response) {
                        if (firstInitDataComplete){
                            layoutForm.setRefreshing(false);
                        }
                        firstInitDataComplete = true;
                        SuperToastUtils.showError("加载报表销售数据失败");
                    }

                    @Override
                    public void failure(Call<BaseResult<SalesDailys>> call, Throwable t) {
                        if (firstInitDataComplete){
                            layoutForm.setRefreshing(false);
                        }
                        firstInitDataComplete = true;
                        SuperToastUtils.showError("加载报表销售数据失败");
                    }
                });
            }
        });
    }

    private void initView() {
        rbRetailAmount.setChecked(true);
        layoutForm.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
        //设置字体
        Typeface typeFace = Typeface.createFromAsset(getActivity().getAssets(),"fonts/universltstd_cn.otf");
//        tvRetailAmount.setTypeface(typeFace);
//        tvUserNumber.setTypeface(typeFace);
    }

    private void initData() {
        showProgress(true);

        Call<BaseResult<SalesReal>> call1 = ApiClient.getApi().getSalesReal();
        Call<BaseResult<SalesDailys>> call2 = ApiClient.getApi().getSalesDaily();


        call1.enqueue(new ApiCallback<BaseResult<SalesReal>>(getActivity().getClass()) {
            @Override
            public void success(Call<BaseResult<SalesReal>> call, Response<BaseResult<SalesReal>> response, BaseResult<SalesReal> result) {

                if (firstInitDataComplete){
                    showProgress(false); }
                firstInitDataComplete = true;
                if (result.isSuccess()){
                    salesReal = result.getBody();
                    setSalesRealView(true);
                }else{
                    SuperToastUtils.showError(result.getMsg());
                }
            }

            @Override
            public void error(Call<BaseResult<SalesReal>> call, Response<BaseResult<SalesReal>> response) {
                if (firstInitDataComplete){
                    showProgress(false);
                }
                firstInitDataComplete = true;
                SuperToastUtils.showError("加载实时销售数据失败");
            }

            @Override
            public void failure(Call<BaseResult<SalesReal>> call, Throwable t) {
                if (firstInitDataComplete){
                    showProgress(false);
                }
                firstInitDataComplete = true;
                SuperToastUtils.showError("加载实时销售数据失败");
            }
        });

        call2.enqueue(new ApiCallback<BaseResult<SalesDailys>>(getActivity().getClass()) {
            @Override
            public void success(Call<BaseResult<SalesDailys>> call, Response<BaseResult<SalesDailys>> response, BaseResult<SalesDailys> result) {
                if (firstInitDataComplete){
                    showProgress(false);
                }
                firstInitDataComplete = true;
                if (result.isSuccess()){
                    todaySalesDailyList = result.getBody().getToday();
                    yesterdaySalesDailyList = result.getBody().getYesterday();
                    setSalesDailyView();
                }else{
                    SuperToastUtils.showError(result.getMsg());
                }
            }

            @Override
            public void error(Call<BaseResult<SalesDailys>> call, Response<BaseResult<SalesDailys>> response) {
                if (firstInitDataComplete){
                    showProgress(false);
                }
                firstInitDataComplete = true;
                SuperToastUtils.showError("加载报表销售数据失败");
            }

            @Override
            public void failure(Call<BaseResult<SalesDailys>> call, Throwable t) {
                if (firstInitDataComplete){
                    showProgress(false);
                }
                firstInitDataComplete = true;
                SuperToastUtils.showError("加载报表销售数据失败");
            }
        });

    }

    /**
     * 设置顶部的实时数据
     * @param isClear true,「销售额」会主动置零;false 则不会
     */
    private void setSalesRealView(boolean isClear){
        if (isClear){
            //在更换门店后主动置零
            tvRetailAmount.setText("0.00");
        }
//        setTextWithObjectAnimator(tvRetailAmount,(float) MathUtils.penny2dollar(salesReal.getSales_amount()));
        tvRetailAmount.setText(String.valueOf(MathUtils.penny2dollar(salesReal.getSales_amount())));
        tvUserNumber.setText(String.valueOf(salesReal.getPassenger_flow()));
        tvClientDepositNumber.setText(String.valueOf(salesReal.getClient_recharge_number()));
        tvDepositAmount.setText(String.valueOf(MathUtils.penny2dollar(salesReal.getClient_recharge_amount())));
        tvProfit.setText(String.valueOf(MathUtils.penny2dollar(salesReal.getEstimates_profit())));
        tvEachIncome.setText(String.valueOf(MathUtils.penny2dollar(salesReal.getEach_profit())));
        tvDamage.setText(String.valueOf(MathUtils.penny2dollar(salesReal.getScrap_value())));
    }


    /**
     * 设置 TextView 的增加动画
     * mDuration,动画的时长;mFormat,格式化数字
     * @param textView
     * @param num 目标数字
     */
    private int mDuration = 1500;
    private DecimalFormat mFormat = new DecimalFormat("##0.00");
    private void setTextWithAnimator(final TextView textView, float num){
        ValueAnimator animator = ValueAnimator.ofFloat(0.0f,num);
        animator.setDuration(mDuration);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                textView.setText(mFormat.format(animation.getAnimatedValue()));
            }
        });
        animator.setStartDelay(1500);
        animator.start();
    }
    private void setTextWithObjectAnimator(final TextView textView, float num){
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(textView, "text",0f,num);
        objectAnimator.setDuration(mDuration);
        objectAnimator.setInterpolator(new LinearInterpolator());
        objectAnimator.setStartDelay(1500);
        objectAnimator.start();
    }

    private void setSalesDailyView(){
        setUserFragment();
        setRetailFragment();
    }

    private void setRetailFragment() {

        Option option = new Option();
        option.title().text("");
        option.tooltip().trigger(Trigger.axis);
        option.legend().data("今日","昨日");
        option.grid().containLabel(true).top(30).bottom(10);

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.boundaryGap(false);
        List<Integer> xAxisList = new LinkedList<>();

        Map<Integer,Double> todayMap = new HashMap<>();
        Map<Integer,Double> yesterdayMap = new HashMap<>();

        for (int i = 0; i < todaySalesDailyList.size(); i++) {
            //TODO
            SalesDaily sales = todaySalesDailyList.get(i);
            double y =  (rgRetail.getCheckedRadioButtonId() == R.id.rb_retail_amount?MathUtils.penny2dollar(sales.getTotal_sales_amount()):MathUtils.penny2dollar(sales.getTotal_estimates_profit()));
            todayMap.put(sales.getHours(),y);
        }

        for (int i = 0; i < yesterdaySalesDailyList.size(); i++) {
            //TODO
            SalesDaily sales = yesterdaySalesDailyList.get(i);
            double y =  (rgRetail.getCheckedRadioButtonId() == R.id.rb_retail_amount?MathUtils.penny2dollar(sales.getTotal_sales_amount()):MathUtils.penny2dollar(sales.getTotal_estimates_profit()));
            yesterdayMap.put(sales.getHours(),y);
        }

        //生成xAxis坐标轴数据
        Set<Integer> todayKeySet = todayMap.keySet();
        Set<Integer> yesterdayKeySet = yesterdayMap.keySet();

        int todayMaxHour = 0;

        for (Integer key : todayKeySet) {
            xAxisList.add(key);
            if (key>=todayMaxHour){
                todayMaxHour = key;
            }
        }
        for (Integer key : yesterdayKeySet) {
            if (!xAxisList.contains(key)) {
                xAxisList.add(key);
            }
        }
        List<Integer> morrowXAxisList = new LinkedList<>();//次日
        List<Integer> currentXAxisList = new LinkedList<>();//今日

        for (Integer hour:xAxisList) {
            if (hour>4){
                currentXAxisList.add(hour);
            }else{
                morrowXAxisList.add(hour);
            }
        }

        Collections.sort(currentXAxisList, new Comparator<Integer>() {
            @Override
            public int compare(Integer lhs, Integer rhs) {
                return lhs - rhs;
            }
        });

        Collections.sort(morrowXAxisList, new Comparator<Integer>() {
            @Override
            public int compare(Integer lhs, Integer rhs) {
                return lhs - rhs;
            }
        });
        xAxisList.clear();
        //先放入今日的5-24时的，再放入次日1-4的
        xAxisList.addAll(currentXAxisList);
        xAxisList.addAll(morrowXAxisList);

        //生成两条线的数据
        List<Double> todayList = new ArrayList<>();
        double[] yesterdayData = new double[xAxisList.size()];
        boolean isMax = false;
        for (int i = 0; i < xAxisList.size(); i++) {
            Integer hour = xAxisList.get(i);
            if (yesterdayMap.containsKey(hour)){
                yesterdayData[i] = yesterdayMap.get(hour);
            }else{
                yesterdayData[i] = 0;
            }


            if (todayMap.containsKey(hour)){
                todayList.add(todayMap.get(hour));
            }else if(hour>4&&hour<=todayMaxHour&&!isMax){
                todayList.add(0.0);
            }else{
                isMax = true;
            }


        }

        xAxis.setData(xAxisList);
        option.xAxis(xAxis);

        option.yAxis(new ValueAxis());

        Line todayLine = new Line("今日");
        todayLine.setData(todayList);

        Line yesterdayLine = new Line("昨日");
        List<Double> yesterdayList = new LinkedList<>();
        for (int i = 0; i < yesterdayData.length; i++) {
            yesterdayList.add(yesterdayData[i]);
        }
        yesterdayLine.setData(yesterdayList);


        option.series(todayLine,yesterdayLine);

        retailWebView.setOption(option);

    }


    private void setUserFragment() {

        Option option = new Option();
        option.title().text("");
        option.tooltip().trigger(Trigger.axis);
        option.legend().data("今日","昨日");
        option.grid().containLabel(true).top(30).bottom(10);

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.boundaryGap(false);
        List<Integer> xAxisList = new LinkedList<>();

        Map<Integer,Integer> todayMap = new HashMap<>();
        Map<Integer,Integer> yesterdayMap = new HashMap<>();

        for (int i = 0; i < todaySalesDailyList.size(); i++) {
            SalesDaily sales = todaySalesDailyList.get(i);
            todayMap.put(sales.getHours(),sales.getTotal_order());
        }

        for (int i = 0; i < yesterdaySalesDailyList.size(); i++) {
            SalesDaily sales = yesterdaySalesDailyList.get(i);
            yesterdayMap.put(sales.getHours(),sales.getTotal_order());
        }

        //生成xAxis坐标轴数据
        Set<Integer> todayKeySet = todayMap.keySet();
        Set<Integer> yesterdayKeySet = yesterdayMap.keySet();

        int todayMaxHour = 0;

        for (Integer key : todayKeySet) {
            xAxisList.add(key);
            if (key>=todayMaxHour){
                todayMaxHour = key;
            }
        }
        for (Integer key : yesterdayKeySet) {
            if (!xAxisList.contains(key)) {
                xAxisList.add(key);
            }
        }

        List<Integer> morrowXAxisList = new LinkedList<>();//次日
        List<Integer> currentXAxisList = new LinkedList<>();//今日

        for (Integer hour:xAxisList) {
            if (hour>4){
                currentXAxisList.add(hour);
            }else{
                morrowXAxisList.add(hour);
            }
        }

        Collections.sort(currentXAxisList, new Comparator<Integer>() {
            @Override
            public int compare(Integer lhs, Integer rhs) {
                return lhs - rhs;
            }
        });

        Collections.sort(morrowXAxisList, new Comparator<Integer>() {
            @Override
            public int compare(Integer lhs, Integer rhs) {
                return lhs - rhs;
            }
        });
        xAxisList.clear();
        //先放入今日的5-24时的，再放入次日1-4的
        xAxisList.addAll(currentXAxisList);
        xAxisList.addAll(morrowXAxisList);

        //生成两条线的数据
        List<Integer> todayList= new ArrayList<>();
        int[] yesterdayData = new int[xAxisList.size()];

        boolean isMax =false;
        for (int i = 0; i < xAxisList.size(); i++) {
            Integer hour = xAxisList.get(i);
            if (yesterdayMap.containsKey(hour)){
                yesterdayData[i] = yesterdayMap.get(hour);
            }else{
                yesterdayData[i] = 0;
            }

            if (todayMap.containsKey(hour)){
                todayList.add(todayMap.get(hour));
            }else if(hour>4&&hour<=todayMaxHour&&!isMax){
                todayList.add(0);
            }else{
                isMax = true;
            }

        }

        xAxis.setData(xAxisList);
        option.xAxis(xAxis);

        option.yAxis(new ValueAxis());

        Line todayLine = new Line("今日");

        todayLine.setData(todayList);

        Line yesterdayLine = new Line("昨日");
        List<Integer> yesterdayList = new LinkedList<>();
        for (int i = 0; i < yesterdayData.length; i++) {
            yesterdayList.add(yesterdayData[i]);
        }
        yesterdayLine.setData(yesterdayList);
        todayLine.smooth(true);
        yesterdayLine.smooth(true);
        option.series(todayLine,yesterdayLine);

        userWebView.setOption(option);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            layoutForm.setVisibility(show ? View.GONE : View.VISIBLE);
            layoutForm.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (layoutForm!=null)
                    layoutForm.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            loginProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            loginProgress.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (loginProgress!=null)
                    loginProgress.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            loginProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            layoutForm.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Subscribe
    public void onEvent(ConvertStoreEvent event){
        initData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
