package com.mav.bible;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;


public class BibleApp extends Application {

	private static BibleApp singleton;
	public static DataBaseHelper myDbHelper = null;
	public static SQLiteDatabase myDB = null;
	
	public static final int MYDB_UNINITIALIZED = 0;
	public static final int MYDB_ERROR = -1;
	public static final int MYDB_OK = 1;
	
	public static int nTextSize = 22;
	public static boolean bStFathersComments = true;
	public static boolean bBlackBackground = false;
	public static boolean bOldTestamentFirst = false;
	
	public static int mnMyDBState = MYDB_UNINITIALIZED;
	
	public static BibleApp getInstance() {
        return singleton;
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
    }

}
