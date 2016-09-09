package com.peanutwolf.googleappmonitor.Adapters;


import com.peanutwolf.googleappmonitor.*;
import com.peanutwolf.googleappmonitor.Models.ShakePointModel;
import com.peanutwolf.googleappmonitor.Models.TrekModel;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by vigursky on 10.08.2016.
 */
public class RouteViewsAdapter extends RecyclerView.Adapter<RouteViewsAdapter.RouteViewHolder> {

    private final Context mContext;
    private final Map<Integer , List<ShakePointModel>> mShakePointsMap;
    private PublishSubject<Integer> subject = PublishSubject.create();


    public class RouteViewHolder extends RecyclerView.ViewHolder{
        public TextView mRouteIdTxt;
        public Integer mRouteId;

        public RouteViewHolder(View itemView) {
            super(itemView);
            mRouteIdTxt = (TextView) itemView.findViewById(R.id.txt_card_route_id);
        }
    }

    public RouteViewsAdapter(Context context, @NonNull Map<Integer, List<ShakePointModel>> shakePointsMap){
        this.mContext = context;
        this.mShakePointsMap =  shakePointsMap;
    }

    public RouteViewsAdapter(Context context, @NonNull List<TrekModel> treksList){
        this.mContext = context;
        mShakePointsMap = null;
    }

    @Override
    public RouteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recview_cards_route, parent, false);

        return new RouteViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final RouteViewHolder holder, final int position) {
        Set<Map.Entry<Integer, List<ShakePointModel>>> modelSet = mShakePointsMap.entrySet();
        Iterator<Map.Entry<Integer, List<ShakePointModel>>> modelSetIter = modelSet.iterator();
        Integer currentKey = 0;

        for(int i = 0; i <= position && modelSetIter.hasNext(); i++){
            Map.Entry<Integer, List<ShakePointModel>> entry = modelSetIter.next();
            currentKey = entry.getKey();
        }

        holder.mRouteId = currentKey;
        holder.mRouteIdTxt.setText("Route ID=" + currentKey);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subject.onNext(holder.mRouteId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mShakePointsMap.size();
    }

    public Observable<Integer> getPositionClicks(){
        return subject.asObservable();
    }
}
