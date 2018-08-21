package com.czt.mp3recorder.sample.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import com.czt.mp3recorder.sample.R;
/**
 * Created by Ming.Xiao on 2018/8/21.
 */

public class BaseActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().hide();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setDarkStatusIcon(true);
    }

    public void setDarkStatusIcon(boolean bDark) {
        View decorView = getWindow().getDecorView();
        if(decorView != null){
            int vis = decorView.getSystemUiVisibility();
            if(bDark){
                vis |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            else {
                vis &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            decorView.setSystemUiVisibility(vis);
        }
        getWindow().setStatusBarColor(getResources().getColor(R.color.notification_bg_color));
    }
}
