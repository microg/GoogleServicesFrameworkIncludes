package com.google.android.gsf;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

public class AccountHandler {
	public static final String ACCOUNT_TYPE = "com.google";

	public static Account findAccount(String name, Context context) {
		if (name != null && !name.isEmpty()) {
			Account[] accounts = getAccounts(context);
			for (Account account : accounts) {
				if (name.equalsIgnoreCase(account.name)) {
					return account;
				}
			}
		}
		return null;
	}

	public static Account[] getAccounts(Context context) {
		if (context == null) {
			return new Account[0];
		}
		return AccountManager.get(context).getAccountsByType(ACCOUNT_TYPE);
	}

	public static Account getBestAccount(String name, Context context) {
		if (hasAccount(name, context)) {
			return findAccount(name, context);
		}
		return getFirstAccount(context);
	}

	public static Account getFirstAccount(Context context) {
		Account[] accounts = getAccounts(context);
		if (accounts.length == 0) {
			return null;
		}
		return accounts[0];
	}

	public static boolean hasAccount(String name, Context context) {
		return (findAccount(name, context) != null);
	}
}
