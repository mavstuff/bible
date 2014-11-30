package com.mav.bible;

import java.util.ArrayList;
import java.util.Iterator;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

class StFathersContentsItem{
	String strTitle = "";
	String strStFather = "";
	int nStFatherBookId = -1;
	int nStFatherBesedaId = -1;
	boolean bBook = false;
	boolean bOpened = false;
	int nLevel = 0;
	
	public StFathersContentsItem(String _strTitle, String _strStFather, int _nStFatherBookId,
			int _nStFatherBesedaId, boolean _bBook, boolean _bOpened, int _nLevel) 
	{
		strTitle = _strTitle;
		strStFather = _strStFather;
		nStFatherBookId = _nStFatherBookId;
		nStFatherBesedaId = _nStFatherBesedaId;
		bBook = _bBook;
		bOpened = _bOpened;
		nLevel = _nLevel;
	}

	@Override
	public String toString() 
	{
		int i;
		StringBuilder sb = new StringBuilder();
		
	    for(i=0; i < nLevel * 4; i++)
	    {
	        sb.append(" ");
	    }

		if (bBook)
		{
			if (bOpened)
			{
			    sb.append("[-] ");
			}
			else
			{
				sb.append("[+] ");
			}
		}
		else
		{
			//sb.append("   ");
		}
		
		sb.append(strTitle);
		
		return sb.toString();
	}

}

public class StFathersContentsActivity extends ListActivity {
	
	protected ArrayList<StFathersContentsItem> arrContents = null; 
	protected ArrayAdapter<StFathersContentsItem> arradContents = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (arrContents == null)
		{
			arrContents = new ArrayList<StFathersContentsItem>();
			arrContents.add(new StFathersContentsItem("Иоанн Златоуст", "zlatoust", -1, -1, true, false, 0));
			arrContents.add(new StFathersContentsItem("Ефрем Сирин", "efrem_sirin", -1, -1, true, false, 0));
			arrContents.add(new StFathersContentsItem("Феофилакт Болгарский", "feofilakt", -1, -1, true, false, 0));
		}

		if (arradContents == null)
		{
			arradContents = new ArrayAdapter<StFathersContentsItem>(StFathersContentsActivity.this, android.R.layout.simple_list_item_1, arrContents)
				{
				    @Override
				    public View getView(int position, View convertView, ViewGroup parent) 
				    {
				        TextView textView = (TextView) super.getView(position, convertView, parent);
						textView.setTextColor(BibleApp.bBlackBackground ? Color.WHITE : Color.BLACK);
				        return textView;
				    }	
				};
		}
		
		setListAdapter(arradContents);
		
		SetBackgroundColors();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		StFathersContentsItem item = (StFathersContentsItem)getListView().getItemAtPosition(position);
		
		if (item.bBook)
		{
			item.bOpened = !item.bOpened;
			
			if (item.bOpened)
			{
				OpenBookItem(item);
			}
			else
			{
				CloseBookItem(item);
			}
		}
		else
		{
			OpenBesedaItem(item);
		}
	
		arradContents.notifyDataSetChanged();
		
	}
	
	protected void OpenBookItem(StFathersContentsItem item)
	{
		try 
		{
			if (BibleApp.myDB != null)
			{
				String q;
				Cursor c;
				int i;

				int nItem = arrContents.indexOf(item);
				if (nItem != -1)
				{
					if (item.nStFatherBookId == -1)
					{
						q = "SELECT stfather_book_id,stfather_book_title FROM tolkov_books WHERE stfather_name='"+item.strStFather+
								"' ORDER BY stfather_book_id";
						c = BibleApp.myDB.rawQuery(q, null);
						
						i = 1;
						while (c.moveToNext()) 
						{
							arrContents.add(nItem+i,
									new StFathersContentsItem(c.getString(c.getColumnIndex("stfather_book_title")),
											item.strStFather, c.getInt(c.getColumnIndex("stfather_book_id")), -1,
											true, false, 1));
							i++;
						}
					}
					else if (item.nStFatherBookId != -1 && item.strStFather.equals("zlatoust"))
					{
						q = "SELECT MAX(beseda_id) as max_beseda_id FROM tolkov_zlatoust WHERE zlatoust_book_id='" + item.nStFatherBookId +"'";
						c = BibleApp.myDB.rawQuery(q, null);
						
						if (c.moveToFirst()) 
						{
							int nMaxBesedaId = c.getInt(c.getColumnIndex("max_beseda_id"));
							for (i=1; i<=nMaxBesedaId; i++)
							{
								arrContents.add(nItem+i,
										new StFathersContentsItem("Беседа " + i,
											item.strStFather, item.nStFatherBookId, i, false, false, 2));
							}
						}
					}
					else if (item.nStFatherBookId != -1 && item.strStFather.equals("efrem_sirin"))
					{
						q = "SELECT MAX(glava_id) as max_glava_id FROM tolkov_efrem_sirin WHERE efrem_sirin_book_id='" + item.nStFatherBookId +"'";
						c = BibleApp.myDB.rawQuery(q, null);
						
						if (c.moveToFirst()) 
						{
							int nMaxGlavaId = c.getInt(c.getColumnIndex("max_glava_id"));
							for (i=1; i<=nMaxGlavaId; i++)
							{
								arrContents.add(nItem+i,
										new StFathersContentsItem("Глава " + i,
											item.strStFather, item.nStFatherBookId, i, false, false, 2));
							}
						}
					}
					else if (item.nStFatherBookId != -1 && item.strStFather.equals("feofilakt"))
					{
						q = "SELECT MAX(glava_id) as max_glava_id FROM tolkov_feofilakt WHERE feofilakt_book_id='" + item.nStFatherBookId +"'";
						c = BibleApp.myDB.rawQuery(q, null);
						
						if (c.moveToFirst()) 
						{
							int nMaxGlavaId = c.getInt(c.getColumnIndex("max_glava_id"));
							for (i=1; i<=nMaxGlavaId; i++)
							{
								arrContents.add(nItem+i,
										new StFathersContentsItem("Глава " + i,
											item.strStFather, item.nStFatherBookId, i, false, false, 2));
							}
						}
					}

					
				}
			}
		} catch (SQLException sqlex) {
			
		}

	}
	
	
	protected void CloseBookItem(StFathersContentsItem item)
	{
		int nItem = arrContents.indexOf(item);
		if (nItem != -1)
		{
			if (item.nStFatherBookId == -1)
			{
				Iterator<StFathersContentsItem> iter = arrContents.iterator();
				while (iter.hasNext()) 
				{
					StFathersContentsItem itm = iter.next();
					if (itm.strStFather.equals(item.strStFather) && itm.nStFatherBookId != -1)
					{
				        iter.remove();
					}
				}
			}
			else
			{
				Iterator<StFathersContentsItem> iter = arrContents.iterator();
				while (iter.hasNext()) 
				{
					StFathersContentsItem itm = iter.next();
					
					if (itm.strStFather.equals(item.strStFather) && 
						itm.nStFatherBookId == item.nStFatherBookId && itm.nStFatherBesedaId != -1 )
					{
				        iter.remove();
					}
				}
				
			}
		}
	}
	
	protected void OpenBesedaItem(StFathersContentsItem item)
	{
		try 
		{
			if (item.nStFatherBookId != -1 && item.nStFatherBesedaId != -1 && item.strStFather.length() != 0)
			{
				Uri.Builder builder = new Uri.Builder();
				builder.scheme("com.mav.bible");
				
				if (item.strStFather.equals("zlatoust"))
				{
					builder.authority("zlatoust").path("bs");
				}
				else if (item.strStFather.equals("efrem_sirin"))
				{
					builder.authority("efrem-sirin").path("gl");
				
				}
				else if (item.strStFather.equals("feofilakt"))
				{
					builder.authority("feofilakt").path("gl");
				}

				builder.query(Integer.toString(item.nStFatherBookId));
				builder.fragment(Integer.toString(item.nStFatherBesedaId));
	        	
	            Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
	            startActivity(intent);
			}
		}
		catch (Exception e)
		{
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
			getListView().setBackgroundColor(Color.WHITE);
		}
	}
	
}




