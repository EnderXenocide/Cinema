package com.silvestre.Cinema;

import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TooManyListenersException;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.LoaderManager;
import android.app.TimePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;


public class Film extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {


	final static int ESPACES = 11;   // pour activity Espace

	public static final String INTENT_NOUVEAU="sceance.nouveau";
	public static final String INTENT_SUPPRIMER="sceance.supprimer"; // retour
	public static final String INTENT_MODIFIER="sceance.modifier";   // retour
	public static final String INTENT_AJOUTER="sceance.ajouter";   // retour	  
	public static final String INTENT_ID_FILM="sceance.idFilm";

	// champs data
	private long idSceance;	
	private long idTheater=CinemaProvider.NO_ID;
	private Calendar date;  	
	private int mHour;
	private int mMinute;
	public SimpleDateFormat dateFormat;

	// TODO champs date_debut et date_fin
	private Spinner spinnerCinema;      
	private EditText editTextTitre;
	private EditText editTextDetail;
	private EditText editTextNbPersonnes;
	private EditText editTextPrix;
	private CheckBox chkDate;
	private CheckBox chkTime;
	private Button pickDate;
	private Button pickTime;

	private static final int LOADER_THEATER_ID = 10;

	private SimpleCursorAdapter mTheaterAdapter;

	private boolean NavigationBarVisible;

	// --------- INHERITED FUNCTIONS -------------------------


	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.film);
		
		NavigationBarVisible = false;
		
		InitSpinner(); 

		editTextTitre = (EditText) findViewById(R.id.editTextTitre);
		editTextDetail = (EditText) findViewById(R.id.editTextDetail);
		editTextNbPersonnes = (EditText) findViewById(R.id.editTextNbPersonnes);
		editTextPrix = (EditText) findViewById(R.id.editTextPrix);
		chkDate = (CheckBox) findViewById(R.id.chkDate);
		chkTime = (CheckBox) findViewById(R.id.chkTime);
		pickDate = (Button) findViewById(R.id.pickDate);
		pickTime = (Button) findViewById(R.id.pickTime);        

		date = new GregorianCalendar();
		dateFormat = new SimpleDateFormat(this.getString(R.string.DateFormat));

		// 	    // Check from the saved Instance
		// 	    Uri todoUri = (savedInstanceState == null) ? null : (Uri) savedInstanceState.getParcelable(CinemaProvider.sceance.CONTENT_ITEM_TYPE);
		// 	    // Or passed from the other activity
		// 	    Bundle extras = getIntent().getExtras();
		// 	    if (extras != null) {
		// 	      todoUri = extras.getParcelable(MyTodoContentProvider.CONTENT_ITEM_TYPE);
		// 	      LoadFilm(todoUri);
		// 	    }

		Intent intent = getIntent();
		Boolean ajouter = true;
		if (intent != null) {
			if (! intent.getBooleanExtra(INTENT_NOUVEAU,true)) {
				idSceance = intent.getLongExtra(INTENT_ID_FILM, (long) CinemaProvider.NO_ID);
				if (idSceance!=CinemaProvider.NO_ID) { 
					ajouter = false; 	 	        	
				}
			}	        
		}
		if (ajouter) {
			idSceance = CinemaProvider.NO_ID;
			//ToggleAddModify(false);
			setCurrentDateTimeInPickers();
		}
		else {
			//ToggleAddModify(true);
			LoadSceance(idSceance); 
		}	
	}  

	private void InitSpinner() {
		spinnerCinema = (Spinner) findViewById(R.id.spinnerCinemas);
		mTheaterAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item,
				null, 
				new String[] {CinemaProvider.theater.NAME}, 
				new int[] {android.R.id.text1},
				0);        
		mTheaterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerCinema.setAdapter(mTheaterAdapter);
		getLoaderManager().initLoader(LOADER_THEATER_ID, null, this);
	}
	
	/*  Ne pas faire :
     @Override
      protected void onStop(){
    	  db.close();
      } */
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		switch (id) {
		case LOADER_THEATER_ID:
			return new CursorLoader(Film.this, CinemaProvider.theater.CONTENT_URI, 
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
			SetIdCinemaSelected(idTheater);
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


	// ---------  MENU ------------
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (idSceance != CinemaProvider.NO_ID) {
			getMenuInflater().inflate(R.menu.modifierfilmmenu, menu);			
		} 
		else {
			getMenuInflater().inflate(R.menu.ajouterfilmmenu, menu);		     

		}
		//	     ActionBar actionBar = getActionBar();
		getActionBar().setDisplayHomeAsUpEnabled(true);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: ;
		case R.id.itemAnnuler:
			onBouttonAnnulerClick(null);
			return true;  
		case R.id.itemAjouter: ;
		case R.id.itemModifier:
			onBouttonValiderClick(null);
			return true;   
		case R.id.itemSupprimer:
			onBouttonSupprimerClick(null);
			return true;  
		case R.id.itemAnnulerAjouterFilm:
			onBouttonAnnulerClick(null);
			return true;  
		case R.id.itemNavigationBar:
			ToggleNavigationBar(!NavigationBarVisible);
			return true;  
			/* case R.id.itemFirst:
		    	onBouttonFirstClick(null);
		        return true;
		    case R.id.itemNext:
		 		onBouttonNextClick(null);
		    	return true;
		    case R.id.itemPrevious:
		 		onBouttonPreviousClick(null);
		    	return true; 	
		    case R.id.itemLast:
	            onBouttonLastClick(null);
	            return true;*/	
		default:
			return super.onOptionsItemSelected(item);
		}
	}	
	// ---------  FIN MENU ------------  	
	
	private void ToggleNavigationBar(boolean Show) {
		if (Show) {
			((View) findViewById(R.id.NavigationLayout)).setVisibility(View.VISIBLE);
		}
		else {
			((View) findViewById(R.id.NavigationLayout)).setVisibility(View.GONE);
		}   
		NavigationBarVisible = Show;
	}
	
//	private void ToggleAddModify(boolean Modify) {
//		if (Modify) {
//			//((Button) findViewById(R.id.BouttonSupprimer)).setVisibility(View.VISIBLE);
//			//((Button) findViewById(R.id.BouttonValider)).setText(R.string.Modifier);
//			((Button) findViewById(R.id.BouttonFirst)).setVisibility(View.VISIBLE);
//			((Button) findViewById(R.id.BouttonPrevious)).setVisibility(View.VISIBLE);
//			((Button) findViewById(R.id.BouttonNext)	).setVisibility(View.VISIBLE);
//			((Button) findViewById(R.id.BouttonLast)).setVisibility(View.VISIBLE);  
//			//((Button) findViewById(R.id.BouttonAnnuler)).setVisibility(View.GONE);  
//		}
//		else {
//			//((Button) findViewById(R.id.BouttonSupprimer)).setVisibility(View.GONE);
//			//((Button) findViewById(R.id.BouttonValider)).setText(R.string.Ajouter);
//			((Button) findViewById(R.id.BouttonFirst)).setVisibility(View.GONE);
//			((Button) findViewById(R.id.BouttonPrevious)).setVisibility(View.GONE);
//			((Button) findViewById(R.id.BouttonNext)).setVisibility(View.GONE);
//			((Button) findViewById(R.id.BouttonLast)).setVisibility(View.GONE);  
//			//((Button) findViewById(R.id.BouttonAnnuler)).setVisibility(View.VISIBLE);  
//		}    		  
//	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ESPACES: 
			if (resultCode==RESULT_OK) {
				getLoaderManager().restartLoader(LOADER_THEATER_ID, null, Film.this);
			}
			break;
		default : ;
		}
	}     

	// --------- DATE & TIME PICKERS -------------------------
	//private Button mPickDate;
	//private Button mPickTime;

	static final int DATE_DIALOG_ID = 20;
	static final int TIME_DIALOG_ID = 21;


	// the callback received when the user "sets" the date in the dialog
	private DatePickerDialog.OnDateSetListener mDateSetListener =
			new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			date.set(year, monthOfYear, dayOfMonth);
			chkDate.setChecked(true);
			updateDisplayDate(year, monthOfYear+1, dayOfMonth);    	  		
		}
	};

	// the callback received when the user "sets" the time in the dialog
	private TimePickerDialog.OnTimeSetListener mTimeSetListener =
			new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			mHour = hourOfDay;
			mMinute = minute;
			chkTime.setChecked(true);
			updateDisplayTime();
		}
	};


	// updates the date in the button
	private void updateDisplayDate() {
		int mDay = date.get(Calendar.DAY_OF_MONTH); 
		int mMonth = date.get(Calendar.MONTH)+1; 
		int mYear = date.get(Calendar.YEAR);
		updateDisplayDate(mYear,mMonth,mDay);
	}

	private void updateDisplayDate(int year, int month, int day) {
		if ((year==0) || (month==0) || (day==0))
			pickDate.setText(R.string.DateVide);
		else	   
			pickDate.setText(
					new StringBuilder().append(pad(day)).append("/").append(pad(month)).append("/").append(year));
	}

	// updates the time we display in the TextView
	private void updateDisplayTime() {
		if (GoodTime())
			pickTime.setText(new StringBuilder().append(pad(mHour)).append(":").append(pad(mMinute)));
		else
			pickTime.setText(R.string.TimeVide);

	}

	private boolean GoodTime() {
		return (mHour>=0) && (mMinute>=0);
	}


	private static String pad(int c) {
		if (c >= 10)
			return String.valueOf(c);
		else
			return "0" + String.valueOf(c);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_DIALOG_ID:
			if (! date.isSet(Calendar.YEAR)) 
				date = Calendar.getInstance();

			int mDay = date.get(Calendar.DAY_OF_MONTH); 
			int mMonth = date.get(Calendar.MONTH)+1; 
			int mYear = date.get(Calendar.YEAR);
			return new DatePickerDialog(this,
					mDateSetListener,
					mYear, mMonth-1, mDay);
		case TIME_DIALOG_ID:    
			if (!GoodTime()) {
				Calendar now = Calendar.getInstance();          
				mHour = now.get(Calendar.HOUR_OF_DAY);
				mMinute = now.get(Calendar.MINUTE);
			}        		  
			return new TimePickerDialog(this,
					mTimeSetListener, mHour, mMinute, true);
		}
		return null;
	}          

	public void onPickDateClick(View v) {
		showDialog(DATE_DIALOG_ID);
	}

	public void onPickTimeClick(View v) {
		showDialog(TIME_DIALOG_ID);
	}

	public void onChkDateClick(View v) {
		if ( ! chkDate.isChecked()) {
			date.clear();
			updateDisplayDate();
		}        
	}  

	public void onChkTimeClick(View v) {
		if ( ! chkTime.isChecked()) {
			mHour = -1; 
			mMinute = -1; 
			updateDisplayTime();
		}        
	} 

	private void setCurrentDateTimeInPickers() {
		// get the current date        
		date = GregorianCalendar.getInstance();          
		mHour = date.get(Calendar.HOUR_OF_DAY);
		mMinute = date.get(Calendar.MINUTE);

		// display the current date
		updateDisplayDate();
		// display the current time
		updateDisplayTime();    	  
	}
	// --------- FIN DATE & TIME PICKERS -------------------------

	private long GetIdCinemaSelected() {
		Cursor cursor = (Cursor) spinnerCinema.getSelectedItem();
		if (cursor != null) {
			return cursor.getLong(cursor.getColumnIndex(CinemaProvider.theater._ID));
		}
		else 
			return CinemaProvider.NO_ID; 
	}

	private void SetIdCinemaSelected(long Id) {
		boolean find = false;
		if (Id != CinemaProvider.NO_ID)
			for (int i = 0; i < spinnerCinema.getCount(); i++) {
				Cursor value = (Cursor) spinnerCinema.getItemAtPosition(i);
				long idCine = value.getLong(value.getColumnIndex(CinemaProvider.theater._ID));
				if (idCine == Id) {
					spinnerCinema.setSelection(i);
					find = true;
					break;
				}
			}
		if (! find)
			spinnerCinema.setSelection(Spinner.INVALID_POSITION);  
	}

	//	  private void LoadSpinnerCinema() {		
	//		long cinema;
	//		if (idCinema==CinemaProvider.NO_ID)
	//			cinema = GetIdCinemaSelected();
	//		else
	//			cinema = idCinema;
	// 	    Cursor cursor = getContentResolver().query(CinemaProvider.theater.CONTENT_URI, 
	// 	    			CinemaProvider.theater.PROJECTION_ALL, null, null, CinemaProvider.theater.SORT_ORDER_DEFAULT);
	//		if (cursor.moveToFirst()) { 
	//			SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
	//			    android.R.layout.simple_spinner_item, 
	//			    cursor, 
	//			    new String[] {DBAdapter.KEY_NOM}, 
	//			    new int[] {android.R.id.text1}); 		                                         
	//			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	//			spinnerCinema.setAdapter(adapter);
	//		}
	//		SetIdCinemaSelected(cinema);	
	//	  }

	private String getString(Cursor cursor, String sCol) {
		int col = cursor.getColumnIndex(sCol);
		if (col> -1)
			return cursor.getString(col);
		else 
			return "";  
	}     

	private void LoadSceance(long idSceance) {	
		Uri uri = ContentUris.withAppendedId(CinemaProvider.sceance.CONTENT_URI, idSceance); 
		Cursor cursor = getContentResolver().query(uri, CinemaProvider.sceance.PROJECTION_ALL, null, null, null);
		if (cursor.moveToFirst()) { 			  
			this.idSceance = idSceance;
			editTextTitre.setText(getString(cursor, CinemaProvider.sceance.MOVIE));
			//Date datevu = Date.valueOf("2010-07-02");
			try {
				date.setTime(Date.valueOf(getString(cursor, CinemaProvider.sceance.DATE)));
			} catch (Exception e) {
				date.clear();
			}			  
			chkDate.setChecked(date.isSet(Calendar.YEAR));
			updateDisplayDate();
			try {
				String heure = getString(cursor, CinemaProvider.sceance.TIME);
				if ((heure!=null) && (heure.length()==5))
					heure += ":00";	
				Time time = Time.valueOf(heure);
				mHour = time.getHours(); mMinute = time.getMinutes();				  
			} catch (Exception e) {
				mHour = -1 ; mMinute = -1;
			}
			chkTime.setChecked(GoodTime());
			updateDisplayTime();
			editTextDetail.setText(getString(cursor, CinemaProvider.sceance.DETAIL));


			int index = cursor.getColumnIndex( CinemaProvider.sceance.THEATER);
			if (cursor.isNull(index))
				idTheater = CinemaProvider.NO_ID;
			else
				idTheater = cursor.getLong(index);
			SetIdCinemaSelected(idTheater);

			editTextNbPersonnes.setText(getString(cursor, CinemaProvider.sceance.PEOPLE_NBR));
			String prix = getString(cursor, CinemaProvider.sceance.PRICE);
			// TODO transformer au bon format suivant local
			if (prix!=null)
				prix = prix.replace('.', ',');
			editTextPrix.setText(prix);

			// TODO ajouter autres champs
		}
		cursor.close();
	}

	public void onBouttonAnnulerClick(View v) {
		setResult(RESULT_CANCELED);
		finish(); //fermer ?
	}
	
	private String getHeure() {
		Calendar cal = Calendar.getInstance();
		cal.set(0, 0, 0, mHour, mMinute, 0); //heure = new Time(mHour, mMinute, 0);heure = 
		Time heure = new Time(cal.getTimeInMillis());
		return heure.toString();		
	}

	public void onBouttonValiderClick(View v) {
		String titre = editTextTitre.getText().toString().trim();
		if (titre.equals("")) {
			Toast.makeText(Film.this, R.string.TitreVide, Toast.LENGTH_SHORT).show();	
		}
		else {
			ContentValues mNewValues = new ContentValues();

			mNewValues.put(CinemaProvider.sceance.MOVIE, titre);
			if (GoodTime()) {
				mNewValues.put(CinemaProvider.sceance.TIME, getHeure());				  
			}
			else
				mNewValues.putNull(CinemaProvider.sceance.TIME);	

			if ((date != null) && (date.isSet(Calendar.YEAR))) {
				mNewValues.put(CinemaProvider.sceance.DATE, dateFormat.format(date.getTime()));
				mNewValues.put(CinemaProvider.sceance.YEAR, date.get(Calendar.YEAR));
			} 
			else {
				mNewValues.putNull(CinemaProvider.sceance.DATE);	
				mNewValues.putNull(CinemaProvider.sceance.YEAR);	
			}
			String detail = editTextDetail.getText().toString().trim();
			mNewValues.put(CinemaProvider.sceance.DETAIL, detail);
			String sPrix = editTextPrix.getText().toString().replace(',', '.');
			float prix;
			try {
				prix = Float.parseFloat(sPrix);// sPrix.to;
			} catch (NumberFormatException e) {
				prix = 0;
			}
			mNewValues.put(CinemaProvider.sceance.PRICE, prix);
			String sNbPersonnes = editTextNbPersonnes.getText().toString();
			int NbPersonnes;
			try {
				NbPersonnes = Integer.parseInt(sNbPersonnes);
			} catch (NumberFormatException e) {
				NbPersonnes = 1;
			}
			mNewValues.put(CinemaProvider.sceance.PEOPLE_NBR, NbPersonnes);
			long cinema = GetIdCinemaSelected();
			mNewValues.put(CinemaProvider.sceance.THEATER, cinema);

			// TODO ajouter autres champs
			String intentToSend;

			if (idSceance == CinemaProvider.NO_ID) { // INISERTION
				getContentResolver().insert(CinemaProvider.sceance.CONTENT_URI, mNewValues);
				// TODO Valeur de retour Uri mNewUri 
				intentToSend = INTENT_AJOUTER;
			}	
			else { // UPDATE
				Uri uri = ContentUris.withAppendedId(CinemaProvider.sceance.CONTENT_URI, idSceance); 
				getContentResolver().update(uri, mNewValues, null, null);
				// TODO Valeur de retour int nombre 
				intentToSend = INTENT_MODIFIER;
			}

			setResult(RESULT_OK, new Intent(intentToSend));
			
			if (!NavigationBarVisible)
				finish(); 
			else
				Toast.makeText(Film.this, R.string.Fait, Toast.LENGTH_SHORT).show(); 
		}
	}

	public void onBouttonSupprimerClick(View v) {
		if (idSceance != CinemaProvider.NO_ID) { // on a un id à supprimer
			//création d'une boite de dialogue pour confirmer le choix et supprimer le cas échéant
			new AlertDialog.Builder(Film.this)
			.setTitle(R.string.Confirmation)
			.setMessage(R.string.SupprimerSceance)
			.setPositiveButton(R.string.OUI, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					Uri uri = ContentUris.withAppendedId(CinemaProvider.sceance.CONTENT_URI, idSceance); 
					getContentResolver().delete(uri, null, null);
					setResult(RESULT_OK, new Intent(INTENT_SUPPRIMER)); 
					if (!NavigationBarVisible) 
						finish(); 
					else {
						Toast.makeText(Film.this, R.string.Fait, Toast.LENGTH_SHORT).show(); 
						onBouttonPreviousClick(null);
					}	
				}
			})
			.setNegativeButton(R.string.NON, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					setResult(RESULT_CANCELED);
				}
			})        
			.show();		
		}	
	}	      

	public void onBoutonCinemaClick(View v) {
		Intent intent = new Intent(this, com.silvestre.Cinema.Espace.class);
		startActivityForResult(intent, ESPACES);
	} 

	public void onBouttonFirstClick(View v) {
		Cursor cur = getContentResolver().query(CinemaProvider.sceance.CONTENT_URI, 
			    new String[] { CinemaProvider.sceance._ID }, 
         		null, 
        		null, 
        		CinemaProvider.sceance.SORT_DATETIME_ASC+" LIMIT 1"
        		);
		if (cur.moveToFirst())
			LoadSceance(cur.getLong(0));
	}

	public void onBouttonLastClick(View v) {
		Cursor cur = getContentResolver().query(CinemaProvider.sceance.CONTENT_URI, 
			    new String[] { CinemaProvider.sceance._ID }, 
         		null, 
        		null, 
        		CinemaProvider.sceance.SORT_DATETIME_DESC+" LIMIT 1"
        		);
		if (cur.moveToFirst())
			LoadSceance(cur.getLong(0));
	}    

	public void onBouttonPreviousClick(View v) {
		String selection = "("+CinemaProvider.sceance.DATE+"<?) OR ("+CinemaProvider.sceance.DATE+"=? AND " +CinemaProvider.sceance.TIME + "<?)";
		String sDate = dateFormat.format(date.getTime());
		String [] selectionArgs = {sDate, sDate, getHeure()}; 
		Cursor cur = getContentResolver().query(CinemaProvider.sceance.CONTENT_URI, 
			    new String[] { CinemaProvider.sceance._ID }, 
			    selection, 
			    selectionArgs, 
        		CinemaProvider.sceance.SORT_DATETIME_DESC+" LIMIT 1"
        		);
		if (cur.moveToFirst())
			LoadSceance(cur.getLong(0));
	}  

	/**
	 * Affiche la prochaine séance   
	 * @param v
	 */
	public void onBouttonNextClick(View v) {
		String selection = "("+CinemaProvider.sceance.DATE+">?) OR ("+CinemaProvider.sceance.DATE+"=? AND " +CinemaProvider.sceance.TIME + ">?)";
		String sDate = dateFormat.format(date.getTime());
		String [] selectionArgs = {sDate, sDate, getHeure()}; 
		Cursor cur = getContentResolver().query(CinemaProvider.sceance.CONTENT_URI, 
			    new String[] { CinemaProvider.sceance._ID }, 
			    selection, 
			    selectionArgs, 
        		CinemaProvider.sceance.SORT_DATETIME_ASC+" LIMIT 1"
        		);
		if (cur.moveToFirst())
			LoadSceance(cur.getLong(0));

	}
}
