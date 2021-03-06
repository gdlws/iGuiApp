
package cn.fatdeer.isocket.chart;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import cn.fatdeer.isocket.R;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.LimitLine.LimitLabelPosition;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.LineData;

public class LineChartItem extends ChartItem {

    private Typeface mTf;
//at 20151130
//    private int mType; //at 20151117
    private String aimName;
    private float aimValue;
//end 20151130
    public LineChartItem(ChartData<?> cd, Context c, String name,float value) {
        super(cd);
//at 20151130        this.mType=type;//at 20151117
        this.aimName=name;
        this.aimValue=value;
//end 20151130        

//        mTf = Typeface.createFromAsset(c.getAssets(), "OpenSans-Regular.ttf");
    }

    @Override
    public int getItemType() {
        return TYPE_LINECHART;
    }

    @Override
    public View getView(int position, View convertView, Context c) {

        ViewHolder holder = null;

        if (convertView == null) {

            holder = new ViewHolder();

            convertView = LayoutInflater.from(c).inflate(
                    R.layout.list_item_linechart, null);
            holder.chart = (LineChart) convertView.findViewById(R.id.chart);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // apply styling
        // holder.chart.setValueTypeface(mTf);
        holder.chart.setDescription("");
        holder.chart.setDrawGridBackground(false);

        XAxis xAxis = holder.chart.getXAxis();
        xAxis.setPosition(XAxisPosition.BOTTOM);
        xAxis.setTypeface(mTf);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);

        YAxis leftAxis = holder.chart.getAxisLeft();
        leftAxis.setTypeface(mTf);
        leftAxis.setLabelCount(5, false);
        
        YAxis rightAxis = holder.chart.getAxisRight();
        rightAxis.setTypeface(mTf);
        rightAxis.setLabelCount(5, false);
        rightAxis.setDrawGridLines(false);

        // set data
        holder.chart.setData((LineData) mChartData);

        // do not forget to refresh the chart
        // holder.chart.invalidate();
        holder.chart.animateX(750);
//at 20151130        
//at 20151117
//        if(mType==0) {
//        	LimitLine ll = new LimitLine(30, "目标温度");
//        	ll.setLineWidth(4f);
//        	ll.enableDashedLine(10f, 10f, 0f);
//        	ll.setLabelPosition(LimitLabelPosition.LEFT_BOTTOM);
//        	ll.setTextSize(10f);
//        	leftAxis.addLimitLine(ll);
//        } else {
//            LimitLine ll = new LimitLine(60, "目标湿度");
//            ll.setLineWidth(4f);
//            ll.enableDashedLine(10f, 10f, 0f);
//            ll.setLabelPosition(LimitLabelPosition.LEFT_BOTTOM);
//            ll.setTextSize(10f);
//            leftAxis.addLimitLine(ll);
//        }

//end 20151117   
        LimitLine ll = new LimitLine(this.aimValue, this.aimName);
        ll.setLineWidth(4f);
        ll.enableDashedLine(10f, 10f, 0f);
        ll.setLabelPosition(LimitLabelPosition.LEFT_BOTTOM);
        ll.setTextSize(10f);
        leftAxis.addLimitLine(ll);
//end 20151130        
        return convertView;
    }

    private static class ViewHolder {
        LineChart chart;
    }
}
