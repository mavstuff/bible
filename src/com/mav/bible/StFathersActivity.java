package com.mav.bible;

import android.app.Activity;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class StFathersActivity extends Activity implements OnClickListener{
	
	protected RelativeLayout activityStFathers;
	protected WebView webView;
	
	protected Button btnPrevComment;
	protected Button btnNextComment;
	
	protected MenuItem mnuIncreaseTextSize;
	protected MenuItem mnuDecreaseTextSize;
	protected MenuItem mnuResetTextSize;
	protected MenuItem mnuBlackBackground;
	
	protected Toast mToast = null;
	
	protected int last_selected_stfather_book_id = 1;
	protected int last_selected_comment_id = 1;
	
	protected String sStFatherName = "Zlatoust"; 
	
	private int MIN_COMMENT_ID = 1;
	private int[] MAX_COMMENT_IDS = {90, 88, 55, 30, 44, 30, 6, 24, 15, 12, 34, 67, 60};
	
	private int MIN_STFATHER_BOOK_ID = 1;
	private int MAX_STFATHER_BOOK_ID = 13;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        setContentView(R.layout.activity_stfathers);
        
        activityStFathers = (RelativeLayout) findViewById(R.id.activityStFathers);
        
        webView = (WebView) findViewById(R.id.webViewMain);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setBackgroundColor(0x00000000);
        if (Build.VERSION.SDK_INT >= 11) webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        
        webView.setWebViewClient(new WebViewClient()
        {
            @Override
            public void onPageFinished(WebView view, String url)
            {
            	view.setBackgroundColor(0x00000000);
                if (Build.VERSION.SDK_INT >= 11) view.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
            }
        });
        
        btnPrevComment = (Button) findViewById(R.id.btn_prev_comment);
        btnNextComment = (Button) findViewById(R.id.btn_next_comment);
        
        if (mToast == null)
        {
        	mToast = Toast.makeText(getApplicationContext(),"" , Toast.LENGTH_SHORT);
        }
        
        // Setup ClickListeners
        btnPrevComment.setOnClickListener(this);
        btnNextComment.setOnClickListener(this);
        
        Uri uri = getIntent().getData();
        if (uri != null)
        {
        	String sHost = uri.getHost();

        	if (sHost.equals("zlatoust"))
        	{
            	sStFatherName = "Zlatoust";
            	MAX_STFATHER_BOOK_ID = 13;
            	MAX_COMMENT_IDS = new int[] {90, 88, 55, 30, 44, 30, 6, 24, 15, 12, 34, 67, 60};
            	setTitle("Иоанн Златоуст");
        	}
        	if (sHost.equals("feofilakt"))
        	{
        		sStFatherName = "Feofilakt";
        		MAX_STFATHER_BOOK_ID = 4;
        		MAX_COMMENT_IDS = new int[] {28, 16, 24, 21};
        		setTitle("Феофилакт Болгарский");
        	}
        	if (sHost.equals("efrem-sirin"))
        	{
        		sStFatherName = "EfremSirin";
        		MAX_STFATHER_BOOK_ID = 31;
        		MAX_COMMENT_IDS = new int[] {51, 40, 26, 33, 34, 66, 51, 47, 12, 14,
        									 3, 9, 1, 1, 7, 14, 4, 23, 16, 16,
        									 13, 6, 6, 4, 4, 5, 3, 6, 4, 3,
        									 13};
        		setTitle("Ефрем Сирин");
        	}
        	
        	last_selected_stfather_book_id = Integer.valueOf(uri.getQuery());
        	last_selected_comment_id = Integer.valueOf(uri.getFragment());
        	ReloadComment();
        }
	}
	

	protected void ReloadComment(){
		
		String strBeseda = "";
		
		if (sStFatherName.equals("Zlatoust"))
			strBeseda = GetZlatoustComment(last_selected_stfather_book_id, last_selected_comment_id);
		else if (sStFatherName.equals("Feofilakt"))
			strBeseda = GetFeofilaktComment(last_selected_stfather_book_id, last_selected_comment_id);
		else if (sStFatherName.equals("EfremSirin"))
			strBeseda = GetEfremSirinComment(last_selected_stfather_book_id, last_selected_comment_id);
    
    	webView.loadDataWithBaseURL(null, strBeseda, "text/html", "UTF-8", null);
    	webView.getSettings().setDefaultFontSize(BibleApp.nTextSize);
    	SetBackgroundColors();
	}
	
	@Override
	public void onClick(View v) {
		if (v == btnNextComment)
		{
			if ((last_selected_comment_id + 1) > MAX_COMMENT_IDS[last_selected_stfather_book_id-1])
			{
				if ((last_selected_stfather_book_id+1) > MAX_STFATHER_BOOK_ID)
				{
					last_selected_stfather_book_id = MIN_STFATHER_BOOK_ID;
					last_selected_comment_id = MIN_COMMENT_ID;
				}
				else
				{
					last_selected_stfather_book_id++;
					last_selected_comment_id = MIN_COMMENT_ID;
				}
			}
			else
			{
				last_selected_comment_id++;
			}
			
			ReloadComment();
			webView.scrollTo(0, 0);
		}
		else if (v == btnPrevComment)
		{
			if ((last_selected_comment_id - 1) < MIN_COMMENT_ID)
			{
				if ((last_selected_stfather_book_id - 1) < MIN_STFATHER_BOOK_ID)
				{
					last_selected_stfather_book_id = MAX_STFATHER_BOOK_ID;
					last_selected_comment_id = MAX_COMMENT_IDS[MAX_STFATHER_BOOK_ID-1];
				}
				else
				{
					last_selected_stfather_book_id--;
					last_selected_comment_id = MAX_COMMENT_IDS[last_selected_stfather_book_id-1];
				}
			}
			else
			{
				last_selected_comment_id--;
			}
			
			ReloadComment();
			webView.scrollTo(0, 0);
		}
	}
	
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putIntArray("StFathersCommentsScrollPosition", new int[]{ webView.getScrollX(), webView.getScrollY()});
        super.onSaveInstanceState(outState);
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        
        final int[] position = savedInstanceState.getIntArray("StFathersCommentsScrollPosition");
        if(position != null) {
            webView.post(new Runnable() {
                public void run() {
                    webView.scrollTo(position[0], position[1]);
                }
            });
        }
    }
	
	public String GetZlatoustComment(int zlatoust_book_id, int beseda_id) 
	{
		StringBuilder sb = new StringBuilder(10240);
		String beseda_intro;
	
		sb.append(GetCommentHtmlHeader());
		
		try {

			if (BibleApp.myDB != null)
			{
				String sQuery = "SELECT beseda_intro,beseda_text FROM tolkov_zlatoust " +
							"WHERE zlatoust_book_id='" + zlatoust_book_id + "' AND beseda_id='" + beseda_id + "'";
						
				Cursor c = BibleApp.myDB.rawQuery(sQuery,null);
				if (c.moveToFirst()) {
					
					sb.append("<b>БЕСЕДА "+beseda_id+"</b><br/>");
					
					beseda_intro = c.getString(c.getColumnIndex("beseda_intro"));
					
					if (!TextUtils.isEmpty(beseda_intro))
						sb.append("<i>" + beseda_intro + "</i><br/><br/>");
					
					sb.append(c.getString(c.getColumnIndex("beseda_text")));
				}
				c.close();
			}
			
		} catch (SQLException sqlex) {
			sb.append(sqlex.getMessage());
		}
		
		sb.append("</body></html>");
		
		return sb.toString();
	}
	
	public String GetFeofilaktComment(int feofilakt_book_id, int glava_id) 
	{
		StringBuilder sb = new StringBuilder(10240);
		
		sb.append(GetCommentHtmlHeader());

		try {

			if (BibleApp.myDB != null)
			{
				String sQuery = "SELECT glava_text FROM tolkov_feofilakt " +
							"WHERE feofilakt_book_id='" + feofilakt_book_id + "' AND glava_id='" + glava_id + "'";
						
				Cursor c = BibleApp.myDB.rawQuery(sQuery,null);
				if (c.moveToFirst()) {
					
					sb.append("<b>ГЛАВА "+glava_id+"</b><br/>");
					sb.append(c.getString(c.getColumnIndex("glava_text")));
				}
				c.close();
			}
			
		} catch (SQLException sqlex) {
			sb.append(sqlex.getMessage());
		}
		
		sb.append("</body></html>");
		
		return sb.toString();
	}
	
	public String GetEfremSirinComment(int efrem_sirin_book_id, int glava_id) 
	{
		StringBuilder sb = new StringBuilder(10240);
		
		sb.append(GetCommentHtmlHeader());

		try {

			if (BibleApp.myDB != null)
			{
				String sQuery = "SELECT glava_text FROM tolkov_efrem_sirin " +
							"WHERE efrem_sirin_book_id='" + efrem_sirin_book_id + "' AND glava_id='" + glava_id + "'";
						
				Cursor c = BibleApp.myDB.rawQuery(sQuery,null);
				if (c.moveToFirst()) {
					
					sb.append("<b>ГЛАВА "+glava_id+"</b><br/>");
					sb.append(c.getString(c.getColumnIndex("glava_text")));
				}
				c.close();
			}
			
		} catch (SQLException sqlex) {
			sb.append(sqlex.getMessage());
		}
		
		sb.append("</body></html>");
		
		return sb.toString();
	}
	
	protected String GetCommentHtmlHeader()
	{
		StringBuilder sb = new StringBuilder(500);
		sb.append("<html><head><style type=\"text/css\">");
		
		if (BibleApp.bBlackBackground)
			sb.append("body {color: white; background-color: black;}");
		else
			sb.append("body {color: black; background-color: white;}");

		sb.append("</style></head><body>");
		
		return sb.toString();
	}
	
	private void SetBackgroundColors()
	{
		if (BibleApp.bBlackBackground)
		{
			activityStFathers.setBackgroundColor(Color.BLACK);
			webView.loadUrl("javascript:document.body.style.backgroundColor='black';document.body.style.color='white';");
		}
		else
		{
			activityStFathers.setBackgroundColor(Color.WHITE);
			webView.loadUrl("javascript:document.body.style.backgroundColor='white';document.body.style.color='black';");
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

     	return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onPrepareOptionsMenu (Menu menu)
    {
    	mnuBlackBackground.setChecked(BibleApp.bBlackBackground);
   	
    	return super.onPrepareOptionsMenu(menu);
    }
	
	@Override
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
    	else if (item == mnuBlackBackground)
    	{
    		BibleApp.bBlackBackground = !item.isChecked();
    		item.setChecked(BibleApp.bBlackBackground);
    		SetBackgroundColors();
    	}
    	
    	return true;
    } 
}
