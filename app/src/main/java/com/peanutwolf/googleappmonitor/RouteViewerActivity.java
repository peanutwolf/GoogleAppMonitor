package com.peanutwolf.googleappmonitor;

import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.github.brnunes.swipeablerecyclerview.SwipeableRecyclerViewTouchListener;
import com.peanutwolf.googleappmonitor.Adapters.RouteViewCursorAdapter;
import com.peanutwolf.googleappmonitor.Adapters.RouteViewsAdapter;
import com.peanutwolf.googleappmonitor.Database.ShakeDBContentProvider;
import com.peanutwolf.googleappmonitor.Database.ShakeDatabase;
import com.peanutwolf.googleappmonitor.Fragments.TrekViewDialog;
import com.peanutwolf.googleappmonitor.Models.ShakePointPOJO;
import com.peanutwolf.googleappmonitor.Models.TrekModel;
import com.peanutwolf.googleappmonitor.Models.TrekModelDAO;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import rx.Subscription;
import rx.functions.Action1;

/**
 * Created by vigursky on 10.08.2016.
 */
public class RouteViewerActivity extends FragmentActivity {
    public static final int TREK_LOADER_ID = 1;
    private static final String TAG = RouteViewerActivity.class.getSimpleName();
    private RecyclerView recyclerView;
    private Map<Integer, List<ShakePointPOJO>> mRoutesMap;
    private RouteViewCursorAdapter mCursorAdapter;
    private Subscription mAdapterClickHandler;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapterClickHandler.unsubscribe();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_route_view);

        final TrekLoader trekLoader = new TrekLoader();

        this.recyclerView = (RecyclerView) findViewById(R.id.recview_routes);

        this.mRoutesMap = new LinkedHashMap<>();

        this.mCursorAdapter = new RouteViewCursorAdapter(null);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(this.mCursorAdapter);
        mAdapterClickHandler = this.mCursorAdapter.getPositionClicks().subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer trekId) {
                DialogFragment routeViewFragment = TrekViewDialog.newInstance(trekId);
                routeViewFragment.show(getSupportFragmentManager().beginTransaction(), "dialog");
            }
        });

        SwipeableRecyclerViewTouchListener swipeTouchListener =
                new SwipeableRecyclerViewTouchListener(recyclerView,
                        new SwipeableRecyclerViewTouchListener.SwipeListener() {
                            TrekModelDAO trekModelDAO = new TrekModelDAO(RouteViewerActivity.this);
                            @Override
                            public boolean canSwipeLeft(int position) {
                                return true;
                            }

                            @Override
                            public boolean canSwipeRight(int position) {
                                return true;
                            }

                            @Override
                            public void onDismissedBySwipeLeft(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    trekModelDAO.removeTrek(mCursorAdapter.getTrekId(position));
                                    mCursorAdapter.notifyItemRemoved(position);
                                }
                                mCursorAdapter.notifyDataSetChanged();
                                RouteViewerActivity.this.getLoaderManager().restartLoader(TREK_LOADER_ID, null, trekLoader);
                            }

                            @Override
                            public void onDismissedBySwipeRight(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    trekModelDAO.removeTrek(mCursorAdapter.getTrekId(position));
                                    mCursorAdapter.notifyItemRemoved(position);
                                }
                                mCursorAdapter.notifyDataSetChanged();
                                RouteViewerActivity.this.getLoaderManager().restartLoader(TREK_LOADER_ID, null, trekLoader);
                            }
                        });

        recyclerView.addOnItemTouchListener(swipeTouchListener);

        this.getLoaderManager().initLoader(TREK_LOADER_ID, null, trekLoader);
    }

    private void prepareRoutes(){
        ContentResolver resolver = this.getContentResolver();
        Cursor cursor = resolver.query(ShakeDBContentProvider.CONTENT_SHAKES_URI, null, null, null, null);

        cursor.moveToFirst();
        if(cursor.getCount() == 0)
            return;
        do{
            ShakePointPOJO model = new ShakePointPOJO(cursor);
            if(mRoutesMap.get(model.getRouteId()) == null){
                mRoutesMap.put(model.getRouteId(), new LinkedList<ShakePointPOJO>());
            }
            mRoutesMap.get(model.getRouteId()).add(model);
        }while (cursor.moveToNext());

        cursor.close();
    }

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }


    private class TrekLoader implements LoaderManager.LoaderCallbacks<Cursor> {
        private final String TAG = RouteViewerActivity.TAG + TrekLoader.class.getSimpleName();

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Log.d(TAG, "[onCreateLoader]");
            CursorLoader loader = new CursorLoader(
                    RouteViewerActivity.this,
                    ShakeDBContentProvider.CONTENT_TREK_URI,
                    new String[]{ShakeDatabase.COLUMN_ID,
                                 ShakeDatabase.COLUMN_TIMESTAMP,
                                 ShakeDatabase.COLUMN_DISTANCE},
                    null, null, null);
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            Log.d(TAG, "[onLoadFinished]");
            RouteViewerActivity.this.mCursorAdapter.swapCursor(cursor);
            RouteViewerActivity.this.mCursorAdapter.notifyDataSetChanged();
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }
}
