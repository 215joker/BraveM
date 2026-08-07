package com.bravem.app.ui.marketplace;

import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bravem.app.R;
import com.bravem.app.databinding.ActivityMarketplaceItemDetailBinding;
import com.bravem.app.model.MarketplaceItem;
import com.bravem.app.utils.SessionManager;
import com.bravem.app.utils.UiUtils;
import java.util.Locale;

public class MarketplaceItemDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM = "extra_item";
    private ActivityMarketplaceItemDetailBinding binding;
    private MarketplaceItem item;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.applyEdgeToEdge(this);
        super.onCreate(savedInstanceState);
        binding = ActivityMarketplaceItemDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        item = (MarketplaceItem) getIntent().getSerializableExtra(EXTRA_ITEM);

        if (item == null) {
            finish();
            return;
        }

        setupToolbar();
        displayItemDetails();
        setupActionButton();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void displayItemDetails() {
        binding.textItemName.setText(item.getName());
        binding.textItemCategory.setText(item.getCategory());
        binding.textItemPrice.setText(String.format(Locale.getDefault(), "ZMW %.2f", item.getPrice()));
        binding.textSellerName.setText(item.getSellerName());
        binding.textItemDescription.setText(item.getDescription());
    }

    private void setupActionButton() {
        boolean isSeller = item.getSellerId().equals(sessionManager.getUid());

        if (isSeller) {
            binding.btnAction.setText(R.string.change_price);
            binding.btnAction.setOnClickListener(v -> showChangePriceDialog());
        } else {
            binding.btnAction.setText(R.string.send_proposal);
            binding.btnAction.setOnClickListener(v -> showSendProposalDialog());
        }
    }

    private void showChangePriceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.change_price);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setText(String.valueOf(item.getPrice()));
        builder.setView(input);

        builder.setPositiveButton(R.string.update, (dialog, which) -> {
            String newPriceStr = input.getText().toString();
            if (!newPriceStr.isEmpty()) {
                double newPrice = Double.parseDouble(newPriceStr);
                item.setPrice(newPrice);
                binding.textItemPrice.setText(String.format(Locale.getDefault(), "ZMW %.2f", item.getPrice()));
                Toast.makeText(this, R.string.price_updated, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showSendProposalDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.send_proposal);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint(R.string.enter_offer_hint);
        builder.setView(input);

        builder.setPositiveButton(R.string.submit, (dialog, which) -> {
            String offerStr = input.getText().toString();
            if (!offerStr.isEmpty()) {
                String msg = getString(R.string.proposal_sent_msg, offerStr, item.getSellerName());
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
