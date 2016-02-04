package cn.fatdeer.isocket;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.ToggleButton;
import cn.fatdeer.isocket.entity.Module;
import cn.fatdeer.isocket.pub.Const;
import cn.fatdeer.isocket.pub.Const.whichActivity;
import cn.fatdeer.isocket.pub.DoJson;

/**
 * @author GD.lws
 * @version 20160120
 * @Discribe This Dialog deploy the module
 *            
 *           at 20151127
 *            1. new file , now can send SET order S1 to S6  awesome resource; 
 *           at 20151203
 *            1. after DEPLOYDIALOG use ! replace -; 
 *            2. aimH&aimT control;
 *           at 20151211
 *            1. fix bug: SET+S6 and SET+HA SET+TA; 
 *            2. submit send order SET+F=300;
 *           at 20151214
 *            1. restore wanted value in module's hash map, W+parameter index;
 *           at 20160120
 *            1. change the order of switch :UVB(S3 to S5); C75W(S4 to S6); Humidifier(S5 to S3); FAN(S6 to S4);
 *            2. fix the bug of Standard board, except exit ;  
 *           at 20160202
 *            1. new button for expert , open AimSettingDialog; 
 */
public class DeployDialog {
	private static String tag = DeployDialog.class.getSimpleName();
	Context mContext=null;
	Module mModule =null;
	String mName =null;
	Handler mHandler = null;
	private whichActivity mActivity;

	public DeployDialog(Context context,String name, Module module,whichActivity activity) {
		this.mContext=context;
		this.mModule=module;
		this.mName=name;
		this.mActivity=activity;
		if(Const.DEBUG_FLAG) CLog.i(tag, "DeployDialog begin");
	}

	public void showLoginDialog() {
		final Dialog dialog = new Dialog(mContext, R.style.CommonDialog);
		dialog.setContentView(R.layout.deploy_dialog);
		final ToggleButton btC50W = (ToggleButton) dialog.findViewById(R.id.TB_C50W);
		final ToggleButton btC75W = (ToggleButton) dialog.findViewById(R.id.TB_C75W);
		final ToggleButton btUVA = (ToggleButton) dialog.findViewById(R.id.TB_UVA);
		final ToggleButton btUVB = (ToggleButton) dialog.findViewById(R.id.TB_UVB);
		final ToggleButton btHMD = (ToggleButton) dialog.findViewById(R.id.TB_HUMIDIFIER);
		final SeekBar sbFAN = (SeekBar) dialog.findViewById(R.id.SB_FAN);
		final EditText et_aimh = (EditText) dialog.findViewById(R.id.et_aimh); //at 20151203
		final EditText et_aimt = (EditText) dialog.findViewById(R.id.et_aimt); //at 20151203
		
		btC50W.setChecked(mModule.getValue("S1").equals("1"));
		btUVA.setChecked(mModule.getValue("S2").equals("1"));
		btUVB.setChecked(mModule.getValue("S3").equals("1"));
		btC75W.setChecked(mModule.getValue("S4").equals("1"));
		btHMD.setChecked(mModule.getValue("S5").equals("1"));
		sbFAN.setProgress(Integer.parseInt(mModule.getValue("S6"))*51);
		et_aimh.setText(mModule.getValue("HA"));
		et_aimt.setText(mModule.getValue("TA"));
		
		OnClickListener ocl = new OnClickListener() {
			public void onClick(View v) {
				if (v.getId() == R.id.btn_dialog_ok) {
					StringBuilder orderStringBuilder = new StringBuilder();
					if(btC50W.isChecked()!=mModule.getValue("S1").equals("1")) {
						mModule.setStatus("WS1", (btC50W.isChecked()?"1":"0"));
						orderStringBuilder.append("SET+S1="+(btC50W.isChecked()?"1 ":"0 "));
					}
					if(btUVA.isChecked()!=mModule.getValue("S2").equals("1")) {
						mModule.setStatus("WS2", (btUVA.isChecked()?"1":"0"));
						orderStringBuilder.append("SET+S2="+(btUVA.isChecked()?"1 ":"0 "));
					}
					if(btUVB.isChecked()!=mModule.getValue("S3").equals("1")) {
						mModule.setStatus("WS3", (btUVB.isChecked()?"1":"0"));
						orderStringBuilder.append("SET+S3="+(btUVB.isChecked()?"1 ":"0 "));
					}
					if(btC75W.isChecked()!=mModule.getValue("S4").equals("1")) {
						mModule.setStatus("WS4", (btC75W.isChecked()?"1":"0"));
						orderStringBuilder.append("SET+S4="+(btC75W.isChecked()?"1 ":"0 "));
					}
					if(btHMD.isChecked()!=mModule.getValue("S5").equals("1")) {
						mModule.setStatus("WS5", (btHMD.isChecked()?"1":"0"));
						orderStringBuilder.append("SET+S5="+(btHMD.isChecked()?"1 ":"0 "));
					}
					if(sbFAN.getProgress()!=Integer.parseInt(mModule.getValue("S6"))) {
						mModule.setStatus("WS6", ""+(51*sbFAN.getProgress()));
						orderStringBuilder.append("SET+S6="+(51*sbFAN.getProgress())+" ");
					}
	
					if(Integer.parseInt(et_aimh.getText().toString())!=
						Integer.parseInt(mModule.getValue("HA"))) {
						mModule.setStatus("WHA", et_aimh.getText().toString()); //at 20151214
						orderStringBuilder.append("SET+HA="+et_aimh.getText()+" ");
					}
					if(Integer.parseInt(et_aimt.getText().toString())!=
								Integer.parseInt(mModule.getValue("TA"))) {
						mModule.setStatus("WTA", et_aimt.getText().toString()); //at 20151214
						orderStringBuilder.append("SET+TA="+et_aimt.getText()+" ");
					}
					orderStringBuilder.append("SET+F=300");
					String orderStr=orderStringBuilder.toString();
					CLog.i(tag, "orderStr="+orderStr);
					if(orderStr.length()>0) { //at 20151203
						Const.broadCastToActivity("SUCC", "DEPLOYDIALOG!" + 
						DoJson.instance().toJSon("SEND", mName,
									mModule.getName(), orderStr), null,mActivity);
						mModule.setInOrder(true);
						dialog.dismiss();
					}
				}
				//at 20160202
				else if (v.getId() == R.id.btn_dialog_expert) {
					dialog.dismiss();
					Const.broadCastToActivity("EXPERT", "DIALOG", null,
							mActivity);
				}
				//end 20160202
				else {
					dialog.dismiss();
				}
			}
		};
		dialog.findViewById(R.id.btn_dialog_ok).setOnClickListener(ocl);
		dialog.findViewById(R.id.btn_dialog_cancel).setOnClickListener(ocl);
		dialog.findViewById(R.id.btn_dialog_expert).setOnClickListener(ocl); //at 20160202
		dialog.setCancelable(false);
		dialog.show();
	}



}
