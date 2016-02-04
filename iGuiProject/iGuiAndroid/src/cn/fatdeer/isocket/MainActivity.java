package cn.fatdeer.isocket;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cn.fatdeer.isocket.apmode.WifiActivity;
import cn.fatdeer.isocket.chart.iGuiDBHelper;
import cn.fatdeer.isocket.entity.Login;
import cn.fatdeer.isocket.entity.Module;
import cn.fatdeer.isocket.entity.MsgEntity;
import cn.fatdeer.isocket.entity.SysParameter;
import cn.fatdeer.isocket.network.ISockHeartThread;
import cn.fatdeer.isocket.network.ISockInputThread;
import cn.fatdeer.isocket.network.ISockOutputThread;
import cn.fatdeer.isocket.network.NetManager;
import cn.fatdeer.isocket.network.TCPHeartThread;
import cn.fatdeer.isocket.network.TCPInputThread;
import cn.fatdeer.isocket.network.TCPOutputThread;
import cn.fatdeer.isocket.pub.Const;
import cn.fatdeer.isocket.pub.DoJson;

/**
 * @author GD.lws
 * @version 20150608
 * @Discribe This Activity Start 3 Thread by SocketThreadManager, User can
 *           choose module List to control by order
 *            
 *           at 20150603
 *            1. showLoginDialog() for ready of SMS LOGIN;
 *           at 20150604
 *            1. new Parameter HOSTPORT; 
 *           at 20150605
 *            1. remove all UDPClient & TCPClient; 
 *           at 20150608
 *            1. LoginDialog peel off to independent class;
 *            2. moduleStr will hold true moduleName, remove after -'s character;
 *            3. LAN UDP P2P is OK now ; 
 *           at 20150610
 *            1. fix the bug of closeMsg(), can not close in server USERLIST;
 *           at 20150706
 *            1. All SET order add {}; 
 *           at 20150707
 *            1. Lock SCREEN_ORIENTATION_LANDSCAPE; 
 *            2. remove UDPHoleThread; 
 *           at 20150709
 *            1. ModuleList remove null String; 
 *            2. add listen to ListView; 
 *            3. orderStr rename to sendOrder; 
 *            4. remove spinner for choose module;
 *            5. new function updListView() for update module's status; 
 *            6. display status in ListView; 
 *            7. remove textTemperature; 
 *           at 20150710
 *            1. remove send_txt/ textModuleStatus; 
 *            2. process ERROR&WARN response ; 
 *           at 20150711
 *            1. addMessage() merge into showMsg();
 *            2. new entity Login for hold Login information; 
 *           at 20150722
 *            1. new parameter image_tortoise ;
 *           at 20150925
 *            1. remove UDP ; 
 *           at 20150929
 *            1. First use APP, auto fill the Login information; 
 *           at 20151029
 *            1. new module's values H2/A2/S3/S4/S5;
 *           at 20151121
 *            1. fix the bug of SNAPSHOOT updListView; 
 *            2. display T1/T2 for temperature; 
 *           at 20151124
 *            1. Get module's status from USERLIST after successful Login;
 *            2. LongClick item to send {SET+F=1};
 *            3. remove many button; 
 *           at 20151125
 *            1. LongClick item to send {SET+F=1} one time;
 *           at 20151126
 *            1. updListView() work correctly
 *            2. The condition of setImage() is moved here; 
 *            3. new Parameter mNickName;
 *           at 20151127
 *            1. build up module's status in hashmap; 
 *            2. receive message from DeployDialog;
 *           at 20151130
 *            1. USERLIST package delim - change to ! 
 *           at 20151202
 *            1. new getSysParameter() for get module's index name and display order;
 *           at 20151203
 *            1. SET+F=<minutes> change to SET+F=<seconds>;
 *           at 20151211
 *            1. unlock picture after receive SEND!SUCC:ORDER=SET+F=60
 *           at 20151214
 *            1. fix bug: display of hourclass;  
 *           at 20160107
 *            1. remove clickedPosition, unlock icon according module's status;
 * 
 */
public class MainActivity extends Activity implements OnClickListener {
	private static String tag = MainActivity.class.getSimpleName();

	public static Context s_context;

	private BroadcastReceiver bcReceiver=null;
	private TextView msg_txt;
	private String sendStr=null;
//	private String moduleStr;
//	private ToggleButton mTB_unlock; //at 20151124
	private CheckBox cb_debug;
	private ImageView image_tortoise;//at 20150722
	private long exitTime = 0;
//	private static final String[] orderStrs = { "GET+S", 
//		"SET+S1=1","SET+S1=0", "SET+S2=1", "SET+S2=0" };
	private List<String> moduleList;
	private MsgEntity msgEntity = null;

	private ISockInputThread mSockInputThread = null;
	private ISockOutputThread mSockOutputThread = null;
	private ISockHeartThread mSockHeartThread = null;
//at 20151202	DateFormat df = new SimpleDateFormat("hh:mm:ss.SSS",Locale.CHINA);
	DateFormat df = new SimpleDateFormat("HH:mm:ss.SSS",Locale.CHINA);

	private Login login=null;

	//For ListView
	private List<Module> modules;
	private ListView lvModules;
	private BaseAdapter moduleAdapt;
//    private ProgressDialog progressDialog;
	//at 20151124
    int lastPosition=-1;
//	int[] resImags = { R.drawable.lockedon,R.drawable.unlockon,
//			R.drawable.lockedoff,R.drawable.unlockoff,
//			R.drawable.hourclass //at 20151126
//	};
    //end 20151124
    String userlistDelim = "!"; //at 20151130
//at 20160107    int clickedPosition=-1; //at 20151211
    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //at 20150707
		//SCREEN_ORIENTATION_LANDSCAPE
		setContentView(R.layout.activity_main);
		msg_txt = (TextView) this.findViewById(R.id.res_txt);
		cb_debug=(CheckBox) this.findViewById(R.id.cb_debug);
//		mTB_unlock =(ToggleButton) findViewById(R.id.tb_unlock); //at 20151124
		image_tortoise=(ImageView) findViewById(R.id.image_tortoise);//at 20150722
		cb_debug.setOnClickListener(this);

		moduleList = new ArrayList<String>();
		s_context = this;
		msg_txt.setText("接收的数据");
		NetManager.instance().init(this);
	
		if (NetManager.instance().isNetworkConnected()) {
			msg_txt.setText("网络可用。" + msg_txt.getText());
//			progressDialog = ProgressDialog.show(MainActivity.this, "Loading...", "Please wait...", true, true); //at 20151120

		} else {
			msg_txt.setText("网络不可用。" + msg_txt.getText());
			this.showMsg("网络不可用。",1);
		}		
 
		loadSharePrefrence();

	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.cb_debug:
			if(!this.cb_debug.isChecked()) {
				this.msg_txt.setVisibility(View.GONE);
				this.image_tortoise.setVisibility(View.VISIBLE);
			} else {
				this.msg_txt.setVisibility(View.VISIBLE);
				this.image_tortoise.setVisibility(View.GONE);
			}
			break;
		}
	}

	private void initModule(String name) {
		Intent intent = new Intent();
		intent.setClass(MainActivity.this, WifiActivity.class);
	    Bundle bundle=new Bundle();
    	bundle.putString("username", name);
	    intent.putExtras(bundle);
		this.startActivity(intent);
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
				this.exitToSystem();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onPause() {
		CLog.i(tag, "Enter onPause(MainActivity)");
		if (bcReceiver != null) {
			unregisterReceiver(bcReceiver);
		}
		closeNetWork();
		super.onPause();
		exitToSystem();//at 20151127
	}

	@Override
	protected void onResume() {
		CLog.i(tag, "Enter onResume(MainActivity)");
		regBroadcast();
		new LoginDialog(s_context,login,Const.whichActivity.MAIN).showLoginDialog();
		
//tmpmod		new AimSettingDialog(s_context,login.getName(), null, //tmpmod modules.get(position),
//				Const.whichActivity.MAIN).showDialog();
		super.onResume();

	}

	private void regBroadcast() {
		bcReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				final String response = intent.getStringExtra("response");
				final String request = intent.getStringExtra("request");
				final String order = intent.getStringExtra("order");
				processOrder(order, response, request);
			}
		};
		IntentFilter intentToReceiveFilter = new IntentFilter();
		intentToReceiveFilter.addAction(Const.BC);
		registerReceiver(bcReceiver, intentToReceiveFilter);
	}


	private void initNetWork() {
		if (login.getConnType().equals("TCP")) {
			if (mSockInputThread == null) {
				mSockInputThread = new TCPInputThread(login);
			} else {
				this.showMsg("INIT[" + df.format(new Date()) + "]"
						+ "mSockInputThread had begun",-1);
			}
			if (mSockOutputThread == null) {
				mSockOutputThread = new TCPOutputThread(login);
			}
		}
	}

	private void initListView() {
		modules = new ArrayList<Module>();
		modules.clear();

		for (int i = 0; i < moduleList.size(); i++) {


			//build-up modules
			CLog.i(tag,"moduleList.get(i).split(-)[0]="+moduleList.get(i).split(userlistDelim)[0]);
			CLog.i(tag,"moduleList.get(i).split(-)[2]="+moduleList.get(i).split(userlistDelim)[2]);
			CLog.i(tag,"moduleList.get(i).split(-)[3]="+moduleList.get(i).split(userlistDelim)[3]);
			Module module = new Module(//at 20151126chosenImage, 
					moduleList.get(i).split(userlistDelim)[0],
					moduleList.get(i).split(userlistDelim)[2],
					Integer.parseInt(moduleList.get(i).split(userlistDelim)[3]) ,
					getSysParameter() //at 20151201
					);
			if(moduleList.get(i).split(userlistDelim)[1].equals("ON")) {
				module.setOnOffline(true);
			} else {
				module.setOnOffline(false);
			}
			if(moduleList.get(i).split(userlistDelim)[4].indexOf("&F=0&")>=0	) {
				module.setLocked(true);
			} else {
				module.setLocked(false);
			}
			module.setImage();

			CLog.i(tag,"module.getName()="+module.getName());
			if(module.getName()!=null)
			modules.add(module);
			CLog.i(tag,"modules="+modules.toString());

			String module_status = moduleList.get(i).split(userlistDelim)[4];//module's status
			this.showMsg(" status="+module_status, -1);
			if(module_status!=null)	updListView(module_status); 
		}
		lvModules = (ListView) findViewById(R.id.lvModules);
		moduleAdapt = new ModuleAdapter();
		lvModules.setAdapter(moduleAdapt);
		setListener();
	}
	
	private void updListView(String status) {
		String thisModule=null;
		boolean inOrderControl=false;//at 20151211
	    
		StringTokenizer st = new StringTokenizer(status, "[]&");
		thisModule = st.nextElement().toString(); // module_name
		CLog.i(tag, "thisModule=" + thisModule);
		if (thisModule.equals("SNAPSHOOT")) {
			thisModule = st.nextElement().toString();
			CLog.i(tag, "thisModule(SNAPSHOOT)=" + thisModule);
		}
		for (Module module : modules) {
			CLog.i(tag, "query:" + module.getName());
			if (module.getName().equals(thisModule)) {
				while (st.countTokens() > 0) {
					String status_snap = st.nextElement().toString();
					String name = status_snap.split("=")[0];
					String value = status_snap.split("=")[1];
					if (!value.equals("NAN")) {
						if (name.equals("S1") || name.equals("S2")
						 || name.equals("S3") || name.equals("S4")
						 || name.equals("S5") || name.equals("S6")
						 || name.equals("HA") || name.equals("TA")) {
							String wantedValue = module.getValue("W" + name);
							if (wantedValue != null
									&& !value.equals(wantedValue)) {
								CLog.i(tag, "module in orderControl:name="
										+ name + ":" + wantedValue + "<>"
										+ value);
								inOrderControl = true;
							} else {
								module.removeStatus("W" + name);
							}
						}
						module.setStatus(name, value);
					}
				}
				if (inOrderControl) {
// at 20151214 module.setWaiting();
					module.setInOrder(true);
				} else {
					module.setInOrder(false); // at 20151214
					if (status.indexOf("&F=0&") >= 0) {
						module.setLocked(true);
					} else {
						module.setLocked(false);
					}
				}
				module.setImage();
				module.setModTimes();// at 20151202
				CLog.i(tag, "module=" + module.toString());
				if (moduleAdapt != null)
					moduleAdapt.notifyDataSetChanged();
				break; //at 20151214
			}
		}

	}

	private void setListener() {
		// 短按事件监听
		lvModules.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				//at 20151124
				lastPosition=position;
				if(position==0) {
					image_tortoise.setImageResource(R.drawable.sulcata);
				} else if(position==1) {
					image_tortoise.setImageResource(R.drawable.elegans);
				} else if(position==2) {
					image_tortoise.setImageResource(R.drawable.radiata);
				} else if(position==3) {
					image_tortoise.setImageResource(R.drawable.genoa);
				}

				if(modules.get(position).isInOrder()) {
					showMsg("让子弹飞一会儿", 0);
				}
				else if(modules.get(position).isLocked()) {
					showMsg("请先长按解锁", 0);
				} 
				else {
					//open dialog for deployment
					new DeployDialog(s_context,login.getName(),modules.get(position),
							Const.whichActivity.MAIN).showLoginDialog();
				}

			
			}
		});

		// 长按事件监听
		lvModules.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
//				Toast.makeText(MainActivity.this,
//						modules.get(position).getName() + ":被长按 ",
//						Toast.LENGTH_SHORT).show();
				String str = DoJson.instance().toJSon(
						"SEND", login.getName(), 
						moduleList.get(position).split(userlistDelim)[0],
//at 20151203						"{SET+F=1}");
						"SET+F=60");

				try {
					msgEntity = new MsgEntity(str.getBytes("ISO-8859-1"));
					msgEntity.setComplete();//at 20151125
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(mSockOutputThread!=null)
					mSockOutputThread.addMsgToSendList(msgEntity);
//at 20151214				modules.get(position).setWaiting();
				modules.get(position).setInOrder(true);
				if(moduleAdapt!=null) moduleAdapt.notifyDataSetChanged();
//at 20160107				clickedPosition=position; //at 20151211
				return true;// 1、如果返回false，长按后，他也会触发短按事件2、如果返回true的话，长按后就不会触发短按事件
			}
		});
	}

	private void showMsg(String str, int type) {
		if (type == 0) {
			Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
		} else if (type==1) {
			Toast.makeText(MainActivity.this, str, Toast.LENGTH_LONG).show();
		}
		msg_txt.setText( str + "\n"+ msg_txt.getText());
		if(Const.DEBUG_FLAG) CLog.i(tag, str);
	}

	private void processOrder(String order, String response, String request) {

		//Request for dialog normally
		if (request != null) {
			// Display The message of Send
			this.showMsg("SND[" + df.format(new Date()) + "]" + order + userlistDelim //at 20151130 "-"
					+ request, -1);

			if (order.equals("SUCC")) {
				if (request.indexOf("DIALOG") == 0) { //From LoginDialog
					login.refresh(request);
					this.showMsg(login.toString(), 1);
					setSharePrefrence();
					initNetWork();
//					initUI();
				} else if (request.indexOf("R_DIALOG") == 0) { // From RegisterDialog
					new LoginDialog(s_context, login,Const.whichActivity.MAIN).showLoginDialog();
				} else if(request.indexOf("DEPLOYDIALOG")==0) {
					try {
						String orderStr=request.split(userlistDelim)[1];
						this.showMsg(orderStr, -1);
						msgEntity = new MsgEntity(orderStr.getBytes("ISO-8859-1"));
						msgEntity.setComplete();
						if (mSockOutputThread != null) {
							mSockOutputThread.addMsgToSendList(msgEntity);
						}
						if(moduleAdapt!=null) moduleAdapt.notifyDataSetChanged();//at 20151211
//at 20151203					} catch (UnsupportedEncodingException e) {
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						this.showMsg("Send order error:"+order+userlistDelim+request, -1);
					}
				}
			} else if (order.equals("REGISTER") && request.equals("DIALOG")) {
				//Open WifiActivity for deployment of module
				this.initModule("REG");
			} else if (order.equals("NEW") && request.equals("DIALOG")) {
				if (login.getName() != null)
					this.initModule(login.getName());
				else
					this.initModule("REG");
			}	
			else if (order.equals("CLOSE")) {
				if (request.equals("DIALOG")) {
					this.exitToSystem();
				} else if (request.equals("R_DIALOG")) {
					new LoginDialog(s_context, login,Const.whichActivity.MAIN).showLoginDialog();
				}
			}
			//at 20160202
			else if (order.equals("EXPERT") && request.equals("DIALOG")) {
				new AimSettingDialog(s_context, login.getName(), null, // tmpmod
																		// modules.get(position),
						Const.whichActivity.MAIN).showDialog();
			}
			//end 20160202
		}

		if (response != null) {
			// Display The Message of Receive
			this.showMsg("RCV[" + df.format(new Date()) + "]" +order + userlistDelim + response,-1);
			if(order.equals("WARN")) {
//				initUI();
			} else if(order.equals("ERROR")) {
				this.closeNetWork();
				new LoginDialog(s_context,login,Const.whichActivity.MAIN).showLoginDialog();
			}
			if (order.equals("SEND")) {
//at 20160107				
				//at 20151211
//				if(response.indexOf("SUCC:ORDER=SET+F")==0) {
//					modules.get(clickedPosition).setLocked(false);
////at 20151214					modules.get(clickedPosition).setImage();
//					modules.get(clickedPosition).setInOrder(false);
//					modules.get(clickedPosition).setModTimes();//at 20151202
//					CLog.i(tag, "module=" + modules.get(clickedPosition).toString());
//					if (moduleAdapt != null)
//						moduleAdapt.notifyDataSetChanged();
//					clickedPosition=-1;
//				} else 
				//end 20151211
//end 20160107				
				this.updListView(response);
			}
			if (order.equals("USERLIST")) {
				moduleList.clear();
				String[] sArray = response.split("@");
				CLog.i(tag, "build up moduleList");
				for (int i = 0; i < sArray.length; i++) {
					if(sArray[i].length()>0) { //at 20150709
						CLog.i(tag, "USERLIST"+i+":" + sArray[i]);
						moduleList.add(sArray[i]);
					}
				}
				if(mSockHeartThread==null) {
						mSockHeartThread = new TCPHeartThread(login);
				}
				initListView();
//				initUI();
			}

			if(sendStr!=null) {
				String compareStr="ORDER={" + sendStr+"}";
				CLog.i(tag, "response="+response+";len="+response.length());
				CLog.i(tag, "compareStr="+compareStr+";len="+compareStr.length());
				CLog.i(tag, "response.indexOf(compareStr)="+response.indexOf(compareStr));
				if (msgEntity != null
					&& response.indexOf(compareStr) >= 0) {
					msgEntity.setComplete();
					sendStr = null;
				}
			}

		}
	}
	
	private void loadSharePrefrence() {
		SharedPreferences p = getSharedPreferences("ISOCKET", Context.MODE_PRIVATE);
		String connType=null;
		String ip = null;
		String tcpport = null;
		String udpport = null;
		String name = null;
		String password = null;
		connType=p.getString("CONNTYPE", connType);
		ip=p.getString("HOSTIP", ip);
		tcpport=p.getString("TCPPORT", tcpport);
		udpport=p.getString("UDPPORT", tcpport);
		name=p.getString("NAME", name);
		password=p.getString("PASSWORD", password);
		if(connType==null) connType="TCP";
		if(ip==null) ip="58.64.167.178";
		if(tcpport==null) tcpport="8013";
		if(udpport==null) udpport="8001";
		if(name==null) name="note3";
		if(password==null) password="crdj,cjwr";
	
		this.login=new Login(connType,ip,tcpport,udpport,name,password);
		this.showMsg("Load LOGIN:"+login.toString(), -1); //at 20150927 1);
	}

	private void setSharePrefrence() {
		SharedPreferences p = getSharedPreferences("ISOCKET", Context.MODE_PRIVATE);
		Editor edit = p.edit();
		this.showMsg("SAVE LOGIN:"+login.toString(), -1); //at 20150927 1);
		edit.putString("CONNTYPE", login.getConnType());
		edit.putString("HOSTIP", login.getHostIP());
		edit.putString("HOSTPORT", login.getTCPPort());
		edit.putString("NAME", login.getName());
		edit.putString("PASSWORD", login.getPassword());
		edit.commit();
	}
	
	private void exitToSystem() {
		closeMsg();
		finish();
		System.exit(0);
	}

	private void closeMsg() {
		try {
			String closeStr = DoJson.instance().toJSon("CLOSE", login.getName(),
					"SERVER", "@OB@"+login.getName()+"@"+login.getPassword()+"@127.0.0.1@1024");
			msgEntity = new MsgEntity(closeStr.getBytes("ISO-8859-1"));
			msgEntity.setComplete();
			if (mSockOutputThread != null) {
				mSockOutputThread.addMsgToSendList(msgEntity);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			this.exitToSystem();
		}
	}

	private void closeNetWork() {
		this.closeMsg();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
			this.exitToSystem();
		}

		if (mSockHeartThread != null) {
			mSockHeartThread.closeConnect();
		}
		if (mSockInputThread != null) {
			mSockInputThread.setStart(false);
			mSockInputThread = null;
		}
		if (mSockOutputThread != null) {
			mSockOutputThread.setStart(false);
			mSockOutputThread = null;
		}
		if (mSockHeartThread != null) {
			mSockHeartThread.setStart(false);
			mSockHeartThread = null;
		}
	}

	private class ModuleAdapter extends BaseAdapter {
		// 得到listView中item的总数
		@Override
		public int getCount() {
			return modules.size();
		}
		@Override
		public Module getItem(int position) {			
			return modules.get(position);
		}
		@Override
		public long getItemId(int position) {			
			return position;
		}
		
		// 简单来说就是拿到单行的一个布局，然后根据不同的数值，填充主要的listView的每一个item
		@SuppressLint("ViewHolder")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {			
			View layout = View.inflate(MainActivity.this,
					R.layout.activity_item_modules_, null);
			ImageView ivThumb = (ImageView) layout.findViewById(R.id.ivThumb);
			TextView tvName = (TextView) layout.findViewById(R.id.tvName);
			TextView tvStatus = (TextView) layout.findViewById(R.id.tvStatus);
			
			Module module = modules.get(position);
			ivThumb.setImageResource(module.getImage());
//at 20151126			tvName.setText(module.getName());
			tvName.setText(module.getNickName());
			tvStatus.setText(module.getStatus());

			return layout;
		}
	}

	//at 20151130
		private ArrayList<SysParameter> getSysParameter() {
			ArrayList<SysParameter> sSets = new ArrayList<SysParameter>();

			iGuiDBHelper dbHelper = new iGuiDBHelper(
					MainActivity.this, "igui.db", null, 1);
			// 得到一个可写的数据库
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			Cursor cursor = 
					db.query("sys_parameter", new String[] { "index_code", "index_name",
					"chart_type", "ser_no"},
					null, null, null, null, null);

			Log.i(tag, "begin");
			String result = "result=";
			while (cursor.moveToNext()) {
				String iCode = cursor.getString(cursor.getColumnIndex("index_code"));
				String iName = cursor.getString(cursor.getColumnIndex("index_name"));
				int cType = cursor.getInt(cursor.getColumnIndex("chart_type"));
				int sNo   = cursor.getInt(cursor.getColumnIndex("ser_no"));
				result += " iCode：" + iCode +" iName:"+iName + " cType：" + cType + " sNo：" + sNo
						+ "\n";
				Log.i(tag, result);
				SysParameter sysParameter = new SysParameter();
				sysParameter.setIndex_code(iCode);
				sysParameter.setIndex_name(iName);
				sysParameter.setChart_type(cType);
				sysParameter.setSer_no(sNo);
				sSets.add(sysParameter);
			}
			cursor.close();
			db.close();

			return sSets;
		}
	//end 20151130

}
