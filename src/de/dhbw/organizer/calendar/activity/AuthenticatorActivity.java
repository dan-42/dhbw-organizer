/**
 * LICENSE: GPL v3 
 * 
 * Copyright (c) 2013 by
 * Daniel Friedrich <friedrda@dhbw-loerrach.de>
 * Simon Riedinger <riedings@dhbw-loerrach.de>
 * Patrick Strittmatter <strittpa@dhbw-loerrach.de> 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3.0 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * @author friedrda
 */

package de.dhbw.organizer.calendar.activity;

import java.util.ArrayList;
import java.util.Collections;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import de.dhbw.organizer.R;
import de.dhbw.organizer.calendar.Constants;
import de.dhbw.organizer.calendar.objects.SpinnerItem;

/**
 * Activity which displays login screen to the user.
 */
public class AuthenticatorActivity extends AccountAuthenticatorActivity {
	
	private static final String TAG = "iCalAuthenticatorActivity";

	private static final String DEFAULT_PASSWORD = "DEADBEAF";	
	
	private AccountManager mAccountManager;

	private TextView mMessage;	

	private Spinner mIcalSpinner;

	ArrayList<SpinnerItem> itemList = null;

	/**
	 * {@inheritDoc}
	 * 
	 * @param mAvailableCalendars
	 */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		mAccountManager = AccountManager.get(this);

		requestWindowFeature(Window.FEATURE_LEFT_ICON);
		setContentView(R.layout.select_ical_activity);
		getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_calendar_main);

		mMessage = (TextView) findViewById(R.id.message);
		mMessage.setText(R.string.select_ical_activity_newaccount_text);

		mIcalSpinner = (Spinner) findViewById(R.id.ical_calendar_spinner);

		String[] calendars = getResources().getStringArray(R.array.calendars_array);

		itemList = new ArrayList<SpinnerItem>();

		for (int i = 0; i < calendars.length; i++) {
			itemList.add(new SpinnerItem(calendars[i]));
		}

		// sort
		Collections.sort(itemList);

		ArrayAdapter<SpinnerItem> adapter = new ArrayAdapter<SpinnerItem>(this, android.R.layout.simple_spinner_item, itemList);

		adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		mIcalSpinner.setAdapter(adapter);

	}

	/*
	 * {@inheritDoc}
	 */
	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		final ProgressDialog dialog = new ProgressDialog(this);

		dialog.setMessage(getText(R.string.ui_activity_authenticating));
		dialog.setIndeterminate(true);
		dialog.setCancelable(true);

		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				Log.i(TAG, "user cancelling authentication");
			}
		});
		return dialog;
	}

	/**
	 * Handles onClick event on the Submit button. Sends username/password to
	 * the server for authentication. The button is configured to call
	 * handleLogin() in the layout XML.
	 * 
	 * @param view
	 *            The Submit button for which this method is invoked
	 */
	public void handleLogin(View view) {
		SpinnerItem selected = (SpinnerItem) mIcalSpinner.getSelectedItem();

		String mICalUrl = selected.getValue();

		Log.i(TAG, "Selected : " + mICalUrl);
		Log.i(TAG, "finishLogin()");

		final Account account = new Account(mICalUrl, Constants.ACCOUNT_TYPE);

		mAccountManager.addAccountExplicitly(account, DEFAULT_PASSWORD, null);

		ContentResolver.setIsSyncable(account, CalendarContract.AUTHORITY, 1);
		ContentResolver.setSyncAutomatically(account, CalendarContract.AUTHORITY, true);
		ContentResolver.addPeriodicSync(account, CalendarContract.AUTHORITY, new Bundle(), Constants.SYNC_INTERVALL_IN_SEC);

		final Intent intent = new Intent();
		intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, mICalUrl);
		intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
		setAccountAuthenticatorResult(intent.getExtras());
		setResult(RESULT_OK, intent);
		finish();

	}

	public void onAuthenticationCancel() {
		Log.i(TAG, "onAuthenticationCancel()");
	}

}
