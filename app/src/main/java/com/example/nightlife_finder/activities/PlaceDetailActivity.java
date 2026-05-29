package com.example.nightlife_finder.activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nightlife_finder.R;
import com.example.nightlife_finder.models.Place;
import com.example.nightlife_finder.repositories.PlaceRepository;

public class PlaceDetailActivity extends BaseActivity {

    private Place place;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_detail);

        getWindow().setStatusBarColor(Color.parseColor("#0F0F12"));
        getWindow().setNavigationBarColor(Color.parseColor("#1A1A1F"));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        String placeId = getIntent().getStringExtra("place_id");
        place = PlaceRepository.getPlaceById(placeId);

        if (place == null) {
            Toast.makeText(this, "Không tìm thấy địa điểm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindData();

        findViewById(R.id.btnDirection).setOnClickListener(v -> openDirection());
    }

    private void bindData() {
        TextView txtPlaceEmoji = findViewById(R.id.txtPlaceEmoji);
        TextView txtPlaceName = findViewById(R.id.txtPlaceName);
        TextView txtPlaceType = findViewById(R.id.txtPlaceType);
        TextView txtPlaceAddress = findViewById(R.id.txtPlaceAddress);
        TextView txtPlaceTime = findViewById(R.id.txtPlaceTime);
        TextView txtPlaceScore = findViewById(R.id.txtPlaceScore);

        txtPlaceEmoji.setText(getEmoji(place.category));
        txtPlaceName.setText(place.name);
        txtPlaceType.setText(place.type + " • ⭐ " + place.rating);
        txtPlaceAddress.setText("📍 " + place.address);
        txtPlaceTime.setText("🕒 " + place.openingHours);
        txtPlaceScore.setText("🌙 Điểm phù hợp đi đêm: " + place.nightScore + "/100");
    }

    private String getEmoji(String category) {
        switch (category) {
            case "food":
                return "🍜";
            case "cafe":
                return "☕";
            case "tea":
                return "🧋";
            case "store":
                return "🏪";
            case "supermarket":
                return "🛒";
            default:
                return "📍";
        }
    }

    private void openDirection() {
        String uri = "google.navigation:q=" + place.latitude + "," + place.longitude + "&mode=w";

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");

        try {
            startActivity(intent);
        } catch (Exception e) {
            String fallbackUri = "geo:0,0?q="
                    + place.latitude
                    + ","
                    + place.longitude
                    + "("
                    + Uri.encode(place.name)
                    + ")";

            Intent fallbackIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUri));

            try {
                startActivity(fallbackIntent);
            } catch (Exception ex) {
                Toast.makeText(this, "Máy chưa có ứng dụng bản đồ để chỉ đường", Toast.LENGTH_SHORT).show();
            }
        }
    }
}