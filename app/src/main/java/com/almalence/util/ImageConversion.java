/*
The contents of this file are subject to the Mozilla Public License
Version 1.1 (the "License"); you may not use this file except in
compliance with the License. You may obtain a copy of the License at
http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS"
basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
License for the specific language governing rights and limitations
under the License.

The Original Code is collection of files collectively known as Open Camera.

The Initial Developer of the Original Code is Almalence Inc.
Portions created by Initial Developer are Copyright (C) 2013 
by Almalence Inc. All Rights Reserved.
 */

package com.almalence.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.view.Display;
import android.view.WindowManager;
import com.almalence.opencam.MainScreen;

public class ImageConversion
{
	public static native int JpegConvert(byte[] in, int sx, int sy, boolean rotate, boolean mirrored, int rotationDegree);
	public static native int JpegConvertN(int in, int length, int sx, int sy, boolean rotate, boolean mirrored, int rotationDegree);

	public static native void sumByteArraysNV21(byte[] data1, byte[] data2, byte[] out, int width, int height);

	public static native void TransformNV21(byte[] InPic, byte[] OutPic, int sx, int sy, int flipLR, int flipUD,
			int rotate90);

	public static native void TransformNV21N(int InPic, int OutPic, int sx, int sy, int flipLR, int flipUD, int rotate90);

	public static native void convertNV21toGL(byte[] ain, byte[] aout, int width, int height, int outWidth,
			int outHeight);
	public static native void convertNV21toGLN(int ain, byte[] aout, int width, int height, int outWidth,
			int outHeight);
	
	public static native void addCornersRGBA8888(byte[] rgb_out, int outWidth, int outHeight);

	
	static
	{
		System.loadLibrary("utils-image");
		System.loadLibrary("utils-jni");
	}

	public static native void resizeJpeg2RGBA(int jpeg, int jpeg_length, byte[] rgb_out,
			int inHeight, int inWidth, int outWidth, int outHeight, boolean mirror);

	/**
	 * Lets use this method to check pointers for NULL. Maybe move it to
	 * somewhere else?
	 */
	private static int checkPtr(final int ptr)
	{
		if (ptr == 0)
		{
			throw new OutOfMemoryError();
		}

		return ptr;
	}

	public static Bitmap decodeJPEGfromBuffer(byte[] data)
	{
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Config.ARGB_8888;
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(data, 0, data.length, options);

		Display display = ((WindowManager) MainScreen.getInstance().getSystemService(Context.WINDOW_SERVICE))
				.getDefaultDisplay();
		int mDisplayWidth = display.getHeight();
		int mDisplayHeight = display.getWidth();

		float widthScale = (float) options.outWidth / (float) mDisplayWidth;
		float heightScale = (float) options.outHeight / (float) mDisplayHeight;
		float scale = widthScale > heightScale ? widthScale : heightScale;
		float imageRatio = (float) options.outWidth / (float) options.outHeight;
		float displayRatio = (float) mDisplayWidth / (float) mDisplayHeight;

		Bitmap bitmap = null;

		if (scale >= 8)
		{
			options.inSampleSize = 8;
		} else if (scale >= 4)
		{
			options.inSampleSize = 4;
		} else if (scale >= 2)
		{
			options.inSampleSize = 2;
		} else
		{
			options.inSampleSize = 1;
		}

		options.inJustDecodeBounds = false;

		Bitmap tempBitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);

		if (imageRatio > displayRatio)
		{
			bitmap = Bitmap.createScaledBitmap(tempBitmap, mDisplayWidth, (int) (mDisplayWidth / displayRatio), true);
		} else
		{
			bitmap = Bitmap.createScaledBitmap(tempBitmap, (int) (mDisplayHeight * imageRatio), mDisplayHeight, true);
		}

		if (bitmap != tempBitmap)
			tempBitmap.recycle();
		return bitmap;
	}

}
