package com.mav.bible;

import com.mav.bible.BibleActivity;
import android.app.ListActivity;
import android.app.SearchManager;

import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SearchActivity extends ListActivity{

	protected CharSequence[] found_verses = null;
	protected String[] found_verses_raw = null;
	protected int[] found_book_ids = null;
	protected int[] found_chapter_ids = null;
	protected int[] found_verse_ids = null;
	
	protected boolean bFoundResults = false;
	
	protected String sQuery;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	    // Get the intent, verify the action and get the query
	    Intent intent = getIntent();
	    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	    	sQuery = intent.getStringExtra(SearchManager.QUERY);
	        
	    	SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
	                BibleSuggestionProvider.AUTHORITY, BibleSuggestionProvider.MODE);
	        suggestions.saveRecentQuery(sQuery, null);
	    	
	    	bFoundResults = false;
	    	ReloadSearch();
	    }

		//ListView lv = getListView();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		int nSel = (int)id;
		if (bFoundResults && nSel >= 0 && nSel < found_book_ids.length)
		{
			BibleActivity.found_book_id = found_book_ids[nSel];
			BibleActivity.found_chapter_id = found_chapter_ids[nSel];
			BibleActivity.found_verse_id = found_verse_ids[nSel];
			BibleActivity.found_verse_text = found_verses_raw[nSel];
			BibleActivity.found_result_ok = true;
		}
		finish();
	}
	
	protected void ReloadSearch(){
		
		GetSearchResults();
		
		if (found_verses != null)
		{
			setListAdapter(new ArrayAdapter<CharSequence>(SearchActivity.this, android.R.layout.simple_list_item_1, found_verses){
			    @Override
			    public View getView(int position, View convertView, ViewGroup parent) {
			        TextView textView = (TextView) super.getView(position, convertView, parent);
					if (BibleApp.bBlackBackground)
					{
						textView.setTextColor(Color.WHITE);
					}
					else
					{
						textView.setTextColor(Color.BLACK);
					}	
	
			        return textView;
			    }	
			});
					
		}
		
		SetBackgroundColors();
		
	}
	
	public void GetSearchResults() {
		
		try {
			if (BibleApp.myDB != null)
			{
				String q,sQueryUpper = sQuery;
				sQueryUpper = sQueryUpper.toUpperCase();
				q = "SELECT verses.book_id, book_short_name, chapter_id, verse_id, verse_text FROM verses, books WHERE verse_text_upper LIKE '%"+sQueryUpper+"%' AND verses.book_id = books.book_id ORDER BY verses.book_id, chapter_id, verse_id";
				
				Cursor c = BibleApp.myDB.rawQuery(q, null);
	
				int nCount = c.getCount();
				if (nCount > 0 )
				{
					found_verses = new CharSequence[nCount];
					found_verses_raw = new String[nCount];
					found_book_ids = new int[nCount]; 
					found_chapter_ids = new int[nCount];
					found_verse_ids = new int[nCount];
					
					int i = 0, book_id, chapter_id, verse_id;
					String strVerse;
					
					while (c.moveToNext()) 
					{
						book_id = c.getInt(c.getColumnIndex("book_id"));
						chapter_id = c.getInt(c.getColumnIndex("chapter_id"));
						verse_id = c.getInt(c.getColumnIndex("verse_id"));
						
						strVerse = c.getString(c.getColumnIndex("verse_text"));
						strVerse = strVerse.replaceAll("\\<.*?>","");
						strVerse = strVerse.replaceAll("(?i)("+sQuery+")", "<b>$1</b>");
						
						found_book_ids[i] = book_id;
						found_chapter_ids[i] = chapter_id;
						found_verse_ids[i] = verse_id;
						found_verses_raw[i] = c.getString(c.getColumnIndex("verse_text"));
						
						found_verses[i] = Html.fromHtml( 
							  c.getString(c.getColumnIndex("book_short_name")) + " " + 
							  chapter_id + ":" +
							  verse_id + " " +
							  strVerse);
	
						i++;
					}
					
					bFoundResults = true;
				}
				else
				{
					found_verses = new CharSequence[1];
					found_verses[0] = Html.fromHtml("Поиск <b>'"+sQuery+"'</b> не дал результатов");
					
					found_book_ids = new int[0]; 
					found_chapter_ids = new int[0];
					found_verse_ids = new int[0];
					found_verses_raw = new String[0];
					
					bFoundResults = false;
				}
			}
			
		} catch (SQLException sqlex) {
			
		}
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
			getListView().setCacheColorHint(Color.WHITE);
		}
	}
}
