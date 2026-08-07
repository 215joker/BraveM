package com.bravem.app.ui.library;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bravem.app.adapter.BookAdapter;
import com.bravem.app.databinding.ActivityLibraryBinding;
import com.bravem.app.model.Book;
import com.bravem.app.utils.UiUtils;
import java.util.ArrayList;
import java.util.List;

public class LibraryActivity extends AppCompatActivity {

    private ActivityLibraryBinding binding;
    private BookAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.applyEdgeToEdge(this);
        super.onCreate(savedInstanceState);
        binding = ActivityLibraryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        binding.toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        
        UiUtils.handleTopInset((android.view.View) binding.toolbar.getParent());

        setupRecyclerView();
        loadBooks();

        binding.btnBookStudySession.setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, BookStudySessionActivity.class));
        });
    }

    private void setupRecyclerView() {
        adapter = new BookAdapter(book -> {
            Toast.makeText(this, "Viewing: " + book.getTitle(), Toast.LENGTH_SHORT).show();
        });
        binding.recyclerBooks.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerBooks.setAdapter(adapter);
        binding.recyclerBooks.setNestedScrollingEnabled(false);
    }

    private void loadBooks() {
        List<Book> mockBooks = new ArrayList<>();
        mockBooks.add(new Book("1", "Anatomy & Physiology", "Kevin Patton", "Medical", "978-0323", true, "Comprehensive guide to human body."));
        mockBooks.add(new Book("2", "Microbiology: An Introduction", "Gerard Tortora", "Biology", "978-0134", true, "Classic textbook for biology students."));
        mockBooks.add(new Book("3", "Pharmacology for Nurses", "Michael Adams", "Nursing", "978-0135", false, "Essential clinical pharmacology."));
        mockBooks.add(new Book("4", "The Cell: A Molecular Approach", "Geoffrey Cooper", "Biochemistry", "978-1605", true, "In-depth biochemistry and cell biology."));
        
        adapter.submitList(mockBooks);
    }
}
