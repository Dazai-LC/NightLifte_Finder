package com.example.nightlife_finder.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.nightlife_finder.R;
import com.example.nightlife_finder.models.Place;

import java.util.ArrayList;
import java.util.List;

/**
 * PlaceAdapter – RecyclerView adapter hiển thị danh sách Place từ Firestore.
 * Hỗ trợ imageUrl dạng http/https (dùng Glide) và key drawable (bunbo, bbq...).
 * Click item trả về documentId thật qua OnPlaceClickListener.
 */
public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder> {

    public interface OnPlaceClickListener {
        void onPlaceClick(String placeId);
    }

    private final Context context;
    private final List<Place> places = new ArrayList<>();
    private OnPlaceClickListener clickListener;

    public PlaceAdapter(Context context) {
        this.context = context;
    }

    public void setOnPlaceClickListener(OnPlaceClickListener listener) {
        this.clickListener = listener;
    }

    /** Thay toàn bộ dữ liệu và refresh list */
    public void setPlaces(List<Place> newPlaces) {
        places.clear();
        if (newPlaces != null) {
            places.addAll(newPlaces);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_place, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        Place place = places.get(position);
        holder.bind(place);

        // Click mở PlaceDetailActivity với documentId thật
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null && place.getId() != null) {
                clickListener.onPlaceClick(place.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    // -------------------------------------------------------
    // ViewHolder
    // -------------------------------------------------------
    class PlaceViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgPlace;
        private final TextView txtPlaceEmoji;
        private final TextView txtPlaceName;
        private final TextView txtPlaceCategory;
        private final TextView txtPlaceAddress;
        private final TextView txtPlaceOpenTime;

        PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPlace        = itemView.findViewById(R.id.imgPlace);
            txtPlaceEmoji   = itemView.findViewById(R.id.txtPlaceEmoji);
            txtPlaceName    = itemView.findViewById(R.id.txtPlaceName);
            txtPlaceCategory = itemView.findViewById(R.id.txtPlaceCategory);
            txtPlaceAddress = itemView.findViewById(R.id.txtPlaceAddress);
            txtPlaceOpenTime = itemView.findViewById(R.id.txtPlaceOpenTime);
        }

        void bind(Place place) {
            // Name
            String name = place.getName() != null ? place.getName() : "Địa điểm";
            txtPlaceName.setText(name);

            // Category
            String cat = place.getCategory() != null ? place.getCategory() : "";
            txtPlaceCategory.setText(cat.isEmpty() ? "Ẩm thực" : cat);

            // Address
            String addr = place.getAddress() != null ? place.getAddress() : "Chưa có địa chỉ";
            txtPlaceAddress.setText(addr);

            // OpenTime
            String ot = place.getOpenTime() != null ? place.getOpenTime() : "Chưa rõ giờ mở";
            txtPlaceOpenTime.setText("🕐 " + ot);

            // Ảnh
            loadImage(place.getImageUrl(), cat);
        }

        /** Load ảnh: URL → Glide, key → drawable, còn lại → emoji */
        private void loadImage(String imageUrl, String category) {
            // Thử map sang drawable resource
            int drawableRes = keyToDrawable(imageUrl);

            if (drawableRes != 0) {
                // Có drawable tương ứng
                imgPlace.setVisibility(View.VISIBLE);
                txtPlaceEmoji.setVisibility(View.GONE);
                imgPlace.setImageResource(drawableRes);

            } else if (imageUrl != null && (imageUrl.startsWith("http://") || imageUrl.startsWith("https://"))) {
                // URL thật → dùng Glide
                imgPlace.setVisibility(View.VISIBLE);
                txtPlaceEmoji.setVisibility(View.GONE);
                Glide.with(context)
                        .load(imageUrl)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .placeholder(R.drawable.bar)   // placeholder tạm
                        .error(R.drawable.bar)
                        .into(imgPlace);

            } else {
                // Không có ảnh → hiện emoji
                imgPlace.setVisibility(View.GONE);
                txtPlaceEmoji.setVisibility(View.VISIBLE);
                txtPlaceEmoji.setText(categoryToEmoji(imageUrl != null ? imageUrl : category));
            }
        }

        /**
         * Map imageUrl key sang drawable resource.
         * Trả về 0 nếu không có drawable tương ứng.
         */
        private int keyToDrawable(String key) {
            if (key == null) return 0;
            switch (key.toLowerCase()) {
                case "bar":    return R.drawable.bar;
                case "burger": return R.drawable.burger;
                case "pizza":  return R.drawable.pizza;
                case "diner":  return R.drawable.diner;
                case "sushi":  return R.drawable.sushi;
                default:       return 0;
            }
        }

        /** Map category/imageUrl sang emoji đại diện */
        private String categoryToEmoji(String key) {
            if (key == null) return "🍽️";
            switch (key.toLowerCase()) {
                case "bunbo":   case "bún bò":    return "🍜";
                case "bbq":     case "nướng":     return "🍢";
                case "lau":     case "lẩu":       return "🍲";
                case "pizza":                      return "🍕";
                case "che":     case "trà sữa":   return "🧋";
                case "bar":                        return "🍺";
                case "diner":   case "cơm":        return "🍚";
                case "phở":                        return "🍜";
                default:                           return "🍽️";
            }
        }
    }
}
