/**
 * Copyright (C) 2011 Joseph Lehner <joseph.c.lehner@gmail.com>
 *
 * This file is part of RxDroid.
 *
 * RxDroid is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RxDroid is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RxDroid.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package at.caspase.rxdroid.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import android.content.Context;
import android.preference.ListPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TimePicker;
import at.caspase.rxdroid.DumbTime;
import at.caspase.rxdroid.R;
import at.caspase.rxdroid.db.Drug;

public final class Util
{
	private static final String TAG = Util.class.getName();

	public static int getDoseTimeDrawableFromDoseViewId(int doseViewId)
	{
		switch(doseViewId)
		{
				case R.id.morning:
						return R.drawable.ic_morning;
				case R.id.noon:
						return R.drawable.ic_noon;
				case R.id.evening:
						return R.drawable.ic_evening;
				case R.id.night:
						return R.drawable.ic_night;
		}

		throw new IllegalArgumentException();
	}

	public static int getDoseTimeDrawableFromDoseTime(int doseTime)
	{
		return getDoseTimeDrawableFromDoseViewId(Constants.getDoseViewId(doseTime));
	}

	public static int getDoseTimeFromDoseViewId(int doseViewId)
	{
		switch(doseViewId)
		{
				case R.id.morning:
						return Drug.TIME_MORNING;
				case R.id.noon:
						return Drug.TIME_NOON;
				case R.id.evening:
						return Drug.TIME_EVENING;
				case R.id.night:
						return Drug.TIME_NIGHT;
		}

		throw new IllegalArgumentException();
	}

	/**
	 * Obtains a string attribute from an AttributeSet.
	 * <p>
	 * Note that this function automatically resolves string references.
	 *
	 * @param context The context
	 * @param attrs An AttributeSet to query
	 * @param namespace The attribute's namespace (in the form of <code>http://schemas.android.com/apk/res/&lt;package&gt;</code>)
	 * @param attribute The name of the attribute to query
	 * @param defaultValue A default value, in case there's no such attribute
	 * @return The attribute's value, or <code>null</code> if it does not exist
	 */
	public static String getStringAttribute(Context context, AttributeSet attrs, String namespace, String attribute, String defaultValue)
	{
		int resId = attrs.getAttributeResourceValue(namespace, attribute, -1);
		String value;

		if(resId == -1)
			value = attrs.getAttributeValue(namespace, attribute);
		else
			value = context.getString(resId);

		return value == null ? defaultValue : value;
	}

	public static<T> int toInteger(T t)
	{
		if(t == null)
			throw new NullPointerException();

		final String str = t.toString();

		if(str.length() == 0)
			return 0;

		return Integer.parseInt(str, 10);
	}


	public static void populateListPreferenceEntryValues(Preference preference)
	{
		ListPreference pref = (ListPreference) preference;
		int entryCount = pref.getEntries().length;

		String[] values = new String[entryCount];
		for(int i = 0; i != entryCount; ++i)
			values[i] = Integer.toString(i);

		pref.setEntryValues(values);
	}

	public static String millis(long millis)
	{
		return millis + "ms (" + new DumbTime(millis, true).toString(true) + ")";
	}

	public static boolean equals(Object a, Object b)
	{
		if(a == null && b == null)
			return true;
		else if(a != null)
			return a.equals(b);

		return b.equals(a);
	}

	/**
	 * Gets a named id from <code>android.R.style</code>.
	 * <p>
	 * With the help of this function, you can use style resources only
	 * available in later versions of android than the one you're developing
	 * for.
	 *
	 * @param resIdFieldName The name of the resource id (e.g. "textAppearanceLarge").
	 * @param defaultResId The resource id to return if the requested resource does not exist.
	 * @return The resource id of the requested name, or the supplied default value.
	 */

	public static int getStyleResId(String resIdFieldName, int defaultResId)
	{
		try
		{
			Field f = android.R.style.class.getField(resIdFieldName);
			return f.getInt(null);
		}
		catch(IllegalAccessException e)
		{
			// eat exception
		}
		catch(SecurityException e)
		{
			// eat exception
		}
		catch(NoSuchFieldException e)
		{
			// eat exception
		}

		Log.i(TAG, "getAppearance: inaccessible field in android.R.style: " + resIdFieldName);
		return defaultResId;
	}

	/**
	 * Returns an array index for the given weekday.
	 *
	 * @param weekday a weekday as found in Calendar's DAY_OF_WEEK field.
	 * @return <code>0</code> for Monday, <code>1</code> for Tuesday, etc.
	 */
	public static int calWeekdayToIndex(int weekday) {
		return CollectionUtils.indexOf(weekday, Constants.WEEK_DAYS);
	}

	public static void dumpObjectMembers(String tag, int priority, Object object, String name)
	{
		final Class<?> clazz = object.getClass();
		final StringBuilder sb = new StringBuilder();
		sb.append("dumpObjectMembers: (" + clazz.getSimpleName() + ") " + name + "\n");

		for(Field f : clazz.getDeclaredFields())
		{
			int m = f.getModifiers();

			if(Modifier.isStatic(m))
				continue;

			sb.append("  (" + f.getType().getSimpleName() + ") " + f.getName() + "=");

			try
			{
				Reflect.makeAccessible(f);

				if(f.getType().isArray())
				{
					try
					{
						sb.append(Arrays.toString((Object[]) f.get(object)));
					}
					catch(ClassCastException e)
					{
						sb.append(f.get(object));
					}
				}
				else
					sb.append(f.get(object));
			}
			catch(IllegalArgumentException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch(IllegalAccessException e)
			{
				sb.append("(inaccessible)");
			}

			sb.append("\n");
		}

		sb.append("----------\n");

		Log.println(priority, tag, sb.toString());
	}

	public static void sleepAtMost(long millis)
	{
		try
		{
			Thread.sleep(millis);
		}
		catch(InterruptedException e)
		{
			// ignore
		}
	}

	public static void setTimePickerTime(TimePicker picker, DumbTime time)
	{
		picker.setCurrentHour(time.getHours());
		picker.setCurrentMinute(time.getMinutes());
	}
}
