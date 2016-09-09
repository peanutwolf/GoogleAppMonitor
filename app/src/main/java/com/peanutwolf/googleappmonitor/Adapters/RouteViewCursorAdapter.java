package com.peanutwolf.googleappmonitor.Adapters;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.peanutwolf.googleappmonitor.R;

/**
 * Created by vigursky on 26.04.2016.
 */
public class RouteViewCursorAdapter extends RecyclerView.Adapter<RouteViewCursorAdapter.RouteViewHolder>{
    private static final String TAG = RouteViewCursorAdapter.class.getSimpleName();
    private Cursor mCursor;

    public RouteViewCursorAdapter(Cursor cursor){
        mCursor = cursor;
    }

    public class RouteViewHolder extends RecyclerView.ViewHolder{
        public TextView mRouteIdTxt;

        public RouteViewHolder(View itemView) {
            super(itemView);
            mRouteIdTxt = (TextView) itemView.findViewById(R.id.txt_card_route_id);
        }
    }

    @Override
    public RouteViewCursorAdapter.RouteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recview_cards_route, parent, false);

        return new RouteViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RouteViewCursorAdapter.RouteViewHolder holder, int position) {
        Log.d(TAG, "[onBindViewHolder] Requested position = " + position);
        if(this.mCursor == null)
            return;
        mCursor.moveToPosition(position);
        int id = mCursor.getInt(0);
        holder.mRouteIdTxt.setText("RouteId" + id);
    }

    @Override
    public int getItemCount() {
        if(mCursor == null)
            return 0;
        else
            return mCursor.getCount();
    }

    public void swapCursor(Cursor cursor){
        mCursor = cursor;
        mCursor.moveToFirst();
        this.notifyDataSetChanged();
    }
}
