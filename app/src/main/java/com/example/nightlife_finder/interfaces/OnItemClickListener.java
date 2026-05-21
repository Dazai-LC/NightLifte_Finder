package com.example.nightlife_finder.interfaces;

import android.view.View;

/**
 * OnItemClickListener – Callback chung cho sự kiện click item trong RecyclerView / ListView.
 * Được dùng bởi BaseAdapter và mọi Adapter con của app.
 */
public interface OnItemClickListener {

    /**
     * Gọi khi người dùng click vào một item trong danh sách.
     *
     * @param view     View được nhấn (item view của RecyclerView).
     * @param position Vị trí của item trong danh sách (0-based).
     */
    void onItemClick(View view, int position);
}