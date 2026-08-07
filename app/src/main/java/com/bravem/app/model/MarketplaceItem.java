package com.bravem.app.model;

import java.io.Serializable;
import java.util.List;

public class MarketplaceItem implements Serializable {
    private String id;
    private String sellerId;
    private String sellerName;
    private String name;
    private String category;
    private double price;
    private String description;
    private List<String> imageUris;
    private long timestamp;

    public MarketplaceItem() {}

    public MarketplaceItem(String id, String sellerId, String sellerName, String name, String category, double price, String description, List<String> imageUris) {
        this.id = id;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.name = name;
        this.category = category;
        this.price = price;
        this.description = description;
        this.imageUris = imageUris;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<String> getImageUris() { return imageUris; }
    public void setImageUris(List<String> imageUris) { this.imageUris = imageUris; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
