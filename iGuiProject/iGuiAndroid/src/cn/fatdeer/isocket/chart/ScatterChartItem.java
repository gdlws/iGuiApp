
package cn.fatdeer.isocket.chart;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import cn.fatdeer.isocket.R;

import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.LimitLine.LimitLabelPosition;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.ScatterData;

public class ScatterChartItem extends ChartItem {

    private Typeface mTf;

    public ScatterChartItem(ChartData<?> cd, Context c) {
        super(cd);

//        mTf = Typeface.createFromAsset(c.getAssets(), "OpenSans-Regular.ttf");
    }

    @Override
    public int getItemType() {
        return TYPE_SCATTERCHART;
    }

    @Override
    public View getView(int position, View convertView, Context c) {

        ViewHolder holder = null;

        if (convertView == null) {

            holder = new ViewHolder();

            convertView = LayoutInflater.from(c).inflate(
                    R.layout.list_item_scatterchart, null);
            holder.chart = (ScatterChart) convertView.findViewById(R.id.chart);

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
//at 20151117        leftAxis.setLabelCount(5, false);
        leftAxis.setLabelCount(1, false);
        leftAxis.setDrawLabels(false);//at 20151117
        
        YAxis rightAxis = holder.chart.getAxisRight();
        rightAxis.setTypeface(mTf);
//at 20151117        rightAxis.setLabelCount(5, false);
        rightAxis.setLabelCount(1, false);
        rightAxis.setDrawGridLines(false);
        rightAxis.setDrawLabels(false);//at 20151117

        // set data
        holder.chart.setData((ScatterData) mChartData);

        // do not forget to refresh the chart
        // holder.chart.invalidate();
        holder.chart.animateX(750);

		String[] names = {"陶瓷50W","UVA","UVB","陶瓷75W", 
				"加湿机"};
		for (int i = 0; i < names.length; i++) {
			LimitLine ll = new LimitLine(5 * (i + 1), names[i]);
			ll.setLineWidth(1f);
			ll.enableDashedLine(2f, 2f, 0f);
			ll.setLabelPosition(LimitLabelPosition.LEFT_BOTTOM);
			ll.setTextSize(10f);
			leftAxis.addLimitLine(ll);
		}
    	
        return convertView;
    }

    private static class ViewHolder {
        ScatterChart chart;
    }
}
