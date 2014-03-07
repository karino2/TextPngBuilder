package com.livejournal.karino2.textpngbuilder;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;

public class TextImage {
    public static final int REF_CHAR_SIZE = 20;

    private ArrayList<String> mLines;
    private Bitmap mBitmap = null;

    private int mCharSize = 10;
    private int mCharMargin = 0;
    private int mLineMargin = 0;

    public TextImage()
    {
        mLines = new ArrayList<String>();
    }

    public Bitmap bitmap()
    {
        return mBitmap;
    }

    public void setCharSize( int cs )
    {
        mCharSize = cs;
    }

    public void setCharMargin( int cm )
    {
        mCharMargin = cm;
    }

    public void setLineMargin( int lm )
    {
        mLineMargin = lm;
    }

    static public void setPaint( Paint paint, int charSize )
    {
        paint.setColor( 0xFF000000 );
        paint.setTextSize( charSize );
        paint.setAntiAlias( true );
    }

    static public Rect lineSize( String str, int charSize )
    {
        Paint paint = new Paint();
        setPaint( paint, charSize );

        FontMetrics fm = paint.getFontMetrics();
        float testWidth = paint.measureText( str );
        int testHeight = (int)(Math.abs( fm.bottom - fm.top ));

        return new Rect( 0,0, (int)(testWidth + 1), testHeight );
    }

    private Rect textRect( int charSize )
    {
        Rect r = new Rect();

        for (int i=0; i<mLines.size(); i++)
        {
            String line = mLines.get( i );
            Rect lineRect = lineSize( line, charSize );
            if (lineRect.width() > r.right) r.right = lineRect.width();
            r.bottom += lineRect.height();
        }

        return r;
    }

    public void autoCharSize()
    {
        if (mBitmap == null) return;

        Rect r = textRect( REF_CHAR_SIZE );
        float zx = (float)mBitmap.getWidth() / r.width();
        float zy = (float)mBitmap.getHeight() / r.height();
        if (zy < zx) zx = zy;

        mCharSize = (int)(zx * REF_CHAR_SIZE);
    }

    public void clearLines()
    {
        mLines.clear();
    }

    public void addLine( String str )
    {
        mLines.add( str );
    }

    public void resize( int width, int height )
    {
        if (mBitmap != null) mBitmap.recycle();
        mBitmap = Bitmap.createBitmap( width, height, Config.ARGB_8888 );
    }

    public void rasterlize()
    {
        mBitmap.eraseColor( 0xFFFFFFFF );

        Paint paint = new Paint();
        setPaint( paint, mCharSize );

        Rect rect = textRect( mCharSize );

        Canvas c = new Canvas( mBitmap );
        for (int i=0; i<mLines.size(); i++)
        {
            String str = mLines.get( i );

            // センタリング
            float w = paint.measureText( str );
            int dx = mBitmap.getWidth()/2 - (int)(w/2);
            int dy = mCharSize*(i + 1);
            dy += (mBitmap.getHeight() - rect.height())/2;

            c.drawText( str, dx, dy, paint );
        }

        // 輝度を不透明度に
        int w = mBitmap.getWidth();
        int h = mBitmap.getHeight();
        for (int j=0; j<h; j++)
        {
            for (int i=0; i<w; i++)
            {
                int col = mBitmap.getPixel( i, j );
                if (col == 0xFFFFFFFF)
                {
                    mBitmap.setPixel( i, j, 0 );
                    continue;
                }

                int a = 255 - (col & 0x000000FF);
                col = (a << 24);
                mBitmap.setPixel( i, j, col );
            }
        }
    }

    public void setTestString()
    {
        clearLines();
        addLine( "ハピネスチャージ" );
        addLine( "pretty cure" );
        addLine( "は面白い" );
        addLine( "1" );
        addLine( "2" );
        addLine( "3" );
    }
}
