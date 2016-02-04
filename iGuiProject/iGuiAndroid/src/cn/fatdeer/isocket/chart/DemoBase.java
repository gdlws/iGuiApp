
package cn.fatdeer.isocket.chart;

import android.support.v4.app.FragmentActivity;
import cn.fatdeer.isocket.R;


/**
 * Baseclass of all Activities of the Demo Application.
 * 
 * @author Philipp Jahoda
 */
public abstract class DemoBase extends FragmentActivity {

    protected String[] mMonths = new String[] {
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dec"
    };
//at 20151116
    protected String[] mTimes = new String[] {
            "9:00", "9:05", "9:10", "9:15", "9:20", "9:25"
            , "9:30", "9:35", "9:40", "9:45", "9:50", "9:55"
    };
//end 20151116    
    protected String[] mParties = new String[] {
            "Party A", "Party B", "Party C", "Party D", "Party E", "Party F", "Party G", "Party H",
            "Party I", "Party J", "Party K", "Party L", "Party M", "Party N", "Party O", "Party P",
            "Party Q", "Party R", "Party S", "Party T", "Party U", "Party V", "Party W", "Party X",
            "Party Y", "Party Z"
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_right_out_activity);
    }
}
