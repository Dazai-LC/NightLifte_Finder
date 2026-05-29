package com.example.nightlife_finder.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.nightlife_finder.R;
import com.example.nightlife_finder.models.Place;
import com.example.nightlife_finder.repositories.PlaceRepository;

import java.util.List;

public class PlaceListActivity extends BaseActivity {

    private LinearLayout placeListContainer;
    private TextView txtListTitle;
    private TextView txtListSubtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_list);

        getWindow().setStatusBarColor(Color.parseColor("#0F0F12"));
        getWindow().setNavigationBarColor(Color.parseColor("#1A1A1F"));

        placeListContainer = findViewById(R.id.placeListContainer);
        txtListTitle = findViewById(R.id.txtListTitle);
        txtListSubtitle = findViewById(R.id.txtListSubtitle);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        String mode = getIntent().getStringExtra("mode");
        String key = getIntent().getStringExtra("key");
        String title = getIntent().getStringExtra("title");

        if (mode == null) mode = "dish";
        if (key == null) key = "bach_tuoc";
        if (title == null) title = "Địa chỉ quán";

        txtListTitle.setText(title);
        txtListSubtitle.setText("Các địa chỉ quán phù hợp. Bấm vào quán để xem chi tiết.");

        List<Place> places;

        if (mode.equals("category")) {
            places = PlaceRepository.getPlacesByCategory(key);
        } else {
            places = PlaceRepository.getPlacesByDish(key);
        }

        renderPlaces(places);
    }

    private void renderPlaces(List<Place> places) {
        placeListContainer.removeAllViews();

        if (places.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Chưa có quán phù hợp với món này.");
            empty.setTextColor(getColor(R.color.text_secondary));
            empty.setTextSize(15);
            empty.setPadding(4, 30, 4, 30);
            placeListContainer.addView(empty);
            return;
        }

        for (Place place : places) {
            placeListContainer.addView(createPlaceCard(place));
        }
    }

    private LinearLayout createPlaceCard(Place place) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(18, 16, 18, 16);
        card.setBackgroundResource(R.drawable.profile_group_bg);
        card.setClickable(true);
        card.setFocusable(true);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 14);
        card.setLayoutParams(cardParams);

        TextView name = new TextView(this);
        name.setText(place.name);
        name.setTextColor(getColor(R.color.text_primary));
        name.setTextSize(17);
        name.setTypeface(null, Typeface.BOLD);

        TextView type = new TextView(this);
        type.setText(place.type + " • ⭐ " + place.rating);
        type.setTextColor(getColor(R.color.text_secondary));
        type.setTextSize(13);
        type.setPadding(0, 6, 0, 0);

        TextView address = new TextView(this);
        address.setText("📍 " + place.address);
        address.setTextColor(getColor(R.color.primary_color));
        address.setTextSize(13);
        address.setPadding(0, 6, 0, 0);

        TextView time = new TextView(this);
        time.setText("🕒 " + place.openingHours + " • Điểm đi đêm: " + place.nightScore + "/100");
        time.setTextColor(getColor(R.color.text_secondary));
        time.setTextSize(13);
        time.setPadding(0, 6, 0, 0);

        card.addView(name);
        card.addView(type);
        card.addView(address);
        card.addView(time);

        card.setOnClickListener(v -> openPlaceDetail(place.id));
        address.setOnClickListener(v -> openPlaceDetail(place.id));

        return card;
    }

    private void openPlaceDetail(String placeId) {
        Intent intent = new Intent(PlaceListActivity.this, PlaceDetailActivity.class);
        intent.putExtra("place_id", placeId);
        startActivity(intent);
    }
}