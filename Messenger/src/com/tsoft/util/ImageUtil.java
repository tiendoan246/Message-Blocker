package com.tsoft.util;

import com.tsoft.messenger.R;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextPaint;

public class ImageUtil {

	/** The number of available tile colors (see R.array.letter_tile_colors) */
	private static final int NUM_OF_TILE_COLORS = 8;

	/** The {@link TextPaint} used to draw the letter onto the tile */
	private final TextPaint mPaint = new TextPaint();
	/** The bounds that enclose the letter */
	private final Rect mBounds = new Rect();
	/** The {@link Canvas} to draw on */
	private final Canvas mCanvas = new Canvas();
	/** The first char of the name being displayed */
	private final char[] mFirstChar = new char[1];

	/** The background colors of the tile */
	private final TypedArray mColors;
	/** The font size used to display the letter */
	private final int mTileLetterFontSize;
	/** The default image to display */
	private final Bitmap mDefaultBitmap;

	/**
	 * Constructor for <code>LetterTileProvider</code>
	 * 
	 * @param context
	 *            The {@link Context} to use
	 */
	public ImageUtil(Context context) {
		final Resources res = context.getResources();

		mPaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
		mPaint.setColor(Color.WHITE);
		mPaint.setTextAlign(Align.CENTER);
		mPaint.setAntiAlias(true);

		mColors = res.obtainTypedArray(R.array.letter_tile_colors);
		mTileLetterFontSize = res
				.getDimensionPixelSize(R.dimen.tile_letter_font_size);

		mDefaultBitmap = BitmapFactory.decodeResource(res,
				android.R.drawable.sym_def_app_icon);
	}

	public Bitmap getLetterTile(String displayName, String key, int width,
			int height) {
		final Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		final char firstChar = displayName.charAt(0);

		final Canvas c = mCanvas;
		c.setBitmap(bitmap);
		c.drawColor(pickColor(key));

		if (isEnglishLetterOrDigit(firstChar)) {
			mFirstChar[0] = Character.toUpperCase(firstChar);
			mPaint.setTextSize(mTileLetterFontSize);
			mPaint.getTextBounds(mFirstChar, 0, 1, mBounds);
			c.drawText(mFirstChar, 0, 1, 0 + width / 2, 0 + height / 2
					+ (mBounds.bottom - mBounds.top) / 2, mPaint);
		} else {
			c.drawBitmap(mDefaultBitmap, 0, 0, null);
		}
		return bitmap;
	}

	private static boolean isEnglishLetterOrDigit(char c) {
		return 'A' <= c && c <= 'Z' || 'a' <= c && c <= 'z' || '0' <= c
				&& c <= '9';
	}

	private int pickColor(String key) {
		// String.hashCode() is not supposed to change across java versions, so
		// this should guarantee the same key always maps to the same color
		final int color = Math.abs(key.hashCode()) % NUM_OF_TILE_COLORS;
		try {
			return mColors.getColor(color, Color.BLACK);
		} finally {
			mColors.recycle();
		}
	}

	public static Bitmap getRoundedShape(Bitmap scaleBitmapImage) {
		int targetWidth = 50;
		int targetHeight = 50;
		Bitmap targetBitmap = Bitmap.createBitmap(targetWidth, targetHeight,
				Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(targetBitmap);
		Path path = new Path();
		path.addCircle(((float) targetWidth - 1) / 2,
				((float) targetHeight - 1) / 2,
				(Math.min(((float) targetWidth), ((float) targetHeight)) / 2),
				Path.Direction.CCW);

		canvas.clipPath(path);
		Bitmap sourceBitmap = scaleBitmapImage;
		canvas.drawBitmap(sourceBitmap, new Rect(0, 0, sourceBitmap.getWidth(),
				sourceBitmap.getHeight()), new Rect(0, 0, targetWidth,
				targetHeight), null);
		return targetBitmap;
	}

}
