package cn.fatdeer.isocket;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cn.fatdeer.isocket.apmode.WifiActivity;
import cn.fatdeer.isocket.chart.HistoryChartActivity;
import cn.fatdeer.isocket.chart.SQLiteActivity;
import cn.fatdeer.isocket.entity.Login;
import cn.fatdeer.isocket.pub.Const;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;


public class MenuActivity extends Activity implements OnItemClickListener, Callback {

	private static String tag = MenuActivity.class.getSimpleName();
	public static Context s_context;
	private Login login=null;
	private BroadcastReceiver bcReceiver=null;
	DateFormat df = new SimpleDateFormat("hh:mm:ss.SSS",Locale.CHINA);
	private long exitTime = 0;

	private String APPKEY="8bc9e79a3ee0";
	private String APPSECRET="a7b35ae9154eba9bd8af64affbace08c";
	private boolean ready;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		s_context = this;
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.acivity_menu);

		ArrayList<ContentItem> objects = new ArrayList<ContentItem>();
		// 0
		objects.add(new ContentItem("实时控制",
				"实时获取每一个龟箱传来的数据，并可以解锁并手工控制"));
		objects.add(new ContentItem("历史图表",
				"检查每一个龟箱的历史数值指标"));
		objects.add(new ContentItem("新增龟箱",
				"初始化龟箱各项参数并绑定到这个账号下管理"));
		objects.add(new ContentItem("系统参数",
				"注册新用户，设置服务器地址、用户名、密码并配置其他系统参数。"));
		objects.add(new ContentItem("维护本地数据库", "同步本地SQLite和服务器数据库"));

		MyAdapter adapter = new MyAdapter(this, objects);

		ListView lv = (ListView) findViewById(R.id.listView1);
		lv.setAdapter(adapter);

		lv.setOnItemClickListener(this);
		loadSharePrefrence();
		initSMSSDK();
	}
	@Override
	protected void onDestroy() {
		if (ready) {
			// 销毁回调监听接口
			SMSSDK.unregisterAllEventHandler();
		}
		super.onDestroy();
	}
	@Override
	protected void onPause() {
		if (bcReceiver != null) {
			unregisterReceiver(bcReceiver);
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		regBroadcast();
		super.onResume();

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

	@Override
	public void onItemClick(AdapterView<?> av, View v, int pos, long arg3) {

        Intent i;
	    Bundle bundle=new Bundle();
    	bundle.putString("sid", "7");  // tmpmod should be modified
    	bundle.putString("username", login.getName());
    	bundle.putString("ip", login.getHostIP());

        switch (pos) {
            case 0:
                i = new Intent(this, MainActivity.class);
            	bundle.putString("port", login.getTCPPort());
                startActivity(i);
                break;
            case 1:
                i = new Intent(this, HistoryChartActivity.class);
            	bundle.putString("port", login.getUDPPort());
        	    i.putExtras(bundle);
				
                startActivity(i);
                break;
            case 2:
                i = new Intent(this, WifiActivity.class);
            	bundle.putString("port", login.getUDPPort());
        	    i.putExtras(bundle);
        	    
                startActivity(i);
                break;
            case 3:
        		new LoginDialog(MenuActivity.this,login,Const.whichActivity.MENU).showLoginDialog();
                break;
            case 4:
                i = new Intent(this, SQLiteActivity.class);
                startActivity(i);
                break;                
        }

        overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
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

	private void showMsg(String str, int type) {
		if (type == 0) {
			Toast.makeText(MenuActivity.this, str, Toast.LENGTH_SHORT).show();
		} else if (type==1) {
			Toast.makeText(MenuActivity.this, str, Toast.LENGTH_LONG).show();
		}
		if(Const.DEBUG_FLAG) CLog.i(tag, str);
	}
	
	private void processOrder(String order, String response, String request) {

		if (request != null) {
			// Display The message of Send
			this.showMsg("SND[" + df.format(new Date()) + "]" + order + "-"
					+ request, -1);

			if (order.equals("SUCC")) {
				if (request.indexOf("DIALOG") == 0) { //From LoginDialog
					login.refresh(request);
					this.showMsg(login.toString(), 1);
					setSharePrefrence();
				} else if (request.indexOf("R_DIALOG") == 0) { // From RegisterDialog
					new LoginDialog(MenuActivity.this, login,Const.whichActivity.MENU).showLoginDialog();
				}
			} else if (order.equals("REGISTER") && request.equals("DIALOG")) {
//at 20151122				new RegisterDialog(MenuActivity.this,Const.whichActivity.MENU).showRegisterDialog();
				new RegisterDialog(MenuActivity.this,Const.whichActivity.MENU,login.getHostIP(),login.getUDPPort()).showRegisterDialog();
			} else if (order.equals("CLOSE")) {
				if (request.equals("DIALOG")) {
					
				} else if (request.equals("R_DIALOG")) {
					new LoginDialog(MenuActivity.this, login,Const.whichActivity.MENU).showLoginDialog();
				}
			}
//at 20160121
			else if(order.equals("FAIL")) {
				this.showMsg("Login Fail"+request, 1);
			}
//end 20160121			
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
		tcpport=p.getString("TCTPORT", tcpport);
		udpport=p.getString("UDTPORT", udpport);
		name=p.getString("NAME", name);
		password=p.getString("PASSWORD", password);
		if(connType==null) connType="TCP";
		if(ip==null) ip="58.64.167.178";
		if(tcpport==null) tcpport="8013";
		if(udpport==null) udpport="8001";
		if(name==null) name="note3";
		if(password==null) password="crdj,cjwr";
	
		this.login=new Login(connType,ip,tcpport,udpport,name,password);
	}

	private void setSharePrefrence() {
		SharedPreferences p = getSharedPreferences("ISOCKET", Context.MODE_PRIVATE);
		Editor edit = p.edit();
		edit.putString("CONNTYPE", login.getConnType());
		edit.putString("HOSTIP", login.getHostIP());
		edit.putString("TCPPORT", login.getTCPPort());
		edit.putString("UDPPORT", login.getUDPPort());
		edit.putString("NAME", login.getName());
		edit.putString("PASSWORD", login.getPassword());
		edit.commit();
	}
	
    private class ContentItem {
        String name;
        String desc;

        public ContentItem(String n, String d) {
            name = n;
            desc = d;
        }
    }
    
    private class MyAdapter extends ArrayAdapter<ContentItem> {

        public MyAdapter(Context context, List<ContentItem> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ContentItem c = getItem(position);

            ViewHolder holder = null;

            if (convertView == null) {

                holder = new ViewHolder();

                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, null);
                holder.tvName = (TextView) convertView.findViewById(R.id.tvName);
                holder.tvDesc = (TextView) convertView.findViewById(R.id.tvDesc);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.tvName.setText(c.name);
            holder.tvDesc.setText(c.desc);

            return convertView;
        }

        private class ViewHolder {

            TextView tvName, tvDesc;
        }
    }

	private void initSMSSDK() {
		// 初始化短信SDK
		SMSSDK.initSDK(this, APPKEY, APPSECRET);
		final Handler handler = new Handler(this);
		EventHandler eventHandler = new EventHandler() {
			public void afterEvent(int event, int result, Object data) {
			
				Message msg = new Message();
				msg.arg1 = event;
				msg.arg2 = result;
				msg.obj = data;
				handler.sendMessage(msg);
				
			}
		};
		// 注册回调监听接口
		SMSSDK.registerEventHandler(eventHandler);
		ready = true;

	}
	
	@Override
	public boolean handleMessage(Message msg) {
		int event = msg.arg1;
		int result = msg.arg2;
		Object data = msg.obj;
		// 短信注册成功后，返回MainActivity,然后提示新好友
		if (result == SMSSDK.RESULT_COMPLETE) {
			if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
				// 提交验证码成功
				Toast.makeText(this, "SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE",
						Toast.LENGTH_LONG).show();
				System.out.println("Send OK!");
			} else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
				// 获取验证码成功
				Toast.makeText(this, "SMSSDK.EVENT_GET_VERIFICATION_CODE",
						Toast.LENGTH_LONG).show();
				System.out.println("Got OK!");
			} else {
				Toast.makeText(this, "Other OK!", Toast.LENGTH_LONG).show();
				System.out.println("Other OK!");

			}
		} else {
			((Throwable) data).printStackTrace();
		}

		return false;
	}
}
