package cn.fatdeer.isocket;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;
import cn.fatdeer.isocket.apmode.UDPHelper;
import cn.fatdeer.isocket.entity.User;
import cn.fatdeer.isocket.pub.Const;
import cn.fatdeer.isocket.pub.Const.whichActivity;
import cn.fatdeer.isocket.pub.DoJson;
import cn.smssdk.SMSSDK;
import cn.smssdk.gui.SMSReceiver;

/**
 * @author GD.lws
 * @version 20150929
 * @Discribe This Dialog open by MainActivity for user Register 
 *            
 *           at 20150929
 *            1. request SMS verify code by user fill the mobile;
 *           at 20160121
 *            1. Register is on UDP ,change SocketHelper(TCP) to UDPHelper(UDP); 
 *            2. loginStr add email ; 
 *            3. SUCC-UPD close dialog; 
 * 
 */
public class RegisterDialog {
	private static String TAG = RegisterDialog.class.getSimpleName();
	Context mContext=null;
	private whichActivity mActivity;

	private BroadcastReceiver smsReceiver;

	private Dialog dialog = null;//new Dialog(mContext, R.style.CommonDialog);
	private EditText etUserName =null;// (EditText) dialog.findViewById(R.id.et_r_user_name);
	private EditText etNickName =null;// (EditText) dialog.findViewById(R.id.et_r_nick_name);
	private EditText etPassword = null;//(EditText) dialog.findViewById(R.id.et_r_password);
	private EditText etMobile =null;// (EditText) dialog.findViewById(R.id.et_r_mobile);
	private EditText etEMail =null;// (EditText) dialog.findViewById(R.id.et_r_email);
	private EditText etIdentifyNum =null; 
	private String sendStr=null;
	private String loginStr=null;
	private String refreshStr=null;
	private String serverIP=null;
	private String serverPort=null;

	public RegisterDialog(Context context,whichActivity activity,String ip,String port) {
		this.mContext=context;
		this.mActivity=activity;
		this.serverIP=ip;
		this.serverPort=port;

		dialog = new Dialog(mContext, R.style.CommonDialog);
		dialog.setContentView(R.layout.register_dialog);
		etUserName = (EditText) dialog.findViewById(R.id.et_r_user_name);
		etNickName = (EditText) dialog.findViewById(R.id.et_r_nick_name);
		etPassword = (EditText) dialog.findViewById(R.id.et_r_password);
		etMobile = (EditText) dialog.findViewById(R.id.et_r_mobile);
		etEMail = (EditText) dialog.findViewById(R.id.et_r_email);
		etIdentifyNum = (EditText) dialog.findViewById(R.id.et_r_identify_num);
		smsReceiver = new SMSReceiver(new SMSSDK.VerifyCodeReadListener() {
			@Override
			public void onReadVerifyCode(final String verifyCode) {
				etIdentifyNum.setText(verifyCode);
			}
		});
		mContext.registerReceiver(smsReceiver, new IntentFilter(
				"android.provider.Telephony.SMS_RECEIVED"));
	}

	public void showRegisterDialog() {


		OnClickListener ocl = new OnClickListener() {
			public void onClick(View v) {
				User user=new User();
				user.setRole("OB");
				user.setUsername(etUserName.getText().toString().trim());
				user.setNickname(etNickName.getText().toString().trim());
				user.setPassword(etPassword.getText().toString().trim());
				user.setMobile(etMobile.getText().toString().trim());
				user.setEmail(etEMail.getText().toString().trim());
				
				if (v.getId() == R.id.btn_dialog_r_ok) {
					if (!user.isChecked()) {
						Toast.makeText(v.getContext(), "信息录入有问题",
								Toast.LENGTH_SHORT).show();
					}
					else if(etIdentifyNum.getText().length()!=4) {
						Toast.makeText(v.getContext(), "请输入4位短信验证码",
								Toast.LENGTH_SHORT).show();
					}
					else {  	
						loginStr="OB|"+user.getUsername()+"|"+user.getNickname()
								+"|"+user.getPassword()+"|"+user.getMobile()
								+"|"+user.getEmail() //at 20160121
								+"|"+etIdentifyNum.getText().toString();
						refreshStr="TCP:"+serverIP+":"+serverPort+":"+user.getUsername()+":"+user.getPassword();
						sendStr=DoJson.instance().toJSon("REGISTER", "REG", null, loginStr);
						CLog.i(TAG, "sendStr="+sendStr);
						sendRegister();
						
					}
				} else if(v.getId()==R.id.btn_dialog_debug) {
					etUserName.setText("Gaius");
					etNickName.setText("凯撒");
					etPassword.setText("Roma@BC100!");
					etMobile.setText("15920486449");
					etEMail.setText("caesar@roma.org");
				} else if(v.getId()==R.id.btn_dialog_r_getSMS ) {
					if(etMobile.getText().toString().length()==11)
						SMSSDK.getVerificationCode("86", etMobile.getText().toString());
					else 
						Toast.makeText(v.getContext(), "请输入11位国内手机号",
								Toast.LENGTH_SHORT).show();
				}
				else if(v.getId()==R.id.btn_dialog_r_cancel) {
					dialog.dismiss();
					Const.broadCastToActivity("CLOSE", "R_DIALOG", null,mActivity);
				}
			}
		};
		dialog.findViewById(R.id.btn_dialog_r_ok).setOnClickListener(ocl);
		dialog.findViewById(R.id.btn_dialog_r_cancel).setOnClickListener(ocl);
		dialog.findViewById(R.id.btn_dialog_debug).setOnClickListener(ocl);
		dialog.findViewById(R.id.btn_dialog_r_getSMS).setOnClickListener(ocl);
		dialog.setCancelable(false);
		dialog.show();
	}


	private void sendRegister() {
		Thread socketThread = new Thread() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
//at 20160121				
//				SocketHelper sockethelper = new SocketHelper(serverIP,serverPort);
//				if (sockethelper.openSocket()) {
//					sockethelper.sendMessage(sendStr);
//					String rtnStr= sockethelper.rcvMessage();
//					CLog.i(tag, "rtnStr="+rtnStr);
//					if(rtnStr.equals("{\"O\":\"REGISTER\",\"M\":\"SUCC-ADD\"}")) {
//						Const.broadCastToActivity("SUCC", "R_DIALOG:"+refreshStr ,null,mActivity);
//						dialog.dismiss();
//					}
//					sockethelper.closeSocket();
//				}
				UDPHelper udphelper = new UDPHelper(serverIP,serverPort,"REG");
				if (udphelper.openSocket()) {
					udphelper.sendMessage(sendStr);
					String rtnStr= udphelper.rcvMessage();
					Log.i(TAG, "rtnStr:" + rtnStr);
					if(rtnStr.length()>0&&
//at 20160121						rtnStr.equals("{\"O\":\"REGISTER\",\"M\":\"SUCC-ADD\"}")) {
						(rtnStr.indexOf("SUCC-ADD")>=0||rtnStr.indexOf("SUCC-UPD")>=0)) {
						Const.broadCastToActivity(
								"SUCC", "R_DIALOG:"+refreshStr ,null,mActivity);
						dialog.dismiss();
					} else {
						CLog.i(TAG, "Register User fail");
						Const.broadCastToActivity(
								"FAIL", "R_DIALOG:"+rtnStr ,null,mActivity);
					}
					CLog.i(TAG, "Ready to close UDP Socket");
					udphelper.closeSocket();
				}
				
//end 20160121				
			}
		};
		socketThread.start();
	}

}
