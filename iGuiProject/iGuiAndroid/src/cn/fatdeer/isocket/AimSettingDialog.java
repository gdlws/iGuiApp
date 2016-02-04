package cn.fatdeer.isocket;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import cn.fatdeer.isocket.chart.HistoryChartActivity;
import cn.fatdeer.isocket.chart.iGuiDBHelper;
import cn.fatdeer.isocket.entity.Module;
import cn.fatdeer.isocket.pub.Const;
import cn.fatdeer.isocket.pub.Const.whichActivity;

/**
 * @author GD.lws
 * @version 20160201
 * @Discribe This Dialog deploy the module
 *            
 *           at 20160201
 *            1. UI can work now; 
 *           at 20160202
 *            1. spinner_animal for user choose what kind of animal; 
 *            2. query aim value from SQLite; 
 *           
 */
public class AimSettingDialog implements OnSeekBarChangeListener{
	private static String tag = AimSettingDialog.class.getSimpleName();

	private TextView tv_aim1;
	private TextView tv_aim2;
	private TextView tv_aim3;
	private EditText et_aimt1;
	private EditText et_aimh1;
	private EditText et_aimt2;
	private EditText et_aimh2;
	private EditText et_aimt3;
	private EditText et_aimh3;
	
	Context mContext=null;
	Module mModule =null;
	String mName =null;
	Handler mHandler = null;
	private whichActivity mActivity;
	private Button btn_lastclicked;
	private EditText et_focus;
	private boolean setHumidity = false;
//	int MIN_TEMPERATURE = 20;
//	int MIN_HUMIDITY = 50;
//	int[] codes =  {1,2,3,4};
	String[] names ; //= {"印度星","苏卡达","辐射","豹纹" };
	private ArrayAdapter<String> adapter;
	//2 - 12 - 3
//	private int[][][] aimValue = {
//			{ 	{ 1, 27, 60 }, { 1, 26, 62 }, { 1, 25, 64 }, 
//				{ 1, 26, 68 }, { 1, 28, 70 }, { 1, 30, 72 }, 
//				{ 1, 31, 74 }, { 1, 32, 76 }, { 1, 31, 78 }, 
//				{ 1, 30, 74 }, { 1, 29, 68 }, { 1, 28, 64 } },
//			{ 	{ 2, 37, 50 }, { 2, 36, 52 }, { 2, 35, 54 }, 
//				{ 2, 36, 58 }, { 2, 38, 60 }, { 2, 40, 62 }, 
//				{ 2, 41, 64 }, { 2, 42, 66 }, { 2, 41, 68 }, 
//				{ 2, 40, 64 }, { 2, 39, 58 }, { 2, 38, 54 } },
//			{ 	{ 3, 47, 40 }, { 3, 36, 42 }, { 3, 35, 44 }, 
//				{ 3, 36, 48 }, { 3, 48, 50 }, { 3, 40, 52 }, 
//				{ 3, 41, 54 }, { 3, 42, 56 }, { 3, 51, 58 }, 
//				{ 3, 40, 54 }, { 3, 39, 48 }, { 3, 38, 44 } },
//			{ 	{ 4, 57, 30 }, { 4, 56, 32 }, { 4, 55, 34 }, 
//				{ 4, 56, 38 }, { 4, 58, 40 }, { 4, 50, 42 }, 
//				{ 4, 61, 44 }, { 4, 62, 46 }, { 4, 61, 48 }, 
//				{ 4, 60, 44 }, { 4, 59, 38 }, { 4, 58, 34 } } 
//	};
	private int chosenAnimal=0;
	private int chosenTimeZone=1;
	private int chosenValue=0;
	private final static int valueNum=24; // how many value send to Server
	private int[] modified = new int[valueNum];

	public AimSettingDialog(Context context,String name, Module module,whichActivity activity) {
		this.mContext=context;
		this.mModule=module;
		this.mName=name;
		this.mActivity=activity;
		chosenAnimal=0;
		chosenTimeZone=1;
		chosenValue=0;
		for(int i=0;i<valueNum;i++) modified[i]=-999;
		if(Const.DEBUG_FLAG) CLog.i(tag, "AimSettingDialog begin");
	}

	public void showDialog() {
		final Dialog dialog = new Dialog(mContext, R.style.CommonDialog);
		dialog.setContentView(R.layout.aimsetting_dialog);

		tv_aim1 = (TextView) dialog.findViewById(R.id.tv_aim1);
		tv_aim2 = (TextView) dialog.findViewById(R.id.tv_aim2);
		tv_aim3 = (TextView) dialog.findViewById(R.id.tv_aim3);
		et_aimt1 = (EditText) dialog.findViewById(R.id.et_aimt1);
		et_aimh1 = (EditText) dialog.findViewById(R.id.et_aimh1);
		et_aimt2 = (EditText) dialog.findViewById(R.id.et_aimt2);
		et_aimh2 = (EditText) dialog.findViewById(R.id.et_aimh2);
		et_aimt3 = (EditText) dialog.findViewById(R.id.et_aimt3);
		et_aimh3 = (EditText) dialog.findViewById(R.id.et_aimh3);
		final Button btn_dawn = (Button) dialog.findViewById(R.id.btn_dialog_dawn);
		final Button btn_morning = (Button) dialog.findViewById(R.id.btn_dialog_morning);
		final Button btn_afternoon = (Button) dialog.findViewById(R.id.btn_dialog_afternoon);
		final Button btn_night = (Button) dialog.findViewById(R.id.btn_dialog_night);
		final SeekBar sb_aimset = (SeekBar) dialog.findViewById(R.id.sb_setvalue);
		final Spinner sp_animal = (Spinner) dialog.findViewById(R.id.spinner_animal);

		names=this.getBoxNames();
		adapter = new ArrayAdapter<String>(mContext,
				android.R.layout.simple_spinner_item, names);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sp_animal.setAdapter(adapter);
		sp_animal.setOnItemSelectedListener(new SpinnerSelectedListener());
		sp_animal.setVisibility(View.VISIBLE);
		sb_aimset.setOnSeekBarChangeListener(this);
		btn_lastclicked=btn_morning;
		et_focus=et_aimt1;
		sb_aimset.setMax(Const.AIMSETTING_MAXT-Const.AIMSETTING_MINT);
		sb_aimset.setProgress(Integer.parseInt(et_aimt1.getText().toString())-Const.AIMSETTING_MINT);
		
		
		OnFocusChangeListener ofcl = new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean arg1) {
				CLog.i(tag, "arg1="+arg1);
				if(arg1&&v.getId()==R.id.et_aimt1) {
					CLog.i(tag, "edittext aimt1 is focused");
					chosenValue=0;
					onFocusET(et_aimt1,sb_aimset,false);
				} else if(arg1&&v.getId()==R.id.et_aimh1) {
					CLog.i(tag, "edittext aimh1 is focused");
					chosenValue=1;
					onFocusET(et_aimh1,sb_aimset,true);
				} else if(arg1&&v.getId()==R.id.et_aimt2) {
					CLog.i(tag, "edittext aimt2 is focused");
					chosenValue=2;
					onFocusET(et_aimt2,sb_aimset,false);
				} else if(arg1&&v.getId()==R.id.et_aimh2) {
					CLog.i(tag, "edittext aimh2 is focused");
					chosenValue=3;
					onFocusET(et_aimh2,sb_aimset,true);
				} else if(arg1&&v.getId()==R.id.et_aimt3) {
					CLog.i(tag, "edittext aimt3 is focused");
					chosenValue=4;
					onFocusET(et_aimt3,sb_aimset,false);
				} else if(arg1&&v.getId()==R.id.et_aimh3) {
					CLog.i(tag, "edittext aimh3 is focused");
					chosenValue=5;
					onFocusET(et_aimh3,sb_aimset,true);
				}
				
			}
			
		};
		OnClickListener ocl = new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (v.getId() == R.id.btn_dialog_morning) {
					tv_aim1.setText("06:00-08:00");
					tv_aim2.setText("08:00-10:00");
					tv_aim3.setText("10:00-12:00");
					chosenTimeZone=1;
					recolorBtn(btn_morning);
				} else if (v.getId() == R.id.btn_dialog_afternoon) {
					tv_aim1.setText("12:00-14:00");
					tv_aim2.setText("14:00-16:00");
					tv_aim3.setText("16:00-18:00");
					chosenTimeZone=2;
					recolorBtn(btn_afternoon);
				} else if (v.getId() == R.id.btn_dialog_night) {
					tv_aim1.setText("18:00-20:00");
					tv_aim2.setText("20:00-22:00");
					tv_aim3.setText("22:00-24:00");
					chosenTimeZone=3;
					recolorBtn(btn_night);
				} else if (v.getId() == R.id.btn_dialog_dawn) {
					tv_aim1.setText("00:00-02:00");
					tv_aim2.setText("02:00-04:00");
					tv_aim3.setText("04:00-06:00");
					chosenTimeZone=4;
					recolorBtn(btn_dawn);
				} else
				if (v.getId() == R.id.btn_dialog_ok) {
					StringBuilder orderStringBuilder = new StringBuilder();
					for(int i=0;i<valueNum;i++) {
						orderStringBuilder.append(modified[i]+",");
					}
//					if(btC50W.isChecked()!=mModule.getValue("S1").equals("1")) {
//						mModule.setStatus("WS1", (btC50W.isChecked()?"1":"0"));
//						orderStringBuilder.append("SET+S1="+(btC50W.isChecked()?"1 ":"0 "));
//					}
//					if(btUVA.isChecked()!=mModule.getValue("S2").equals("1")) {
//						mModule.setStatus("WS2", (btUVA.isChecked()?"1":"0"));
//						orderStringBuilder.append("SET+S2="+(btUVA.isChecked()?"1 ":"0 "));
//					}
//					if(btUVB.isChecked()!=mModule.getValue("S3").equals("1")) {
//						mModule.setStatus("WS3", (btUVB.isChecked()?"1":"0"));
//						orderStringBuilder.append("SET+S3="+(btUVB.isChecked()?"1 ":"0 "));
//					}
//					if(btC75W.isChecked()!=mModule.getValue("S4").equals("1")) {
//						mModule.setStatus("WS4", (btC75W.isChecked()?"1":"0"));
//						orderStringBuilder.append("SET+S4="+(btC75W.isChecked()?"1 ":"0 "));
//					}
//					if(btHMD.isChecked()!=mModule.getValue("S5").equals("1")) {
//						mModule.setStatus("WS5", (btHMD.isChecked()?"1":"0"));
//						orderStringBuilder.append("SET+S5="+(btHMD.isChecked()?"1 ":"0 "));
//					}
//					if(sbFAN.getProgress()!=Integer.parseInt(mModule.getValue("S6"))) {
//						mModule.setStatus("WS6", ""+(51*sbFAN.getProgress()));
//						orderStringBuilder.append("SET+S6="+(51*sbFAN.getProgress())+" ");
//					}
//	
//					if(Integer.parseInt(et_aimh.getText().toString())!=
//						Integer.parseInt(mModule.getValue("HA"))) {
//						mModule.setStatus("WHA", et_aimh.getText().toString()); //at 20151214
//						orderStringBuilder.append("SET+HA="+et_aimh.getText()+" ");
//					}
//					if(Integer.parseInt(et_aimt.getText().toString())!=
//								Integer.parseInt(mModule.getValue("TA"))) {
//						mModule.setStatus("WTA", et_aimt.getText().toString()); //at 20151214
//						orderStringBuilder.append("SET+TA="+et_aimt.getText()+" ");
//					}
//					orderStringBuilder.append("SET+F=300");
					String orderStr=orderStringBuilder.toString();
					CLog.i(tag, "orderStr="+orderStr);
//					if(orderStr.length()>0) { //at 20151203
//						Const.broadCastToActivity("SUCC", "DEPLOYDIALOG!" + 
//						DoJson.instance().toJSon("SEND", mName,
//									mModule.getName(), orderStr), null,mActivity);
//						mModule.setInOrder(true);
//						dialog.dismiss();
//					}
				}
				else {
					dialog.dismiss();
				}
			}
		};
		dialog.findViewById(R.id.btn_dialog_ok).setOnClickListener(ocl);
		dialog.findViewById(R.id.btn_dialog_cancel).setOnClickListener(ocl);
		dialog.findViewById(R.id.btn_dialog_dawn).setOnClickListener(ocl);
		dialog.findViewById(R.id.btn_dialog_morning).setOnClickListener(ocl);
		dialog.findViewById(R.id.btn_dialog_afternoon).setOnClickListener(ocl);
		dialog.findViewById(R.id.btn_dialog_night).setOnClickListener(ocl);

		dialog.findViewById(R.id.et_aimt1).setOnFocusChangeListener(ofcl);
		dialog.findViewById(R.id.et_aimh1).setOnFocusChangeListener(ofcl);
		dialog.findViewById(R.id.et_aimt2).setOnFocusChangeListener(ofcl);
		dialog.findViewById(R.id.et_aimh2).setOnFocusChangeListener(ofcl);
		dialog.findViewById(R.id.et_aimt3).setOnFocusChangeListener(ofcl);
		dialog.findViewById(R.id.et_aimh3).setOnFocusChangeListener(ofcl);
		
		dialog.setCancelable(false);
		dialog.show();
	}
	
	class SpinnerSelectedListener implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			chosenAnimal=arg2;
			refreshValues();
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}


	private void refreshValues() {
//		et_aimt1.setText(""+aimValue[chosenAnimal][3*chosenTimeZone][1]);
//		et_aimh1.setText(""+aimValue[chosenAnimal][3*chosenTimeZone][2]);
//		et_aimt2.setText(""+aimValue[chosenAnimal][3*chosenTimeZone+1][1]);
//		et_aimh2.setText(""+aimValue[chosenAnimal][3*chosenTimeZone+1][2]);
//		et_aimt3.setText(""+aimValue[chosenAnimal][3*chosenTimeZone+2][1]);
//		et_aimh3.setText(""+aimValue[chosenAnimal][3*chosenTimeZone+2][2]);
		int[] aimValues=getAimValue(names[chosenAnimal],chosenTimeZone);
		et_aimt1.setText(""+aimValues[0]);
		et_aimh1.setText(""+aimValues[1]);
		et_aimt2.setText(""+aimValues[2]);
		et_aimh2.setText(""+aimValues[3]);
		et_aimt3.setText(""+aimValues[4]);
		et_aimh3.setText(""+aimValues[5]);
	}

	private void onFocusET(EditText thisET, SeekBar sb, boolean isHumidity){

		et_focus=thisET;
		setHumidity=isHumidity;
		if(isHumidity) {
			sb.setProgress((Integer.parseInt(thisET.getText().toString())-Const.AIMSETTING_MINH)/2);
			sb.setMax((Const.AIMSETTING_MAXH-Const.AIMSETTING_MINH)/2);
		} else {
			sb.setProgress(Integer.parseInt(thisET.getText().toString())-Const.AIMSETTING_MINT);
			sb.setMax((Const.AIMSETTING_MAXT-Const.AIMSETTING_MINT));
		}

		thisET.setInputType(InputType.TYPE_NULL); // close software keyboard
	}

	@Override
	public void onProgressChanged(SeekBar sb, int arg1, boolean arg2) {
		int value;
		if(arg2) {
			if(this.setHumidity) {
				value=Const.AIMSETTING_MINH+2*sb.getProgress();
			} else {
				value=Const.AIMSETTING_MINT+sb.getProgress();
			}
			this.et_focus.setText(""+value);
			modified[Const.NUMINTIMEZONE*(chosenTimeZone-1)+chosenValue]=value;
			
		}
		CLog.i(tag, "arg2="+arg2);
		CLog.i(tag, "changeValues=");
		for(int i=0;i<modified.length;i++) {
			CLog.i(tag, modified[i]+" ");
		}
		CLog.i(tag, "progress="+sb.getProgress());
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {}

	private void recolorBtn(Button thisButton){
		thisButton.setTextColor(0xFFFF00FF);
		thisButton.setBackgroundColor(0xFF00FF00);
		btn_lastclicked.setTextColor(0xFFFFFFFF);
		btn_lastclicked.setBackgroundColor(0xFF000000);
		btn_lastclicked=thisButton;
		refreshValues();
	}
	

	private String[] getBoxNames() {
		String[] result=new String[8];
		iGuiDBHelper dbHelper = new iGuiDBHelper(
				mContext , "igui.db", null, 1);
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor cursor = db
				.rawQuery(
						"SELECT distinct box_name as boxName FROM sys_index_aim", null);
		
		int i=0;
		while (cursor.moveToNext()) {
			result[i++] = cursor.getString(cursor.getColumnIndex("boxName"));
			if(i==8) break;
		}
		cursor.close();
		db.close();

		for(int j=0;j<result.length;j++)
			Log.i(tag, "result["+j+"]:" + result[j] + "\n");
		return result;
	}
	
	private int[] getAimValue(String tortoiseType, int timeZone) {
		int[] result=new int[6];

		iGuiDBHelper dbHelper = new iGuiDBHelper(
				mContext , "igui.db", null, 1);
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor cursor = db
				.rawQuery(
				"SELECT aimt_1,aimh_1,aimt_2,aimh_2,aimt_3,aimh_3 FROM sys_index_aim "+
				"where box_name=? and hour_name=?", 
						new String[] { tortoiseType, timeZone + ""  });
		
		if (cursor.moveToNext()) {
			result[0] = cursor.getInt(cursor.getColumnIndex("aimt_1"));
			result[1] = cursor.getInt(cursor.getColumnIndex("aimh_1"));
			result[2] = cursor.getInt(cursor.getColumnIndex("aimt_2"));
			result[3] = cursor.getInt(cursor.getColumnIndex("aimh_2"));
			result[4] = cursor.getInt(cursor.getColumnIndex("aimt_3"));
			result[5] = cursor.getInt(cursor.getColumnIndex("aimh_3"));
		}
		cursor.close();
		db.close();

		for(int j=0;j<result.length;j++)
			Log.i(tag, "result["+j+"]:" + result[j] + "\n");
		
		return result;
	}
}
