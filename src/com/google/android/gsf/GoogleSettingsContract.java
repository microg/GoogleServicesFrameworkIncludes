package com.google.android.gsf;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public final class GoogleSettingsContract {

	public static final Uri CONTENT_URI = Uri.parse("content://com.google.settings");
	private static final String TAG = "GoogleSettingsContract";

	public static class NameValueTable implements BaseColumns {
		protected static boolean putString(final ContentResolver resolver, final Uri uri, final String name,
										   final String value) {
			Log.w(TAG, "Not yet implemented: NameValueTable.putString");
			return false;
		}
	}

	public static final class Partner extends NameValueTable {
		public static final Uri CONTENT_URI = Uri.withAppendedPath(GoogleSettingsContract.CONTENT_URI, "partner");

		public static int getInt(final ContentResolver resolver, final String name, final int defaultValue) {
			final String value = getString(resolver, name);
			if (value != null) {
				try {
					return Integer.parseInt(value);
				} catch (final Throwable t) {
					return defaultValue;
				}
			}
			return defaultValue;
		}

		public static long getLong(ContentResolver resolver, String name, int defaultValue) {
			final String value = getString(resolver, name);
			if (value != null) {
				try {
					return Long.parseLong(value);
				} catch (final Throwable t) {
					return defaultValue;
				}
			}
			return defaultValue;
		}

		public static String getString(ContentResolver r, String name, String defaultValue) {
			String value = getString(r, name);
			if (value != null) {
				return value;
			}
			return defaultValue;
		}

		public static String getString(final ContentResolver resolver, final String name) {
			Cursor c = resolver.query(Uri.withAppendedPath(CONTENT_URI, name), null, null, null, null);
			String value = null;
			if (c != null) {
				if (c.getCount() > 0) {
					c.moveToFirst();
					value = c.getString(c.getColumnIndex("value"));
				}
				c.close();
			}
			return value;
		}

		public static boolean putInt(final ContentResolver resolver, final String name, final int value) {
			return putString(resolver, name, Integer.toString(value));
		}

		public static boolean putString(final ContentResolver resolver, final String name, final String value) {
			ContentValues values = new ContentValues();
			values.put("name", name);
			values.put("value", value);
			return (resolver.insert(CONTENT_URI, values) != null);
		}
	}
}
