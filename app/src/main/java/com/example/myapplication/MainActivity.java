package com.example.myapplication;

import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private DBHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        dbHandler = new DBHandler(this);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        GenerateAndStoreLocations addressGenerator = new GenerateAndStoreLocations(this);
        addressGenerator.generateAndStoreLocations();
    }

    private String getAddressInput() {
        TextInputLayout inputAddressView = findViewById(R.id.inputAddressTextField);
        return inputAddressView.getEditText().getText().toString().trim();
    }

    private double getLatitudeInput() {
        TextInputLayout inputLatitudeView = findViewById(R.id.inputLatitudeTextField);
        String latText = inputLatitudeView.getEditText().getText().toString().trim();
        return latText.isEmpty() ? 0 : Double.parseDouble(latText);
    }

    private double getLongitudeInput() {
        TextInputLayout inputLongitudeView = findViewById(R.id.inputLongitudeTextField);
        String longText = inputLongitudeView.getEditText().getText().toString().trim();
        return longText.isEmpty() ? 0 : Double.parseDouble(longText);
    }

    public void search(View v) {
        String addressText = getAddressInput();
        TextView addressOutputView = findViewById(R.id.addressOutputView);

        ArrayList<Location> searchResults = new ArrayList<>(dbHandler.searchLocationsByAddress(addressText));
        if (searchResults.isEmpty()) {
            addressOutputView.setText("No Matching Locations Found");
        } else {
            String locationsString = LocationUtils.convertLocationsToString(searchResults);
            addressOutputView.setText(locationsString);
        }
    }

    public void addLocation(View v) {
        String address = getAddressInput();
        double latitude = getLatitudeInput();
        double longitude = getLongitudeInput();

        dbHandler.addLocation(address, latitude, longitude);
        showMessage("Location added successfully!");
    }

    public void updateLocation(View v) {
        String address = getAddressInput();
        double latitude = getLatitudeInput();
        double longitude = getLongitudeInput();

        Location location = dbHandler.searchLocationsByAddress(address).stream().findFirst().orElse(null);
        if (location != null) {
            Location updatedLocation = new Location(location.getId(), address, latitude, longitude);
            dbHandler.updateLocation(updatedLocation);
            showMessage("Location updated successfully!");
        } else {
            showMessage("Location not found for update.");
        }
    }

    public void deleteLocation(View v) {
        String address = getAddressInput();
        Location location = dbHandler.searchLocationsByAddress(address).stream().findFirst().orElse(null);
        if (location != null) {
            dbHandler.deleteLocation(location.getId());
            showMessage("Location deleted successfully!");
        } else {
            showMessage("Location not found for deletion.");
        }
    }

    private void showMessage(String message) {
        TextView addressOutputView = findViewById(R.id.addressOutputView);
        addressOutputView.setText(message);
    }
}
