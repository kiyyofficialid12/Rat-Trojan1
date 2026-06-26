package com.rat.premium;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (id == R.id.nav_rat) {
                selectedFragment = new RATFragment();
            } else if (id == R.id.nav_settings) {
                selectedFragment = new SettingsFragment();
            }
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.container, selectedFragment).commit();
            }
            return true;
        });
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new HomeFragment()).commit();
    }
}
