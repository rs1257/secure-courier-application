package com.gpig.a;

import android.content.res.Resources;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gpig.a.TicketFragment.OnListFragmentInteractionListener;
import com.gpig.a.tickets.Ticket;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Ticket} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class MyTicketRecyclerViewAdapter extends RecyclerView.Adapter<MyTicketRecyclerViewAdapter.ViewHolder> {

    private final List<Ticket> mValues;
    private final OnListFragmentInteractionListener mListener;
    private final Resources mResources;

    public MyTicketRecyclerViewAdapter(List<Ticket> items, OnListFragmentInteractionListener listener, Resources resources) {
        mValues = items;
        mListener = listener;
        mResources = resources;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_ticket, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).id);
        holder.mContentView.setText(mValues.get(position).title);
        if(mValues.get(position).details != null) {
            holder.mDescrition.setText(mValues.get(position).details);
        }
        if(mValues.get(position).image != -1) {
            holder.mImageView.setImageDrawable(ResourcesCompat.getDrawable(mResources, mValues.get(position).image, null));
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public final TextView mDescrition;
        public final ImageView mImageView;
        public Ticket mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.item_number);
            mContentView = (TextView) view.findViewById(R.id.content);
            mDescrition = (TextView) view.findViewById(R.id.description);
            mImageView = (ImageView) view.findViewById(R.id.image);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
