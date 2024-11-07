package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DBHandler extends SQLiteOpenHelper {

    // Database name, version, and table details
    private static final String DB_NAME = "locationsdb";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "locations";
    private static final String DB_PATH = "/data/data/com.example.myapplication/databases/"; // Update with your package

    // Column names
    private static final String ID_COL = "id";
    private static final String ADDRESS_COL = "address";
    private static final String LATITUDE_COL = "latitude";
    private static final String LONGITUDE_COL = "longitude";

    // Constructor
    public DBHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL query to create locations table
        String createTableQuery = "CREATE TABLE " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ADDRESS_COL + " TEXT,"
                + LATITUDE_COL + " REAL,"
                + LONGITUDE_COL + " REAL)";
        db.execSQL(createTableQuery); // Execute the create table query
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop the old table if it exists and create a new one
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Method to add a new location to the database
    public void addLocation(String address, double latitude, double longitude) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Put the location data into ContentValues
        values.put(ADDRESS_COL, address);
        values.put(LATITUDE_COL, latitude);
        values.put(LONGITUDE_COL, longitude);

        // Insert the row into the database
        db.insert(TABLE_NAME, null, values);
        db.close(); // Close the database connection
    }

    // Method to fetch all locations from the database
    public Cursor getAllLocations() {
        SQLiteDatabase db = this.getReadableDatabase();

        // Query to get all rows from the locations table
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }

    // Method to fetch all locations from the database as an arraylist
    public List<Location> getAllLocationsAsList(){
        List<Location> locations = new ArrayList<>();
        Cursor cursor = getAllLocations();

        if(cursor.moveToFirst()){
            do{
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(ID_COL));
                String address = cursor.getString(cursor.getColumnIndexOrThrow(ADDRESS_COL));
                double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(LATITUDE_COL));
                double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(LONGITUDE_COL));

                locations.add(new Location(id, address, latitude, longitude));

            }
            while (cursor.moveToNext());
        }
        cursor.close();
        return locations;
    }

    // Method to delete a specific location by ID
    public void deleteLocation(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, ID_COL + "=?", new String[]{String.valueOf(id)});
        db.close();
    }
    public void deleteAllLocations() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);  // Deletes all rows in the table
        db.close();  // Always close the database when done
    }
    // Method to search for locations with a partial address match
    public List<Location> searchLocationsByAddress(String partialAddress) {
        List<Location> matchingLocations = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Use LIKE operator for partial matching
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + ADDRESS_COL + " LIKE ?";
        Cursor cursor = db.rawQuery(query, new String[]{"%" + partialAddress + "%"});

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(ID_COL));
                String address = cursor.getString(cursor.getColumnIndexOrThrow(ADDRESS_COL));
                double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(LATITUDE_COL));
                double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(LONGITUDE_COL));

                matchingLocations.add(new Location(id, address, latitude, longitude));

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close(); // Close the database when done
        return matchingLocations;
    }

    // Method to copy the current database to internal storage
    public void backupDatabase(Context context) {
        File dbFile = new File(DB_PATH + DB_NAME);
        File backupFile = new File(context.getFilesDir(), "backup_locationsdb");

        try (FileInputStream fis = new FileInputStream(dbFile);
             FileOutputStream fos = new FileOutputStream(backupFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Method to restore the saved database file
    public void restoreDatabase(Context context) {
        File backupFile = new File(context.getFilesDir(), "backup_locationsdb");
        File dbFile = new File(DB_PATH + DB_NAME);

        if (backupFile.exists()) {
            try (FileInputStream fis = new FileInputStream(backupFile);
                 FileOutputStream fos = new FileOutputStream(dbFile)) {

                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Method to update a location
    public void updateLocation(Location location) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ADDRESS_COL, location.getAddress());
        values.put(LATITUDE_COL, location.getLatitude());
        values.put(LONGITUDE_COL, location.getLongitude());

        db.update(TABLE_NAME, values, ADDRESS_COL + "=?", new String[]{location.getAddress()});
        db.close();
    }

    // Method to delete a location by address
    public void deleteLocationByAddress(String address) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, ADDRESS_COL + "=?", new String[]{address});
        db.close();
    }

    // Method to get a location by address
    public Location getLocationByAddress(String address) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, ADDRESS_COL + "=?", new String[]{address}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(ID_COL));
            double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(LATITUDE_COL));
            double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(LONGITUDE_COL));
            cursor.close();
            return new Location(id, address, latitude, longitude);
        }
        return null;
    }


}
