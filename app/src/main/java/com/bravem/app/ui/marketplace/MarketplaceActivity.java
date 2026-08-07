package com.bravem.app.ui.marketplace;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bravem.app.adapter.MarketplaceAdapter;
import com.bravem.app.databinding.ActivityMarketplaceBinding;
import com.bravem.app.model.MarketplaceItem;
import com.bravem.app.utils.SessionManager;
import com.bravem.app.utils.UiUtils;
import java.util.ArrayList;
import java.util.List;

public class MarketplaceActivity extends AppCompatActivity {

    private ActivityMarketplaceBinding binding;
    private MarketplaceAdapter adapter;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.applyEdgeToEdge(this);
        super.onCreate(savedInstanceState);
        binding = ActivityMarketplaceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        binding.toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        
        UiUtils.handleTopInset((android.view.View) binding.toolbar.getParent());
        UiUtils.handleFabBottomInset(binding.fabSell, 24);

        setupRecyclerView();
        loadMockData();

        binding.fabSell.setOnClickListener(v -> 
                startActivity(new Intent(this, SellItemActivity.class)));
    }

    private void setupRecyclerView() {
        adapter = new MarketplaceAdapter(item -> {
            Intent intent = new Intent(this, MarketplaceItemDetailActivity.class);
            intent.putExtra(MarketplaceItemDetailActivity.EXTRA_ITEM, item);
            startActivity(intent);
        });
        binding.recyclerMarketplace.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerMarketplace.setAdapter(adapter);
    }

    private void loadMockData() {
        List<MarketplaceItem> mockItems = new ArrayList<>();
        
        // Item owned by current user (Seller view)
        mockItems.add(new MarketplaceItem(
                "1", 
                sessionManager.getUid(), 
                sessionManager.getFullName(), 
                "White Lab Coat - Large", 
                "Lab Coats", 
                250.00, 
                "Gently used lab coat, no stains, size Large. Perfect for chemistry labs.", 
                null));

        // Other items (Buyer view)
        mockItems.add(new MarketplaceItem(
                "2", 
                "other_uid_1", 
                "John Doe", 
                "Medical Dictionary 30th Ed", 
                "Books", 
                450.00, 
                "Essential for medical students. Like new condition.", 
                null));

        mockItems.add(new MarketplaceItem(
                "3", 
                "other_uid_2", 
                "Sarah Smith", 
                "Stethoscope - Littmann", 
                "Nurse Materials", 
                1200.00, 
                "High-quality stethoscope used for one semester. Comes with original box.", 
                null));

        adapter.submitList(mockItems);
    }
}
