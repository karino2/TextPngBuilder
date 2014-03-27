package com.livejournal.karino2.textpngbuilder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.NumberKeyListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class ComboBox extends LinearLayout implements OnClickListener, android.view.View.OnClickListener {

    EditText mText;
    AlertDialog mPopup;

	public ComboBox(Context context) {
		this(context, null);
	}
	public ComboBox(Context context, AttributeSet attrs) {
		super(context, attrs);
	    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.combo_box, this, true);
        ((Button)findViewById(R.id.combo_expand_button)).setOnClickListener(this);
        
        mText = (EditText)findViewById(R.id.combo_edit);
	}


	// font-family: Verdana, Geneva, sans-serif;
	private String[] mCandidates = new String[] {"Verdana, Roboto, sans-serif",
            "TimesNewRoman, \"Times New Roman\", Times, Baskerville, Georgia, serif",
            "\"Courier New\", Courier, \"Lucida Sans Typewriter\", \"Lucida Typewriter\", monospace",
            "Rockwell, \"Courier Bold\", Courier, Georgia, Times, \"Times New Roman\", serif",   "Verdana, Roboto, 'Droid Sans', sans-serif"};

    public void setCandidates(String[] candidates) {
        mCandidates = candidates;
    }

	public void onClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        ArrayAdapter<String> adapter = 
        	new ArrayAdapter<String>(getContext(), R.layout.combo_item, getCandidates());
        mPopup = builder.setSingleChoiceItems(adapter, -1, this)
        .setCancelable(true).create();
        mPopup.setCanceledOnTouchOutside(true);
        mPopup.show();
 	}


	public void onClick(DialogInterface dialog, int which) {
		setSelection(which);
		dialog.dismiss();
	}
	private void setSelection(int which) {
		String newVal = getCandidates()[which];
		setStringValue(newVal);
	}
	public void setStringValue(String newVal) {
		mText.setText(newVal);
		mText.setSelection(newVal.length());
	}
    public String getStringValue() {
        return String.valueOf(mText.getText());
    }

    @Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		mText.setEnabled(enabled);
	}

    public String[] getCandidates() {
        return mCandidates;
    }

    public void setInputFilter(InputFilter inputFilter) {
        mText.setFilters(new InputFilter[] { inputFilter });
    }
}
