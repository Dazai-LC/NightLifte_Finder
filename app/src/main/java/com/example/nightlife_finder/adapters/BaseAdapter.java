package com.example.nightlife_finder.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nightlife_finder.interfaces.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * BaseAdapter<T, VH> – Generic RecyclerView adapter base.
 * Subclasses override onCreateViewHolder and onBindHolder.
 * Supports item-click via OnItemClickListener.
 */
public abstract class BaseAdapter<T, VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    protected List<T> items = new ArrayList<>();
    protected OnItemClickListener onItemClickListener;

    // -------------------------------------------------------
    // Data management
    // -------------------------------------------------------

    /** Replace the entire data set and refresh the list. */
    public void setItems(List<T> newItems) {
        this.items.clear();
        if (newItems != null) {
            this.items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    /** Append a single item to the end of the list. */
    public void addItem(T item) {
        if (item != null) {
            items.add(item);
            notifyItemInserted(items.size() - 1);
        }
    }

    /** Remove the item at the specified position. */
    public void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
        }
    }

    /** Clear all items. */
    public void clearItems() {
        items.clear();
        notifyDataSetChanged();
    }

    /** Return item at position. */
    public T getItem(int position) {
        return items.get(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // -------------------------------------------------------
    // Click listener
    // -------------------------------------------------------

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    // -------------------------------------------------------
    // Abstract binding — subclasses implement this
    // -------------------------------------------------------

    /**
     * Bind a data item to the provided ViewHolder.
     * Called by the default {@link #onBindViewHolder} implementation.
     */
    protected abstract void onBindHolder(@NonNull VH holder, T item, int position);

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        T item = items.get(position);
        onBindHolder(holder, item, position);

        // Wire the item-click listener if set
        if (onItemClickListener != null) {
            holder.itemView.setOnClickListener(v ->
                    onItemClickListener.onItemClick(holder.itemView, position));
        }
    }
}
