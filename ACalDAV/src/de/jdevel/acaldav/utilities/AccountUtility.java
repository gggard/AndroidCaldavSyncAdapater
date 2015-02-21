package de.jdevel.acaldav.utilities;

import android.accounts.Account;
import android.accounts.AccountManager;

import de.jdevel.acaldav.App;

/**
 * @author Joseph Weigl
 */
public class AccountUtility {

    /**
     * Returns all available Google accounts
     * @return array of google accounts. Empty (never null) if no accounts of the specified type have been added.
     */
    public static Account[] getGoogleAccounts(){
        Account[] accounts = AccountManager.get(App.getContext()).getAccountsByType("com.google");
        return accounts;
    }

    /**
     * Returns the email of the first found Google account or an empty string
     * @return google email address or an empty string
     */
    public static String getGoogleMail(){
        Account[] accounts = getGoogleAccounts();
        if(accounts.length > 0){
            return accounts[0].name;
        }
        else return "";
    }

    /**
     * Private constructor for utility class
     */
    private AccountUtility(){
        // nothing to do
    }

}
