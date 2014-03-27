package com.livejournal.karino2.textpngbuilder;

import android.content.Context;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.NumberKeyListener;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by karino on 3/28/14.
 */
public class NumberComboBox extends ComboBox {

    private final InputFilter mNumberInputFilter;
    private static final char[] DIGIT_CHARACTERS = new char[] {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    };

    private String[] candidates = new String[] {"12", "24", "48", "64", "128", "256"};

    @Override
    public String[] getCandidates() { return candidates; }

    public NumberComboBox(Context context, AttributeSet attrs) {
        super(context, attrs);

        mText.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        mNumberInputFilter = new NumberKeyListener() {

            public int getInputType() {
                return InputType.TYPE_CLASS_NUMBER;
            }

            @Override
            protected char[] getAcceptedChars() {
                return DIGIT_CHARACTERS;
            }
        };
        InputFilter inputFilter = mNumberInputFilter;
        setInputFilter(inputFilter);

        TextView tv = (TextView)findViewById(R.id.combo_label);
        tv.setVisibility(VISIBLE);
        tv.setText("pt");
    }


    public int getIntegerValue() {
        return Integer.parseInt(getStringValue());
    }

    public void setIntegerValue(int newVal) {
        setStringValue(Integer.toString(newVal));
    }
}
