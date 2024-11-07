package com.example.myapplication;
import java.util.ArrayList;
public class LocationUtils {

        public static String convertLocationsToString(ArrayList<Location> locationsList) {
            StringBuilder result = new StringBuilder();
            String item;
            for (Location location : locationsList) {
                item = location.getAddress() + "\n" + "Lat: "+location.getLatitude() +" Long: " + location.getLongitude();
                result.append(item).append("\n\n");

            }
            return result.toString().trim(); // Trim to remove the last newline if needed
        }

}
