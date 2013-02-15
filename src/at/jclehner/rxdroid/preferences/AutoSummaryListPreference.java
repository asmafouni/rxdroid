package at.jclehner.rxdroid.preferences;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.util.Log;
import at.jclehner.rxdroid.util.CollectionUtils;

public class AutoSummaryListPreference extends ListPreference
{
	private static final String TAG = AutoSummaryListPreference.class.getName();

	public AutoSummaryListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected boolean callChangeListener(Object newValue)
	{
		if(super.callChangeListener(newValue))
		{
			setSummaryFromValue((String) newValue);
			return true;
		}

		return false;
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
	{
		super.onSetInitialValue(restoreValue, defaultValue);

		setSummaryFromValue(getValue());
	}

//	@Override
//	protected String getPersistedString(String defaultReturnValue)
//	{
//		final String value = super.getPersistedString(defaultReturnValue);
//		setSummaryFromValue(value);
//		return value;
//	}

	private void setSummaryFromValue(String value)
	{
		final CharSequence[] entries = getEntries();
		final CharSequence[] values = getEntryValues();

		int index = CollectionUtils.indexOf(value, values);
		if(index != -1 && index < entries.length)
			setSummary(entries[index]);
		else
			Log.d(TAG, "setSummaryFromValue: no entry for value " + value);
	}
}
