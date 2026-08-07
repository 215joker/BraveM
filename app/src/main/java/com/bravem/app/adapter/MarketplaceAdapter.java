package com.bravem.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bravem.app.R;
import com.bravem.app.model.MarketplaceItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MarketplaceAdapter extends RecyclerView.Adapter<MarketplaceAdapter.ViewHolder> {

    private List<MarketplaceItem> items = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(MarketplaceItem item);
    }

    public MarketplaceAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<MarketplaceItem> newList) {
        this.items = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_marketplace, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MarketplaceItem item = items.get(position);
        holder.nameText.setText(item.getName());
        holder.categoryText.setText(item.getCategory());
        holder.priceText.setText(String.format(Locale.getDefault(), "ZMW %.2f", item.getPrice()));
        holder.sellerText.setText(item.getSellerName());

        if (item.getImageUris() != null && !item.getImageUris().isEmpty()) {
            // Load image if available (using simple URI for now as placeholder for proper image loading)
            // holder.imageView.setImageURI(Uri.parse(item.getImageUris().get(0)));
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameText, categoryText, priceText, sellerText;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.img_item);
            nameText = itemView.findViewById(R.id.text_item_name);
            categoryText = itemView.findViewById(R.id.text_item_category);
            priceText = itemView.findViewById(R.id.text_item_price);
            sellerText = itemView.findViewById(R.id.text_seller_name);
        }
    }
}
