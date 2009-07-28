package com.evancharlton.magnatune.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.CompoundButton;

import com.evancharlton.magnatune.R;

public class ToggleImageButton extends CompoundButton {
	private int mImageOn = 0;
	private int mImageOff = 0;
	private CompoundButton.OnCheckedChangeListener mListener = null;

	public ToggleImageButton(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ToggleImageButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		super.setOnCheckedChangeListener(mChangeListener);
		TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.ToggleImageButton);
		mImageOn = arr.getResourceId(R.styleable.ToggleImageButton_imageOn, 0);
		mImageOff = arr.getResourceId(R.styleable.ToggleImageButton_imageOff, 0);

		setFocusable(true);
		setClickable(true);

		updateImage();
	}

	private void updateImage() {
		if (isChecked()) {
			setButtonDrawable(mImageOn);
		} else {
			setButtonDrawable(mImageOff);
		}
	}

	@Override
	public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
		mListener = listener;
	}

	private CompoundButton.OnCheckedChangeListener mChangeListener = new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			updateImage();
			if (mListener != null) {
				mListener.onCheckedChanged(buttonView, isChecked);
			}
		}
	};
}
