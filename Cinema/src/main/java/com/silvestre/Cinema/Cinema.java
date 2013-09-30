package com.silvestre.Cinema;

import com.silvestre.Cinema.CinemaProvider.year;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Cinema extends ListActivity implements OnQueryTextListener, LoaderManager.LoaderCallbacks<Cursor> {

	static final private int REQUEST_FILM = 10;
	static final private int REQUEST_PREFERENCES = REQUEST_FILM+1;

	private Spinner spinnerYear;  
	private ListView list;

	private static final int LOADER_ID = 0;	// ListView
	private static final int LOADER_YEAR_ID = 1; // Spinner

	private LoaderManager loaderManager;

	// The adapter that binds our data to the ListView
	private SimpleCursorAdapter mSceanceAdapter;
	// The adapter that binds our data to the Spinner
	private SimpleCursorAdapter mYearAdapter;

	private ActivityState state;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		state = new ActivityState();

		InitPreferences();        

		InitListView();

		InitSpinner();

		// Initialize the Loader with id "0" and callbacks "mCallbacks".
		loaderManager = getLoaderManager();
		loaderManager.initLoader(LOADER_YEAR_ID, null, Cinema.this);
		loaderManager.initLoader(LOADER_ID, null, Cinema.this);

		GetCountAllSceances();
		//        
		//        handleIntent(getIntent());
	}

	private void InitSpinner() {
		mYearAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item,
				null, 
				new String[] {CinemaProvider.year.YEAR}, 
				new int[] {android.R.id.text1},
				0);        
		mYearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerYear = (Spinner) findViewById(R.id.spinnerYears); 
		spinnerYear.setAdapter(mYearAdapter);
		spinnerYear.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (state.selection.whichYear != id) { 
					state.selection.whichYear = id;/// parent.getItemAtPosition(position).toString();
					state.selection.searchMode = false;
					loaderManager.restartLoader(LOADER_ID, null, Cinema.this);
				}	
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// nothing :-)
			}  
		});
	}

	private void InitListView() {
		mSceanceAdapter = new SimpleCursorAdapter(this, R.layout.list_item,
				null, 
				new String[] { CinemaProvider.sceance.MOVIE, CinemaProvider.sceance.FORMATED_DATE, CinemaProvider.sceance.DETAIL, CinemaProvider.sceance.PEOPLE_NBR },
				new int[] {R.id.li_titre, R.id.li_date, R.id.li_detail, R.id.li_nbPersonnes},
				0);        
		setListAdapter(mSceanceAdapter);
		list = (ListView) findViewById(android.R.id.list);
		list.setOnItemClickListener( new OnItemClickListener() { 
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {  
				state.idFilmSelected = id;
				OnModifier(id);  
			}  
		}); 
		// fonction appui long sur la listview
		list.setOnItemLongClickListener( new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
				OnSupprimer(id);
				return true;
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();	
		// restore saved statements
	} 

	@Override
	protected void onStop(){
		super.onStop();
		// We need an Editor object to make preference changes.
		// All objects are from android.context.Context
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		if (settings.getBoolean(Preferences.KEY_FILTER_BY_YEAR, false)) {
			String annee = (String) spinnerYear.getSelectedItem(); 
			if ( annee != null) { 
				SharedPreferences.Editor editor = settings.edit();	      
				editor.putString(Preferences.KEY_FILTER_YEAR, annee);
				// Commit the edits!
				editor.commit();
			}
		}
		//db.close(); // stay commented sinon big bug
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		switch (id) {
		case LOADER_ID:
			String select = CinemaProvider.getWhereSceance(state.selection);
			String orderBy = CinemaProvider.getOrderBySceance(state.selection);
			return new CursorLoader(Cinema.this, CinemaProvider.CONTENT_URI, CinemaProvider.sceance.PROJECTION,
					select, null, orderBy);
		case LOADER_YEAR_ID:	
			return new CursorLoader(Cinema.this, CinemaProvider.year.CONTENT_URI, null, null, null, CinemaProvider.year.SORT_ORDER_DEFAULT);
		default:
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		switch (loader.getId()) {
		case LOADER_ID:
			GetCountVisiblesSceances(cursor);
			ShowCountSceances();
			mSceanceAdapter.swapCursor(cursor);
			break;
		case LOADER_YEAR_ID:
			mYearAdapter.swapCursor(cursor);
			break;	        
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		switch (loader.getId()) {
		case LOADER_ID:
			mSceanceAdapter.swapCursor(null);
			break;
		case LOADER_YEAR_ID:
			mYearAdapter.swapCursor(null);
			break;	   
		}		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// pas utile pour l'instant
		switch (requestCode) {
		case REQUEST_FILM : ;
		case REQUEST_PREFERENCES :
			if (resultCode==RESULT_OK)
				handleIntent(data); 
		default : ;
		}		
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		String newFilter = !TextUtils.isEmpty(newText) ? newText : null;
		if (state.selection.searchLikeFilm == null && newFilter == null) {
			state.selection.searchMode = false;
			return true;
		}
		if (state.selection.searchLikeFilm != null && state.selection.searchLikeFilm.equals(newFilter)) {
			state.selection.searchMode = false; 
			return true;
		}
		state.selection.searchLikeFilm = newFilter;
		state.selection.searchMode = true;	// utile ?
		loaderManager.restartLoader(LOADER_ID, null, Cinema.this);
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		return true;
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		if (intent == null)
			return;
		else {
			String action = intent.getAction();
			Boolean reload = false;
			if (Intent.ACTION_MAIN.equals(action)) 
				;
			else if (Preferences.INTENT_IMPORT_SAMPLES.equals(action)) {
				Toast.makeText(getApplicationContext(), R.string.restart, Toast.LENGTH_SHORT).show();
				finish();
			}
			else if (Film.INTENT_AJOUTER.equals(action)) {
				// maj du compte ne marche que si il n'y a qu'une modif à la fois
				ChangeCountSceances(1);
				reload = true;
			}
			else if (Film.INTENT_MODIFIER.equals(action)) {
				reload = true;
			}
			else if (Film.INTENT_SUPPRIMER.equals(action)) {
				// maj du compte ne marche que si il n'y a qu'une modif à la fois
				reload = true;
				ChangeCountSceances(-1);
			}					
			else if (Preferences.INTENT_IMPORT.equals(action)) {
				GetCountAllSceances();
				reload = true;
			}
			if (reload) {
				loaderManager.restartLoader(LOADER_ID, null, Cinema.this);
				loaderManager.restartLoader(LOADER_YEAR_ID, null, Cinema.this);
			}				
		}		
	}


	@Override
	public void onBackPressed (){ 
		// TODO verifier avec le nombre d'éléments dans la liste
		//        //TODO mettre le choix de confirmer en quittant dans les preferences
		//    	new AlertDialog.Builder(this)
		//        .setTitle(R.string.Confirmation)
		//        .setMessage(R.string.QuestionQuitter)
		//        .setPositiveButton(R.string.OUI, new DialogInterface.OnClickListener() {
		//             public void onClick(DialogInterface dialog, int whichButton) {
		//            	finish();
		//             }
		//        })
		//        .setNegativeButton(R.string.NON, new DialogInterface.OnClickListener() {
		//        	public void onClick(DialogInterface dialog, int whichButton) {
		//        	}
		//        })        
		//        .show();    	
	}



	private void InitPreferences() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		if (preferences.getBoolean(Preferences.KEY_FILTER_BY_YEAR, false)) {
			state.selection.whichYear = preferences.getLong(Preferences.KEY_FILTER_YEAR, CinemaProvider.year.WHENEVER);        	
			//Toast.makeText(Cinema.this, "debut", Toast.LENGTH_SHORT).show(); //exemple de toast 	
		} 	
	}

	private void SetYearSelected(String year) {
		if (! year.equals("")) {
			for (int i = 0; i < spinnerYear.getCount(); i++) {
				String annee = spinnerYear.getItemAtPosition(i).toString();
				if (annee.equals(year)) {
					spinnerYear.setSelection(i);
					return;
				}
			}
		}	  
	}

	//	private void LoadSpinnerYearWithListener() {		
	//		spinnerYear.setOnItemSelectedListener(null);
	//		LoadSpinnerYear(); 
	//		spinnerYear.setOnItemSelectedListener(ItemSelectedListener);       
	//	}

	//	private void LoadSpinnerYear() {		
	//		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
	//				android.R.layout.simple_spinner_item, 
	//				android.R.id.text1);
	//		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	//		adapter.add(DBAdapter.WHENEVER);
	//		Cursor cursor = db.getAnnees();
	//		startManagingCursor(cursor);
	//		if (cursor.moveToFirst()) {  // important !
	//			do {
	//				adapter.add(cursor.getString(0));				
	//			}
	//			while (cursor.moveToNext()); 			
	//		}
	//		spinnerYear.setAdapter(adapter);
	//	    SetYearSelected(state.selection.whichYear);// TODO remettre selection
	//	}
	/*
	/** version alternative de LoadSpinnerYear 
	 * @deprecated
	 *//*
	private void LoadSpinnerYear2() {		
		ArrayList<String> annees = new ArrayList<String>();
		annees.add(DBAdapter.WHENEVER);
		db.open(false);
		Cursor cursor = db.getAnnees();
		if (cursor.moveToFirst()) {  // important !
			do {
				annees.add(cursor.getString(0));				
			}
			while (cursor.moveToNext()); 
			cursor.close();
		}
		db.close();		
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, annees);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 
        spinnerYear.setAdapter(adapter);    
	}*/


	private void ChangeCountSceances(int diff) {
		state.countSceances = state.countSceances + diff;
		state.countVisiblesSceances = state.countVisiblesSceances + diff;
	}


	private void GetCountAllSceances() {

        Cursor cursor = getContentResolver().query(CinemaProvider.sceance.CONTENT_URI, CinemaProvider.sceance.PROJECTION_COUNT, null, null, null);
		if (cursor.moveToFirst()) { 
			state.countSceances = cursor.getInt(0);
		}
	}

	private void GetCountVisiblesSceances(Cursor cursor) {
		if ( null != cursor)
			state.countVisiblesSceances = cursor.getCount();
		else
			state.countVisiblesSceances = 0;
	}

	private void ShowCountSceances() {		
		TextView tvCountSceances = (TextView) findViewById(R.id.countSceances);
		tvCountSceances.setText(getString(R.string.nbsceances)+state.countVisiblesSceances+"/"+state.countSceances);
	}


	// ---------  MENU ------------
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.mainmenu, menu);
		SearchView sv = new SearchView(this);
		sv.setOnQueryTextListener(this);
		MenuItem mSearchMenuItem = menu.findItem(R.id.itemRechercher);
		mSearchMenuItem.setActionView(sv); 
		mSearchMenuItem.setOnActionExpandListener(new OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                // TODO Auto-generated method stub
                return true;
            }
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                state.selection.searchLikeFilm = "";
                state.selection.searchMode = false;
                loaderManager.restartLoader(LOADER_ID, null, Cinema.this);
                return true;
            }
        });
		return true;
	}
	/*
	private long GetIdListViewSelected() {
    	Cursor cursor = (Cursor) list.getSelectedItem();
    	if (cursor != null) {
    		return cursor.getLong(cursor.getColumnIndex(DBAdapter.KEY_ROWID));
    	}
    	else 
    		return DBAdapter.NO_ID; 
	}*/

	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.itemNouveau:
			OnNouveau();
			return true;
		case R.id.itemGestionCine:
			intent = new Intent(this, com.silvestre.Cinema.Espace.class);
			startActivity(intent);
			return true;
		case R.id.itemTrier:
			OnDialogTrier();
			return true;
		case R.id.itemPreferences:
			intent = new Intent(this, com.silvestre.Cinema.Preferences.class);
			startActivityForResult(intent, REQUEST_PREFERENCES);
			return true; 	
			//case R.id.itemRechercher:
				//    onSearchRequested();
			//    return true;	
		case R.id.itemDetailCout:
			onDetailCout();
			return true;	
		default:
			return super.onOptionsItemSelected(item);
		}
	}	
	// ---------  FIN MENU ------------

	// ---------  ACTION MENU ------------
	/**
	 * TODO à mettre dans une activity
	 */
	private void onDetailCout() {
        StringBuilder info = new StringBuilder();
        Cursor cursor = getContentResolver().query(CinemaProvider.sceance.CONTENT_URI,
                new String [] {"SUM("+CinemaProvider.sceance.PRICE+")" },
                null, null, null); //CinemaProvider.sceance.PRICE+" is not null"
        if (cursor.moveToFirst()) {
            info.append(getString(R.string.BudgetTotal)).append(String.format(": %4.2f €",cursor.getFloat(0)));
        }
        cursor.close();
        cursor  = getContentResolver().query(CinemaProvider.sceance.CONTENT_URI,
                new String [] {CinemaProvider.sceance.YEAR, "COUNT("+CinemaProvider.sceance.YEAR+")", "SUM("+CinemaProvider.sceance.PRICE+")"},
                "1=1) GROUP BY ("+CinemaProvider.sceance.YEAR, null, CinemaProvider.sceance.YEAR+" DESC");
        if (cursor.moveToFirst()) {
            String year; int nbre; String cost;
            do {
                year = cursor.getString(0);
                nbre = cursor.getInt(1);
                cost =  String.format(": %4.2f € - ",cursor.getFloat(2));
                // construction du message
                info.append("\n").append(year).append(cost).append(nbre).append(" ").append(getString(R.string.Sceance));
                if (nbre>1)
                    info.append("s");
            }
            while (cursor.moveToNext());

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.TitreDetailCout);
            builder.setMessage(info);
            builder.setNegativeButton(R.string.Fermer, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    //Do nothing
                }
            });
            builder.show();
        }
        else
            Toast.makeText(getApplicationContext(), info, Toast.LENGTH_SHORT).show();

        cursor.close();
	}

	public void OnNouveau() {
		Intent intent = new Intent(this, com.silvestre.Cinema.Film.class);
		intent.putExtra(Film.INTENT_NOUVEAU, true);
		startActivityForResult(intent, REQUEST_FILM);
	}

	public void OnModifier(long idSceance) {
		Intent intent = new Intent(this, com.silvestre.Cinema.Film.class);
		intent.putExtra(Film.INTENT_NOUVEAU, false);
		// TODO gestion des uri dans activity Film
		//Uri uri = Uri.withAppendedPath(CinemaProvider.sceance.CONTENT_URI, idFilm+"");
		intent.putExtra(Film.INTENT_ID_FILM, idSceance);
		startActivityForResult(intent, REQUEST_PREFERENCES);
	}	

	public void OnSupprimer(long idSceance) {
		final long IdASupprimer = idSceance;
		//création d'un boite de dialogue pour confirmer le choix et supprimer le cas échéant
		new AlertDialog.Builder(Cinema.this)
		.setTitle(R.string.Confirmation)
		.setMessage(R.string.SupprimerSceance)
		.setPositiveButton(R.string.OUI, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				Uri uri = ContentUris.withAppendedId(CinemaProvider.sceance.CONTENT_URI, IdASupprimer); 
				getContentResolver().delete(uri, null, null);
				state.idFilmSelected = CinemaProvider.NO_ID;
				ChangeCountSceances(-1);
				////LoadSpinnerYear(); //LoadFilmsFromSpinnerChoise();
			}
		})
		.setNegativeButton(R.string.NON, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		})        
		.show();	        
	}

	public void SetOrder(int index) {
		switch (index) {
		case 0: // date + au - recent
			state.selection.orderByDate = true;
			state.selection.orderAsc = false;
			break;
		case 1: // date - au + recent
			state.selection.orderByDate = true;
			state.selection.orderAsc = true;
			break;
		case 2: //titre Z..A
			state.selection.orderByDate = false;
			state.selection.orderAsc = false;
			break;
		case 3: //titre A..Z
			state.selection.orderByDate = false;
			state.selection.orderAsc = true;
			break;
		default:
			break;
		}		
	}
	public int GetOrder() {
		int index=0;
		if (! state.selection.orderByDate)
			index += 2;
		if (state.selection.orderAsc)
			index += 1;
		return index;
	}	

	public void OnDialogTrier() {
		final CharSequence[] items = {"Date", "Date inv", "Titre Z..A", "Titre A..Z"};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.Trier);
		builder.setSingleChoiceItems(items, GetOrder(), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				SetOrder(item);
				loaderManager.restartLoader(Cinema.LOADER_ID, null, Cinema.this);
			}
		});
		builder.setNegativeButton(R.string.Fermer, new DialogInterface.OnClickListener() {
			@Override  public void onClick(DialogInterface dialog, int id) { //Do nothing   
			}
		});
		builder.show();
	}

	// --------- FIN ACTION MENU ------------

	/**
	 * Si la liste est vide, un bouton est affiché pour créer un nouvel éléments 
	 * @param v
	 */
	public void onEmptyAdd(View v) {
		OnNouveau();
	}
}