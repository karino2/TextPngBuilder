package com.livejournal.karino2.textpngbuilder;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
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

    final int ID_ACTION_FONT_PATH_PICK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadTextStyleFromPrefs();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_text_png_builder);

        EditText et = (EditText)findViewById(R.id.editText);
        et.requestFocus();


        ((Button)findViewById(R.id.buttonStyle)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStylePopup();
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        textStyle.saveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        textStyle.restoreInstanceState(savedInstanceState);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case ID_ACTION_FONT_PATH_PICK:
                if(resultCode == RESULT_OK)
                {
                    Uri uri = data.getData();
                    String path = null;
                    try {
                        path = getPath(uri);
                        setFontPath(path);
                        textStyle.setFontFamily(ComboBox.EXTERNAL_NAME);


                        if(stylePopup == null) {
                            showStylePopup();
                        }else {
                            if(styleView != null) {
                                ComboBox combo = (ComboBox)styleView.findViewById(R.id.fontfamily_combobox);
                                combo.setStringValue(ComboBox.EXTERNAL_NAME);
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        showMessage("Illegal font path: " + e.getMessage());
                   }

                }
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setFontPath(String path) {
        textStyle.setFontPath(new File(path));
        updateTextStyleToPopup();
    }

    private void updateTextStyleToPopup() {
        if(styleView != null) {
            ((CheckBox) styleView.findViewById(R.id.checkVertical)).setChecked(textStyle.isVertical());
            ComboBox combo = (ComboBox)styleView.findViewById(R.id.fontfamily_combobox);
            combo.setStringValue(textStyle.getFontFamily());

            NumberComboBox fsize = (NumberComboBox)styleView.findViewById(R.id.size_combobox);
            fsize.setIntegerValue(textStyle.getFontSize());
            ((EditText) styleView.findViewById(R.id.editTextFontPath)).setText(textStyle.getFontPathString());
        }
    }

    public String getPath(Uri uri)  {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { "_data" };
            Cursor cursor = null;

            try {
                cursor = getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                throw new IllegalArgumentException();
            }
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        throw new IllegalArgumentException();
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
        updatePopupStyleToParent();

        EditText et = (EditText)findViewById(R.id.editText);
        String text = et.getText().toString();
        List<String> strings = stringToStringList(text);

        handleDoneWithTextList(strings);
    }


    boolean preview = false;
    boolean isPreview() {
        return preview;
    }

    @Override
    protected void onStop() {
        super.onStop();
        closeStylePopup();
    }

    private void closeStylePopup() {
        if(stylePopup != null) {
            updatePopupStyleToParent();
            stylePopup.dismiss();
            stylePopup = null;
            styleView = null;
        }
    }

    TextStyle textStyle = new TextStyle();

    PopupWindow stylePopup;
    View styleView;

    private void showStylePopup()
    {
        if(stylePopup == null) {
            LayoutInflater inflater = getLayoutInflater();
            styleView = inflater.inflate(R.layout.popup_stylesetting, null);

            setupStylePopupView();


            // seems too early for onActivityResult case. delayed.
            findViewById(R.id.rootLayout).post(new Runnable() {
                @Override
                public void run() {
                    View root = findViewById(R.id.rootLayout);
                    stylePopup = new PopupWindow(styleView, root.getMeasuredWidth(), root.getMeasuredHeight() * 1 / 3, false);
                    stylePopup.setFocusable(true);
                    stylePopup.showAsDropDown(findViewById(R.id.buttonStyle));
                }
            });
        } else {
            stylePopup.showAsDropDown(findViewById(R.id.buttonStyle));
        }
    }

    private void setupStylePopupView() {
        NumberComboBox sizeBox = (NumberComboBox) styleView.findViewById(R.id.size_combobox);
        sizeBox.setIntegerValue(12);
        ComboBox fontFamily = (ComboBox) styleView.findViewById(R.id.fontfamily_combobox);
        fontFamily.setStringValue("Verdana, Roboto, sans-serif");

        ((Button) styleView.findViewById(R.id.buttonClose)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeStylePopup();
            }
        });


        ((Button) styleView.findViewById(R.id.buttonApply)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePreview();
            }
        });

        styleView.findViewById(R.id.buttonBrowseFontPath).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePopupStyleToParent();

                showMessage("Please choose font file.");
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);

                try {
                    startActivityForResult(
                            Intent.createChooser(intent, "Select Font file"),
                            ID_ACTION_FONT_PATH_PICK);
                } catch (android.content.ActivityNotFoundException ex) {
                    showMessage("No file manager found. Please install File Manager.");
                }
            }
        });

        updateTextStyleToPopup();
    }


    private void updatePopupStyleToParent() {
        if(styleView != null) {
            TextStyle newStyle = new TextStyle(
                    ((CheckBox) styleView.findViewById(R.id.checkVertical)).isChecked(),
                    getFontFamily(styleView),
                    getFontSize(styleView),
                    getFontPathFromPopup()
            );
            textStyle = newStyle;
        }
        saveTextStyle();
    }

    private File getFontPathFromPopup() {
        String path = ((EditText)styleView.findViewById(R.id.editTextFontPath)).getText().toString();
        if(path.equals(""))
            return null;
        return new File(path);
    }

    private void saveTextStyle() {
        textStyle.saveTo(getSharedPreferences("styleprefs", MODE_PRIVATE));
    }

    private void loadTextStyleFromPrefs() {
        textStyle.loadFrom(getSharedPreferences("styleprefs", MODE_PRIVATE));
    }


    private void handleDoneWithTextList(List<String> strings) {
        WebView webView = (WebView)findViewById(R.id.webView);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);

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



        String resultHtml = textStyle.buildHtml(strings);
        webView.loadDataWithBaseURL(null, resultHtml, "text/html", "UTF-8", null);
    }

    private String getFontFamily(View parent) {
        ComboBox combo = (ComboBox)parent.findViewById(R.id.fontfamily_combobox);
        // simple sanitize.
        return combo.getStringValue().replace("}", "");
    }

    private int getFontSize(View parent) {
        NumberComboBox number = (NumberComboBox)parent.findViewById(R.id.size_combobox);
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
