/**
 * Copyright (c) 2012-2013, Gerald Garcia
 *
 * This file is part of Andoid Caldav Sync Adapter Free.
 *
 * Andoid Caldav Sync Adapter Free is free software: you can redistribute 
 * it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 of the 
 * License, or at your option any later version.
 *
 * Andoid Caldav Sync Adapter Free is distributed in the hope that 
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Andoid Caldav Sync Adapter Free.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.gege.caldavsyncadapter.authenticator;

import org.apache.http.conn.HttpHostConnectException;
import org.gege.caldavsyncadapter.Constants;
import org.gege.caldavsyncadapter.caldav.CaldavFacade;
import org.gege.caldavsyncadapter.caldav.CaldavFacade.TestConnectionResult;
import org.xml.sax.SAXException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;

import de.jdevel.acaldav.App;
import de.jdevel.acaldav.R;
import de.jdevel.acaldav.utilities.AccountUtility;
import de.jdevel.acaldav.widget.DrawableClickListener;
import de.jdevel.acaldav.widget.IconfiedEditText;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class AuthenticatorActivity extends Activity {

    public static final String USER_DATA_URL_KEY = "USER_DATA_URL_KEY";

    public static final String USER_DATA_USERNAME = "USER_DATA_USERNAME";

    public static final String USER_DATA_VERSION = "USER_DATA_VERSION";

    public static final String CURRENT_USER_DATA_VERSION = "1";

    public static final String ACCOUNT_NAME_SPLITTER = "@";

    /**
     * The default email to populate the email field with.
     */
    public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";

    private static final String TAG = "AuthenticatorActivity";

    private static final String ACCOUNT_TYPE = "org.gege.caldavsyncadapter.account";

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // Values for email and password at the time of the login attempt.
    private String mUser;

    private String mPassword;

    private String mTrustAll;

    private Context mContext;

    // UI references.
    private IconfiedEditText mUserView;

    private IconfiedEditText mPasswordText;

    private View mLoginFormView;

    private View mLoginStatusView;

    private TextView mLoginStatusMessageView;

    private CheckBox mTrustCheckBox;

    private AccountManager mAccountManager;

    private String mURL;

    private IconfiedEditText mURLText;

    private String mAccountname;

    private IconfiedEditText mAccountnameText;

    private Drawable mGmailButton;

    private Drawable mClearButton;

    public AuthenticatorActivity() {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAccountManager = AccountManager.get(this);

        setContentView(R.layout.activity_authenticator);

        // Set up the login form.
        mUser = getIntent().getStringExtra(EXTRA_EMAIL);
        mUserView = (IconfiedEditText) findViewById(R.id.user);
        mUserView.setText(mUser);
        mUserView.addClearButton();

        mContext = getBaseContext();
        App.setContext(mContext);

        mPasswordText = (IconfiedEditText) findViewById(R.id.password);
        mPasswordText.addClearButton();
        mPasswordText
                .setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView textView, int id,
                            KeyEvent keyEvent) {
                        if (id == R.id.login || id == EditorInfo.IME_NULL) {
                            attemptLogin();
                            return true;
                        }
                        return false;
                    }
                });

        mURLText = (IconfiedEditText) findViewById(R.id.url);
        mURLText.addClearButton();
        // if the URL start with "https" show the option to disable SSL host verification
        mURLText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String url = ((EditText) findViewById(R.id.url)).getText().toString();
                int visible = url.toLowerCase(Locale.getDefault())
                        .startsWith("https") ? View.VISIBLE : View.GONE;
                (findViewById(R.id.trustall)).setVisibility(visible);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mClearButton = getResources().getDrawable(android.R.drawable.ic_menu_close_clear_cancel);
        mGmailButton = getResources().getDrawable(R.drawable.ic_gmail);
        mAccountnameText = (IconfiedEditText) findViewById(R.id.accountname);
        mAccountnameText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Drawable drawable = TextUtils.isEmpty(s.toString()) ? mGmailButton : mClearButton;
                mAccountnameText
                        .setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
       mAccountnameText.setDrawableClickListener(new DrawableClickListener() {
           @Override
           public void onClick(DrawablePosition target) {
               switch (target) {
                   case RIGHT:
                       if (TextUtils.isEmpty(mAccountnameText.getText().toString())) {
                           mAccountnameText.setText(AccountUtility.getGoogleMail());
                       } else {
                           mAccountnameText.setText("");
                       }
               }
           }
       });

        mLoginFormView = findViewById(R.id.login_form);
        mLoginStatusView = findViewById(R.id.login_status);
        mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

        findViewById(R.id.sign_in_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        attemptLogin();
                    }
                }
        );

        mTrustCheckBox = (CheckBox) findViewById(R.id.trustall);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUserView.setError(null);
        mPasswordText.setError(null);

        // Store values at the time of the login attempt.
        mUser = mUserView.getText().toString();
        mPassword = mPasswordText.getText().toString();
        mURL = mURLText.getText().toString();
        mAccountname = mAccountnameText.getText().toString();
        mTrustAll = (mTrustCheckBox.isChecked() ? "false" : "true");
        boolean cancel = false;
        View focusView = null;

        if (!mAccountname.equals("")) {
            Account TestAccount = new Account(mAccountname, ACCOUNT_TYPE);
            String TestUrl = mAccountManager
                    .getUserData(TestAccount, AuthenticatorActivity.USER_DATA_URL_KEY);
            if (TestUrl != null) {
                mAccountnameText.setError(getString(R.string.error_account_already_in_use));
                focusView = mAccountnameText;
                cancel = true;
            }
        }

        // Check for a valid password.
        if (TextUtils.isEmpty(mPassword)) {
            mPasswordText.setError(getString(R.string.error_field_required));
            focusView = mPasswordText;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(mUser)) {
            mUserView.setError(getString(R.string.error_field_required));
            focusView = mUserView;
            cancel = true;
        }
        //else if (!mUser.contains("@")) {
        //	mUserView.setError(getString(R.string.error_invalid_email));
        //	focusView = mUserView;
        //	cancel = true;
        //}

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
            showProgress(true);
            mAuthTask = new UserLoginTask();
            mAuthTask.execute((Void) null);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(
                    android.R.integer.config_shortAnimTime);

            mLoginStatusView.setVisibility(View.VISIBLE);
            mLoginStatusView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginStatusView.setVisibility(show ? View.VISIBLE
                                    : View.GONE);
                        }
                    });

            mLoginFormView.setVisibility(View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginFormView.setVisibility(show ? View.GONE
                                    : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    protected enum LoginResult {
        MalformedURLException,
        GeneralSecurityException,
        UnkonwnException,
        WrongCredentials,
        InvalidResponse,
        WrongUrl,
        ConnectionRefused,
        Success_Calendar,
        Success_Collection,
        UNTRUSTED_CERT,
        Account_Already_In_Use
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, LoginResult> {

        @Override
        protected LoginResult doInBackground(Void... params) {

            TestConnectionResult result = null;

            try {
                CaldavFacade facade = new CaldavFacade(mUser, mPassword, mURL, mTrustAll);
                String version = "";
                try {
                    version = mContext.getPackageManager()
                            .getPackageInfo(mContext.getPackageName(), 0).versionName;
                } catch (NameNotFoundException e) {
                    version = "unknown";
                    e.printStackTrace();
                }
                facade.setVersion(version);
                result = facade.testConnection();
                Log.i(TAG, "testConnection status=" + result);
            } catch (HttpHostConnectException e) {
                Log.w(TAG, "testConnection", e);
                return LoginResult.ConnectionRefused;
            } catch (MalformedURLException e) {
                Log.w(TAG, "testConnection", e);
                return LoginResult.MalformedURLException;
            } catch (UnsupportedEncodingException e) {
                Log.w(TAG, "testConnection", e);
                return LoginResult.UnkonwnException;
            } catch (ParserConfigurationException e) {
                Log.w(TAG, "testConnection", e);
                return LoginResult.UnkonwnException;
            } catch (SAXException e) {
                Log.w(TAG, "testConnection", e);
                return LoginResult.InvalidResponse;
            } catch (IOException e) {
                Log.w(TAG, "testConnection", e);
                return LoginResult.UnkonwnException;
            } catch (URISyntaxException e) {
                Log.w(TAG, "testConnection", e);
                return LoginResult.MalformedURLException;
            }

            if (result == null) {
                return LoginResult.UnkonwnException;
            }

            switch (result) {

                case SSL_ERROR:
                    return LoginResult.UNTRUSTED_CERT;
                case SUCCESS:
                    boolean OldAccount = false;
                    LoginResult Result = LoginResult.Success_Calendar;

                    if (OldAccount) {
                        final Account account = new Account(mUser, ACCOUNT_TYPE);
                        if (mAccountManager.addAccountExplicitly(account, mPassword, null)) {
                            Log.v(TAG, "new account created");
                            mAccountManager.setUserData(account, USER_DATA_URL_KEY, mURL);
                        } else {
                            Log.v(TAG, "no new account created");
                            Result = LoginResult.Account_Already_In_Use;
                        }
                    } else {
                        final Account account;
                        if (mAccountname.equals("")) {
                            account = new Account(mUser + ACCOUNT_NAME_SPLITTER + mURL,
                                    ACCOUNT_TYPE);
                        } else {
                            account = new Account(mAccountname, ACCOUNT_TYPE);
                        }
                        if (mAccountManager.addAccountExplicitly(account, mPassword, null)) {
                            Log.v(TAG, "new account created");
                            mAccountManager.setUserData(account, USER_DATA_URL_KEY, mURL);
                            mAccountManager.setUserData(account, USER_DATA_USERNAME, mUser);
                            mAccountManager.setUserData(account, USER_DATA_VERSION,
                                    CURRENT_USER_DATA_VERSION);
                            mAccountManager.setUserData(account, Constants.USER_DATA_TRUST_ALL_KEY,
                                    mTrustAll);
                        } else {
                            Log.v(TAG, "no new account created");
                            Result = LoginResult.Account_Already_In_Use;
                        }
                    }

                    return Result;

                case WRONG_CREDENTIAL:
                    return LoginResult.WrongCredentials;

                case WRONG_SERVER_STATUS:
                    return LoginResult.InvalidResponse;

                case WRONG_URL:
                    return LoginResult.WrongUrl;

                case WRONG_ANSWER:
                    return LoginResult.InvalidResponse;

                default:
                    return LoginResult.UnkonwnException;

            }

        }


        @Override
        protected void onPostExecute(final LoginResult result) {
            mAuthTask = null;
            showProgress(false);

            int duration = Toast.LENGTH_SHORT;
            Toast toast = null;

            switch (result) {
                case Success_Calendar:
                    toast = Toast
                            .makeText(getApplicationContext(), R.string.success_calendar, duration);
                    toast.show();
                    finish();
                    break;

                case Success_Collection:
                    toast = Toast.makeText(getApplicationContext(), R.string.success_collection,
                            duration);
                    toast.show();
                    finish();
                    break;

                case MalformedURLException:

                    toast = Toast
                            .makeText(getApplicationContext(), R.string.error_incorrect_url_format,
                                    duration);
                    toast.show();
                    mURLText.setError(getString(R.string.error_incorrect_url_format));
                    mURLText.requestFocus();
                    break;
                case InvalidResponse:
                    toast = Toast
                            .makeText(getApplicationContext(), R.string.error_invalid_server_answer,
                                    duration);
                    toast.show();
                    mURLText.setError(getString(R.string.error_invalid_server_answer));
                    mURLText.requestFocus();
                    break;
                case WrongUrl:
                    toast = Toast
                            .makeText(getApplicationContext(), R.string.error_wrong_url, duration);
                    toast.show();
                    mURLText.setError(getString(R.string.error_wrong_url));
                    mURLText.requestFocus();
                    break;

                case GeneralSecurityException:
                    break;
                case UnkonwnException:
                    break;
                case WrongCredentials:
                    mPasswordText.setError(getString(R.string.error_incorrect_password));
                    mPasswordText.requestFocus();
                    break;

                case ConnectionRefused:
                    toast = Toast
                            .makeText(getApplicationContext(), R.string.error_connection_refused,
                                    duration);
                    toast.show();
                    mURLText.setError(getString(R.string.error_connection_refused));
                    mURLText.requestFocus();
                    break;
                case UNTRUSTED_CERT:
                    toast = Toast.makeText(getApplicationContext(),
                            getString(R.string.error_untrusted_certificate), duration);
                    toast.show();
                    mURLText.setError(getString(R.string.error_ssl));
                    mURLText.requestFocus();
                    break;
                case Account_Already_In_Use:
                    toast = Toast.makeText(getApplicationContext(),
                            R.string.error_account_already_in_use, duration);
                    toast.show();
                    mURLText.setError(getString(R.string.error_account_already_in_use));
                    mURLText.requestFocus();
                    break;
                default:
                    toast = Toast.makeText(getApplicationContext(), R.string.error_unkown_error,
                            duration);
                    toast.show();
                    mURLText.setError(getString(R.string.error_unkown_error));
                    mURLText.requestFocus();
                    break;
            }


        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}