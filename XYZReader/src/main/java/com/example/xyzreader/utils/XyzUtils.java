package com.example.xyzreader.utils;

import android.database.Cursor;
import android.util.Log;

import com.example.xyzreader.data.ArticleLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class XyzUtils
{
  private static final String TAG = XyzUtils.class.getSimpleName();

  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");

  private XyzUtils()
  {

  }

  public static Date parsePublishedDate(Cursor cursor)
  {
    try
    {
      String date = cursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
      return dateFormat.parse(date);
    }
    catch (ParseException ex)
    {
      Log.e(TAG, ex.getMessage());
      Log.i(TAG, "passing today's date");
      return new Date();
    }
  }
}
