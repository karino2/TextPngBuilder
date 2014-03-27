package com.livejournal.karino2.textpngbuilder;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Picture;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TextPngBuilderActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_png_builder);


        NumberComboBox sizeBox = (NumberComboBox)findViewById(R.id.size_combobox);
        sizeBox.setIntegerValue(12);
        sizeBox.findViewById(R.id.combo_edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hidePopupWindow();
            }
        });
        ComboBox fontFamily = (ComboBox) findViewById(R.id.fontfamily_combobox);
        fontFamily.setStringValue("Verdana, Roboto, sans-serif");
        fontFamily.findViewById(R.id.combo_edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hidePopupWindow();
            }
        });

        EditText et = (EditText)findViewById(R.id.editText);
        et.requestFocus();
        et.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus)
                    hidePopupWindow();
            }
        });
        et.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hidePopupWindow();
            }
        });

    }

    private void hidePopupWindow() {
        if(popupWindow != null)
            popupWindow.dismiss();
    }

    private List<String> stringToStringList(String text) {
        ArrayList<String> list = new ArrayList<String>();
        int lastEol = 0;
        while(text.length() > lastEol) {
            int eol = text.indexOf('\n', lastEol);
            if(eol == -1) {
                list.add(text.substring(lastEol));
                return list;
            }
            list.add(text.substring(lastEol, eol));
            lastEol = eol+1;
        }
        return list;
    }

    private void handleDone() {
        preview = false;
        handleDoneOrPreview();
    }

    private void handleDoneOrPreview() {
        EditText et = (EditText)findViewById(R.id.editText);
        String text = et.getText().toString();
        List<String> strings = stringToStringList(text);

        if(isChecked(R.id.checkVertical)) {
            handleDoneWithVerticalTextList(strings);
        }else {
            handleDoneWithHorizontalTextList(strings);
        }
    }

    private void handleDoneWithHorizontalTextList(List<String> strings) {
        handleDoneWithTextList(strings, false);
    }


    boolean preview = false;
    boolean isPreview() {
        return preview;
        // return isChecked(R.id.checkTest);
    }

    private boolean isChecked(int resId) {
        CheckBox box = (CheckBox)findViewById(resId);
        return box.isChecked();
    }

    PopupWindow popupWindow;
    View popupView;
    private void handleDoneWithVerticalTextList(List<String> strings) {
        handleDoneWithTextList(strings, true);


}

    private void handleDoneWithTextList(List<String> strings, boolean isVertical) {
        if(popupView == null) {
            LayoutInflater inflater = getLayoutInflater();
            popupView = inflater.inflate(R.layout.popup_webview, null);
            WebView webView = (WebView)popupView.findViewById(R.id.webView);

            webView.setPictureListener(new WebView.PictureListener() {
                @Override
                public void onNewPicture(WebView view, Picture picture) {
                    if(!isPreview()) {
                        Picture pictureObj = view.capturePicture();

                        Bitmap bitmap = Bitmap.createBitmap(
                                pictureObj.getWidth(),
                                pictureObj.getHeight(),
                                Bitmap.Config.ARGB_8888);

                        Canvas canvas = new Canvas(bitmap);
                        pictureObj.draw(canvas);
                        whiteToTransparent(bitmap);

                        handleDoneWithBitmap(bitmap);
                    }
                }
            });

            View editText = findViewById(R.id.editText);
            popupWindow = new PopupWindow(popupView,editText.getMeasuredWidth(), editText.getMeasuredHeight()*3/4, false);
//            popupWindow = new PopupWindow(popupView, 600, 400, false);
            popupWindow.showAtLocation(findViewById(R.id.editText), Gravity.BOTTOM, 0, 0);
        } else {
            popupWindow.showAtLocation(findViewById(R.id.editText), Gravity.BOTTOM, 0, 0);
        }

        WebView webView = (WebView)popupView.findViewById(R.id.webView);
        webView.getSettings().setUseWideViewPort(true);
        StringBuilder builder = new StringBuilder();
        builder.append("<html><head><style>");
        builder.append("body { ");
        if(isVertical)
            builder.append("-webkit-writing-mode: vertical-rl;");

        builder.append("font-size: ");
        builder.append(String.valueOf(getFontSize()));
        builder.append("pt; ");

        // font-family: "Bodoni MT", Didot, "Didot LT STD", "Hoefler Text", Garamond, "Times New Roman", serif;
        builder.append("font-family: ");
        builder.append(getFontFamily());
        builder.append(";");

        builder.append("}");
        builder.append("</style></head><body><pre>");
        for(String line : strings) {
            builder.append(escapeHtml(line));
            builder.append("<br>");
        }
        builder.append("</pre></body></html>");


        webView.loadData(builder.toString(), "text/html; charset=UTF-8", null);
    }

    private String getFontFamily() {
        ComboBox combo = (ComboBox)findViewById(R.id.fontfamily_combobox);
        // simple sanitize.
        return combo.getStringValue().replace("}", "");
    }

    private int getFontSize() {
        NumberComboBox number = (NumberComboBox)findViewById(R.id.size_combobox);
        try {
            return number.getIntegerValue();
        }catch(NumberFormatException e) {
            return 12; // default;
        }

    }

    private void whiteToTransparent(Bitmap bitmap) {
        int[] buf = new int[bitmap.getWidth()*bitmap.getHeight()];
        bitmap.getPixels(buf, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        for(int i = 0; i < bitmap.getWidth()*bitmap.getHeight(); i++) {
            if(buf[i] == Color.WHITE) {
                buf[i] = Color.TRANSPARENT;
            }
        }
        bitmap.setPixels(buf, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
    }

    private String escapeHtml(String line) {
        return line.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.text_png_builder, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id) {
            case R.id.action_done:
                handleDone();
                return true;
            case R.id.action_preview:
                handlePreview();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void handlePreview() {
        preview = true;
        handleDoneOrPreview();
    }

    private boolean handleDoneWithBitmap(Bitmap bitmap) {
        hidePopupWindow();
        if(bitmap == null) {
            showMessage("No text set.");
            setResult(Activity.RESULT_CANCELED);
            finish();
            return true;
        }
        File file = null;
        try {
            file = saveBitmap(bitmap);
        } catch (IOException e) {
            showMessage("Fail to save: " + e.getMessage());
            setResult(Activity.RESULT_CANCELED);
            finish();
            return true;
        }
        Uri uri = putFileToContentDB(file);
        Intent intent = new Intent();
        intent.setData(uri);
        setResult(Activity.RESULT_OK, intent);
        finish();

        return true;
    }


    public void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG ).show();
    }


    private Uri putFileToContentDB(File file) {
        ContentResolver resolver = getBaseContext().getContentResolver();

        ContentValues content = new ContentValues(4);

        content.put(MediaStore.Images.ImageColumns.TITLE, "Serif");
        content.put(MediaStore.Images.ImageColumns.DATE_ADDED,
                System.currentTimeMillis() / 1000);
        content.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        content.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
        return resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, content);
    }


    private File saveBitmap(Bitmap screen) throws IOException {
        File dir = getFileStoreDirectory();
        File result = new File(dir, newFileName());

        OutputStream stream = new FileOutputStream(result);
        screen.compress(Bitmap.CompressFormat.PNG, 80, stream);
        stream.close();
        return result;
    }

    public static String newFileName() {
        SimpleDateFormat timeStampFormat = new SimpleDateFormat("yyyyMMddHHmmssSS");
        String filename = timeStampFormat.format(new Date()) + ".png";
        return filename;
    }

    public static void ensureDirExist(File dir) throws IOException {
        if(!dir.exists()) {
            if(!dir.mkdir()){
                throw new IOException();
            }
        }
    }
    public static File getFileStoreDirectory() throws IOException {
        File dir = new File(Environment.getExternalStorageDirectory(), "TextPngBuilder");
        ensureDirExist(dir);
        return dir;
    }

}
