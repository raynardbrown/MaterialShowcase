package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>
{

  private Cursor mCursor;
  private long mStartId;

  private ViewPager mPager;
  private MyPagerAdapter mPagerAdapter;

  private int currentFragmentMetaBarMutedColor = 0xFF333333;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
    {
      getWindow().getDecorView().setSystemUiVisibility(
              View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                      View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }
    setContentView(R.layout.activity_article_detail);

    getLoaderManager().initLoader(0, null, this);

    mPager = (ViewPager) findViewById(R.id.pager);
    mPagerAdapter = new MyPagerAdapter(getFragmentManager());
    mPager.setAdapter(mPagerAdapter);
    mPager.setPageMargin((int) TypedValue
            .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
    mPager.setPageMarginDrawable(new ColorDrawable(0x22000000));

    mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener()
    {
      @Override
      public void onPageScrollStateChanged(int state)
      {
        super.onPageScrollStateChanged(state);
      }

      @Override
      public void onPageSelected(int position)
      {
        if(mCursor != null)
        {
          mCursor.moveToPosition(position);

          updateToolbarImage();
        }
      }
    });

    if(savedInstanceState == null)
    {
      if(getIntent() != null && getIntent().getData() != null)
      {
        mStartId = ItemsContract.Items.getItemId(getIntent().getData());
      }
    }

    findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View view)
      {
        startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(ArticleDetailActivity.this)
                .setType("text/plain")
                .setText("Some sample text")
                .getIntent(), getString(R.string.action_share)));
      }
    });

    final Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
    setSupportActionBar(toolbar);
    if(getSupportActionBar() != null)
    {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    // https://stackoverflow.com/questions/31662416/show-collapsingtoolbarlayout-title-only-when-collapsed
    final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_layout);
    AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
    appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener()
    {
      boolean isShow = true;
      int scrollRange = -1;

      @Override
      public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset)
      {
        if(scrollRange == -1)
        {
          scrollRange = appBarLayout.getTotalScrollRange();
        }

        if(scrollRange + verticalOffset == 0)
        {
          collapsingToolbarLayout.setTitle(toolbar.getTitle());
          isShow = true;
        }
        else if(isShow)
        {
          collapsingToolbarLayout.setTitle(" ");
          isShow = false;
        }
      }
    });
  }

  private void updateToolbarImage()
  {
    final ImageView photoView = (ImageView) findViewById(R.id.photo);

    final String photoUrl = mCursor.getString(ArticleLoader.Query.PHOTO_URL);
    ImageLoaderHelper.getInstance(this).getImageLoader()
            .get(photoUrl, new ImageLoader.ImageListener()
            {
              @Override
              public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b)
              {
                Bitmap bitmap = imageContainer.getBitmap();
                if(bitmap != null)
                {
                  Palette p = Palette.from(bitmap)
                          .maximumColorCount(12)
                          .generate();

                  currentFragmentMetaBarMutedColor = p.getDarkMutedColor(0xFF333333);
                  photoView.setImageBitmap(imageContainer.getBitmap());
                }
              }

              @Override
              public void onErrorResponse(VolleyError volleyError)
              {

              }
            });
  }

  @Override
  public Loader<Cursor> onCreateLoader(int i, Bundle bundle)
  {
    return ArticleLoader.newAllArticlesInstance(this);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor)
  {
    mCursor = cursor;
    mPagerAdapter.notifyDataSetChanged();

    // Select the start ID
    if(mStartId > 0)
    {
      mCursor.moveToFirst();
      // TODO: optimize
      while(!mCursor.isAfterLast())
      {
        if(mCursor.getLong(ArticleLoader.Query._ID) == mStartId)
        {
          final int position = mCursor.getPosition();
          mPager.setCurrentItem(position, false);

          updateToolbarImage();

          break;
        }
        mCursor.moveToNext();
      }
      mStartId = 0;
    }
  }

  @Override
  public void onLoaderReset(Loader<Cursor> cursorLoader)
  {
    mCursor = null;
    mPagerAdapter.notifyDataSetChanged();
  }

  private class MyPagerAdapter extends FragmentStatePagerAdapter
  {
    MyPagerAdapter(FragmentManager fm)
    {
      super(fm);
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object)
    {
      super.setPrimaryItem(container, position, object);
      ArticleDetailFragment fragment = (ArticleDetailFragment) object;

      if(fragment != null)
      {
        fragment.updateMetaBarBackground(currentFragmentMetaBarMutedColor);
      }
    }

    @Override
    public Fragment getItem(int position)
    {
      mCursor.moveToPosition(position);
      return ArticleDetailFragment.newInstance(mCursor.getLong(ArticleLoader.Query._ID));
    }

    @Override
    public int getCount()
    {
      return (mCursor != null) ? mCursor.getCount() : 0;
    }
  }
}
