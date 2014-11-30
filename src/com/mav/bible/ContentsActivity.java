package com.mav.bible;

import android.app.ListActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class ContentsActivity extends ListActivity{

	protected String[] contents = null;
	protected int[] content_ids = null;
	
	protected int nMode = 0; // 0 -- list to select a book, 1 -- list to select a chapter
	protected int nBookId = 1; // for chapter selection mode
	
	protected ListView mLv;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		nMode = this.getIntent().getExtras().getInt("mode");
		nBookId = this.getIntent().getExtras().getInt("bookid");
		ReloadContents();
		
		if (nMode == 0)
		{
			setTitle("Выберите книгу");
		}
		else
		{
			setTitle(Html.fromHtml(GetBookTitleById(nBookId)).toString());
		}
	
		mLv = getListView();
		
		SharedPreferences sharedPref = getSharedPreferences("Bible", 0);
		int nPos = sharedPref.getInt("ContentsScrollPosition", 0);
		mLv.setSelectionFromTop(nPos, 0);
	}
	
    @Override
    protected void onStop(){
       super.onStop();
		
	     SharedPreferences sharedPref= getSharedPreferences("Bible", 0);
	     SharedPreferences.Editor editor = sharedPref.edit();
	     
	     editor.putInt("ContentsScrollPosition", mLv.getFirstVisiblePosition());
	     editor.commit();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		int nSelection = content_ids[(int) id];
	    
		Intent intent = new Intent();
		intent.putExtra("mode", nMode);
		intent.putExtra("selection", nSelection);
	    setResult(RESULT_OK, intent);
	    finish();
	}
	
	protected void ReloadContents(){
		
		GetContents();
		
		if (contents != null)
		{
			setListAdapter(new ArrayAdapter<String>(ContentsActivity.this, android.R.layout.simple_list_item_1, contents) {
			    @Override
			    public View getView(int position, View convertView, ViewGroup parent) {
			        TextView textView = (TextView) super.getView(position, convertView, parent);
					textView.setTextColor(BibleApp.bBlackBackground ? Color.WHITE : Color.BLACK);
			        return textView;
			    }	
				
			});
		}
		SetBackgroundColors();
	}
	
	public void GetContents() {
		try {
			if (BibleApp.myDB != null)
			{
				String q, strOrder;
				strOrder = (BibleApp.bOldTestamentFirst)? "book_historical_order":"book_id";
				
				if (nMode == 0 )
					q = "SELECT book_id, book_name FROM books ORDER BY "+strOrder;
				else
					q = "SELECT chapter_id FROM verses WHERE book_id = '"+nBookId+"' GROUP BY chapter_id ORDER BY chapter_id";
				
				Cursor c = BibleApp.myDB.rawQuery(q, null);
				
				
	
				int nCount = c.getCount();
				if (nCount > 0 )
				{
					contents = new String[nCount];
					content_ids = new int[nCount];
					int i = 0, id;
					String str;
					
					while (c.moveToNext()) 
					{
						if (nMode == 0)
						{
							id = c.getInt(c.getColumnIndex("book_id"));
							str = Html.fromHtml(c.getString(c.getColumnIndex("book_name"))).toString();
						}
						else 
						{
							id = c.getInt(c.getColumnIndex("chapter_id"));
							str = "Глава "+id;
						}
						
						contents[i] = str;
						content_ids[i] = id;
						i++;
					}
				}
				else
				{
					contents = new String[0];
					content_ids = new int[0];
				}
			}
			
		} catch (SQLException sqlex) {
			
		}
	}
	
	public String GetBookTitleById(int book_id) {
		String str = "";

		try {

			if (BibleApp.myDB != null)
			{
				Cursor c = BibleApp.myDB.rawQuery("SELECT book_name FROM books WHERE book_id='" + book_id + "'", null);
	
				if (c.moveToFirst()) {
					str = c.getString(c.getColumnIndex("book_name"));
				}
			}
			
		} catch (SQLException sqlex) {
			
		}
		return str;
	}
	
	private void SetBackgroundColors()
	{
		if (BibleApp.bBlackBackground)
		{
			getListView().setBackgroundColor(Color.BLACK);
			getListView().setCacheColorHint(Color.BLACK);
		}
		else
		{
			getListView().setBackgroundColor(Color.WHITE);
			getListView().setBackgroundColor(Color.WHITE);
		}
	}

}
