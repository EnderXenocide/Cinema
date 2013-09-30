package com.silvestre.Cinema;


import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SearchView.OnQueryTextListener;

public class Espace extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {

	private static final int LOADER_THEATER_ID = 20;
	private SimpleCursorAdapter mTheaterAdapter;
	

	// champs data
	private long idCinema;
	private EditText editTextCinema;
	private ListView listCinema;
	private LoaderManager loaderManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.espace);

		mTheaterAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1,
				null, 
				new String[] {CinemaProvider.theater.NAME},          
				new int[] {android.R.id.text1},
				0);        
		setListAdapter(mTheaterAdapter);
		
		loaderManager = getLoaderManager();
		loaderManager.initLoader(LOADER_THEATER_ID, null, this);

		editTextCinema = (EditText) findViewById(R.id.editTextNomCinema);
		listCinema = (ListView) findViewById(android.R.id.list);
		listCinema.setOnItemClickListener(new OnItemClickListener() { 
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {  
				LoadTheater(id);        		
			}  
		}); 
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		switch (id) {
		case LOADER_THEATER_ID:
			return new CursorLoader(Espace.this, CinemaProvider.theater.CONTENT_URI, 
					CinemaProvider.theater.PROJECTION_ALL, null, null, CinemaProvider.theater.SORT_ORDER_DEFAULT);
		default:
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		switch (loader.getId()) {
		case LOADER_THEATER_ID:
			mTheaterAdapter.swapCursor(cursor);
			break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		switch (loader.getId()) {
		case LOADER_THEATER_ID:
			mTheaterAdapter.swapCursor(null);
			break;
		}	
	}

	private long GetIdCinemaSelected() {
		Cursor cursor = (Cursor) listCinema.getSelectedItem();
		if (cursor != null) {
			return cursor.getLong(cursor.getColumnIndex(CinemaProvider.theater._ID));
		}
		else 
			return CinemaProvider.NO_ID; 
	}

	private void SetIdCinemaSelected(long Id) {
		if (Id != CinemaProvider.NO_ID)
			for (int i = 0; i < listCinema.getCount(); i++) {
				Cursor value = (Cursor) listCinema.getItemAtPosition(i);
				long idCine = value.getLong(value.getColumnIndex(CinemaProvider.theater._ID));
				if (idCine == Id) {
					listCinema.setSelection(i);
					return;
				}
			}
	} 

	private void LoadTheater(long id) {		  
		Uri uri = ContentUris.withAppendedId(CinemaProvider.theater.CONTENT_URI, id); 
		Cursor cursor = getContentResolver().query(uri, CinemaProvider.theater.PROJECTION_ALL, null, null, null);
		if (cursor.moveToFirst()) {			  
			SetIdCinema(id);
			int col = cursor.getColumnIndex(CinemaProvider.theater.NAME);
			if (col> -1)
				editTextCinema.setText(cursor.getString(col));
			else 
				editTextCinema.setText("");  
		}
		cursor.close();
	}

	private void SetIdCinema(long Id) {
		idCinema = Id;
		ToggleAddModify(idCinema!=CinemaProvider.NO_ID);
	}

	private void ToggleAddModify(boolean Modify) {
		if (Modify) {
			((Button) findViewById(R.id.BouttonSupprimer)).setVisibility(View.VISIBLE);
			((Button) findViewById(R.id.BouttonModifier)).setVisibility(View.VISIBLE);
		}
		else {
			((Button) findViewById(R.id.BouttonSupprimer)).setVisibility(View.GONE);
			((Button) findViewById(R.id.BouttonModifier)).setVisibility(View.GONE);
		}    		  
	}

	// ---------  MENU ------------
	/*
	  @Override
	  public boolean onCreateOptionsMenu(Menu menu) {
		  MenuInflater inflater = getMenuInflater();
		  inflater.inflate(R.menu.espacemenu, menu);
		  return true;
	  } */

	public void onBouttonFermerClick(View v) {
		setResult(RESULT_OK);
		finish(); //fermer ?
	}

	public void onBouttonAjouterClick(View v) {
		idCinema = CinemaProvider.NO_ID;
		onBouttonValiderClick(v);
	}

	public void onBouttonValiderClick(View v) {
		String espace = editTextCinema.getText().toString().trim();
		if (espace.equals("")) {
			Toast.makeText(Espace.this, R.string.TitreVide, Toast.LENGTH_SHORT).show();	
		}
		else {
			boolean result;		
			ContentValues mValues = new ContentValues();
			mValues.put(CinemaProvider.theater.NAME, espace);
			if (idCinema == CinemaProvider.NO_ID) {
				Uri uri = getContentResolver().insert(CinemaProvider.theater.CONTENT_URI, mValues);
				idCinema = ContentUris.parseId(uri); 
				result = idCinema != CinemaProvider.NO_ID;
			}	
			else {
				Uri uri = ContentUris.withAppendedId(CinemaProvider.theater.CONTENT_URI, idCinema); 
				result = getContentResolver().update(uri, mValues, null, null)>0;				
			}
			if (result) {
				loaderManager.restartLoader(LOADER_THEATER_ID, null, this);
        		Toast.makeText(Espace.this, R.string.Fait, Toast.LENGTH_SHORT).show(); 
			}	
		}
	}

	public void onBouttonSupprimerClick(View v) {
		//		  long IdSelect = GetIdCinemaSelected(); // TODO ne pas confondre celui selectionner et celui en mémoire
		if (idCinema != CinemaProvider.NO_ID) { // on peux supprimer
			//verification possibilité suppression
			if (canDeleteCinema(idCinema)) {

				// Boite de dialogue pour confirmer le choix et supprimer le cas échéant
				new AlertDialog.Builder(Espace.this)
				.setTitle(R.string.Confirmation)
				.setMessage(R.string.SupprimerCinema)
				.setPositiveButton(R.string.OUI, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						Uri uri = ContentUris.withAppendedId(CinemaProvider.theater.CONTENT_URI, idCinema); 
		        		getContentResolver().delete(uri, null, null);
		        		idCinema = CinemaProvider.NO_ID;
		        		loaderManager.restartLoader(LOADER_THEATER_ID, null, Espace.this);
		        		Toast.makeText(Espace.this, R.string.Fait, Toast.LENGTH_SHORT).show(); 
					}
				})
				.setNegativeButton(R.string.NON, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// User clicked Cancel so do some stuff 
					}
				})        
				.show();		
			}
			else {
				Toast.makeText(Espace.this, R.string.SuppressionEspaceImpossible, Toast.LENGTH_SHORT).show(); 
			}
		}	  
	}

	private boolean canDeleteCinema(long idSceance) {
		Uri uri = ContentUris.withAppendedId(CinemaProvider.sceance.CONTENT_URI, idSceance); 
		Cursor cursor = getContentResolver().query(CinemaProvider.sceance.CONTENT_URI, 
				new String [] {"count("+CinemaProvider.sceance._ID+")"},
				CinemaProvider.sceance.THEATER+"="+idSceance, null, null);
    	if (cursor.moveToFirst())
    		return cursor.getInt(0)==0;
    	else 
    		return true; 
	}



}
