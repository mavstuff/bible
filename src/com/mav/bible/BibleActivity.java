package com.mav.bible;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;


public class BibleActivity extends Activity implements OnClickListener {

	protected RelativeLayout activityBible;
	protected WebView webView;
	protected Button btnPrevVerse;
	protected Button btnChangeBook;
	protected Button btnChangeChapter;
	protected Button btnNextVerse;
	protected MenuItem mnuAbout;
	protected MenuItem mnuIncreaseTextSize;
	protected MenuItem mnuDecreaseTextSize;
	protected MenuItem mnuResetTextSize;
	protected MenuItem mnuSearch;
	protected MenuItem mnuStFathersComments;
	protected MenuItem mnuBlackBackground;
	protected MenuItem mnuOldTestamentFirst;
	
	private static int MIN_BOOK_ID = 1;
	private static int MAX_BOOK_ID = 77;
	
	private static int MIN_CHAPTER_ID = 1;
	
	protected int last_selected_book_id = 1;
	protected int last_selected_chapter_id = 1;
	protected boolean bSelectionMode = false;
	
	protected Toast mToast = null;
	
	public static int found_book_id = 0;
	public static int found_chapter_id = 0;
	public static int found_verse_id = 0;
	public static String found_verse_text = "";
	public static boolean found_result_ok = false;
	
	public static int nOffset;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_bible);
        
        activityBible = (RelativeLayout) findViewById(R.id.activityBible);
       
        webView = (WebView) findViewById(R.id.webViewMain);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setBackgroundColor(0x00000000);
        if (Build.VERSION.SDK_INT >= 11) webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        
        webView.setWebViewClient(new WebViewClient()
        {
        	@Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
            	String urltolower = url.toLowerCase();
            	if(urltolower.startsWith("http") || urltolower.startsWith("https") || urltolower.startsWith("file"))
                {
            		view.loadUrl(url);
                }
                else
                {
	                try
	                {
	                	Uri uri = Uri.parse(url);
	                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
	                    startActivity(intent);
	                }
	                catch (Exception e)
	                {
	                }
                }
                return true;
            }
            
            @Override
            public void onPageFinished(WebView view, String url)
            {
            	view.setBackgroundColor(0x00000000);
                if (Build.VERSION.SDK_INT >= 11) view.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
            }
        });
        
        btnPrevVerse = (Button) findViewById(R.id.btn_prev_verse);
        btnChangeBook = (Button) findViewById(R.id.btn_change_book);
        btnChangeChapter = (Button) findViewById(R.id.btn_change_chapter);
        btnNextVerse = (Button) findViewById(R.id.btn_next_verse);
        
        // Setup ClickListeners
        btnPrevVerse.setOnClickListener(this);
        btnChangeBook.setOnClickListener(this);
        btnChangeChapter.setOnClickListener(this);
        btnNextVerse.setOnClickListener(this);
        
        if (mToast == null)
        {
        	mToast = Toast.makeText(getApplicationContext(),"" , Toast.LENGTH_SHORT);
        }
        
		SharedPreferences sharedPref = getSharedPreferences("Bible", 0);
		last_selected_book_id = sharedPref.getInt("last_selected_book_id", 1);
		last_selected_chapter_id = sharedPref.getInt("last_selected_chapter_id", 1);

		
        //ReloadVerses();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	outState.putInt("last_selected_book_id", last_selected_book_id);
    	outState.putInt("last_selected_chapter_id", last_selected_chapter_id);
        outState.putIntArray("ScrollPosition", new int[]{ webView.getScrollX(), webView.getScrollY()});
        super.onSaveInstanceState(outState);
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        
        last_selected_book_id = savedInstanceState.getInt("last_selected_book_id");
        last_selected_chapter_id = savedInstanceState.getInt("last_selected_chapter_id");
        
        final int[] position = savedInstanceState.getIntArray("ScrollPosition");
        if(position != null) {
            webView.post(new Runnable() {
                public void run() {
                    webView.scrollTo(position[0], position[1]);
                }
            });
        }
        
    }
    
    @Override
	protected void onResume() {

    	ReloadVerses();
    	SetBackgroundColors(BibleApp.bBlackBackground);
   		
		super.onResume();
	}

    @Override
    protected void onStop(){
       super.onStop();
		
	     SharedPreferences sharedPref = getSharedPreferences("Bible", 0);
	     SharedPreferences.Editor editor = sharedPref.edit();

	     editor.putInt("last_selected_book_id", last_selected_book_id);
	     editor.putInt("last_selected_chapter_id", last_selected_chapter_id);
	     
	     editor.commit();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	protected void ReloadVerses(){
		
		int nSelectVerse = -1;
    	if (found_result_ok)
    	{
    		last_selected_book_id = found_book_id;
    		last_selected_chapter_id = found_chapter_id;
    		nSelectVerse = found_verse_id;
    	}
        
		//String strShortTitle = GetBookShortTitleById(last_selected_book_id) + " Глава "+last_selected_chapter_id;
        //setTitle(strShortTitle);
    	
    	String strVerses = GetVersesAndStFathersComments(last_selected_book_id, last_selected_chapter_id, nSelectVerse ); 
    	webView.loadDataWithBaseURL(null, strVerses,"text/html", "UTF-8", null);
    	webView.getSettings().setDefaultFontSize(BibleApp.nTextSize);
    	SetBackgroundColors(BibleApp.bBlackBackground);
   		
    	if (found_result_ok)
    		found_result_ok = false;
	}
	
	@Override
	public void onClick(View v) 
	{
		if (v == btnNextVerse)
		{
			if ((last_selected_chapter_id + 1) > GetMaxChapterIdForBookId(last_selected_book_id))
			{
				if ((last_selected_book_id+1) > MAX_BOOK_ID)
				{
					last_selected_book_id = MIN_BOOK_ID;
					last_selected_chapter_id = MIN_CHAPTER_ID;
				}
				else
				{
					last_selected_book_id++;
					last_selected_chapter_id = MIN_CHAPTER_ID;
				}
		
			}
			else
			{
				last_selected_chapter_id++;
			}
			
			ReloadVerses();
			webView.scrollTo(0, 0);
		}
		else if (v == btnPrevVerse)
		{
			
			if ((last_selected_chapter_id - 1) < MIN_CHAPTER_ID)
			{
				if ((last_selected_book_id - 1) < MIN_BOOK_ID)
				{
					last_selected_book_id = MAX_BOOK_ID;
					last_selected_chapter_id = GetMaxChapterIdForBookId(MAX_BOOK_ID);
				}
				else
				{
					last_selected_book_id--;
					last_selected_chapter_id = GetMaxChapterIdForBookId(last_selected_book_id);
				}
		
			}
			else
			{
				last_selected_chapter_id--;
			}
			
			ReloadVerses();
			webView.scrollTo(0, 0);
		
		}
		else if (v == btnChangeBook)
		{
		    Bundle bundle = new Bundle();
		    bundle.putInt("mode", 0);
		    bundle.putInt("bookid", last_selected_book_id);
			
			Intent openContents = new Intent("com.mav.bible.CONTENTS");
			openContents.putExtras(bundle);
			startActivityForResult(openContents, 1);
		
		}
		else if (v == btnChangeChapter)
		{
		    Bundle bundle = new Bundle();
		    bundle.putInt("mode", 1);
		    bundle.putInt("bookid", last_selected_book_id);
			
			Intent openContents = new Intent("com.mav.bible.CONTENTS");
			openContents.putExtras(bundle);
			startActivityForResult(openContents, 1);
		}

	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	mnuIncreaseTextSize = menu.add("Увеличить шрифт");
    	mnuDecreaseTextSize = menu.add("Уменьшить шрифт");
    	mnuResetTextSize = menu.add("Средний шрифт");
    	
    	mnuBlackBackground = menu.add("Черный фон");
    	mnuBlackBackground.setCheckable(true);
    	mnuBlackBackground.setChecked(BibleApp.bBlackBackground);

    	mnuSearch = menu.add("Поиск...");
   	
    	mnuStFathersComments = menu.add("Толкования");
    	mnuStFathersComments.setCheckable(true);
    	mnuStFathersComments.setChecked(BibleApp.bStFathersComments);

    	mnuOldTestamentFirst = menu.add("Сначала Ветхий Завет");
    	mnuOldTestamentFirst.setCheckable(true);
    	mnuOldTestamentFirst.setChecked(BibleApp.bOldTestamentFirst);

     	return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onPrepareOptionsMenu (Menu menu)
    {
    	mnuBlackBackground.setChecked(BibleApp.bBlackBackground);
    	mnuStFathersComments.setChecked(BibleApp.bStFathersComments);
    	mnuOldTestamentFirst.setChecked(BibleApp.bOldTestamentFirst);
    	
    	return super.onPrepareOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
    	super.onOptionsItemSelected(item);
    	
    	if (item == mnuIncreaseTextSize)
    	{
    		if (BibleApp.nTextSize < 54)
    		{
    			BibleApp.nTextSize += 2;
    		}
    		
    		webView.getSettings().setDefaultFontSize(BibleApp.nTextSize);
    		
			mToast.setText("Размер шрифта: " + BibleApp.nTextSize);
			mToast.show();
    		
    	}
    	else if (item == mnuDecreaseTextSize)
    	{
    		if (BibleApp.nTextSize > 8)
    		{
    			BibleApp.nTextSize -= 2;
    		}
    		
    		webView.getSettings().setDefaultFontSize(BibleApp.nTextSize);
    		
			mToast.setText("Размер шрифта: " + BibleApp.nTextSize);
			mToast.show();
    		
    	}
    	else if (item == mnuResetTextSize)
    	{
    		BibleApp.nTextSize = 22;
    		webView.getSettings().setDefaultFontSize(BibleApp.nTextSize);
			
			mToast.setText("Размер шрифта: " + BibleApp.nTextSize);
			mToast.show();
    	}
    	else if (item == mnuSearch)
    	{
    		onSearchRequested();
    	}
    	else if (item == mnuStFathersComments)
    	{
    		BibleApp.bStFathersComments = !item.isChecked();
    		item.setChecked(BibleApp.bStFathersComments);
    		ReloadVerses();
    	}
    	else if (item == mnuBlackBackground)
    	{
    		BibleApp.bBlackBackground = !item.isChecked();
    		item.setChecked(BibleApp.bBlackBackground);
    		SetBackgroundColors(BibleApp.bBlackBackground);
    	}
    	else if (item == mnuOldTestamentFirst)
    	{
    		BibleApp.bOldTestamentFirst = !item.isChecked();
    		item.setChecked(BibleApp.bOldTestamentFirst);
    	}
    	
    	return true;
    } 
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (data == null || resultCode != RESULT_OK) {return;}
		
		int nMode = data.getIntExtra("mode", 1);
		int nSelection = data.getIntExtra("selection", 1);
		
		if (nMode == 0)
		{
			last_selected_book_id = nSelection;
			last_selected_chapter_id = MIN_CHAPTER_ID;
		}
		else
		{
			last_selected_chapter_id = nSelection;
		}
		
		ReloadVerses();
		webView.scrollTo(0, 0);
	}
	
	public String GetVersesAndStFathersComments(int book_id, int chapter_id, int verse_to_sel) 
	{
		StringBuilder sb = new StringBuilder(20480);
		sb.append("<html><head><style type=\"text/css\">");
		
		if (BibleApp.bBlackBackground)
			sb.append("body {color: white; background-color: black;}");
		else
			sb.append("body {color: black; background-color: white;}");

		sb.append("</style></head><body>");
		
		String strShortTitle = "";
		
		try 
		{

			if (BibleApp.myDB != null)
			{
				String sQuery = "SELECT book_short_name, verse_id, verse_text, " + 
				"feofilakt_book_id, tolkov_feofilakt.glava_id AS feofilakt_glava_id, " +
				"efrem_sirin_book_id, tolkov_efrem_sirin.glava_id AS efrem_sirin_glava_id, " +
				"zlatoust_book_id, GROUP_CONCAT(tolkov_zlatoust.beseda_id) AS zlatoust_beseda_ids " +
				"FROM books, verses " +

				"LEFT JOIN tolkov_feofilakt ON tolkov_feofilakt.bible_book_short_name=books.book_short_name AND tolkov_feofilakt.bible_chapter=verses.chapter_id " +
				"AND tolkov_feofilakt.bible_verse_start=verses.verse_id " +

				"LEFT JOIN tolkov_efrem_sirin ON tolkov_efrem_sirin.bible_book_short_name=books.book_short_name AND tolkov_efrem_sirin.bible_chapter=verses.chapter_id " +
				"AND tolkov_efrem_sirin.bible_verse_start=verses.verse_id " +

				"LEFT JOIN tolkov_zlatoust ON tolkov_zlatoust.bible_book_short_name=books.book_short_name AND tolkov_zlatoust.bible_chapter=verses.chapter_id " +
				"AND tolkov_zlatoust.bible_verse_start=verses.verse_id " +

				"WHERE verses.book_id='" + book_id + "' AND books.book_id=verses.book_id AND chapter_id='" + chapter_id + "' GROUP BY verse_id ORDER BY verse_id ASC";
				
				Cursor c = BibleApp.myDB.rawQuery(sQuery,null);
				while (c.moveToNext()) {
					
					if (strShortTitle.isEmpty())
					{
						strShortTitle = c.getString(c.getColumnIndex("book_short_name")) + " Глава "+chapter_id;
						setTitle(strShortTitle);
					}
							
					if (verse_to_sel != -1 && verse_to_sel == c.getInt(c.getColumnIndex("verse_id")) )
					{
						
						sb.append("<script type=\"text/javascript\"> window.onload = function() { document.getElementById('searchresult').scrollIntoView(true); };</script>");
						sb.append("<a id=\"searchresult\"></a><b>");
						sb.append(c.getString(c.getColumnIndex("verse_id"))).append(" ");
						sb.append(c.getString(c.getColumnIndex("verse_text")));
						sb.append("</b>");
					}
					else
					{
						sb.append(c.getString(c.getColumnIndex("verse_id"))).append(" ");
						sb.append(c.getString(c.getColumnIndex("verse_text")));
					}
					
					if (BibleApp.bStFathersComments)
					{
						boolean bCommentsExist = false;
						
						if ( !c.isNull(c.getColumnIndex("zlatoust_book_id")) && 
							 !c.isNull(c.getColumnIndex("zlatoust_beseda_ids")))
						{
							int zlatoust_book_id = c.getInt(c.getColumnIndex("zlatoust_book_id"));
							String zlatoust_beseda_ids = c.getString(c.getColumnIndex("zlatoust_beseda_ids"));
							
							if (zlatoust_beseda_ids.contains(","))
							{
								String[] zlatoust_beseda_ids2 = zlatoust_beseda_ids.split(",");
								for (int i = 0; i < zlatoust_beseda_ids2.length; i++) 
								{
									int zlatoust_beseda_id = Integer.parseInt(zlatoust_beseda_ids2[i]);
									sb.append(" <a href=\"com.mav.bible://zlatoust/bs?"+zlatoust_book_id+"#"+zlatoust_beseda_id+"\">Иоанн Златоуст. Беседа "+zlatoust_beseda_id+"</a>");
									
									if (i != zlatoust_beseda_ids2.length-1 ) sb.append(", ");
								}
							}
							else
							{
								int zlatoust_beseda_id = c.getInt(c.getColumnIndex("zlatoust_beseda_ids"));
								sb.append(" <a href=\"com.mav.bible://zlatoust/bs?"+zlatoust_book_id+"#"+zlatoust_beseda_id+"\">Иоанн Златоуст. Беседа "+zlatoust_beseda_id+"</a>");
							}
							
							bCommentsExist = true;
						}
							
						if ( !c.isNull(c.getColumnIndex("feofilakt_book_id")) &&
							 !c.isNull(c.getColumnIndex("feofilakt_glava_id")))
						{
							int feofilakt_book_id = c.getInt(c.getColumnIndex("feofilakt_book_id"));
							int feofilakt_glava_id = c.getInt(c.getColumnIndex("feofilakt_glava_id"));
							
							if (bCommentsExist) sb.append(", ");
							
							sb.append(" <a href=\"com.mav.bible://feofilakt/gl?"+feofilakt_book_id+"#"+feofilakt_glava_id+"\">Феофилакт Болгарский. Глава "+feofilakt_glava_id+"</a>");
							
							bCommentsExist = true;
						}
							
						if ( !c.isNull(c.getColumnIndex("efrem_sirin_book_id")) && 
							 !c.isNull(c.getColumnIndex("efrem_sirin_glava_id")))
						{
							int efrem_sirin_book_id = c.getInt(c.getColumnIndex("efrem_sirin_book_id"));
							int efrem_sirin_glava_id = c.getInt(c.getColumnIndex("efrem_sirin_glava_id"));
							
							if (bCommentsExist) sb.append(", ");
								
							sb.append(" <a href=\"com.mav.bible://efrem-sirin/gl?"+efrem_sirin_book_id+"#"+efrem_sirin_glava_id+"\">Ефрем Сирин. Глава "+efrem_sirin_glava_id+"</a>");
							
							bCommentsExist = true;
						}
					}

					if (!c.isLast())
					{
						sb.append("<br/>"); 
					}
				}
			}
		} catch (SQLException sqlex) {
			sb.append(sqlex.getMessage());
		}
		
		sb.append("</body></html>");
		
		return sb.toString();
	}
	
	public int GetMaxChapterIdForBookId(int book_id) {
		int nMaxChapter = 1;

		try {

			if (BibleApp.myDB != null)
			{
				Cursor c = BibleApp.myDB.rawQuery("SELECT chapter_id FROM verses " +
							"WHERE book_id='" + book_id + "' ORDER BY chapter_id DESC LIMIT 1", null);
	
				if (c.moveToFirst()) {

					nMaxChapter = c.getInt(c.getColumnIndex("chapter_id"));
				}
			}
			
		} catch (SQLException sqlex) {
			
		}
		return nMaxChapter;
	}
	
	private void SetBackgroundColors(boolean bBlack)
	{
		if (bBlack)
		{
			activityBible.setBackgroundColor(Color.BLACK);
			webView.loadUrl("javascript:document.body.style.backgroundColor='black';document.body.style.color='white';");
		}
		else
		{
			activityBible.setBackgroundColor(Color.WHITE);
			webView.loadUrl("javascript:document.body.style.backgroundColor='white';document.body.style.color='black';");
		}
	}
}