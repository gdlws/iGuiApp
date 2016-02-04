package cn.fatdeer.isocket.chart;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import cn.fatdeer.isocket.R;

/**
 * @author GD.lws
 * @version 20160120
 * @Discribe Initialize SQLite (Module for administrator)
 *            
 *           at 20160120
 *            1. change the order of switch :UVB(S3 to S5); C75W(S4 to S6); Humidifier(S5 to S3); FAN(S6 to S4);
 *           at 20160127
 *            1. insert data move to iGuiDBHelper.java; 
 * 
 */
public class SQLiteActivity extends Activity {
	/** Called when the activity is first created. */
	// 声明各个按钮
//	private Button createBtn;
	private Button dropBtn;
	private Button insertBtn;
//	private Button updateBtn;
	private Button queryBtn;
//	private Button deleteBtn;
//	private Button ModifyBtn;
	private ArrayAdapter<String> adapter;
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
	private Spinner mSpinner;
	private EditText stimeText;
	private EditText etimeText;
	private TextView txtResult;
	private String chosenValue=codes[0];

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sqlite);

		// 调用creatView方法
		creatView();
		// setListener方法
		setListener();
	}

	// 通过findViewById获得Button对象的方法
	private void creatView() {
//		createBtn = (Button) findViewById(R.id.createDatabase);
		dropBtn = (Button) findViewById(R.id.dropDatabase);
//		updateBtn = (Button) findViewById(R.id.updateDatabase);
		insertBtn = (Button) findViewById(R.id.insert);
//		ModifyBtn = (Button) findViewById(R.id.update);
		queryBtn = (Button) findViewById(R.id.query);
//		deleteBtn = (Button) findViewById(R.id.delete);
		mSpinner = (Spinner) findViewById(R.id.spinner_value);
		stimeText = (EditText) this.findViewById(R.id.txt_stime);
		etimeText = (EditText) this.findViewById(R.id.txt_etime);
		txtResult = (TextView) this.findViewById(R.id.txt_res);
		
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, codes);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinner.setAdapter(adapter);
		mSpinner.setOnItemSelectedListener(new SpinnerSelectedListener());
		mSpinner.setVisibility(View.VISIBLE);

//		stimeText.setText("2015-11-16 15:59:00");
//		etimeText.setText("2015-11-16 17:01:00");
		stimeText.setText("2016-01-27 12:00:00");
		etimeText.setText("2016-01-27 13:00:00");
		
	}

	// 使用数组形式操作
	class SpinnerSelectedListener implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			chosenValue=codes[arg2];
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}
	// 为按钮注册监听的方法
	private void setListener() {
		// createBtn.setOnClickListener(new CreateListener());
		dropBtn.setOnClickListener(new DropListener());
		// updateBtn.setOnClickListener(new UpdateListener());
		insertBtn.setOnClickListener(new InsertListener());
		// ModifyBtn.setOnClickListener(new ModifyListener());
		queryBtn.setOnClickListener(new QueryListener());
		// deleteBtn.setOnClickListener(new DeleteListener());
	}

	//
	// // 创建数据库的方法
	// class CreateListener implements OnClickListener {
	//
	// @Override
	// public void onClick(View v) {
	// // 创建iguiDBHelper对象
	// iguiDBHelper dbHelper = new iguiDBHelper(SQLiteActivity.this,
	// "igui.db", null, 1);
	// }
	// }

	// 删除数据库的方法
	class DropListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			SQLiteActivity.this.deleteDatabase("igui.db");
		}
	}


	// 插入数据的方法
	class InsertListener implements OnClickListener {

		@Override
		public void onClick(View v) {

			iGuiDBHelper dbHelper = new iGuiDBHelper(SQLiteActivity.this,
					"igui.db", null, 1);
			// // 得到一个可写的数据库
			SQLiteDatabase db = dbHelper.getWritableDatabase();

//at 20160127
//			int year=2015;
//			int[] values={15,28,30,
//					40,65,60,
//					28,30,32,
//					0,0,0,0,
//					0,0,0
//			};
//			int[] diffs={3,4,0,
//					5,10,0,
//					4,4,4,
//					1,1,1,1,
//					1,5,60
//			};
//			
//			ContentValues cv = new ContentValues();
//			for(int i=0;i<codes.length;i++) {
//				cv.put("index_code", codes[i]);
//				cv.put("index_name", names[i]);
//				cv.put("chart_type", charts[i]);  //at 20151130
//				cv.put("ser_no", orders[i]); //at 20151130
//				db.insert("sys_parameter", null, cv);
//			}
//			
//			 cv = new ContentValues();
//			cv.put("oid", 7);
////			cv.put("name", "温区1");
////			cv.put("value", 28);
//			for (int month = 10; month < 11; month++) {
//				for (int day = 1; day < 2; day++) { // days
//					for (int hour = 14; hour < 15; hour++) {// hours
//						for (int minute = 0; minute < 60; minute += 5) { // minutes
//							String stimeStr = "" + year + "-" + month + "-0"
//									+ day + " " + hour + ":"
//									+ (minute < 10 ? "0" + minute : minute)
//									+ ":00";
//							SimpleDateFormat sdf = new SimpleDateFormat(
//									"yyyy-MM-dd HH:mm:ss",Locale.CHINA);// 小写的mm表示的是分钟
//							Long timeLong = 1L;
//							try {
//								Date date = sdf.parse(stimeStr);
//								timeLong = date.getTime() / 1000;
//							} catch (ParseException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//							for (int size = 0; size < names.length; size++) {
//								float value = getRandom(diffs[size],
//										values[size]);
////								if(names[size].equals("陶瓷50W")||
////								names[size].equals("UVA")||
////								names[size].equals("UVB")||
////								names[size].equals("陶瓷75W")||
////								names[size].equals("加湿机")){
//								if(codes[size].charAt(0)=='S'&&!codes[size].equals("S6")) {
//									if(value>0.5) value=1;
//									else value=0;
//								}
//								cv.put("code", codes[size]);//at 20151123
////								cv.put("name", names[size]);
//								// cv.put("value", getRandom(4, 28));
//								cv.put("value", value);
//								cv.put("stime", timeLong);
//								Log.i("db", "stime=" + stimeStr + " code="
//										+ codes[size]+ " name="
//										+ names[size] + " value=" + value);
//								// 插入ContentValues中的数据
//								db.insert("igui_status", null, cv);
//							}
//						}
//					}
//				}
//			}
//end 20160127			
		}
	}

	// 查询数据的方法
	class QueryListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// at 20151117
			// 参数1：表名
			// 参数2：要想显示的列
			// 参数3：where子句
			// 参数4：where子句对应的条件值
			// 参数5：分组方式
			// 参数6：having条件
			// 参数7：排序方式
			iGuiDBHelper dbHelper = new iGuiDBHelper(SQLiteActivity.this,
					"igui.db", null, 1);
			// 得到一个可写的数据库
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.CHINA);//小写的mm表示的是分钟  
			Date sDate;
			Date eDate;
			long startTime=-1L;
			long endTime=-1L;
			try {
				sDate = sdf.parse(stimeText.getText().toString());
				eDate = sdf.parse(etimeText.getText().toString());
				startTime=sDate.getTime()/1000;
				endTime=eDate.getTime()/1000;
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
			Cursor cursor = 
//					db.query("igui_status", 
//					new String[] { "oid","value", "stime", "instime" }, 
//					"code=? and stime>=? and stime<?", new String[] { 
////					"温区1"
//					chosenValue
//					,startTime+"",endTime+""}, 
//					null, null, null);
					db.rawQuery(
							"SELECT oid,code,b.index_name as name, value,stime,instime FROM igui_status a, sys_parameter b "+
							"WHERE a.code=b.index_code and a.code=? and a.stime>=? and a.stime<? ",
							new String[] {chosenValue, startTime + "", endTime + "" });

			Log.i("db","begin");
			String result="result=";
			while (cursor.moveToNext()) {
				String oid = cursor.getString(cursor.getColumnIndex("oid"));
				String code = cursor.getString(cursor.getColumnIndex("code"));
				String name = cursor.getString(cursor.getColumnIndex("name"));
				double value = cursor.getDouble(cursor.getColumnIndex("value"));
				Long stime = cursor.getLong(cursor.getColumnIndex("stime"));
				String instime = cursor.getString(cursor.getColumnIndex("instime"));
				Date stimeD=new Date(stime*1000);
				 result+=" oid：" + oid 
						+ " code：" + code 
						+ " name：" + name 
						+ " value：" + value 
						+ " stime：" + stime
						+ " stimeD="+sdf.format(stimeD) 
						+ " instime：" +instime+"\n";
				Log.i("db",result);
			}
	        cursor.close();  

	      //at 20151118
			Cursor cursor2 = db
					.rawQuery(
							"SELECT distinct stime FROM igui_status WHERE stime>=? and stime<?",
							new String[] { startTime + "", endTime + "" });

			while (cursor2.moveToNext()) {
				Long stime = cursor2.getLong(cursor2.getColumnIndex("stime"));
				Date stimeD = new Date(stime * 1000);
				result+="XAxis:" + sdf.format(stimeD)+"\n";
				Log.i("db", "XAxis:" + sdf.format(stimeD)+"\n");
			}
			cursor2.close();
	      //end 20151118	
		      //at 20151123
				Cursor cursor3 = db.query("sys_parameter", 
						new String[] { "index_code","index_name" }, 
						null, null, 
						null, null, null);

				while (cursor3.moveToNext()) {
					String index_code = cursor3.getString(cursor3.getColumnIndex("index_code"));
					String index_name = cursor3.getString(cursor3.getColumnIndex("index_name"));
					Log.i("db", "index_code:" + index_code +";index_name:" + index_name+"\n");
				}
				cursor3.close();
				

				Cursor cursor4 = db.query("igui_status", 
						new String[] { "oid","code","stime" },
						null, null, 
						null, null, null);

				while (cursor4.moveToNext()) {
					String oid = cursor4.getString(cursor4.getColumnIndex("oid"));
					String code = cursor4.getString(cursor4.getColumnIndex("code"));
					Long stime = cursor4.getLong(cursor4.getColumnIndex("stime"));
					Date stimeD=new Date(stime*1000);
					Log.i("db", "oid:" + oid +";code:" + code
							+ " stime="+stime 
							+ " stimeD="+sdf.format(stimeD) 
							+"\n");
				}
				cursor4.close();
//
//				Cursor cursor4 = db.query("igui_status", 
//						new String[] { "oid","code","value","stime" }, 
//						null, null, 
//						null, null, null);
////						db.rawQuery(
////								"SELECT * FROM igui_status "+
////								"WHERE 1=1",
////								null);
//
//				while (cursor4.moveToNext()) {
//					int op_id = cursor4.getInt(cursor4.getColumnIndex("oid"));
//					String code = cursor4.getString(cursor4.getColumnIndex("code"));
//					double value = cursor.getDouble(cursor4.getColumnIndex("value"));
//					Long stime = cursor.getLong(cursor4.getColumnIndex("stime"));
////					String instime = cursor.getString(cursor4.getColumnIndex("instime"));
//					Date stimeD=new Date(stime*1000);
////					 result+= "op_id"+op_id
////							 +" code：" + code 
////							+ " value：" + value 
////							+ " stime：" + stime
////							+ " stimeD="+sdf.format(stimeD) 
////							+ " instime：" +instime+"\n";
//					Log.i("db", "op_id:" + op_id 
//							+ " code:" + code
//							+ " value:" + value
//							+ " stimeD="+sdf.format(stimeD) 
////							+ " instime：" +instime
//							+"\n");
//				}
//				cursor4.close();
				
		      //end 20151118	
			txtResult.setText(result);
			Log.i("db","end");
			// end 20151117
			// 关闭数据库
			db.close();
		}
	}

	// // 修改数据的方法
	// class ModifyListener implements OnClickListener {
	//
	// @Override
	// public void onClick(View v) {
	//
	// iguiDBHelper dbHelper = new iguiDBHelper(SQLiteActivity.this,
	// "igui.db", null, 1);
	// // 得到一个可写的数据库
	// SQLiteDatabase db = dbHelper.getWritableDatabase();
	// ContentValues cv = new ContentValues();
	// cv.put("sage", "23");
	// // where 子句 "?"是占位符号，对应后面的"1",
	// String whereClause = "id=?";
	// String[] whereArgs = { String.valueOf(1) };
	// // 参数1 是要更新的表名
	// // 参数2 是一个ContentValeus对象
	// // 参数3 是where子句
	// db.update("stu_table", cv, whereClause, whereArgs);
	// }
	// }

	// // 删除数据的方法
	// class DeleteListener implements OnClickListener {
	//
	// @Override
	// public void onClick(View v) {
	//
	// iguiDBHelper dbHelper = new iguiDBHelper(SQLiteActivity.this,
	// "stu_db", null, 1);
	// // 得到一个可写的数据库
	// SQLiteDatabase db = dbHelper.getReadableDatabase();
	// String whereClauses = "id=?";
	// String[] whereArgs = { String.valueOf(2) };
	// // 调用delete方法，删除数据
	// db.delete("stu_table", whereClauses, whereArgs);
	// }
	// }
//at 20160127	
//	private float getRandom(float range, float startsfrom) {
//		return (float) (Math.random() * range) + startsfrom;
//	}
//end 20160127	
}
