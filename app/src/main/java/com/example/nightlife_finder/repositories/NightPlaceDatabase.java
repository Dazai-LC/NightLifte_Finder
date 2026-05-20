package com.example.nightlife_finder.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.nightlife_finder.models.NightPlace;

import java.util.ArrayList;
import java.util.List;

public class NightPlaceDatabase extends SQLiteOpenHelper {

    private static final String DB_NAME = "nightlife_places.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_PLACES = "places";

    public static final String COL_OSM_ID = "osm_id";
    public static final String COL_NAME = "name";
    public static final String COL_TYPE = "type";
    public static final String COL_CATEGORY = "category";
    public static final String COL_OPENING_HOURS = "opening_hours";
    public static final String COL_LAT = "latitude";
    public static final String COL_LON = "longitude";
    public static final String COL_DISTANCE = "distance_meters";
    public static final String COL_NIGHT_SCORE = "night_score";
    public static final String COL_UPDATED_AT = "updated_at";

    public NightPlaceDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE IF NOT EXISTS " + TABLE_PLACES + " (" +
                COL_OSM_ID + " TEXT PRIMARY KEY, " +
                COL_NAME + " TEXT, " +
                COL_TYPE + " TEXT, " +
                COL_CATEGORY + " TEXT, " +
                COL_OPENING_HOURS + " TEXT, " +
                COL_LAT + " REAL, " +
                COL_LON + " REAL, " +
                COL_DISTANCE + " REAL, " +
                COL_NIGHT_SCORE + " INTEGER, " +
                COL_UPDATED_AT + " INTEGER" +
                ")";

        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLACES);
        onCreate(db);
    }

    public void upsertPlaces(List<NightPlace> places) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        try {
            for (NightPlace place : places) {
                ContentValues values = new ContentValues();

                values.put(COL_OSM_ID, place.getOsmId());
                values.put(COL_NAME, place.getName());
                values.put(COL_TYPE, place.getType());
                values.put(COL_CATEGORY, place.getCategory());
                values.put(COL_OPENING_HOURS, place.getOpeningHours());
                values.put(COL_LAT, place.getLatitude());
                values.put(COL_LON, place.getLongitude());
                values.put(COL_DISTANCE, place.getDistanceMeters());
                values.put(COL_NIGHT_SCORE, place.getNightScore());
                values.put(COL_UPDATED_AT, place.getUpdatedAt());

                db.insertWithOnConflict(
                        TABLE_PLACES,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_REPLACE
                );
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public List<NightPlace> getAllPlaces() {
        List<NightPlace> places = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_PLACES,
                null,
                null,
                null,
                null,
                null,
                COL_NIGHT_SCORE + " DESC, " + COL_DISTANCE + " ASC"
        );

        try {
            while (cursor.moveToNext()) {
                places.add(cursorToPlace(cursor));
            }
        } finally {
            cursor.close();
        }

        return places;
    }

    public List<NightPlace> getPlacesByCategory(String category) {
        List<NightPlace> places = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_PLACES,
                null,
                COL_CATEGORY + " = ?",
                new String[]{category},
                null,
                null,
                COL_NIGHT_SCORE + " DESC, " + COL_DISTANCE + " ASC"
        );

        try {
            while (cursor.moveToNext()) {
                places.add(cursorToPlace(cursor));
            }
        } finally {
            cursor.close();
        }

        return places;
    }

    public List<NightPlace> getNightPlaces() {
        List<NightPlace> places = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_PLACES,
                null,
                COL_NIGHT_SCORE + " >= ?",
                new String[]{"60"},
                null,
                null,
                COL_NIGHT_SCORE + " DESC, " + COL_DISTANCE + " ASC"
        );

        try {
            while (cursor.moveToNext()) {
                places.add(cursorToPlace(cursor));
            }
        } finally {
            cursor.close();
        }

        return places;
    }

    public int countPlaces() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PLACES, null);

        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }

            return 0;
        } finally {
            cursor.close();
        }
    }

    private NightPlace cursorToPlace(Cursor cursor) {
        String osmId = cursor.getString(cursor.getColumnIndexOrThrow(COL_OSM_ID));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME));
        String type = cursor.getString(cursor.getColumnIndexOrThrow(COL_TYPE));
        String category = cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY));
        String openingHours = cursor.getString(cursor.getColumnIndexOrThrow(COL_OPENING_HOURS));
        double lat = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LAT));
        double lon = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LON));
        double distance = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_DISTANCE));
        int nightScore = cursor.getInt(cursor.getColumnIndexOrThrow(COL_NIGHT_SCORE));
        long updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow(COL_UPDATED_AT));

        return new NightPlace(
                osmId,
                name,
                type,
                category,
                openingHours,
                lat,
                lon,
                distance,
                nightScore,
                updatedAt
        );
    }
}