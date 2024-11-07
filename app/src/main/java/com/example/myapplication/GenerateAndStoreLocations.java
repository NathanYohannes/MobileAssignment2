package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.net.URL;
import java.util.Random;
import java.net.HttpURLConnection;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import org.json.JSONObject;

public class GenerateAndStoreLocations {

    private DBHandler dbHandler;
    private static final int TARGET_LOCATION_COUNT = 100;

    // Latitude and Longitude bounds of the Greater Toronto Area
    private static final double NORTH_BOUND = 44.05;
    private static final double SOUTH_BOUND = 43.50;
    private static final double EAST_BOUND = -78.40;
    private static final double WEST_BOUND = -79.90;

    // I used an API key to generate my locations but the database should persist. Feel free to add a google maps API key to test
    private static final String API_KEY = "";

    public GenerateAndStoreLocations(Context context) {
        dbHandler = new DBHandler(context);

        // Check if API key is empty, and restore the backup database if it is
        if (API_KEY.isEmpty()) {
            dbHandler.restoreDatabase(context);
        }
    }

    public void generateAndStoreLocations() {
        // Check the current number of addresses in the database
        Cursor cursor = dbHandler.getAllLocations();
        int currentLocationCount = cursor.getCount();
        cursor.close();

        // If there are fewer than 100 locations, generate additional locations
        if (currentLocationCount < TARGET_LOCATION_COUNT) {
            int locationsToGenerate = TARGET_LOCATION_COUNT - currentLocationCount;
            generateRandomLocations(locationsToGenerate);
        }
    }

    private void generateRandomLocations(int count) {
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            // Generate random latitude and longitude within the GTA bounds
            double latitude = SOUTH_BOUND + (NORTH_BOUND - SOUTH_BOUND) * random.nextDouble();
            double longitude = WEST_BOUND + (EAST_BOUND - WEST_BOUND) * random.nextDouble();

            // Get the street address from the generated latitude and longitude
            String address = getAddressFromLatLong(latitude, longitude);

            // Store the generated location in the database
            dbHandler.addLocation(address, latitude, longitude);
        }
    }

    // Method to get address from latitude and longitude using Google Maps Geocoding API
    private String getAddressFromLatLong(double latitude, double longitude) {
        String address = null;

        try {
            // Build the URL for the geocoding request
            String urlString = String.format(
                    "https://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&key=%s",
                    latitude, longitude, API_KEY
            );

            // Create a URL object and open a connection
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Read the response
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Log the raw response to help debug
            Log.d("GeocodingResponse", response.toString());

            // Parse the JSON response
            JSONObject jsonResponse = new JSONObject(response.toString());

            // Check if the status is OK
            if (jsonResponse.getString("status").equals("OK")) {
                // Get the formatted address from the response
                address = jsonResponse.getJSONArray("results")
                        .getJSONObject(0)
                        .getString("formatted_address");
            } else {
                // Handle any status other than OK
                address = "Address not found (status: " + jsonResponse.getString("status") + ")";
            }
        } catch (Exception e) {
            e.printStackTrace();
            address = "Error: " + e.getMessage();
        }

        return address;
    }


}
