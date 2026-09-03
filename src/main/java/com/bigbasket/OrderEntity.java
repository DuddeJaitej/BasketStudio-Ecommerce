package com.bigbasket;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class OrderEntity {
    @Id @Column(length = 20) private String id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private UserEntity user;
    @Column(nullable = false, length = 30) private String status = "PLACED";
    @Column(name = "payment_method", nullable = false, length = 40) private String paymentMethod;
    @Column(name = "address_name", nullable = false, length = 120) private String addressName;
    @Column(name = "address_phone", nullable = false, length = 20) private String addressPhone;
    @Column(name = "address_line", nullable = false, length = 255) private String addressLine;
    @Column(name = "address_city", nullable = false, length = 100) private String addressCity;
    @Column(name = "address_state", nullable = false, length = 100) private String addressState;
    @Column(name = "address_pincode", nullable = false, length = 10) private String addressPincode;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true) private List<OrderItemEntity> items = new ArrayList<>();

    protected OrderEntity() {}
    public OrderEntity(String id, UserEntity user, String paymentMethod, AddressData address) { this.id=id; this.user=user; this.paymentMethod=paymentMethod; this.addressName=address.name(); this.addressPhone=address.phone(); this.addressLine=address.line(); this.addressCity=address.city(); this.addressState=address.state(); this.addressPincode=address.pincode(); this.createdAt=Instant.now(); }
    public void addItem(ProductEntity product, int quantity) { items.add(new OrderItemEntity(this, product, quantity, product.getPrice())); }
    public String getId() { return id; }
    public UserEntity getUser() { return user; }
    public String getStatus() { return status; }
    public String getPaymentMethod() { return paymentMethod; }
    public Instant getCreatedAt() { return createdAt; }
    public List<OrderItemEntity> getItems() { return items; }
    public record AddressData(String name, String phone, String line, String city, String state, String pincode) {}
}
