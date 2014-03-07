package com.livejournal.karino2.textpngbuilder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;

/**
 * Created by karino on 3/7/14.
 */
public class TextImageView extends View {
    TextImage textImage = new TextImage();
    Point pos;
    Bitmap resultBitmap;

    public TextImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(0xFFFFFFFF);
        if(resultBitmap != null) {
            canvas.drawBitmap(resultBitmap, pos.x, pos.y, null);
        }
    }


    public Bitmap getResultBitmap() {
        return resultBitmap;
    }

    PopupWindow popupWindow;
    View popupRootView;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(popupRootView == null) {
                    pos = new Point(40, 40);
                    textImage.resize(600, 400);
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    popupRootView = inflater.inflate(R.layout.popup_text, null);
                    Button button = (Button)popupRootView.findViewById(R.id.buttonOK);
                    button.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EditText et = (EditText)popupRootView.findViewById(R.id.editText);
                            String text = et.getText().toString();
                            int lastEol = 0;
                            while(text.length() > lastEol) {
                                int eol = text.indexOf('\n', lastEol);
                                if(eol == -1) {
                                    textImage.addLine(text.substring(lastEol));
                                    break;
                                }
                                textImage.addLine(text.substring(lastEol, eol));
                                lastEol = eol+1;
                            }
                            textImage.autoCharSize();
                            textImage.rasterlize();
                            resultBitmap = textImage.bitmap();
                            popupWindow.dismiss();
                        }
                    });
                    popupWindow = new PopupWindow(popupRootView, 600, 400, true);
                }
                popupWindow.showAtLocation(this, Gravity.CENTER, 0, 0);
                return true;
        }
        return super.onTouchEvent(event);
    }
}
