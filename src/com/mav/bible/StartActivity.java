package com.mav.bible;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


public class StartActivity extends Activity implements OnClickListener{

	protected Button btnBible;
	protected Button btnStFathers;
	protected MenuItem mnuAbout;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        
        btnBible = (Button) findViewById(R.id.btn_bible);
        btnStFathers = (Button) findViewById(R.id.btn_stfathers);
        
        // Setup ClickListeners
        btnBible.setOnClickListener(this);
        btnStFathers.setOnClickListener(this);
                
		SharedPreferences sharedPref = getSharedPreferences("Bible", 0);
		BibleApp.nTextSize = sharedPref.getInt("TextSize", 22);
		BibleApp.bStFathersComments = sharedPref.getBoolean("StFathersComments", true);
		BibleApp.bBlackBackground = sharedPref.getBoolean("BlackBackground", false);
		BibleApp.bOldTestamentFirst = sharedPref.getBoolean("OldTestamentFirst", false);

	}
	
	@Override
	public void onClick(View v) {
		if (v == btnBible)
		{
			Intent openBible = new Intent("com.mav.bible.BIBLE");
			startActivity(openBible);
		}
		else if (v == btnStFathers)
		{
			Intent openStFathersContent = new Intent("com.mav.bible.CONTENTS_STFATHERS");
			startActivity(openStFathersContent);
			
		}
		
	}
	
    @Override
    protected void onStop(){
       super.onStop();
		
	     SharedPreferences sharedPref = getSharedPreferences("Bible", 0);
	     SharedPreferences.Editor editor = sharedPref.edit();
	     
	     int current_version = 0;
 		 
	     try {
	    	 
	    	 PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
	    	 current_version = packageInfo.versionCode;
	    	 
	     } catch (NameNotFoundException e) {
	     
	     }

	     editor.putInt("TextSize", BibleApp.nTextSize);
	     editor.putBoolean("StFathersComments", BibleApp.bStFathersComments);
	     editor.putBoolean("BlackBackground", BibleApp.bBlackBackground);
	     editor.putBoolean("OldTestamentFirst", BibleApp.bOldTestamentFirst);
	     
	     editor.putInt("last_version", current_version);
	     
	     editor.commit();
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	
		mnuAbout = menu.add("О приложении...");
		menu.add("Выход");
     	return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	super.onOptionsItemSelected(item);
    	
    	if (item == mnuAbout)
    	{
    		AlertDialog.Builder ad = new AlertDialog.Builder(StartActivity.this);
    		ad.setMessage("Приложение \"Библия. Синодальный перевод.\"\n(c) 2013-2014 Артем Мороз\nТекст перевода с сайта pravoslavie.ru\nТексты толкований с сайта azbyka.ru\nИнформацию о приложении читайте на biblehitech.ru");
    		ad.setNeutralButton("OK", new DialogInterface.OnClickListener() {
	    		public void onClick(DialogInterface dialog, int which) {

	    		}
    		});
    		ad.show();

    	}
    	else
    	{
    		finish();
    	}
    	
    	return true;
    } 

}
