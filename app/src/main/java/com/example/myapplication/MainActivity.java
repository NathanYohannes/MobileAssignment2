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
import java.util.List;

public class MainActivity extends AppCompatActivity {




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

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

    public void search(View v){

        DBHandler dbHandler = new DBHandler(this);
        TextView addressOutputView = (TextView) findViewById(R.id.addressOutputView);
        TextInputLayout inputAddressView = findViewById(R.id.inputAddressTextField);

        String addressText = inputAddressView.getEditText().getText().toString();
        ArrayList<Location> searchResults = new ArrayList<>(dbHandler.searchLocationsByAddress(addressText));
        if (searchResults.isEmpty()){
            addressOutputView.setText("No Matching Locations Found");
        }
        else {
            String locationsString = LocationUtils.convertLocationsToString(searchResults);
            addressOutputView.setText(locationsString);
        }
    }

}