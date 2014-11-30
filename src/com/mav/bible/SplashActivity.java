package com.mav.bible;

import java.io.IOException;
import java.lang.ref.WeakReference;

import com.mav.bible.DataBaseHelper;
import com.mav.bible.BibleApp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
//import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class SplashActivity extends Activity implements OnClickListener {

	protected ImageView splashView;
	protected ProgressBar progressBar;
	protected Thread timer;
	protected Thread dbinit;
	protected StHandler handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash);
        
        splashView = (ImageView) findViewById(R.id.splash_view);
        progressBar = (ProgressBar) findViewById(R.id.splash_progress);
        progressBar.setVisibility(View.VISIBLE);
        
        handler = new StHandler(this);
        splashView.setOnClickListener(this);
		BibleApp.mnMyDBState = BibleApp.MYDB_UNINITIALIZED;
	}
    
	@Override
	protected void onResume() {
		super.onResume();
		//Log.w("BibleApp", "onResume begin");
		
		BibleApp.mnMyDBState = BibleApp.MYDB_UNINITIALIZED;
    
		timer = new Thread(){
			public void run()
			{
				try{
					int sleeptime = 1900;
					
					do {
						sleep(sleeptime);
						sleeptime = 100;
					
					} while (BibleApp.mnMyDBState == BibleApp.MYDB_UNINITIALIZED);
				}
				catch (InterruptedException e){
					//e.printStackTrace();
				}
				finally {
					
					if (BibleApp.mnMyDBState == BibleApp.MYDB_OK)
					{
						Intent openStartingPoint = new Intent("com.mav.bible.STARTINGPOINT");
						startActivity(openStartingPoint);
						overridePendingTransition(0,0);
					}
				}
			
			}
		
		};
		
		dbinit = new Thread(){
			public void run()
			{
				InitDatabase();
				handler.sendEmptyMessage(0);
			}
		};

		dbinit.start();
		timer.start();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (BibleApp.mnMyDBState == BibleApp.MYDB_OK)
		{
			finish();
		}
	}

	@Override
    protected void onDestroy() {
		if (handler != null)
			handler.removeCallbacksAndMessages(null);
		
        super.onDestroy();
	}
    

	public void onClick(View v) {
		if (v == splashView && BibleApp.mnMyDBState != BibleApp.MYDB_UNINITIALIZED){
			timer.interrupt();
		}
	}
	
	public void InitDatabase() {
		try {

			BibleApp.myDbHelper = new DataBaseHelper(this);
			BibleApp.myDbHelper.createDataBase();
			BibleApp.myDbHelper.openDataBase();
			BibleApp.myDB = BibleApp.myDbHelper.getWritableDatabase();
			
			BibleApp.mnMyDBState = BibleApp.MYDB_OK;
		
		} catch (SQLException sqlex) {
			BibleApp.mnMyDBState = BibleApp.MYDB_ERROR;
		} catch (IOException ioex) {
			BibleApp.mnMyDBState = BibleApp.MYDB_ERROR;
		}
		finally {
		
			if (BibleApp.mnMyDBState == BibleApp.MYDB_ERROR)
			{
				runOnUiThread(new Runnable() {
	                public void run() {
	                	
	        			AlertDialog.Builder ad = new AlertDialog.Builder(SplashActivity.this);
	        			ad.setMessage("При установке произошла ошибка. Попробуйте освободить "+(BibleApp.myDbHelper.nSpaceRequired / 1048576)+
	        						"МБ внутренней памяти.");
	        			
	        			ad.setNeutralButton("OK", new DialogInterface.OnClickListener() {
				    		public void onClick(DialogInterface dialog, int which) {
								
				    			finish();
				    		}
	        			});
	        			ad.setCancelable(false);
	        			ad.show();
	                }
	            });
				
				
			}
		}
	}
	
    static class StHandler extends Handler 
    {
        WeakReference<SplashActivity> wrActivity;

        public StHandler(SplashActivity activity) {
                wrActivity = new WeakReference<SplashActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
        	super.handleMessage(msg);
            SplashActivity activity = wrActivity.get();
            if (activity != null)
            {
                activity.progressBar.setVisibility(View.GONE);
            }
        }
    }

}
