package com.silvestre.Cinema;

import java.text.SimpleDateFormat;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class CinemaProvider extends ContentProvider {

    private static final int DATABASE_VERSION = 3;

	private static final String TAG = "CinemaProvider";

	private static final String AUTHORITY = "com.silvestre.Cinema.CinemaProvider";
	private static final int CONTENT_SCEANCE = 100;
	private static final int CONTENT_SCEANCE_ID = 110;
	private static final int CONTENT_YEAR = 120;
	private static final int CONTENT_THEATER = 130;
	private static final int CONTENT_THEATER_ID = 140;
	private static final int CONTENT_SCEANCE_INFO = 150;
			
	private static final String CONTENT_BASE_URI = "content://" + AUTHORITY  + "/";
	
	public static final Uri CONTENT_URI = Uri.parse(CONTENT_BASE_URI + sceance.CONTENT_PATH);
	
	//public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/mt-tutorial";
	//public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE  + "/mt-tutorial";	
	private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		sUriMatcher.addURI(AUTHORITY, sceance.CONTENT_PATH, CONTENT_SCEANCE);
		sUriMatcher.addURI(AUTHORITY, sceance.CONTENT_PATH + "/#", CONTENT_SCEANCE_ID);
		sUriMatcher.addURI(AUTHORITY, year.CONTENT_PATH, CONTENT_YEAR);
		sUriMatcher.addURI(AUTHORITY, theater.CONTENT_PATH, CONTENT_THEATER);
		sUriMatcher.addURI(AUTHORITY, theater.CONTENT_PATH + "/#", CONTENT_THEATER_ID);
	}
	
    public static final long NO_ID = -1;

    public static interface sceance extends BaseColumns {
    	    public static final Uri CONTENT_URI = CinemaProvider.CONTENT_URI;
    	    public static final String MOVIE = "film";    	
    	    public static final String DATE="dateSéance";
    	    public static final String TIME="heureSéance";
    	    public static final String DETAIL="détail";
    	    public static final String PEOPLE_NBR="spectateurs";
    	    public static final String PRICE="prix";
    	    public static final String THEATER="cinéma";
    	    public static final String ROOM="salle"; // unused yet
    	    public static final String DATE_START="datePremièreSéance";// unused yet
    	    public static final String DATE_END="dateDernièreSéance";// unused yet
    	    public static final String YEAR="année";
    	    public static final String FORMATED_DATE="dateSéanceFormatée";
    	    public static final String CONTENT_PATH = "sceance";
    	    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.silvestre.Cinema";
    	    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.silvestre.Cinema";
    	    public static final String[] PROJECTION_ALL = {_ID, MOVIE, DATE, TIME, DETAIL, PEOPLE_NBR, PRICE, THEATER}; //, ROOM, DATE_START, DATE_END
    	    public static final String[] PROJECTION = {_ID, MOVIE, " strftime('%d/%m/%Y',`"+DATE+"`) AS "+FORMATED_DATE,
    			DETAIL, PEOPLE_NBR+"||\" pers.\" AS "+PEOPLE_NBR};
    	    public static final String[] PROJECTION_COUNT = {"count("+_ID+") AS "+_COUNT};
        		
    	    public static final String SORT_DATE  = DATE;
    	    public static final String SORT_DATE_DESC  = DATE+" DESC";
    	    public static final String SORT_TITLE  = MOVIE;
    	    public static final String SORT_TITLE_DESC  = MOVIE+" DESC";
    	    public static final String SORT_DATETIME_DESC=SORT_DATE_DESC+", "+TIME+" DESC";
    	    public static final String SORT_DATETIME_ASC=SORT_DATE+", "+TIME+" ASC";
    	    public static final String SORT_ORDER_DEFAULT = SORT_DATE_DESC;
    }
	
	public static interface theater extends BaseColumns {
	    public static final String CONTENT_PATH = "theater";
		public static final Uri CONTENT_URI = Uri.parse(CONTENT_BASE_URI + CONTENT_PATH);
  	  	public static final String NAME = "nom";
		public static final String[] PROJECTION_ALL =  {_ID, NAME};
		public static final String SORT_ORDER_DEFAULT = NAME; 
  	}
	
	public static interface year extends BaseColumns {
	    public static final String CONTENT_PATH = "year";
	  	public static final Uri CONTENT_URI = Uri.parse(CONTENT_BASE_URI + CONTENT_PATH);
  	    public static final String YEAR = sceance.YEAR;
  	    public static long WHENEVER = 0;
  	    public static final String COST_BY_YEAR = "coûtParAnnée";
  	    public static final String COUNT_SCEANCE = "nombreSéance";
 	    //public static final String[] PROJECTION = {" DISTINCT "+YEAR+" AS "+_ID, YEAR};
 	    //public static final String[] PROJECTION_STAT = {_ID, ANNEE};
	    public static final String SORT_ORDER_DEFAULT = YEAR + " DESC";
	}
    
    private static final String YEAR_PART_FIELD_DATE= "strftime('%Y',`"+sceance.DATE+"`)";
        
    public static final String TABLE_SCEANCE="séances";
    public static final String TABLE_THEATER="cinémas";
    
         
    public static final String DATABASE_NAME = "seances"; // le seul sans accent...
    public static final String DATABASE_PATH = "/data/com.silvestre.Cinema/databases/";  // sans le 1er /data

    private static final String TABLE_THEATER_CREATE =
    	 "create table IF NOT EXISTS "+TABLE_THEATER+" ( "
        + theater._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
        + theater.NAME + " TEXT NOT NULL"
        + ");";
    	
    private static final String TABLE_SCEANCE_CREATE =
         "create table IF NOT EXISTS "+TABLE_SCEANCE+" ( "
        + sceance._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
        + sceance.MOVIE + " TEXT NOT NULL, "
        + sceance.DATE + " DATE DEFAULT CURRENT_DATE, "
        + sceance.TIME + " TIME DEFAULT CURRENT_TIME, "
        + sceance.DATE_START + " DATE DEFAULT NULL, "
        + sceance.DATE_END + " DATE DEFAULT NULL, "
        + sceance.DETAIL + " TEXT DEFAULT NULL, "
        + sceance.PEOPLE_NBR + " INTEGER DEFAULT 1, "
        + sceance.PRICE + " REAL DEFAULT NULL, "
        + sceance.THEATER + " INTEGER DEFAULT NULL, "
        + sceance.ROOM + " TEXT DEFAULT NULL, "
        + sceance.YEAR + " INTEGER DEFAULT NULL " // l'année de dateSéance
        + ");" ;

    private DatabaseHelper DBHelper;
    
	public SimpleDateFormat dateFormat;
    
    public CinemaProvider() {
    }
        
    private class DatabaseHelper extends SQLiteOpenHelper {
 
    	DatabaseHelper(Context context)  {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);

        }    
        
        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.w(TAG, "Create database with tables.");
            // TODO à revoir : soit on créé une base vide soit copy from asset  
            db.execSQL(TABLE_SCEANCE_CREATE);
            db.execSQL(TABLE_THEATER_CREATE);
        }

        // à modifier
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, 
                              int newVersion) 
        {
            String info = "do nothing";
            if ((oldVersion==1) && (newVersion==2)) { 
            	info = " add field "+sceance.YEAR;
            	db.execSQL("ALTER TABLE "+TABLE_SCEANCE+" ADD COLUMN "+sceance.YEAR+" INTEGER DEFAULT NULL; ");
            	db.execSQL("UPDATE "+TABLE_SCEANCE+" SET "+sceance.YEAR+"="+YEAR_PART_FIELD_DATE); 
            }
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will "+info);
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            Log.w(TAG, "Open database.");
            db.execSQL("PRAGMA encoding = \"UTF-8\"");
         }
    }

	@Override
	public boolean onCreate() {
    	DBHelper = new DatabaseHelper(getContext());
  		return true;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
	    String table;
	    switch (sUriMatcher.match(uri)) {
	    	case CONTENT_SCEANCE:
	      		table = TABLE_SCEANCE;
	    		break;
	       	case CONTENT_SCEANCE_ID:
	      		table = TABLE_SCEANCE;
	    		selection = sceance._ID + " = " + uri.getLastPathSegment();
	    		break;
	    	case CONTENT_THEATER:
	    		table = TABLE_THEATER;
	    		break;
	    	case CONTENT_THEATER_ID:
	    		table = TABLE_THEATER;
	    		selection = theater._ID + " = " + uri.getLastPathSegment();
	    		break;
	     	default:
	    		throw new IllegalArgumentException("Unknown Delete URI: " + uri);
	    }
	    SQLiteDatabase db = DBHelper.getWritableDatabase();
	    try {
		    int count = db.delete(table, selection, selectionArgs);
		    getContext().getContentResolver().notifyChange(uri, null);
		    return count;
	    } 
		finally {
			db.close();
		}
	}

	@Override
	public String getType(Uri uri) {
		// TODO: Implement this to handle requests for the MIME type of the data
		// at the given URI.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		String table;
		switch (sUriMatcher.match(uri)) {
	    	case CONTENT_SCEANCE:
	      		table = TABLE_SCEANCE;
	    		break;
	    	case CONTENT_THEATER:
	    		table = TABLE_THEATER;
	    		break;
	     	default:
	    		throw new IllegalArgumentException("Unknown Insert URI: " + uri);
	    		//throw new UnsupportedOperationException("Not yet implemented");
	    }
 		SQLiteDatabase db = DBHelper.getWritableDatabase();
		try {
			long id = db.insertOrThrow(table, null, values);
			if (id == -1) {
				throw new RuntimeException(String.format("%s : Failed to insert [%s] for unknown reasons.",TAG, values, uri));
			} else {
				return ContentUris.withAppendedId(uri, id);
			}			
		} 
		finally {
			db.close();
		}
	}


	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
	    
		SQLiteQueryBuilder queryBuilder;
	    SQLiteDatabase db = DBHelper.getReadableDatabase();
	    Cursor cursor;
	    
	    switch (sUriMatcher.match(uri)) {
	    	case CONTENT_SCEANCE:
	    		queryBuilder = new SQLiteQueryBuilder();
	    	    queryBuilder.setTables(TABLE_SCEANCE);
	    	    cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
	    		break;
	    	case CONTENT_SCEANCE_ID:
	    		queryBuilder = new SQLiteQueryBuilder();
	    	    queryBuilder.setTables(TABLE_SCEANCE);
	    		queryBuilder.appendWhere(sceance._ID + "=" + uri.getLastPathSegment());
	    	    cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
	    		break;
	    	case CONTENT_YEAR:
	    		String s = "SELECT 0 "+year._ID+", \""+getContext().getString(R.string.YearAll)+"\" "+year.YEAR+" union "
	    			+"SELECT DISTINCT année, année FROM "+TABLE_SCEANCE+" WHERE année is not null ORDER BY ";
	    		if ((sortOrder != null) && (!sortOrder.trim().equals("")))
	    			s = s + sortOrder;
	    		else 
	    			s = s + year.SORT_ORDER_DEFAULT;
	    		cursor = db.rawQuery(s,null);
	    		break;
	    	case CONTENT_THEATER:
	    		queryBuilder = new SQLiteQueryBuilder();
	    	    queryBuilder.setTables(TABLE_THEATER);
	    	    cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
	    		break;
	    	case CONTENT_THEATER_ID:
	    		queryBuilder = new SQLiteQueryBuilder();
	    	    queryBuilder.setTables(TABLE_THEATER);
	    	    queryBuilder.appendWhere(sceance._ID + "=" + uri.getLastPathSegment());
	    	    cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
	    		break;
	    	default:
	    		throw new IllegalArgumentException("Unknown URI: " + uri);
	    		//throw new UnsupportedOperationException("Not yet implemented");
	    }
	    // Make sure that potential listeners are getting notified
	    cursor.setNotificationUri(getContext().getContentResolver(), uri);
	    return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		String table;
		switch (sUriMatcher.match(uri)) {
	    	case CONTENT_SCEANCE:
	      		table = TABLE_SCEANCE;
	    		break;
	       	case CONTENT_SCEANCE_ID:
	      		table = TABLE_SCEANCE;
	    		selection = sceance._ID + " = " + uri.getLastPathSegment();
	    		break;
	    	case CONTENT_THEATER:
	    		table = TABLE_THEATER;
	    		break;
	    	case CONTENT_THEATER_ID:
	    		table = TABLE_THEATER;
	    		selection = theater._ID + " = " + uri.getLastPathSegment();
	    		break;
	     	default:
	    		throw new IllegalArgumentException("Unknown Update URI: " + uri);
	    		//throw new UnsupportedOperationException("Not yet implemented");
	    }
 		SQLiteDatabase db = DBHelper.getWritableDatabase();
		try {
			return db.update(table, values, selection, selectionArgs);
		} 
		finally {
			db.close();
		}
	}
	
    public static String getWhereSceance(FilmsSelection selection) {
       	String sqlWhere="";
    	if (! selection.searchMode) {
	       	if (selection.whichYear != year.WHENEVER) {    	
	    		sqlWhere = sceance.YEAR+"='"+selection.whichYear+"'";
	    		//sqlWhere = "("+FIELD_DATE+">='"+selection.whichYear+"-01-01') AND ("+FIELD_DATE+"<='"+selection.when+"-12-31')";
	    	}
	       	else 	
        		sqlWhere = null;
    	} // on fait une recherche sur le titre
    	else { 
    		if (selection.searchLikeFilm != null) {
        		sqlWhere += "("+sceance.MOVIE+" like '%"+selection.searchLikeFilm.replaceAll("'", "\'")+"%')";
    		}	
        	else 	
        		sqlWhere = null;
        }        
        		
    	return sqlWhere;
    }

	public static String getOrderBySceance(FilmsSelection selection) {
      	String sqlOrderBy=null;
    	if (selection.orderByDate) {
	       	if (selection.orderAsc)  {    	
	       		sqlOrderBy = sceance.SORT_DATETIME_ASC;
	    	}
	       	else 	
	       		sqlOrderBy = sceance.SORT_DATETIME_DESC;
    	} // on trie suivant le titre
    	else { 
    		if (selection.orderAsc) { 
    			sqlOrderBy = sceance.SORT_TITLE;
    		}	
        	else 	
        		sqlOrderBy = sceance.SORT_TITLE_DESC;
        }        
        		
    	return sqlOrderBy;
	}


    public void Close()  {
        DBHelper.close();
    }

}
