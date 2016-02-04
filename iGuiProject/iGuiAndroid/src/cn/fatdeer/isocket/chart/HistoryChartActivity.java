
package cn.fatdeer.isocket.chart;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import cn.fatdeer.isocket.CLog;
import cn.fatdeer.isocket.R;
import cn.fatdeer.isocket.apmode.UDPHelper;
import cn.fatdeer.isocket.entity.ChartDataSet;
import cn.fatdeer.isocket.entity.Message4JSON;
import cn.fatdeer.isocket.entity.UserStatus;
import cn.fatdeer.isocket.pub.Const;
import cn.fatdeer.isocket.pub.DoJson;

import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * Demonstrates the use of charts inside a ListView. IMPORTANT: provide a
 * specific height attribute for the chart inside your listview-item
 * 
 * @author GD.lws
 * 
 *           at 20151124
 *            1. Send Data order display AlertDialog;
 *           at 20151127
 *            1. showDate type from EditText to TextView;
 *           at 20151130
 *            1. refresh data , remove old data firstly;
 *            2. data's index is pick from SQLite;
 *            3. LineChart AimName & AimValue from SQLite;
 *           at 20160127
 *            1. use UDPHelper get DATA from Server, remove UDP Connect Thread; 
 *            2. fix the bug , flash back in first user for no SQLite Database; 
 *            3. spinner for choose 3 kinds box; 
 */
public class HistoryChartActivity extends DemoBase implements
		OnSeekBarChangeListener {
	String TAG="HistoryChartActivity";
	private Spinner mSpinner;
	private SeekBar mSeekBarValues;
	private TextView mTvCount;
	private ListView lv;
	private TextView showDate;
	private Button pickDate;
	private Button btnRefresh; //at 20151120
	private static final int SHOW_DATAPICK = 0;
	private static final int DATE_DIALOG_ID = 1;

	Calendar mCalendar = null;//new GregorianCalendar(2015, 11, 4);
	ArrayList<String> mTimes = new ArrayList<String>();
	public static Context s_context;
	private String username=null;
	DateFormat df = new SimpleDateFormat("hh:mm:ss.SSS",Locale.CHINA);
	private BroadcastReceiver bcReceiver=null;
	private String serverIP=null;
	private String serverPort=null;

	//at 20151123 be modified in future
//		String[] mCodes={"A","A2","TA","H","H2","HA",
//				"T1","T2","T3","S1","S2",
//				"S3","S4","S5","S6","OC"};
//		String[] mNames={"外温","内温","目标温度",
//				"外湿","内湿","目标湿度", 
//				"温区1","温区2","温区3",   
//				"陶瓷50W","UVA","UVB","陶瓷75W", 
//				"加湿机","风扇速度", "自律倒计时"};
		//end 20151123
		private ProgressDialog progressDialog; //at 20151124
//at 20151130
		String[] aimNames={"","",""};
		float[] aimValues={0,0,0};
//end 20151130		
//at 20160127
	private ArrayAdapter<String> adapter;
	//TODO: Get box information from Server;
	int[] codes = 
//		{"IS21553001","IS11509001", "IS21510001", "IS21516001", "IS21517001" };
		{10,7,5,6};
	String[] names = {"试运行","亚达", "印度星", "辐射龟"   };

	private int chosenModule=codes[0];
//end 20160127		
		
		@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_listview_chart);
		s_context = this;
        Bundle bundle = this.getIntent().getExtras();
        this.username = bundle.getString("username");
        this.serverIP=bundle.getString("ip");
        this.serverPort=bundle.getString("port");
        if(username==null||username.length()==0) {
        	HistoryChartActivity.this.finish();
        }
        if(serverIP==null||serverIP.length()==0) {
        	HistoryChartActivity.this.finish();
        }
        if(serverPort==null||serverPort.length()==0) {
        	HistoryChartActivity.this.finish();
        }
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, names);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	mSpinner = (Spinner) findViewById(R.id.spinner_module);
    	mSpinner.setAdapter(adapter);
    	mSpinner.setOnItemSelectedListener(new SpinnerSelectedListener());
    	mSpinner.setVisibility(View.VISIBLE);
        // initialize the utilities (MPChart)
        Utils.init(this); //at 20151119
        
		int[] maxTime=this.getMaxTime();
		mCalendar=new GregorianCalendar(maxTime[0], maxTime[1]-1, maxTime[2], maxTime[3],0,0);
		Log.i(TAG,"mCalendar.month"+mCalendar.get(Calendar.MONTH));
		getTimes(); // at 20151118
		lv = (ListView) findViewById(R.id.listView1);

//		ArrayList<ChartItem> list = new ArrayList<ChartItem>();

		mTvCount = (TextView) findViewById(R.id.tvValueCount);
		mSeekBarValues = (SeekBar) findViewById(R.id.seekbarValues);
		
//		dayStr = cal2String();
		showDate = (TextView) findViewById(R.id.showdate);
		pickDate = (Button) findViewById(R.id.pickdate);
		btnRefresh = (Button) findViewById(R.id.refresh);
		updateDateDisplay();//at 20151123
		pickDate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Message msg = new Message();
				if (pickDate.equals((Button) v)) {
					msg.what = HistoryChartActivity.SHOW_DATAPICK;
				}
				HistoryChartActivity.this.dateandtimeHandler
						.sendMessage(msg);
			}
		});
		btnRefresh.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
					progressDialog = ProgressDialog.show(HistoryChartActivity.this, "Loading...", "Please wait...", true, true);
					String initStr = DoJson.instance().toJSon("DATA", username,""+chosenModule,
						cal2String(mCalendar));
				//Use UDPHelper, get DATA
				send2Server(initStr);
			}
		});


		mSeekBarValues.setProgress(mCalendar.get(Calendar.HOUR_OF_DAY));
		mSeekBarValues.setOnSeekBarChangeListener(this);
		this.refreshChart();
	}

	class SpinnerSelectedListener implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			chosenModule = codes[arg2];
			int temp=chosenModule;
			refreshChart();
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}

	private void send2Server(final String sendStr) {
		showMsg("Send init:" + sendStr, -1);
		Thread udpThread = new Thread() {
			@Override
			public void run() {
				try {
					super.run();
					UDPHelper udphelper = new UDPHelper(serverIP, serverPort,
							username);
					if (udphelper.openSocket()) {
						udphelper.sendMessage(sendStr);
						String rtnStr = udphelper.rcvMessage();
						Log.i(TAG, "rtnStr:" + rtnStr);
						if (rtnStr != null && rtnStr.length() > 0
								&& rtnStr.indexOf("DATA") >= 0) {
							Message4JSON message = DoJson.instance().fromJson(
									rtnStr.trim());
							CLog.i(TAG, "Message:" + rtnStr);
							if (message == null) {
								Const.broadCastToActivity(
										"INF",
										null,
										"UDPInputThread.run():JSON error-inf="
												+ rtnStr + "lengh="
												+ rtnStr.length(),
										Const.whichActivity.HISTORY);
							} else {
								Const.broadCastToActivity(message.getToOrder(),
										null, message.getuMSG(),
										Const.whichActivity.HISTORY);
							}
						} else {
							CLog.i(TAG, "Initialize to Server fail");
							Const.broadCastToActivity("ERROR", null,
									"Server Connect fail",
									Const.whichActivity.HISTORY);
						}
						CLog.i(TAG, "Ready to close UDP Socket");
						udphelper.closeSocket();
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();

					Log.e(TAG,
							"send2Server():Connection Exception: "
									+ e.getMessage());
				}
			}
		};
		udpThread.start();
	}

	@Override
	protected void onPause() {
		CLog.i(TAG, "Enter onPause(HistoryChartActivity)");	
		if (bcReceiver != null) {
			unregisterReceiver(bcReceiver);
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		CLog.i(TAG, "Enter onResume(HistoryChartActivity)");
		regBroadcast();
		super.onResume();

	}

	private void regBroadcast() {
		bcReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				final String response = intent.getStringExtra("response"); 
				final String order = intent.getStringExtra("order");
				processOrder(order, response);
			}
		};
		IntentFilter intentToReceiveFilter = new IntentFilter();
		intentToReceiveFilter.addAction(Const.BC);
		registerReceiver(bcReceiver, intentToReceiveFilter);
	}

	private void processOrder(String order, String response) {

		if (order.equals("ERROR")) {
			this.showMsg("RCV[" + df.format(new Date()) + "]" + order + "-"
					+ response, 1);
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			HistoryChartActivity.this.finish();
		} else if(order.equals("DATA")) {
			this.showMsg("RCV[" + df.format(new Date()) + "]" + order + "-len="
					+ response.length()+"response="+response, -1);
			Gson gson = new GsonBuilder().
					setDateFormat("yyyy-MM-dd HH:mm:ss").
					disableHtmlEscaping().create();
			iGuiDBHelper dbHelper = new iGuiDBHelper(HistoryChartActivity.this,
					"igui.db", null, 1);

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
					Locale.CHINA);  // 小写的mm表示的是分钟

			Date sDate;
			long startTime = -1L;
			long endTime = -1L;
			Log.i(TAG, "cal2String(mCalendar)=" + cal2String(mCalendar));
			try {
				sDate = sdf.parse(cal2String(mCalendar));
				startTime = sDate.getTime() / 1000;
				endTime = 3600 + (sDate.getTime() / 1000);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			SQLiteDatabase delFromDB = dbHelper.getReadableDatabase();
			String whereClauses = "stime>=? and stime<? and oid=?";
			String[] whereArgs = { "" + startTime, "" + endTime, ""+chosenModule };
			// 调用delete方法，删除数据
			delFromDB.delete("igui_status", whereClauses, whereArgs);
			delFromDB.close();
			SQLiteDatabase db = dbHelper.getWritableDatabase();		
			List<UserStatus> userStatus=gson.fromJson(response,
					new TypeToken<List<UserStatus>>(){}.getType());
			for(UserStatus us:userStatus) {
				this.showMsg(us.toString(),-1);

				ContentValues cv = new ContentValues();
				cv.put("oid", us.getOp_id());
				cv.put("code", us.getIndex_code());
				cv.put("value", us.getIndex_value());
				long timeLong = us.getCreate_time().getTime() / 1000;
				cv.put("stime", timeLong);
				Log.i(TAG, " code="
						+ us.getIndex_code() + " value=" + us.getIndex_value());
				// 插入ContentValues中的数据
				db.insert("igui_status", null, cv);
			}
			db.close();
			refreshChart();
			progressDialog.dismiss();  //at 20151120
		} else {
			this.showMsg("RCV[" + df.format(new Date()) + "]" + order + "-"
					+ response, -1);
		}

	}

	private void showMsg(String str, int type) {
		if (type == 0) {
			Toast.makeText(HistoryChartActivity.this, str, Toast.LENGTH_SHORT)
					.show();
		} else if (type == 1) {
			Toast.makeText(HistoryChartActivity.this, str, Toast.LENGTH_LONG)
					.show();
		}
		if (Const.DEBUG_FLAG)
			CLog.i(TAG, str);
	}

	private void updateDateDisplay() {
		showDate.setText(
				mCalendar.get(Calendar.YEAR)+"年"+
				(mCalendar.get(Calendar.MONTH)+1)+"月"+
				mCalendar.get(Calendar.DAY_OF_MONTH)+"日");
	}

	String cal2String(Calendar cal) {
        int year = cal.get(Calendar.YEAR); 
        int month = cal.get(Calendar.MONTH)+1; 
        int day = cal.get(Calendar.DAY_OF_MONTH); 
        int hour = cal.get(Calendar.HOUR_OF_DAY);  
        
    	return year+"-"+((month) < 10 ? "0" + (month) : (month))+"-"
    	  +((day < 10) ? "0" + day : day ) +" "
    	  +((hour < 10) ? "0" + hour : hour )
    	  +":00:00";
    }

	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			Log.i(TAG,"monthOfYear="+monthOfYear);
			mCalendar=new GregorianCalendar(
					year, 
					monthOfYear, 
					dayOfMonth, 
					mSeekBarValues.getProgress(),0,0);
			refreshChart();
			Log.i(TAG,"mCalendar.month="+mCalendar.get(Calendar.MONTH));
			updateDateDisplay();
		}
	};

	Handler dateandtimeHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HistoryChartActivity.SHOW_DATAPICK:
				showDialog(DATE_DIALOG_ID);
				break;
			}
		}
	};

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_DIALOG_ID:
			int year = mCalendar.get(Calendar.YEAR);
			int month = mCalendar.get(Calendar.MONTH);
			int day = mCalendar.get(Calendar.DAY_OF_MONTH);
			return new DatePickerDialog(this, mDateSetListener,
					year, month, day);
		}

		return null;
	}

	private void refreshChart() {
		mTvCount.setText("" + mSeekBarValues.getProgress());
		mCalendar=new GregorianCalendar(
				mCalendar.get(Calendar.YEAR), 
				mCalendar.get(Calendar.MONTH), 
				mCalendar.get(Calendar.DAY_OF_MONTH), 
				mSeekBarValues.getProgress(),0,0);

		getTimes();
		ArrayList<ChartItem> list = new ArrayList<ChartItem>();
		LineData tLineData=generateDataLineT(mSeekBarValues
				.getProgress());
		LineData hLineData=generateDataLineH(mSeekBarValues
				.getProgress());
		list.add(new LineChartItem(
				tLineData, getApplicationContext(),aimNames[0],aimValues[0]));
		list.add(new LineChartItem(
				hLineData, getApplicationContext(),aimNames[1],aimValues[1]));
		list.add(new BarChartItem(
				generateDataBar(mSeekBarValues.getProgress()),
				getApplicationContext()));
		list.add(new ScatterChartItem(generateDataScatter(mSeekBarValues
				.getProgress()), getApplicationContext()));

		ChartDataAdapter cda = new ChartDataAdapter(getApplicationContext(),
				list);
		lv.setAdapter(cda);

	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {

		refreshChart();

	}

	/** adapter that supports 3 different item types */
	private class ChartDataAdapter extends ArrayAdapter<ChartItem> {

		public ChartDataAdapter(Context context, List<ChartItem> objects) {
			super(context, 0, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return getItem(position).getView(position, convertView,
					getContext());
		}

		@Override
		public int getItemViewType(int position) {
			// return the views type
			return getItem(position).getItemType();
		}

		// @Override
		// public int getViewTypeCount() {
		// //at 20151117 return 3; // we have 3 different item-types
		// return 4;
		// }
	}

	/**
	 * generates a random ChartData object with just one DataSet
	 * 
	 * @return
	 */
	private LineData generateDataLineT(int hour) {

//		String[] codes = { "A", "A2", "T1", "T2", "T3" };
//		String[] names = { "外温","箱温","温区1","温区2","温区3"};
		ArrayList<ChartDataSet> cSets = new ArrayList<ChartDataSet>();
		cSets=this.getChartSets(1);

		ArrayList<LineDataSet> sets = new ArrayList<LineDataSet>();
		int i=0;
		for(ChartDataSet cDataSet:cSets) {
			LineDataSet dataset = new LineDataSet(getValue(cDataSet.getCode(), 1),cDataSet.getName());
			dataset.setLineWidth(2.5f);
			dataset.setCircleSize(4.5f);
			dataset.setHighLightColor(Color.rgb(244, 117, 117));
			dataset.setColor(ColorTemplate.VORDIPLOM_COLORS[i%5]);
			dataset.setCircleColor(ColorTemplate.VORDIPLOM_COLORS[i%5]);
			dataset.setDrawValues(false);
			sets.add(dataset);
			i++;
		}
		LineData cd = new LineData(mTimes, sets);
		return cd;
	}

	private LineData generateDataLineH(int hour) {

//		String[] codes = { "H", "H2" };
//		String[] names = { "外湿","箱湿" }; 
		ArrayList<ChartDataSet> cSets = new ArrayList<ChartDataSet>();
		cSets=this.getChartSets(2);
		ArrayList<LineDataSet> sets = new ArrayList<LineDataSet>();
		int i=0;
		for(ChartDataSet cDataSet:cSets) {
			LineDataSet dataset = new LineDataSet(getValue(cDataSet.getCode(), 1),cDataSet.getName());
			dataset.setLineWidth(2.5f);
			dataset.setCircleSize(4.5f);
			dataset.setHighLightColor(Color.rgb(244, 117, 117));
			dataset.setColor(ColorTemplate.VORDIPLOM_COLORS[i%5]);
			dataset.setCircleColor(ColorTemplate.VORDIPLOM_COLORS[i%5]);
			dataset.setDrawValues(false);
			sets.add(dataset);
			i++;
		}

		LineData cd = new LineData(mTimes, sets);
		return cd;
	}

	/**
	 * generates a random ChartData object with just one DataSet
	 * 
	 * @return
	 */
	private BarData generateDataBar(int hour) {

//		BarDataSet d = new BarDataSet(this.getBarValue("S6"), "风扇速度");
		ArrayList<ChartDataSet> cSets = new ArrayList<ChartDataSet>();
		cSets=this.getChartSets(3);
		BarDataSet d = new BarDataSet(
				getBarValue(cSets.get(0).getCode()), 
				cSets.get(0).getName());
		d.setBarSpacePercent(20f);
		d.setColors(ColorTemplate.VORDIPLOM_COLORS);
		d.setHighLightAlpha(255);

		BarData cd = new BarData(mTimes, d);
		return cd;
	}

	private ScatterData generateDataScatter(int hour) {

//		String[] codes = { "S1", "S2", "S3", "S4", "S5" };
//		String[] names = {"陶瓷50W","UVA","UVB","陶瓷75W", 
//				"加湿机"};

		ArrayList<ChartDataSet> cSets = new ArrayList<ChartDataSet>();
		cSets=this.getChartSets(4);
		ArrayList<ScatterDataSet> sets = new ArrayList<ScatterDataSet>();
//at 20151130		for (int i = 0; i < codes.length; i++) {
		int i=0;
		for(ChartDataSet cDataSet:cSets) {
			
			ScatterDataSet dataset = new ScatterDataSet(getValue(cDataSet.getCode(),
					5 * (i + 1)), cDataSet.getName());
			dataset.setColor(ColorTemplate.VORDIPLOM_COLORS[i%5]);
			dataset.setScatterShapeSize(7.5f);
			dataset.setDrawValues(false);
			dataset.setValueTextSize(10f);
			sets.add(dataset);
			i++;
		}

		ScatterData cd = new ScatterData(mTimes, sets);

		return cd;
	}

	private int[] getMaxTime() {
		int[] result=new int[4];
		iGuiDBHelper dbHelper = new iGuiDBHelper(
				HistoryChartActivity.this, "igui.db", null, 1);
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor cursor2 = db
				.rawQuery(
						"SELECT max(stime) as stime FROM igui_status WHERE oid=?", 
		new String[] {  chosenModule + "" });
		
		SimpleDateFormat sdfHM = new SimpleDateFormat("yyyy:MM-dd HH:mm:ss", Locale.CHINA);
		if (cursor2.moveToNext()) {
			Long stime = cursor2.getLong(cursor2.getColumnIndex("stime"));
			Date stimeD = new Date(stime * 1000);
			Log.i(TAG, "Max Time:" + sdfHM.format(stimeD) + "\n");
			Calendar cal = Calendar.getInstance();
			cal.setTime(stimeD);
			result[0]=cal.get(Calendar.YEAR);
			result[1]=cal.get(Calendar.MONTH)+1;
			result[2]=cal.get(Calendar.DAY_OF_MONTH);
			result[3]=cal.get(Calendar.HOUR_OF_DAY);
		}
		cursor2.close();
		db.close();

		for(int i=0;i<result.length;i++)
			Log.i(TAG, "result["+i+"]:" + result[i] + "\n");
		return result;
	}
	
	private void getTimes() {
		iGuiDBHelper dbHelper = new iGuiDBHelper(
				HistoryChartActivity.this, "igui.db", null, 1);
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
				Locale.CHINA);// 小写的mm表示的是分钟
		Date sDate;
		long startTime = -1L;
		long endTime = -1L;
		try {
			sDate = sdf.parse(cal2String(mCalendar));
			startTime = sDate.getTime() / 1000;
			endTime = 3600+(sDate.getTime()/1000);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Cursor cursor2 = db
				.rawQuery(
						"SELECT distinct stime FROM igui_status WHERE oid=? and stime>=? and stime<?",
		new String[] { ""+chosenModule, startTime + "", endTime + "" });

		mTimes.removeAll(mTimes);
		SimpleDateFormat sdfHM = new SimpleDateFormat("HH:mm", Locale.CHINA);
		while (cursor2.moveToNext()) {
			Long stime = cursor2.getLong(cursor2.getColumnIndex("stime"));
			Date stimeD = new Date(stime * 1000);
			Log.i(TAG, "XAxis:" + sdfHM.format(stimeD) + "\n");
			mTimes.add(sdfHM.format(stimeD));
		}
		cursor2.close();
		db.close();
	}

	private ArrayList<Entry> getValue(String code, int base) {
		ArrayList<Entry> entry = new ArrayList<Entry>();

		iGuiDBHelper dbHelper = new iGuiDBHelper(HistoryChartActivity.this,
				"igui.db", null, 1);
		// 得到一个可写的数据库
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
				Locale.CHINA);// 小写的mm表示的是分钟
		Date sDate;
		long startTime = -1L;
		long endTime = -1L;
		try {
			Log.i(TAG, "cal2String(mCalendar)=" + cal2String(mCalendar));
			sDate = sdf.parse(cal2String(mCalendar));
			startTime = sDate.getTime() / 1000;
			endTime = 3600 + (sDate.getTime() / 1000);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Cursor cursor = db
				.rawQuery(
						" SELECT a.oid,b.index_name,a.value,a.stime,a.instime "
								+ " , b.ser_no ,b.chart_type "
								+ // at 20151130
								" FROM igui_status a, sys_parameter b "
								+ " WHERE a.code=b.index_code and a.oid=? and a.code=? and a.stime>=? and a.stime<? ",
						new String[] { "" + chosenModule, code, startTime + "",
								endTime + "" });

		Log.i(TAG, "begin");
		String result = "result=";
		int i = 0;
		while (cursor.moveToNext()) {

			float value = cursor.getFloat(cursor.getColumnIndex("value"));
			Long stime = cursor.getLong(cursor.getColumnIndex("stime"));
			String instime = cursor.getString(cursor.getColumnIndex("instime"));
			String index_name = cursor.getString(cursor
					.getColumnIndex("index_name"));
			Date stimeD = new Date(stime * 1000);
			int chartType = cursor.getInt(cursor.getColumnIndex("chart_type"));
			int serNo = cursor.getInt(cursor.getColumnIndex("ser_no"));
			if (serNo == 0) {
				aimNames[chartType - 1] = index_name;
				aimValues[chartType - 1] = value;
			} else {
				entry.add(new Entry(value * base, i++));
			}
			result += " code：" + code + " index_name:" + index_name + " value："
					+ value + " stime：" + stime + "stimeD="
					+ sdf.format(stimeD) + " instime：" + instime + "\n";
			Log.i(TAG, result);
		}
		cursor.close();
		db.close();

		return entry;
	}

	private ArrayList<BarEntry> getBarValue(String code) {
		ArrayList<BarEntry> barEntry = new ArrayList<BarEntry>();

		iGuiDBHelper dbHelper = new iGuiDBHelper(
				HistoryChartActivity.this, "igui.db", null, 1);
		// 得到一个可写的数据库
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
				Locale.CHINA);// 小写的mm表示的是分钟
		Date sDate;
		long startTime = -1L;
		long endTime = -1L;
		try {
			sDate = sdf.parse(cal2String(mCalendar));
			startTime = sDate.getTime() / 1000;
			endTime = 3600+(sDate.getTime()/1000);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Cursor cursor = 
//				db.query("igui_status", new String[] { "oid", "name",
//				"value", "stime", "instime" },
//				"oid=? and name=? and stime>=? and stime<?", new String[] { sid, name,
//						startTime + "", endTime + "" }, null, null, null);
				db.rawQuery(
						"SELECT a.oid,b.index_name,a.value,a.stime,a.instime "+
						"FROM igui_status a, sys_parameter b "+
						"WHERE a.code=b.index_code and a.oid=? and a.code=? and a.stime>=? and a.stime<? ",
//at 20160127						new String[] {sid, code, startTime + "", endTime + "" });
		new String[] {""+chosenModule, code, startTime + "", endTime + "" });

		Log.i(TAG, "begin");
		String result = "result=";
		int i = 0;
		while (cursor.moveToNext()) {
			float value = cursor.getFloat(cursor.getColumnIndex("value"));
			Long stime = cursor.getLong(cursor.getColumnIndex("stime"));
			String instime = cursor.getString(cursor.getColumnIndex("instime"));
			String index_name=cursor.getString(cursor.getColumnIndex("index_name"));
			Date stimeD = new Date(stime * 1000);
			result += " code：" + code +" index_name:"+index_name + " value：" + value + " stime：" + stime
					+ "stimeD=" + sdf.format(stimeD) + " instime：" + instime
					+ "\n";
			Log.i(TAG, result);
			barEntry.add(new BarEntry(value, i++));
		}
		cursor.close();
		db.close();

		return barEntry;
	}

	private ArrayList<ChartDataSet> getChartSets(int type) {
		ArrayList<ChartDataSet> cSets = new ArrayList<ChartDataSet>();

		iGuiDBHelper dbHelper = new iGuiDBHelper(
				HistoryChartActivity.this, "igui.db", null, 1);
		// 得到一个可写的数据库
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = 
				db.query("sys_parameter", new String[] { "index_code", "index_name",
				"chart_type", "ser_no"},
				"chart_type=? ", new String[] { type + "" }, null, null, null);

		Log.i(TAG, "begin");
		String result = "result=";
		while (cursor.moveToNext()) {
			String iCode = cursor.getString(cursor.getColumnIndex("index_code"));
			String iName = cursor.getString(cursor.getColumnIndex("index_name"));
			int cType = cursor.getInt(cursor.getColumnIndex("chart_type"));
			int sNo   = cursor.getInt(cursor.getColumnIndex("ser_no"));
			result += " iCode：" + iCode +" iName:"+iName + " cType：" + cType + " sNo：" + sNo
					+ "\n";
			Log.i(TAG, result);
			cSets.add(new ChartDataSet(iCode,iName,cType,sNo));
		}
		cursor.close();
		db.close();

		return cSets;
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub

	}
}
