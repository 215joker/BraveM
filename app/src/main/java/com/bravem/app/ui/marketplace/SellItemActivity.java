package com.bravem.app.ui.marketplace;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bravem.app.R;
import com.bravem.app.adapter.ImageAdapter;
import com.bravem.app.databinding.ActivitySellItemBinding;
import com.bravem.app.utils.UiUtils;
import java.util.ArrayList;
import java.util.List;

public class SellItemActivity extends AppCompatActivity {

    private ActivitySellItemBinding binding;
    private final List<Uri> selectedImages = new ArrayList<>();
    private ImageAdapter imageAdapter;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    if (result.getData().getClipData() != null) {
                        int count = result.getData().getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            selectedImages.add(result.getData().getClipData().getItemAt(i).getUri());
                        }
                    } else if (result.getData().getData() != null) {
                        selectedImages.add(result.getData().getData());
                    }
                    imageAdapter.notifyDataSetChanged();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.applyEdgeToEdge(this);
        super.onCreate(savedInstanceState);
        binding = ActivitySellItemBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        binding.toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        UiUtils.handleTopInset(binding.appBar);

        setupCategorySpinner();
        setupImageRecyclerView();

        binding.btnPost.setOnClickListener(v -> {
            if (validateFields()) {
                Toast.makeText(this, R.string.listing_success, Toast.LENGTH_LONG).show();
                finish();
            }
        });

        binding.btnAddImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            pickImageLauncher.launch(Intent.createChooser(intent, "Select Pictures"));
        });
    }

    private void setupCategorySpinner() {
        String[] categories = {
            getString(R.string.lab_coats),
            getString(R.string.books),
            getString(R.string.nurse_materials)
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        binding.spinnerCategory.setAdapter(adapter);
    }

    private void setupImageRecyclerView() {
        imageAdapter = new ImageAdapter(selectedImages, position -> {
            selectedImages.remove(position);
            imageAdapter.notifyItemRemoved(position);
            imageAdapter.notifyItemRangeChanged(position, selectedImages.size());
        });
        binding.recyclerImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerImages.setAdapter(imageAdapter);
    }

    private boolean validateFields() {
        if (binding.etItemName.getText().toString().trim().isEmpty()) {
            binding.etItemName.setError(getString(R.string.error_required_field));
            return false;
        }
        if (binding.etItemPrice.getText().toString().trim().isEmpty()) {
            binding.etItemPrice.setError(getString(R.string.error_required_field));
            return false;
        }
        if (binding.spinnerCategory.getText().toString().trim().isEmpty()) {
            binding.spinnerCategory.setError(getString(R.string.error_required_field));
            return false;
        }
        return true;
    }
}
