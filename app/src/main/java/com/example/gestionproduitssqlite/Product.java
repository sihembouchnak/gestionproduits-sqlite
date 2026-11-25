package com.example.gestionproduitssqlite;

import com.google.firebase.firestore.Exclude;

public class Product {
    private String documentId;
    private int id;
    private String name;
    private String description;
    private double price;

    public Product() {
    }

    public Product(int id, String name, String description, double price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
    }

    public Product(String documentId, String name, String description, double price) {
        this.documentId = documentId;
        this.name = name;
        this.description = description;
        this.price = price;
    }

    @Exclude
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        if (description != null && !description.trim().isEmpty()) {
            return name + " - " + price + " € (" + description + ")";
        }
        return name + " - " + price + " €";
    }
}
