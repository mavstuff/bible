package com.mav.bible;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment;
import android.os.StatFs;

public class DataBaseHelper //extends SQLiteOpenHelper
{

    //The Android's default system path of your application database.
    private static String DB_PATH = "/data/data/com.mav.bible/databases/";
    private static String DB_EXT_SUBPATH = "/Android/data/com.mav.bible/files/";
    private static String DB_NAME = "bible.db";
    private static long DB_SIZE = 50*1048576; 
    private SQLiteDatabase myDataBase; 
    private final Context myContext;
    public long nSpaceRequired = DB_SIZE;
    
	public DataBaseHelper(Context context) {
		//super(context, DB_NAME, null, 1);
		this.myContext = context;
	}


	  /**
     * Creates a empty database on the system and rewrites it with your own database.
     * */
    public void createDataBase() throws IOException{
 
		boolean dbExist = checkDataBase();

		if (dbExist) {
			// do nothing - database already exist
		} else {

			// By calling this method and empty database will be created into
			// the default system path
			// of your application so we are gonna be able to overwrite that
			// database with our database.
			//this.getReadableDatabase();

			try {
				copyDataBase();
				
			} catch (IOException e) {

				deleteMyDatabase();
				throw e;
			}
		}
    }
    
    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase(){
 
    	SQLiteDatabase checkDB = null;
 
    	try{
        	
    		PackageInfo packageInfo = myContext.getPackageManager().getPackageInfo(myContext.getPackageName(), 0);
        	int current_version = packageInfo.versionCode;
    		
        	SharedPreferences sharedPref = myContext.getSharedPreferences("Bible", 0);
    		int last_version = sharedPref.getInt("last_version", 0);
    		
    		if (last_version == current_version)
    		{
    			String myPath;
    			
    			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) 
    			{
    				myPath = Environment.getExternalStorageDirectory() + DB_EXT_SUBPATH + DB_NAME; 
    				checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
    			}
    			else
    			{
		    		myPath = DB_PATH + DB_NAME;
		    		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
    			}
    		}
 
    	}catch(SQLiteException e){
    		//database does't exist yet.
    	} catch (NameNotFoundException e) {

    	}
 
    	if(checkDB != null){
    		checkDB.close();
    	}
 
    	return (checkDB != null) ? true : false;
    }
 
    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    private void copyDataBase() throws IOException
    {
    	//Open your local db as the input stream
    	InputStream myInput = myContext.getAssets().open(DB_NAME);
    	nSpaceRequired = myInput.available();
    	if (nSpaceRequired == 0) nSpaceRequired = DB_SIZE; 
    	
    	String sDBPath, sDBRootStoragePath;
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) 
		{
			sDBPath = Environment.getExternalStorageDirectory() + DB_EXT_SUBPATH;
			sDBRootStoragePath = Environment.getExternalStorageDirectory().toString();
		}
		else
		{
    		sDBPath = DB_PATH;
    		sDBRootStoragePath = "/data/data/com.mav.bible/";
		}
    	
    	StatFs stat = new StatFs(sDBRootStoragePath);
    	long bytesAvailable = (long)stat.getBlockSize() *(long)stat.getBlockCount();
    	
    	if (bytesAvailable <= nSpaceRequired)
    	{
    		throw new IOException();
    	}
    	
    	File sDBPathFile = new File(sDBPath);
    	sDBPathFile.mkdirs();
    	
 
    	// Path to the just created empty db
    	String outFileName = sDBPath + DB_NAME;
 
    	//Open the empty db as the output stream
    	OutputStream myOutput = new FileOutputStream(outFileName);
 
    	//transfer bytes from the inputfile to the outputfile
    	byte[] buffer = new byte[20480];
    	int length;
    	
    	while ( (length = myInput.read(buffer)) > 0  )
    	{
    		myOutput.write(buffer, 0, length);
    		nSpaceRequired -= length;
    	}
 
    	//Close the streams
    	myOutput.flush();
    	myOutput.close();
    	myInput.close();
    }
 
    public void openDataBase() throws SQLException
    {
    	String sDBPath;
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) 
		{
			sDBPath = Environment.getExternalStorageDirectory() + DB_EXT_SUBPATH; 
		}
		else
		{
			sDBPath = DB_PATH;
		}
    	myDataBase = SQLiteDatabase.openDatabase(sDBPath + DB_NAME, null, SQLiteDatabase.OPEN_READWRITE);
    }
    
    public SQLiteDatabase getWritableDatabase() {
        
    	if (myDataBase == null)
        	openDataBase();
        
        return myDataBase;
     }
 
	public void close() 
    {
 		if (myDataBase != null)
			myDataBase.close();

 	}
    
    private void deleteMyDatabase() {
    	
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) 
		{
			new File(Environment.getExternalStorageDirectory() + DB_EXT_SUBPATH + DB_NAME).delete();
		}
		
		new File(DB_PATH + DB_NAME).delete();
    }

}


