package com.peanutwolf.googleappmonitor.Adapters;


import com.peanutwolf.googleappmonitor.*;
import com.peanutwolf.googleappmonitor.Models.ShakePointModel;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by vigursky on 10.08.2016.
 */
public class RouteViewsAdapter extends RecyclerView.Adapter<RouteViewsAdapter.RouteViewHolder> {

    private final Context mContext;
    private final List<ShakePointModel> mShakePoints;

    public class RouteViewHolder extends RecyclerView.ViewHolder{
        public TextView mRouteIdTxt;

        public RouteViewHolder(View itemView) {
            super(itemView);
            mRouteIdTxt = (TextView) itemView.findViewById(R.id.txt_card_route_id);
        }
    }

    public RouteViewsAdapter(Context context, @NonNull List<ShakePointModel> shakePoints){
        this.mContext = context;
        this.mShakePoints = shakePoints;
    }

    @Override
    public RouteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recview_cards_route, parent, false);

        return new RouteViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final RouteViewHolder holder, int position) {
        holder.mRouteIdTxt.setText(mShakePoints.get(position).getCurrentTimestamp()+"");
    }

    @Override
    public int getItemCount() {
        return mShakePoints.size();
    }


}
