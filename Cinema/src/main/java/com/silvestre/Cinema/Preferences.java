package com.silvestre.Cinema;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;

public class Preferences extends PreferenceActivity {
	

	public static final String INTENT_IMPORT="preferences.import";
	public static final String INTENT_IMPORT_SAMPLES="preferences.importSamples";
	public static final String INTENT_EXPORT="preferences.export";
	
    public static final String KEY_FILTER_BY_YEAR="FilterByYear";
    public static final String KEY_FILTER_YEAR="FilterYear";
    public static final String SD_SILVESTRE_PATH = "";///Android/data/com.silvestre.databackup/";
    //TODO mettre backup database dans un dossier	
    
    public DialogInterface.OnClickListener onClickNoListener = new DialogInterface.OnClickListener() {
    	public void onClick(DialogInterface dialog, int whichButton) {}
    };
    public DialogInterface.OnClickListener onImportSampDataClickYesListener = new DialogInterface.OnClickListener() {
    	public void onClick(DialogInterface dialog, int whichButton) {
     		int Result;
     		if (ImportSampleDataBase()) 
     			Result = RESULT_OK;
     		else
     			Result = RESULT_CANCELED;
    		setResult(Result, new Intent(INTENT_IMPORT_SAMPLES)); 
    		finish();
         }
    };   
    
    public DialogInterface.OnClickListener onImpDataClickYesListener = new DialogInterface.OnClickListener() {
    	public void onClick(DialogInterface dialog, int whichButton) {
     		int Result;
     		if (ImportDataBase()) 
     			Result = RESULT_OK;
     		else
     			Result = RESULT_CANCELED;
    		setResult(Result, new Intent(INTENT_IMPORT)); 
    		finish();
         }
    }; 
    
    private void Confirm(DialogInterface.OnClickListener yes, DialogInterface.OnClickListener no) {
    	new AlertDialog.Builder(Preferences.this)
        .setTitle(R.string.Confirmation)
        .setMessage(R.string.ConfirmationImporter)
        .setPositiveButton(R.string.OUI, yes)
        .setNegativeButton(R.string.NON, no)        
        .show();
    }
    
    
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    addPreferencesFromResource(R.xml.preferences);
	    
	    Preference pref = (Preference) getPreferenceScreen().findPreference("ImportSampleData");
	    pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Confirm(onImportSampDataClickYesListener, onClickNoListener);
				return false;
			}
		});
	    pref = (Preference) getPreferenceScreen().findPreference("ImportData");
	    pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Confirm(onImpDataClickYesListener, onClickNoListener);
				return false;
			}
		}); 
	    pref = (Preference) getPreferenceScreen().findPreference("ExportData");
	    pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				int Result;
	     		if (ExportDataBase()) 
	     			Result = RESULT_OK;
	     		else
	     			Result = RESULT_CANCELED;
	    		setResult(Result, new Intent(INTENT_EXPORT));  // pas obliger de creer un intent
	    		finish();
				return false;
			}
		}); 
	}
	
    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    private void copyDataBaseFromAsset() throws IOException{
    	//Open your local db as the input stream
    	InputStream myInput = getAssets().open(CinemaProvider.DATABASE_NAME); 
    	// Path to the just created empty db
    	String outFileName = CinemaProvider.DATABASE_PATH + CinemaProvider.DATABASE_NAME; 
    	//Open the empty db as the output stream
    	File data = Environment.getDataDirectory();
    	File outFile = new File(data, outFileName);
    	OutputStream myOutput = new FileOutputStream(outFile); 
    	//transfer bytes from the inputfile to the outputfile
    	byte[] buffer = new byte[1024];
    	int length;
    	while ((length = myInput.read(buffer))>0){
    		myOutput.write(buffer, 0, length);
    	}     
    	//Close the streams
    	myOutput.flush();
    	myOutput.close();
    	myInput.close();  
    }
    
    private boolean ImportSampleDataBase() {
 		try {
			copyDataBaseFromAsset();
		} catch (IOException e) { 
			throw new Error("Error copying database");    		
    	}
		showToast(R.string.importOk);
		return true;			
    }
    
    private boolean ImportDataBase() {
       	try {
    		File sd = Environment.getExternalStorageDirectory();
    		File data = Environment.getDataDirectory();
    		if (sd.canRead()) {
	    		String currentDBPath = CinemaProvider.DATABASE_PATH + CinemaProvider.DATABASE_NAME;
	    		String restoreDBPath = SD_SILVESTRE_PATH + CinemaProvider.DATABASE_NAME;
	    		File currentDB = new File(data, currentDBPath);
	    		File restoreDB = new File(sd, restoreDBPath);
	    		if (restoreDB.exists()) {
		    		FileChannel src = new FileInputStream(restoreDB).getChannel();
		    		FileChannel dst = new FileOutputStream(currentDB).getChannel();
		    		dst.transferFrom(src, 0, src.size());
		    		src.close();
		    		dst.close();
	    		}
	    		else {
	    			showToast("Saved Database don't exist !");
	    			return false;
	    		}
    		}
    		else {
    			showToast("Can't read on SD!");
    			return false;
    		}	
    	} 
    	catch (Exception e) {};
    	showToast(R.string.importOk);
    	return true;
    }
    
    private boolean ExportDataBase() {
    	try {
    		File sd = Environment.getExternalStorageDirectory();
    		File data = Environment.getDataDirectory();
    		if (sd.canWrite()) {
	    		String currentDBPath = CinemaProvider.DATABASE_PATH + CinemaProvider.DATABASE_NAME;
	    		String backupDBPath = SD_SILVESTRE_PATH + CinemaProvider.DATABASE_NAME;
	    		File currentDB = new File(data, currentDBPath);
	    		File backupDB = new File(sd, backupDBPath);
	    		if (currentDB.exists()) {
		    		FileChannel src = new FileInputStream(currentDB).getChannel();
		    		FileChannel dst = new FileOutputStream(backupDB).getChannel();
		    		dst.transferFrom(src, 0, src.size());
		    		src.close();
		    		dst.close();
	    		}
	    		else {
	    			showToast("Database don't exist !");
	    			return false;
	    		}
    		}
    		else {
    			showToast("Can't write on SD !");
    			return false;
    		}	
    	} 
    	catch (Exception e) {};
    	showToast(R.string.exportOk);
    	return true;
    }
    
    /**
     * Show a toast on the screen with the given message.  If a toast is already
     * being displayed, the message is replaced and timer is restarted.
     *
     * @param message Resource id of the text to display in the toast.
     */
    private void showToast(int message) {
      showToast(getText(message));
    }

    /**
     * Show a toast on the screen with the given message.  If a toast is already
     * being displayed, the message is replaced and timer is restarted.
     *
     * @param message Text to display in the toast.
     */
    private void showToast(CharSequence message) {
      Toast.makeText(Preferences.this, message, Toast.LENGTH_LONG).show();
    }    
}

