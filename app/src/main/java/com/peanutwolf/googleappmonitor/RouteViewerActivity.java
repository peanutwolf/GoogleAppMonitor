package com.peanutwolf.googleappmonitor;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;

import com.peanutwolf.googleappmonitor.Adapters.RouteViewsAdapter;
import com.peanutwolf.googleappmonitor.Database.ShakeDBContentProvider;
import com.peanutwolf.googleappmonitor.Models.ShakePointModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vigursky on 10.08.2016.
 */
public class RouteViewerActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private List<ShakePointModel> mRoutesList;
    private RouteViewsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_route_view);

        this.recyclerView = (RecyclerView) findViewById(R.id.recview_routes);

        this.mRoutesList = new ArrayList<>();
        this.mAdapter = new RouteViewsAdapter(this, this.mRoutesList);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(this.mAdapter);

        prepareRoutes();
    }

    private void prepareRoutes(){
        ContentResolver resolver = this.getContentResolver();
        Cursor cursor = resolver.query(ShakeDBContentProvider.CONTENT_URI, null, null, null, null);

        cursor.moveToFirst();
        if(cursor.getCount() == 0)
            return;

        do{
            ShakePointModel model = new ShakePointModel();
            model.dataToModel(cursor);
            mRoutesList.add(model);
        }while (cursor.moveToNext());

            cursor.close();
    }

    /**
     * RecyclerView item decoration - give equal margin around grid item
     */
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
}
