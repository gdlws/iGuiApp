package cn.fatdeer.isocket;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import cn.fatdeer.isocket.entity.Login;
import cn.fatdeer.isocket.pub.Const;
import cn.fatdeer.isocket.pub.RegExpValidatorUtils;
import cn.fatdeer.isocket.pub.Const.whichActivity;

import cn.fatdeer.isocket.R;

/**
 * @author GD.lws
 * @version 20150929
 * @Discribe This Dialog open by MainActivity for user Login 
 *            
 *           at 20150929
 *            1. remove UDP; 
 *           at 20160202
 *            1. new button for expert , open AimSettingDialog; 
 * 
 */
public class LoginDialog {
	private static String tag = LoginDialog.class.getSimpleName();
	Context mContext=null;
	Handler mHandler = null;
	Login mLogin=null;
	private whichActivity mActivity; //at 20151120

	public LoginDialog(Context context,Login login,whichActivity activity) {
		this.mContext=context;
		this.mLogin=login;
		this.mActivity=activity;
		if(Const.DEBUG_FLAG) CLog.i(tag, "LoginDialog:"+login.toString());
	}

	public void showLoginDialog() {
		final Dialog dialog = new Dialog(mContext, R.style.CommonDialog);
		dialog.setContentView(R.layout.login_dialog);
		final EditText etConnType = (EditText) dialog.findViewById(R.id.et_conntype);
		final EditText etHostIP = (EditText) dialog.findViewById(R.id.et_hostip);
		final EditText etTCPPort = (EditText) dialog.findViewById(R.id.et_tcpport);
		final EditText etUDPPort = (EditText) dialog.findViewById(R.id.et_udpport);
		final EditText etName = (EditText) dialog.findViewById(R.id.et_name);
		final EditText etPassword = (EditText) dialog.findViewById(R.id.et_pwd);
		etConnType.setText(mLogin.getConnType());
		etHostIP.setText(mLogin.getHostIP());
		etTCPPort.setText(mLogin.getTCPPort());
		etUDPPort.setText(mLogin.getUDPPort());
		etName.setText(mLogin.getName());
		etPassword.setText(mLogin.getPassword());

		OnClickListener ocl = new OnClickListener() {
			public void onClick(View v) {
				String connType = null;
				String ip = null;
				String tcpport = null;
				String udpport = null;
				String name = null;
				String password = null;
				if (v.getId() == R.id.btn_dialog_ok) {
					connType = etConnType.getText().toString().trim();
					ip = etHostIP.getText().toString().trim();
					tcpport = etTCPPort.getText().toString().trim();
					udpport = etUDPPort.getText().toString().trim();
					name = etName.getText().toString().trim();
					password = etPassword.getText().toString().trim();
					if (TextUtils.isEmpty(connType) 
					|| TextUtils.isEmpty(ip)
					|| TextUtils.isEmpty(tcpport)
					|| TextUtils.isEmpty(udpport)
					|| TextUtils.isEmpty(name)
					|| TextUtils.isEmpty(password)) {
						Toast.makeText(v.getContext(), "各项都需要录入信息",
								Toast.LENGTH_SHORT).show();
					} else if (!connType.equals("TCP")
//at 20150929							&& !connType.equals("UDP")
							) {
						Toast.makeText(v.getContext(), "网络类型只能是TCP",
								Toast.LENGTH_SHORT).show();
					} else if (!RegExpValidatorUtils.isIP(ip)) {
						Toast.makeText(v.getContext(), "IP地址不符合要求",
								Toast.LENGTH_SHORT).show();
					} else if (!RegExpValidatorUtils.IsIntNumber(tcpport)
							||!RegExpValidatorUtils.IsIntNumber(udpport)
							) {
						Toast.makeText(v.getContext(), "端口号必须是正整数",
								Toast.LENGTH_SHORT).show();
					} else if (Integer.parseInt(tcpport) < 0
							|| Integer.parseInt(tcpport) > 65536
							|| Integer.parseInt(udpport) < 0
							|| Integer.parseInt(udpport) > 65536) {
						Toast.makeText(v.getContext(), "端口号必须0-65535之间",
								Toast.LENGTH_SHORT).show();
					} else {
						dialog.dismiss();
						Const.broadCastToActivity("SUCC", "DIALOG:" + connType
								+ ":" + ip + ":" + tcpport +":"+udpport + ":" + name + ":"
								+ password, null,mActivity);
					}
				}
				else if (v.getId() == R.id.tvName) {	
					Const.broadCastToActivity("INF", "Enter Input Name",null,mActivity);
				}
				else if (v.getId() == R.id.btn_dialog_register) {
					dialog.dismiss(); //at 20150722
					Const.broadCastToActivity("REGISTER", "DIALOG", null,mActivity);
				}
				else {
					dialog.dismiss();
					Const.broadCastToActivity("CLOSE", "DIALOG", null,mActivity);
				}
			}
		};
		dialog.findViewById(R.id.btn_dialog_ok).setOnClickListener(ocl);
		dialog.findViewById(R.id.btn_dialog_cancel).setOnClickListener(ocl);
		dialog.findViewById(R.id.btn_dialog_register).setOnClickListener(ocl);
		dialog.setCancelable(false);
		dialog.show();
	}



}
