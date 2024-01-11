package org.protege.editor.owl.rdf;

import javax.annotation.Nonnull;

import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SPARQLPreferences {

    private static final Logger logger = LoggerFactory.getLogger(SPARQLPreferences.class);

    

    public static final String SPARQL_SERVER_PREFERENCES_KEY = "SPARQLServerPreferences";
    public static final String SERVER_LOCATION = "SERVER_LOCATION";
    public static final String DEFAULT_SERVER = "http://localhost:8890/sparql";
    public static final String UPDATE_ON_COMMIT = "updateOnCommit";
    
    


  

    private static Preferences getPreferences() {
        return PreferencesManager.getInstance().getApplicationPreferences(SPARQL_SERVER_PREFERENCES_KEY);
    }


    

    /**
     * Gets the base directory to store the index files. By default, the base
     * directory is the user home directory.
     *
     * @return Returns the base directory location.
     */
    @Nonnull
    public static String getServerLocation() {
        return getPreferences().getString(SERVER_LOCATION, DEFAULT_SERVER);
    }
    
    public static void setServerLocation(String s) {
        getPreferences().putString(SERVER_LOCATION, s);
    }
    
    public static boolean getUpdateOnCommit() {
        return getPreferences().getBoolean(UPDATE_ON_COMMIT, true);
    }

    public static void setUpdateOnCommit(boolean upc) {
        getPreferences().putBoolean(UPDATE_ON_COMMIT, upc);
    }
    
    


    
    public static void clear() {
        Preferences preferences = getPreferences();
        preferences.clear();
        preferences.putString(SERVER_LOCATION, DEFAULT_SERVER);
    }

   
}
