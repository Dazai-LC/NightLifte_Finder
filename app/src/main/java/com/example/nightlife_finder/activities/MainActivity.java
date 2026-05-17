package com.example.nightlife_finder.activities;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nightlife_finder.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );

            return insets;
        });

        // TEST FIREBASE REALTIME DATABASE
        FirebaseDatabase database = FirebaseDatabase.getInstance(
                "https://nightlife-finder-b7a6a-default-rtdb.asia-southeast1.firebasedatabase.app/"
        );

        DatabaseReference myRef = database.getReference("test");

        myRef.setValue("Hello Firebase")
                .addOnSuccessListener(unused -> {
                    Log.d("FIREBASE", "SUCCESS");
                })
                .addOnFailureListener(e -> {
                    Log.e("FIREBASE", "ERROR: " + e.getMessage());
                });
    }
}