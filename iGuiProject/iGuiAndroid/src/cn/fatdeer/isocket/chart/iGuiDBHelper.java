package cn.fatdeer.isocket.chart;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


/**
 * For mobile's SQLite Database iGuiDB read & write
 * 
 * @author GD.lws
 * 
 *           at 20151130
 *            1. sys_parameter add column chart_type & ser_no; 
 *           at 20160127
 *            1. insert data move from SQLiteActivity to here;
 *           at 20160202
 *            1. new table [sys_index_aim] for user's aim setting;
 */
public class iGuiDBHelper extends SQLiteOpenHelper {

	private static final String TAG = "iGuiDBHelper";
	public static final int VERSION = 1;

	// 必须要有构造函数
	public iGuiDBHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}


	@Override
	// 当第一次创建数据库的时候，调用该方法
	public void onCreate(SQLiteDatabase db) {
		String sql = "create table igui_status(" +
				"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"oid INTEGER ,code VARCHAR,value REAL," +
				"stime INTEGER, instime DATETIME DEFAULT CURRENT_TIMESTAMP)";
		// 输出创建数据库的日志信息
		Log.i(TAG, "create Database------------->");
		// execSQL函数用于执行SQL语句
		db.execSQL(sql);
		sql = "create table sys_parameter(" +
				"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"index_code VARCHAR," +
				"index_name VARCHAR," +
				"chart_type INTEGER," +
				"ser_no INTEGER " +
				")";
		db.execSQL(sql);
//at 20160202
		String[] tortorise={"印度星(幼体)","苏卡达(幼体)","辐射(幼体)","豹纹(幼体)" ,
				"印度星(成体)","苏卡达(成体)","辐射(成体)","豹纹(成体)"};
		String[] hourName={"早上","下午","晚上","凌晨"};
		sql = "create table sys_index_aim(" +
				"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"box_type INTEGER," +
				"box_name VARCHAR," +
				"hour_name INTEGER," +
				"aimt_1 INTEGER," +
				"aimh_1 INTEGER," +
				"aimt_2 INTEGER," +
				"aimh_2 INTEGER," +
				"aimt_3 INTEGER," +
				"aimh_3 INTEGER " +
				")";
		db.execSQL(sql);

		ContentValues cv_sys_index_aim = new ContentValues();
		for(int i=0;i<8;i++) {
			for(int j=0;j<4;j++) {
			cv_sys_index_aim.put("box_type", (i+1));
			cv_sys_index_aim.put("box_name", tortorise[i]);
			cv_sys_index_aim.put("hour_name", (j+1)); 
			cv_sys_index_aim.put("aimt_1", getRandom(10,20));
			cv_sys_index_aim.put("aimt_2", getRandom(10,20));
			cv_sys_index_aim.put("aimt_3", getRandom(10,20));
			cv_sys_index_aim.put("aimh_1", getRandom(30,50));
			cv_sys_index_aim.put("aimh_2", getRandom(30,50));
			cv_sys_index_aim.put("aimh_3", getRandom(30,50));
			db.insert("sys_index_aim", null, cv_sys_index_aim);
			Log.i("db", "box_type=" + (i+1) + " box_name="
					+ tortorise[i]+ " hour_name="
					+ hourName[j] );
			}
		}
		
//end 20160202		
		//TODO: synchoronous with Server's MySQL 
		int year=2015;
		int[] values={15,28,30,
				40,65,60,
				28,30,32,
				0,0,0,0,
				0,0,0
		};
		int[] diffs={3,4,0,
				5,10,0,
				4,4,4,
				1,1,1,1,
				1,5,60
		};
		String[] codes={"A","A2","TA","H","H2","HA",
				"T1","T2","T3","S1","S2",
				"S3","S4","S5","S6","F"};
		String[] names={
				"外温","箱温","目标温度",
				"外湿","箱湿","目标湿度", 
				"温区1","温区2","温区3",   
				"陶瓷50W","UVA",
				"UVB","陶瓷75W", 
				"加湿机","风扇速度", 
				"手控倒计时"};
		// which Chart displayed
		int[] charts={
				1,1,1,
				2,2,2,
				1,1,1,
				4,4,4,4,
				4,3,0};
		// Display Order
		int[] orders={
				2,1,0,
				2,1,0,
				3,4,5,
				1,3,4,2,
				5,1,0};
		ContentValues cv = new ContentValues();
		for(int i=0;i<codes.length;i++) {
			cv.put("index_code", codes[i]);
			cv.put("index_name", names[i]);
			cv.put("chart_type", charts[i]);  //at 20151130
			cv.put("ser_no", orders[i]); //at 20151130
			db.insert("sys_parameter", null, cv);
		}
		
		 cv = new ContentValues();
		cv.put("oid", 7);
//		cv.put("name", "温区1");
//		cv.put("value", 28);
		for (int month = 10; month < 11; month++) {
			for (int day = 1; day < 2; day++) { // days
				for (int hour = 14; hour < 15; hour++) {// hours
					for (int minute = 0; minute < 60; minute += 5) { // minutes
						String stimeStr = "" + year + "-" + month + "-0"
								+ day + " " + hour + ":"
								+ (minute < 10 ? "0" + minute : minute)
								+ ":00";
						SimpleDateFormat sdf = new SimpleDateFormat(
								"yyyy-MM-dd HH:mm:ss",Locale.CHINA);// 小写的mm表示的是分钟
						Long timeLong = 1L;
						try {
							Date date = sdf.parse(stimeStr);
							timeLong = date.getTime() / 1000;
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						for (int size = 0; size < names.length; size++) {
							float value = getRandom(diffs[size],
									values[size]);
//							if(names[size].equals("陶瓷50W")||
//							names[size].equals("UVA")||
//							names[size].equals("UVB")||
//							names[size].equals("陶瓷75W")||
//							names[size].equals("加湿机")){
							if(codes[size].charAt(0)=='S'&&!codes[size].equals("S6")) {
								if(value>0.5) value=1;
								else value=0;
							}
							cv.put("code", codes[size]);//at 20151123
//							cv.put("name", names[size]);
							// cv.put("value", getRandom(4, 28));
							cv.put("value", value);
							cv.put("stime", timeLong);
							Log.i("db", "stime=" + stimeStr + " code="
									+ codes[size]+ " name="
									+ names[size] + " value=" + value);
							// 插入ContentValues中的数据
							db.insert("igui_status", null, cv);
						}
					}
				}
			}
		}
//end 20160127		
	}

	@Override
	// 当更新数据库的时候执行该方法
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// 输出更新数据库的日志信息
		Log.i(TAG, "update Database------------->");
	}
	

	private float getRandom(float range, float startsfrom) {
		return (float) (Math.random() * range) + startsfrom;
	}
}
