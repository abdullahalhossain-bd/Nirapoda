package com.example.nirapod;


import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.safety.app.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup bottom navigation
        BottomNavigationView navView = binding.bottomNavView;

        // Define the top-level destinations
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home,
                R.id.navigation_activity,
                R.id.navigation_map,
                R.id.navigation_profile
        ).build();

        // Setup Navigation Controller with Bottom Navigation
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navView, navController);

        // Listen to destination changes
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            // Show/hide bottom nav based on destination
            int id = destination.getId();
            if (id == R.id.navigation_home ||
                    id == R.id.navigation_activity ||
                    id == R.id.navigation_map ||
                    id == R.id.navigation_profile) {
                binding.bottomNavView.setVisibility(View.VISIBLE);
            } else {
                binding.bottomNavView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}
