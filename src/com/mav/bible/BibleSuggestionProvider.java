package com.mav.bible;

import android.content.SearchRecentSuggestionsProvider;

public class BibleSuggestionProvider extends SearchRecentSuggestionsProvider {
	
    public final static String AUTHORITY = "com.mav.bible.BibleSuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public BibleSuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}
