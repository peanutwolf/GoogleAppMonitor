package com.peanutwolf.googleappmonitor.Adapters;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.peanutwolf.googleappmonitor.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by vigursky on 26.04.2016.
 */
public class RouteViewCursorAdapter extends RecyclerView.Adapter<RouteViewCursorAdapter.RouteViewHolder>{
    private static final String TAG = RouteViewCursorAdapter.class.getSimpleName();
    public Set<Integer> mRemovedItemsSwap = new HashSet<>();
    private RecyclerView.AdapterDataObserver mDataChangeObserver;
    private PublishSubject<Integer> subject = PublishSubject.create();
    private Cursor mCursor;

    public RouteViewCursorAdapter(Cursor cursor){
        mCursor = cursor;

        mDataChangeObserver =  new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                Log.d(TAG, "[onItemRangeRemoved] positionStart=" + positionStart + " itemCount = " + itemCount);
                for (int i = positionStart; i < itemCount + positionStart; i++)
                    mRemovedItemsSwap.add(i);
            }
        };

        this.registerAdapterDataObserver(mDataChangeObserver);
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
    public void onBindViewHolder(RouteViewCursorAdapter.RouteViewHolder holder, final int position) {
        Log.d(TAG, "[onBindViewHolder] Requested position = " + position);
        if (this.mCursor == null){
            return;
        }

        mCursor.moveToPosition(position);
        final int id = mCursor.getInt(0);
        holder.mRouteIdTxt.setText("RouteId" + id);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subject.onNext(id);
            }
        });
    }

    @Override
    public int getItemCount() {
        int itemCount;

        if(mCursor == null)
            itemCount =  0;
        else
            itemCount =  mCursor.getCount() - mRemovedItemsSwap.size();

        Log.d(TAG, "[getItemCount] count = " + itemCount);

        return itemCount;
    }

    public void swapCursor(Cursor cursor){
        Log.d(TAG, "[swapCursor]");
        mRemovedItemsSwap.clear();
        if(mCursor != null) {
            Log.d(TAG, "[swapCursor] closing old cursor");
            mCursor.close();
        }
        mCursor = cursor;
    }

    public int getTrekId(final int position){
        mCursor.moveToPosition(position);
        final int id = mCursor.getInt(0);
        return id;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        Log.d(TAG, "[onDetachedFromRecyclerView]");
        super.onDetachedFromRecyclerView(recyclerView);
        this.unregisterAdapterDataObserver(mDataChangeObserver);
    }

    public Observable<Integer> getPositionClicks(){
        return subject.asObservable();
    }
}
