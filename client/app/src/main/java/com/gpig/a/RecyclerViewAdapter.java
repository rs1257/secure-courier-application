package com.gpig.a;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.osmdroid.bonuspack.routing.RoadNode;

import java.util.ArrayList;


public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{

    ArrayList<String> instructionsName;
    ArrayList<Double> instructionsDistance;
    ArrayList<Double> instructionsTime;
    Context context;
    View view;
    ViewHolder viewHolder;

    public RecyclerViewAdapter(Context cx, ArrayList<RoadNode> node){
        instructionsName = new ArrayList<>();
        instructionsDistance = new ArrayList<>();
        instructionsTime = new ArrayList<>();
        for (RoadNode n: node){
            instructionsName.add(n.mInstructions);
            instructionsDistance.add(n.mLength);
            instructionsTime.add(n.mDuration);
        }
        context = cx;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView instructionView;
        public TextView distanceView;
        public TextView timeView;

        public ViewHolder(View v){
            super(v);
            instructionView = (TextView) v.findViewById(R.id.direction);
            distanceView = (TextView) v.findViewById(R.id.distance);
            timeView = (TextView) v.findViewById(R.id.time);
        }
    }

    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        view = LayoutInflater.from(context).inflate(R.layout.recyclerview_items,parent,false);
        viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position){
        holder.instructionView.setText(instructionsName.get(position));
        holder.distanceView.setText(String.format("%.2f", instructionsDistance.get(position)));
        holder.timeView.setText(String.format("%.2f", instructionsTime.get(position) / 60));
    }

    @Override
    public int getItemCount(){
        return instructionsName.size();
    }
}