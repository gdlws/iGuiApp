package cn.fatdeer.isocket.apmode;

//import java.util.List;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import cn.fatdeer.isocket.CLog;
import cn.fatdeer.isocket.R;
import cn.fatdeer.isocket.pub.Const;
import cn.fatdeer.isocket.pub.Const.InitStage;
import cn.fatdeer.isocket.pub.DoJson;

/**
 * @author GD.lws
 * @version 20150401
 * @Discribe This Activity setup esp8266's WIFI, User can communicate to esp8266
 *           as socket client
 * 
 *           at 20150401 
 *            1) SocketThread begin move to OnResume(), SocketThread stop move to OnPause();
 *            2) Add new Hot-SPOT CoC; 
 *           at 20150728
 *            1. lvModules for chosen module's AP; 
 *            2. Const.prefix_AP for seartch AP; 
 *           at 20150917
 *            1. new Parameter orderStr for The orders that send to Arduino; 
 *            2. new Parameter keyStr for Arduino's key of communication; 
 *            3. new function getRandomString() for seeding the keyStr;
 *           at 20150922
 *            1. upload button for init arduino's TEA key; 
 *            2. mContext rename to s_context for broadcast;
 *           at 20150924 
 *            1. new button opensta for inform arduino to STA mode; 
 *            2. new function send2module() for INIT:SUCC;
 *           at 20150925
 *            1. new parameter username from MainActivity.java;
 *            2. clickedAP for chosen AP, clickedModule for chosen module, 
 *                e.g. clickedAP=esp8266_IS21516001  clickedModule=IS21516001; 
 *            3. after join module's AP, close internet's WIFI; 
 *            4. regBroadcast() remove request;
 *           at 20151214
 *            1. prefix 6636 before order for sending module SET+A&SET+E; 
 *           at 20151217
 *            1. send2Server() for UDP short connection(UDPHelper); 
 *            2. remove heavy UDP connection mode, use light UDPHelper;
 *            3. new parameter initStage for status of init stage; 
 *           at 20151218
 *            1. send2module() add <NAN> </NAN> to packet;
 *            2. setup packet  send SET+A=1; 
 *            3. after send2module() do not receive data from module close socket directly; 
 *           at 20160127
 *            1. new widget oper_hint&cb_debug;
 *            2. 5 stage for initialize new module;
 */
public class WifiActivity extends Activity {

	public static final String TAG = WifiActivity.class.getSimpleName();
	private long exitTime = 0;
	private TextView status;
	private TextView oper_hint; //at 20160127
	private Button lookup;
	private Button upload;
	private Button join;
	private Button setup;
	private Button changewifi;
	private Button connect;
	private Button scan;
	private Button check;
	private Button rtn_btn;
	private TextView ssidText;
	private EditText pwdText;
	private CheckBox cb_debug; //at 20160127
	private ProgressBar circleProgressBar;

	private WifiAdmin mWifiAdmin;
	public static Context s_context;
	private String keyStr; // Arduino's TEA Key for communication\
	DateFormat df = new SimpleDateFormat("hh:mm:ss.SSS",Locale.CHINA);
	private BroadcastReceiver bcReceiver=null;
	private String esp8266Name = null; // pre with esp8266_
	private String moduleName=null; // remove esp8266_ of esp8266Name
	private String APName=null; // esp8266 will connect AP 
	private String username=null;
	private String serverIP=null;
	private String serverPort=null;
	private InitStage initStage=InitStage.INIT; //at 20160127 LOOKUP;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		s_context = this;
		setContentView(R.layout.activity_wifi);
		//Initialize widget
		lookup = (Button) findViewById(R.id.lookup);
		join = (Button) findViewById(R.id.join);
		setup = (Button) findViewById(R.id.setup);
		upload = (Button) findViewById(R.id.upload);
		connect = (Button) findViewById(R.id.connect);
		changewifi = (Button) findViewById(R.id.changewifi);
		scan = (Button) findViewById(R.id.scan);
		check = (Button) findViewById(R.id.check);
		rtn_btn = (Button) findViewById(R.id.rtn_btn);
		status = (TextView) findViewById(R.id.status);
		oper_hint= (TextView) findViewById(R.id.oper_hint); //at 20160127
		ssidText = (TextView) this.findViewById(R.id.tv_ssid);
		pwdText = (EditText) this.findViewById(R.id.et_pwd);
		circleProgressBar = (ProgressBar) findViewById(R.id.circleProgressBar);
		cb_debug=(CheckBox) this.findViewById(R.id.cb_debug);
		//bind Listener to widget
		lookup.setOnClickListener(new MyListener());
		join.setOnClickListener(new MyListener());
		setup.setOnClickListener(new MyListener());
		scan.setOnClickListener(new MyListener());
		check.setOnClickListener(new MyListener());
		upload.setOnClickListener(new MyListener());
		connect.setOnClickListener(new MyListener());
		changewifi.setOnClickListener(new MyListener());
		rtn_btn.setOnClickListener(new MyListener());
		cb_debug.setOnClickListener(new MyListener()); //at 20160127
		//lock the button
		this.lookup.setEnabled(false);//at 20160127
		this.upload.setEnabled(false);
		this.join.setEnabled(false);
		this.setup.setEnabled(false);
		this.changewifi.setEnabled(false);

		circleProgressBar.setIndeterminate(false);
		mWifiAdmin = new WifiAdmin(s_context);
		if (mWifiAdmin.checkState() != 3) {
			Toast.makeText(getApplicationContext(), 
					"请打开wifi连接!",
					Toast.LENGTH_LONG).show();
			WifiActivity.this.finish();
		}
        Bundle bundle = this.getIntent().getExtras();
        this.username = bundle.getString("username");
        this.serverIP=bundle.getString("ip");
        this.serverPort=bundle.getString("port");
        if(username==null||username.length()==0) {
			Toast.makeText(getApplicationContext(), 
					"请在 系统参数 设置用户名!",
					Toast.LENGTH_LONG).show();
			WifiActivity.this.finish();
        }
        if(serverIP==null||serverIP.length()==0) {
			Toast.makeText(getApplicationContext(), 
					"请在 系统参数 设置服务器地址!",
					Toast.LENGTH_LONG).show();
        	WifiActivity.this.finish();
        }
        if(serverPort==null||serverPort.length()==0) {
			Toast.makeText(getApplicationContext(), 
					"请在 系统参数 设置服务器端口!",
					Toast.LENGTH_LONG).show();
        	WifiActivity.this.finish();
        }
		//Make sure Server in connected
		APName=new WifiAdmin(this).getSSID();
		circleProgressBar.setVisibility(View.VISIBLE);
        if(APName.length()>=3) {
    		ssidText.setText(APName.substring(1, APName.length()-1));
        } else {
    		ssidText.setText("");
        }
//at 20160127		pwdText.setText("clic778899");
		this.oper_hint.setText("操作提示：请连接互联网热点，正在尝试连接服务器");
        send2Server(DoJson.instance().toJSon("SHAKEHAND", username,"SERVER",null));
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
			WifiActivity.this.finish();
		} else if(order.equals("PROGRESS")) { 
			circleProgressBar.setVisibility(View.INVISIBLE);
			if(initStage==InitStage.JOIN) {
				this.initStage=InitStage.SETUP;
			}
			this.refreshBtn();
		} else if(order.equals("INIT")) { 
			if(initStage==InitStage.UPLOAD) {
				this.initStage=InitStage.JOIN;
			}
//at 20160127			
//			else if(initStage==InitStage.SETUP) {
//				this.initStage=InitStage.OPENSTA;
//			}
			else if(initStage==InitStage.INIT) {
				this.initStage=InitStage.LOOKUP;
			}
//end 20160127			
			this.refreshBtn();
			this.circleProgressBar.setVisibility(View.INVISIBLE);
		} else {
			this.showMsg("RCV[" + df.format(new Date()) + "]" + order + "-"
					+ response, -1);
		}

	}

	@Override
	protected void onPause() {
		CLog.i(TAG, "Enter onPause(WifiActivity)");	
		if (bcReceiver != null) {
			unregisterReceiver(bcReceiver);
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		CLog.i(TAG, "Enter onResume(WifiActivity)");
		regBroadcast();
		super.onResume();
	}

	public class MsgReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String msg = intent.getStringExtra("msg");
			showMsg(msg,-1);
			int progress = intent.getIntExtra("progress", 0);
			Log.i(TAG, "msg=" + msg);
			Log.i(TAG, "progress=" + progress);
			circleProgressBar.setProgress(progress);
			if (progress == 100) {
				circleProgressBar.setVisibility(View.GONE);
			}
		}

	};

	private void doLookup() {
		esp8266Name = mWifiAdmin.lookUpScan(Const.prefix_AP);
		if(esp8266Name == null ||esp8266Name.length()==0||pwdText.getText().toString().length()==0) {
			this.oper_hint.setText("操作提示：wifi名或wifi密码为空，请填写后，点'寻找模块'按钮");
			return ; 
		}
		if (esp8266Name != null) {
			moduleName = esp8266Name.split("_")[1];
			initStage = InitStage.UPLOAD;
//at 20160127			refreshBtn();
			showMsg("module founded"+moduleName,1);
		} else {
			showMsg("no module founded",1);
		}
		refreshBtn();
	}

	private void doUpload() {
		if(initStage == InitStage.UPLOAD) {
			if(moduleName==null) {
				showMsg("Please choose a module in List",-1);
			} else {
				keyStr=getRandomString(16);
				send2Server(DoJson.instance().toJSon("INIT", username,moduleName,keyStr));
			}
		} else {
			showMsg("status is not InitStage.UPLOAD",-1);
		}
		
	}

	private void doJoin() {
		if (initStage == InitStage.JOIN) {
			if (moduleName != null) {
				circleProgressBar.setVisibility(View.VISIBLE);
				circleProgressBar.setProgress(0);
				mWifiAdmin.connect(esp8266Name, "1234567890",
						WifiAdmin.WifiCipherType.WIFICIPHER_WPA);
				Log.i(TAG, "Join network:" + esp8266Name);
				initStage = InitStage.SETUP;
			} else {
				showMsg("Please choose a module in List", -1);
			}
		} else {
			showMsg("status is not InitStage.JOIN", -1);
		}
	}
	
	private void doSetup() {
		showMsg("ssid="
				+mWifiAdmin.getSSID()
				+"esp8266Name="
				+esp8266Name
				+"ipAddress="
				+mWifiAdmin.getIPAddress()
				, 1);
		if (initStage == InitStage.SETUP) {
			if (mWifiAdmin.getSSID().indexOf(esp8266Name)>=0
			  &&mWifiAdmin.getIPAddress().indexOf("192.168.4")>=0
					) {
				if (keyStr == null) {
					showMsg("Please upload first", 1);
				} else {

					if (mWifiAdmin.checkState() != 3) {
						showMsg("Please make sure connect module's AP", -1);
					} else {
						StringBuilder strBuilder = new StringBuilder(
								"<NAN>{6636 SET+E21=");
						strBuilder.append(keyStr);
						strBuilder.append(" SET+E41=");
						strBuilder
								.append(ssidText.getText().toString());
						strBuilder.append(" SET+E61=");
						strBuilder.append(pwdText.getText().toString());
						strBuilder.append(" SET+A=0");
						strBuilder.append("}</NAN>");
						showMsg("toModule:"+strBuilder.toString().trim(), -1);
						send2module(strBuilder.toString().trim());
						initStage = InitStage.CHANGEWIFI;
						refreshBtn();
					}
				}
			} else {
				showMsg("Please waiting connect module" , -1);
			}
		} else {
			showMsg("status is not InitStage.SETUP", -1);
		}
	}
 
	private void doChangewifi() {
		if (initStage == InitStage.CHANGEWIFI ) {
//			circleProgressBar.setVisibility(View.VISIBLE);
			if(mWifiAdmin.getSSID().indexOf(esp8266Name)>=0
					||mWifiAdmin.getSSID().length()==0) {
				mWifiAdmin.reconnect();
				this.oper_hint.setText("操作提示：WIFI恢复连接,设置完成，请静待模块变为绿灯，如果不成功，请返回后重新进入设置。");
			} else {
				this.oper_hint.setText("操作提示：设置完成，请静待模块变为绿灯，如果不成功，请返回后重新进入设置。");
			}
		} else {
			showMsg("status is not InitStage.CHANGEWIFI", -1);
		}
	}
	
	private class MyListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.lookup: // Seek module prefix with esp8266_
				doLookup();
				doUpload();
				break;
				
			case R.id.upload: // Send module's information to Server order:INIT
				doUpload();
				break;
				
			case R.id.join: // connect module's AP
				doJoin();
				break;
				
			case R.id.setup:// Deploy module send SET+E order
				doSetup();
//at 20160127				doChangewifi();
				break;

			case R.id.changewifi:// Reconnect WIFI last time
				doChangewifi();
				break;
//bottom button is for DEBUG 
			case R.id.connect:// Join the WIFI
				circleProgressBar.setVisibility(View.VISIBLE);
				circleProgressBar.setProgress(0);

				mWifiAdmin.connect("test1", "clic778899",
						WifiAdmin.WifiCipherType.WIFICIPHER_WPA);

				break;
			case R.id.scan:// Scan WIFI in the room
				showMsg("Scanned WIFI：\n" + mWifiAdmin.lookUpScan(),-1);
				break;

			case R.id.check:// Status of Wifi
				showMsg("ssid=" + mWifiAdmin.getSSID() 
						+ "wifi status="
						+ mWifiAdmin.checkState() 
						+ "IP="
						+ mWifiAdmin.getIPAddress()
						+ "initStage="
						+ initStage
						
						,-1);
				break;
			case R.id.rtn_btn:// return to main menu
				WifiActivity.this.finish();
				break;

			case R.id.cb_debug:
				if(!cb_debug.isChecked()) {
					status.setVisibility(View.GONE);
					connect.setVisibility(View.GONE);
					scan.setVisibility(View.GONE);
					check.setVisibility(View.GONE);
				} else {
					status.setVisibility(View.VISIBLE); 
					connect.setVisibility(View.VISIBLE);
					scan.setVisibility(View.VISIBLE);
					check.setVisibility(View.VISIBLE);
				}
				break;
			
			default:
				break;
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			if ((System.currentTimeMillis() - exitTime) > 2000) {
				Toast.makeText(getApplicationContext(), "再按一次返回键退出",
						Toast.LENGTH_SHORT).show();

				exitTime = System.currentTimeMillis();
			} else {
				finish();
				System.exit(0);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public static String getRandomString(int length) { //length表示生成字符串的长度
	    String base = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";   
	    Random random = new Random();   
	    StringBuffer sb = new StringBuffer();   
	    for (int i = 0; i < length; i++) {   
	        int number = random.nextInt(base.length());   
	        sb.append(base.charAt(number));   
	    }   
	    return sb.toString();   
	 }  

	private void showMsg(String str, int type) {
		if (type == 0) {
			Toast.makeText(WifiActivity.this, str, Toast.LENGTH_SHORT).show();
		} else if (type==1) {
			Toast.makeText(WifiActivity.this, str, Toast.LENGTH_LONG).show();
		}
		if(Const.DEBUG_FLAG) CLog.i(TAG, str);
		status.setText(str+"\n"+status.getText().toString());
	}
	
	private void send2module(final String sendStr) {
		Thread socketThread = new Thread() {
			@Override
			public void run() {

				super.run();
				SocketHelper sockethelper = new SocketHelper(Const.ESP8266_IP,Const.ESP8266_PORT);
				if (sockethelper.openSocket()) {
					sockethelper.sendMessage(sendStr);
				
					Const.broadCastToActivity("INIT", null, "SUCC", Const.whichActivity.WIFI);
					sockethelper.closeSocket();
				} else {
					showMsg("esp8266 socket open error",1);
				}
			}
		};
		socketThread.start();
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
								&& rtnStr.indexOf("SUCC") >= 0) {
							CLog.i(TAG, "Initialize to Server success");
							Const.broadCastToActivity("INIT", null, "SUCC",
									Const.whichActivity.WIFI);
						} else {
							CLog.i(TAG, "Initialize to Server fail");
							Const.broadCastToActivity("ERROR", null,
									"Server Connect fail",
									Const.whichActivity.WIFI);
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
	
	private void refreshBtn() {
		if(this.initStage==InitStage.LOOKUP) {
			this.lookup.setEnabled(true);
			this.oper_hint.setText("操作提示：请长按模块面板RESET键，直到变为蓝色，然后点击手机'寻找模块'按钮");
		} else if(this.initStage==InitStage.UPLOAD) {
			this.upload.setEnabled(true);
			this.lookup.setEnabled(false);
			this.oper_hint.setText("操作提示：请点击手机'上传Server'按钮，将基本信息上传服务器");
		} else if(this.initStage==InitStage.JOIN) {
			this.join.setEnabled(true);
			this.upload.setEnabled(false);
			this.oper_hint.setText("操作提示：请点击手机'连接模块'按钮，接入模块热点");
		} else if(this.initStage==InitStage.SETUP) {
			this.setup.setEnabled(true);
			this.join.setEnabled(false);
			this.oper_hint.setText("操作提示：请点击手机'设置模块'按钮，回写模块数据");
		} 
//at 20160127		
//		else if(this.initStage==InitStage.OPENSTA) {
//			this.openap.setEnabled(true);
//			this.setup.setEnabled(false);
//		} 
//end 20160127		
		else if(this.initStage==InitStage.CHANGEWIFI) {
			this.changewifi.setEnabled(true);
			this.setup.setEnabled(false); //at 20160127
			this.oper_hint.setText("操作提示：即将设置完成，请点击手机'更换WIFI'按钮。");
		}
//		status.setText(this.initStage.toString());
		showMsg(this.initStage.toString(),-1);
	}

}