package com.bigbasket;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class ProductEntity {
    @Id
    @Column(length = 80)
    private String id;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(nullable = false, length = 40)
    private String category;
    @Column(nullable = false)
    private int price;
    @Column(nullable = false, length = 255)
    private String image;
    @Column(nullable = false, length = 255)
    private String tagline;

    protected ProductEntity() {
    }

    public ProductEntity(String id, String name, String category, int price, String image, String tagline) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.image = image;
        this.tagline = tagline;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public int getPrice() {
        return price;
    }

    public String getImage() {
        return image;
    }

    public String getTagline() {
        return tagline;
    }
}
