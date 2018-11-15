package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import android.os.Bundle;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.utils.XyzUtils;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
                                                    LoaderManager.LoaderCallbacks<Cursor>
{
  private static final String TAG = "ArticleDetailFragment";

  public static final String ARG_ITEM_ID = "item_id";

  private Cursor mCursor;
  private long mItemId;
  private View mRootView;

  // Use default locale format
  private SimpleDateFormat outputFormat = new SimpleDateFormat();

  // Most time functions can only handle 1902 - 2037
  private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the
   * fragment (e.g. upon screen orientation changes).
   */
  public ArticleDetailFragment()
  {
  }

  public static ArticleDetailFragment newInstance(long itemId)
  {
    Bundle arguments = new Bundle();
    arguments.putLong(ARG_ITEM_ID, itemId);
    ArticleDetailFragment fragment = new ArticleDetailFragment();
    fragment.setArguments(arguments);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    if(getArguments().containsKey(ARG_ITEM_ID))
    {
      mItemId = getArguments().getLong(ARG_ITEM_ID);
    }

    setHasOptionsMenu(true);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState)
  {
    super.onActivityCreated(savedInstanceState);

    // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
    // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
    // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
    // we do this in onActivityCreated.
    getLoaderManager().initLoader(0, null, this);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState)
  {
    mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);

    bindViews();

    return mRootView;
  }

  private void bindViews()
  {
    if(mRootView == null)
    {
      return;
    }

    TextView titleView = (TextView) mRootView.findViewById(R.id.article_title);
    TextView bylineView = (TextView) mRootView.findViewById(R.id.article_byline);
    bylineView.setMovementMethod(new LinkMovementMethod());
    TextView bodyView = (TextView) mRootView.findViewById(R.id.article_body);

    if(mCursor != null)
    {
      mRootView.setAlpha(0);
      mRootView.setVisibility(View.VISIBLE);
      mRootView.animate().alpha(1);

      titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));

      Date publishedDate = XyzUtils.parsePublishedDate(mCursor);
      if(!publishedDate.before(START_OF_EPOCH.getTime()))
      {
        bylineView.setText(Html.fromHtml(
                DateUtils.getRelativeTimeSpanString(
                        publishedDate.getTime(),
                        System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                        DateUtils.FORMAT_ABBREV_ALL).toString()
                        + " by <font color='#ffffff'>"
                        + mCursor.getString(ArticleLoader.Query.AUTHOR)
                        + "</font>"));

      }
      else
      {
        // If date is before 1902, just show the string
        bylineView.setText(Html.fromHtml(
                outputFormat.format(publishedDate) + " by <font color='#ffffff'>"
                        + mCursor.getString(ArticleLoader.Query.AUTHOR)
                        + "</font>"));

      }
      bodyView.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY).replaceAll("(\r\n|\n)", "<br />")));
    }
    else
    {
      mRootView.setVisibility(View.GONE);
      titleView.setText("N/A");
      bylineView.setText("N/A");
      bodyView.setText("N/A");
    }
  }

  @Override
  public Loader<Cursor> onCreateLoader(int i, Bundle bundle)
  {
    return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor)
  {
    if(!isAdded())
    {
      if(cursor != null)
      {
        cursor.close();
      }
      return;
    }

    mCursor = cursor;
    if(mCursor != null && !mCursor.moveToFirst())
    {
      Log.e(TAG, "Error reading item detail cursor");
      mCursor.close();
      mCursor = null;
    }

    bindViews();
  }

  @Override
  public void onLoaderReset(Loader<Cursor> cursorLoader)
  {
    mCursor = null;
    bindViews();
  }

  public void updateMetaBarBackground(int color)
  {
    if(mRootView != null)
    {
      mRootView.findViewById(R.id.meta_bar)
              .setBackgroundColor(color);
    }
  }
}
