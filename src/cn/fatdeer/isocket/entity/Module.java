package cn.fatdeer.isocket.entity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import cn.fatdeer.isocket.CLog;
import cn.fatdeer.isocket.R;

/**
 * @author GD.lws
 * @version 20150929
 * @Discribe This Entity class is for the display of the ListView
 *            
 *           at 20151124
 *            1. new Parameter mLocked for control module;
 *           at 20151126
 *            1. new Parameter mIsonline for module is or not online;
 *            2. The condition of setImage() is moved here; 
 *            3. new Parameter mNickName;
 *           at 20151127
 *            1. use HashMap hold the status of the modules;
 *           at 20151202
 *            1. getStatus(): display Local language ;
 *           at 20151214
 *            1. new parameter mInOrder ; 
 *            2. remove function setWaiting(), merge into setImage();
 *           at 20160204
 *            1. mStatus's value type from String to Double; 
 * 
 */
public class Module {
	private static String TAG = Module.class.getSimpleName();
	private String mName;
	private String mNickName; //at 20151126
	private int mImage;
//at 20160204	HashMap<String , String> mStatus= new HashMap<String , String>();
	HashMap<String , Double> mStatus= new HashMap<String , Double>();  
	private int mID; 
	private boolean mLocked; //If F>0 mLocked is true;
	private boolean mIsonline; //USERLIST ON then mIsonline is true
	private boolean mInOrder; //at 20151214 // module in OrderControl then mInOrder is true
	public int[] resImags = { 
			R.drawable.lockedon,R.drawable.unlockon,
			R.drawable.lockedoff,R.drawable.unlockoff,
			R.drawable.hourclass // inOrderControl
	};
	ArrayList<SysParameter> sSets;
	private Date modTimes;

	public Module(String name,String nickName,int id,ArrayList<SysParameter> sets) {
		this.mName = name;
		this.mNickName=nickName;
//at 20151214		this.mImage=resImags[4];
		this.mImage=resImags[2];
		this.mID=id;
		this.mLocked=true;
		this.sSets=sets;
		this.modTimes=new Date();
	}

	public int getID() {
		return mID;
	}

	public void setID(int id) {
		this.mID = id;
	}

	public Date getModTimes() {
		return modTimes;
	}

	public void setModTimes() {
		this.modTimes = new Date();
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this. mName=name;
	}
	public String getNickName() {
		return mNickName;
	}

	public int getImage() {
		return mImage;
	}

	public void setImage() {
//at 20151214
		if(this.mInOrder) {
			this.mImage = resImags[4];
		} else {
//end 20151214			
			if (this.mIsonline) {
				if (this.mLocked) {
					this.mImage = resImags[0];
				} else {
					this.mImage = resImags[1];
				}
			} else {
				if (!this.mIsonline) {
					this.mImage = resImags[2];
				} else {
					this.mImage = resImags[3];
				}
			}
		}
	}
	
//	public void setWaiting() {
//		this.mImage = resImags[4];
//	}

	public String getStatus() {
		StringBuilder status=new StringBuilder();
//at 20151214		Iterator it = mStatus.keySet().iterator();
		Iterator<String> it = mStatus.keySet().iterator();
		//at 20151202
		List<StatusDisplay> sdSets =new ArrayList<StatusDisplay>();
		DateFormat df = new SimpleDateFormat("[HH:mm:ss]",Locale.CHINA);
		//end 
        while(it.hasNext()) {
            String key = (String)it.next();
    		CLog.i(TAG, "key:" + key);
    		CLog.i(TAG, "value:" + mStatus.get(key));
            if(key.equals("TK")||key.equals("ST")) continue;
            StatusDisplay sd = new StatusDisplay();
            for(SysParameter sysParameter:sSets) {
            	if(key.equals(sysParameter.getIndex_code())) {
            		sd.setName(sysParameter.getIndex_name());
            		sd.setIndex(sysParameter.getChart_type()*100+sysParameter.getSer_no());
//at 20160204            		sd.setValue(mStatus.get(key));
            		sd.setValue(""+mStatus.get(key));
            		sdSets.add(sd);
            		break;
            	}
            }
        }
        Collections.sort(sdSets);
        int lastLine=0;
        status.append(df.format(this.modTimes));
		for (StatusDisplay sd2 : sdSets) {
    		CLog.i(TAG, this.mNickName+"!"+lastLine+"!"+sd2.getIndex() + "!" + sd2.getName() + "!" + sd2.getValue());
    		if(lastLine!=sd2.getIndex()/100) {
    			status.append("\n");
    		}
			status.append(sd2.getName() + "=" + sd2.getValue() + " ");
			lastLine=sd2.getIndex()/100;
		}
		return status.toString();
	}
//at 20160204
//	public String getValue(String s) {
//		String rtn=mStatus.get(s);
//
//		if(toStatus.get(s)==null) return -999;
//		else return toStatus.get(s);
//		
//		return rtn;
//	}
//	
//	public void setStatus(String s, String value) {
//		if (mStatus.get(s) != null) {
//			mStatus.remove(s);
//		}
//		mStatus.put(s, value);
//	}

	public double getValue(String s) {
		if(mStatus.get(s)==null) return -999;
		else return mStatus.get(s);
	}
	
	public void setStatus(String s, double value) {
		if (mStatus.get(s) != null) {
			mStatus.remove(s);
		}
		mStatus.put(s, value);
	}
//end 20160204	
//at 20151214
	public void removeStatus(String s) {
		if (mStatus.get(s) != null) {
			mStatus.remove(s);
		}
	}	
//end 20151214	
	public boolean isLocked() {
		return mLocked;
	}

	public void setLocked(boolean mLocked) {
		this.mLocked = mLocked;
		this.setImage();//at 20151214
	}

	public boolean isOnline() {
		return mIsonline;
	}

	public void setOnOffline(boolean isOnline) {
		this.mIsonline = isOnline;
		this.setImage();//at 20151214
	}

	public boolean isInOrder() {
		return mInOrder;
	}

	public void setInOrder(boolean mInOrder) {
		this.mInOrder = mInOrder;
		this.setImage();//at 20151214
	}

	@Override
	public String toString() {
		return "Module [mName=" + mName + ", mNickName=" + mNickName
				+ ", mImage=" + mImage + ", mStatus=" + mStatus + ", mID="
				+ mID + ", mLocked=" + mLocked + ", mIsonline=" + mIsonline
				+ ", resImags=" + Arrays.toString(resImags) + "]";
	}
  
}
