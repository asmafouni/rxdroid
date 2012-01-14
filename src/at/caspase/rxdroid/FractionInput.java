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

package at.caspase.rxdroid;

import android.content.Context;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import at.caspase.androidutils.StateSaver;
import at.caspase.androidutils.StateSaver.SaveState;

import com.quietlycoding.android.picker.NumberPicker;

/**
 * A widget for fraction input.
 *
 * This implementation basically uses the private NumberPicker implementation
 * available in the pre-Honeycomb sources.
 *
 * @author Joseph Lehner
 */
public class FractionInput extends LinearLayout implements NumberPicker.OnChangedListener, OnClickListener
{
	public interface OnChangedListener
	{
		public void onFractionChanged(FractionInput widget, Fraction oldValue);
	}

	private static final String TAG = FractionInput.class.getName();
	private static final int MAX = 99999;

	private static final boolean LOGV = true;

	public static final int MODE_INTEGER = 1;
	public static final int MODE_MIXED = 2;
	public static final int MODE_FRACTION = 3;
	public static final int MODE_INVALID = 4;

	private NumberPicker mIntegerPicker;
	private NumberPicker mNumeratorPicker;
	private NumberPicker mDenominatorPicker;
	private TextView mFractionBar;
	private Button mModeSwitcher;

	@SaveState
	private int mInteger = 0;
	@SaveState
	private int mNumerator = 0;
	@SaveState
	private int mDenominator = 1;

	@SaveState
	private int mFractionInputMode = MODE_INVALID;
	@SaveState
	private boolean mIsAutoInputModeEnabled = false;

	private OnChangedListener mListener;

	public FractionInput(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		LayoutInflater lf = LayoutInflater.from(context);
		lf.inflate(R.layout.fraction_input2, this, true);

		mIntegerPicker = (NumberPicker) findViewById(R.id.integer);
		mNumeratorPicker = (NumberPicker) findViewById(R.id.numerator);
		mDenominatorPicker = (NumberPicker) findViewById(R.id.denominator);
		mFractionBar = (TextView) findViewById(R.id.fraction_bar);
		mModeSwitcher = (Button) findViewById(R.id.mode_switcher);

		mIntegerPicker.setOnChangeListener(this);
		mIntegerPicker.setRange(0, MAX);
		mIntegerPicker.setWrap(false);

		mNumeratorPicker.setOnChangeListener(this);
		mNumeratorPicker.setRange(0, MAX);
		mNumeratorPicker.setWrap(false);

		mDenominatorPicker.setOnChangeListener(this);
		mDenominatorPicker.setRange(1, MAX);
		mDenominatorPicker.setWrap(false);

		mModeSwitcher.setOnClickListener(this);

		setOrientation(HORIZONTAL);
		setGravity(Gravity.CENTER_HORIZONTAL);
		setFractionInputMode(MODE_FRACTION);

		updateView();
	}

	public void setValue(Fraction value)
	{
		if(mIsAutoInputModeEnabled)
		{
			final int mode;

			if(value.isInteger())
				mode = MODE_INTEGER;
			else if(Double.compare(value.doubleValue(), 1.0) == 1)
				mode = MODE_MIXED;
			else
				mode = MODE_FRACTION;

			if(LOGV) Log.v(TAG, "setValue: mode " + mFractionInputMode + " -> " + mode + " (auto)");

			if(mode != mFractionInputMode)
			{
				if(LOGV) Log.v(TAG, "setValue: mode " + mFractionInputMode + " -> " + mode + " (auto)");
				mFractionInputMode = mode;
			}
		}
		else
			if(LOGV) Log.v(TAG, "setValue: mode=" + mFractionInputMode);

		// for MODE_INTEGER and MODE_MIXED get the value as a mixed number
		int data[] = value.getFractionData(mFractionInputMode != MODE_FRACTION);

		mInteger = mFractionInputMode == MODE_FRACTION ? 0 : data[0];
		mNumerator = data[1];
		mDenominator = data[2];

		updateView();
	}

	public Fraction getValue() {
		return new Fraction(mInteger, mNumerator, mDenominator);
	}

	/**
	 * Sets the widget's input mode.
	 * <p>
	 * Valid input modes are MODE_INTEGER, MODE_FRACTION and MODE_MIXED. The mode set
	 * determines which number picker widgets are visible. Note that a call to
	 * <code>setMode(MODE_INTEGER)</code> is ignored if the widget's underlying value
	 * cannot be converted to an integer.
	 * <p>
	 * Note that automatic fraction intput mode guessing (see {@link #setAutoInputModeEnabled(boolean)})
	 * will be disabled, if a call to this function succeeds (i.e. returns <code>true</code>).
	 *
	 * @param mode either MODE_INTEGER, MODE_FRACTION or MODE_MIXED
	 * @return <code>false</code> if mode is MODE_INTEGER but the underlying value is
	 *         not an integer. For other arguments, this function always returns <code>true</code>.
	 */
	public boolean setFractionInputMode(int mode)
	{
		if(mode == MODE_INTEGER)
		{
			if(!getValue().isInteger())
				return false;
		}
		else if(mode == MODE_INVALID)
			throw new IllegalArgumentException();

		// an explicit request for a specific mode overrides the automatic setting
		setAutoInputModeEnabled(false);

		if(mode != mFractionInputMode)
		{
			mFractionInputMode = mode;
			setValue(getValue());
		}

		return true;
	}

	public int getFractionInputMode() {
		return mFractionInputMode;
	}

	/**
	 * Enables automatic input mode setting.
	 * <p>
	 * If enabled, {@link #setValue(Fraction)} will try to guess the most
	 * appropriate input mode for that value.
	 * <p>
	 * Note that later calls to {@link #setFractionInputMode(int)} will override
	 * this setting.
	 *
	 * @param enabled
	 */
	public void setAutoInputModeEnabled(boolean enabled)
	{
		mIsAutoInputModeEnabled = enabled;
		if(enabled)
			setValue(getValue());
	}

	public boolean isAutoInputModeEnabled(boolean enabled) {
		return mIsAutoInputModeEnabled;
	}

	public OnChangedListener getOnChangeListener() {
		return mListener;
	}

	public void setOnChangeListener(OnChangedListener listener) {
		mListener = listener;
	}

	@Override
	public void onChanged(NumberPicker picker, int oldVal, int newVal)
	{
		Fraction oldValue = getValue();

		if(picker.getId() == R.id.integer)
			mInteger = newVal;
		else if(picker.getId() == R.id.numerator)
			mNumerator = newVal;
		else if(picker.getId() == R.id.denominator)
		{
			if(newVal > 0)
				mDenominator = newVal;
			else // this shouldn't happen
				mDenominator = 1;
		}
		else
			return;

		if(mListener != null)
			mListener.onFractionChanged(this, oldValue);
	}

	@Override
	public void onClick(View v)
	{
		if(v.getId() == R.id.mode_switcher)
		{
			if(mFractionInputMode == MODE_INTEGER)
				setFractionInputMode(MODE_FRACTION);
		}
	}

	@Override
	protected Parcelable onSaveInstanceState()
	{
		Parcelable superState = super.onSaveInstanceState();
		return StateSaver.createInstanceState(this, superState, null);
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state)
	{
		Parcelable superState = StateSaver.getSuperState(state);
		super.onRestoreInstanceState(superState);
		StateSaver.restoreInstanceState(this, state);

		updateView();
	}

	private void updateView()
	{
		Log.d(TAG, "updateView: mFractionInputMode=" + mFractionInputMode);

		// hide in fraction mode
		mIntegerPicker.setVisibility(mFractionInputMode == MODE_FRACTION ? GONE : VISIBLE);
		// hide in integer mode
		mNumeratorPicker.setVisibility(mFractionInputMode == MODE_INTEGER ? GONE: VISIBLE);
		mDenominatorPicker.setVisibility(mFractionInputMode == MODE_INTEGER ? GONE: VISIBLE);
		mFractionBar.setVisibility(mFractionInputMode == MODE_INTEGER ? GONE: VISIBLE);
		// show in integer mode
		mModeSwitcher.setVisibility(mFractionInputMode == MODE_INTEGER ? VISIBLE : GONE);

		mIntegerPicker.setCurrent(mInteger);
		mNumeratorPicker.setCurrent(mNumerator);
		mDenominatorPicker.setCurrent(mDenominator);
	}
}