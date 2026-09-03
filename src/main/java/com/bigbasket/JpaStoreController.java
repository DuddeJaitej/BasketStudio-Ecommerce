package com.bigbasket;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api")
public class JpaStoreController {
    private final UserRepository users;
    private final ProductRepository products;
    private final OrderRepository orders;
    private final Map<String, String> sessions = new ConcurrentHashMap<>();

    public JpaStoreController(UserRepository users, ProductRepository products, OrderRepository orders) { this.users = users; this.products = products; this.orders = orders; }

    @GetMapping("/products")
    public List<ProductResponse> products(@RequestParam(required = false) String category) { List<ProductEntity> result = category == null || category.isBlank() ? products.findAll() : products.findByCategoryIgnoreCase(category); return result.stream().map(JpaStoreController::productResponse).toList(); }
    @GetMapping("/products/{id}") public ProductResponse product(@PathVariable String id) { return productResponse(productOr404(id)); }
    @PostMapping("/products") public ProductResponse createProduct(@Valid @RequestBody ProductRequest request) { String id = request.id().toLowerCase(Locale.ROOT); if (products.existsById(id)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Product already exists"); return productResponse(products.save(new ProductEntity(id, request.name(), request.category(), request.price(), request.image(), request.tagline()))); }
    @PutMapping("/products/{id}") public ProductResponse updateProduct(@PathVariable String id, @Valid @RequestBody ProductRequest request) { productOr404(id); return productResponse(products.save(new ProductEntity(id, request.name(), request.category(), request.price(), request.image(), request.tagline()))); }
    @DeleteMapping("/products/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteProduct(@PathVariable String id) { products.delete(productOr404(id)); }

    @PostMapping("/auth/signup") public UserResponse signup(@Valid @RequestBody SignupRequest request) { String email = request.email().toLowerCase(Locale.ROOT); if (users.findByEmailIgnoreCase(email).isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT, "An account already exists for this email"); UserEntity user = users.save(new UserEntity(request.name(), email, hash(request.password()), request.phone(), LocalDate.parse(request.dateOfBirth()))); return loginUser(user, request.password()); }
    @PostMapping("/auth/login") public UserResponse login(@Valid @RequestBody LoginRequest request) { UserEntity user = users.findByEmailIgnoreCase(request.email()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email or password is incorrect")); return loginUser(user, request.password()); }
    @PostMapping("/auth/logout") @ResponseStatus(HttpStatus.NO_CONTENT) public void logout(@RequestHeader(value = "Authorization", required = false) String authorization) { if (authorization != null && authorization.startsWith("Bearer ")) sessions.remove(authorization.substring(7)); }

    @PostMapping("/orders") @Transactional public OrderResponse createOrder(@RequestHeader(value = "Authorization", required = false) String authorization, @Valid @RequestBody OrderRequest request) { UserEntity user = userFor(authorization); OrderEntity order = new OrderEntity("BB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT), user, request.paymentMethod(), new OrderEntity.AddressData(request.address().name(), request.address().phone(), request.address().line(), request.address().city(), request.address().state(), request.address().pincode())); request.items().forEach(item -> order.addItem(productOr404(item.productId()), item.quantity())); return orderResponse(orders.save(order)); }
    @GetMapping("/orders") @Transactional(readOnly = true) public List<OrderResponse> orderHistory(@RequestHeader(value = "Authorization", required = false) String authorization) { return orders.findByUserEmailOrderByCreatedAtDesc(userFor(authorization).getEmail()).stream().map(this::orderResponse).toList(); }
    @GetMapping("/orders/{id}") @Transactional(readOnly = true) public OrderResponse order(@PathVariable String id, @RequestHeader(value = "Authorization", required = false) String authorization) { return orderResponse(ownedOrder(id, userFor(authorization))); }
    @DeleteMapping("/orders/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteOrder(@PathVariable String id, @RequestHeader(value = "Authorization", required = false) String authorization) { orders.delete(ownedOrder(id, userFor(authorization))); }
    @GetMapping("/profile") public UserResponse profile(@RequestHeader(value = "Authorization", required = false) String authorization) { return userResponse(userFor(authorization), tokenFor(authorization)); }

    private UserResponse loginUser(UserEntity user, String password) { if (!user.getPasswordHash().equals(hash(password))) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email or password is incorrect"); String token = UUID.randomUUID().toString(); sessions.put(token, user.getEmail()); return userResponse(user, token); }
    private UserEntity userFor(String authorization) { String email = tokenEmail(authorization); return users.findByEmailIgnoreCase(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User account not found")); }
    private String tokenEmail(String authorization) { if (authorization == null || !authorization.startsWith("Bearer ")) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please sign in first"); String email = sessions.get(authorization.substring(7)); if (email == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Your session has expired"); return email; }
    private String tokenFor(String authorization) { return authorization.substring(7); }
    private OrderEntity ownedOrder(String id, UserEntity user) { return orders.findByIdAndUserEmail(id, user.getEmail()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found")); }
    private ProductEntity productOr404(String id) { return products.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found")); }
    private static ProductResponse productResponse(ProductEntity p) { return new ProductResponse(p.getId(), p.getName(), p.getCategory(), p.getPrice(), p.getImage(), p.getTagline()); }
    private static UserResponse userResponse(UserEntity u, String token) { return new UserResponse(u.getName(), u.getEmail(), u.getPhone(), u.getDateOfBirth().toString(), token); }
    private OrderResponse orderResponse(OrderEntity order) { List<OrderItemResponse> items = order.getItems().stream().map(item -> new OrderItemResponse(item.getProduct().getId(), item.getProduct().getName(), item.getProduct().getImage(), item.getQuantity(), item.getUnitPrice())).toList(); return new OrderResponse(order.getId(), order.getStatus(), order.getPaymentMethod(), order.getCreatedAt().toString(), items, items.stream().mapToInt(item -> item.quantity() * item.unitPrice()).sum()); }
    private static String hash(String value) { try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); } catch (Exception e) { throw new IllegalStateException(e); } }

    record ProductResponse(String id, String name, String category, int price, String image, String tagline) {}
    record ProductRequest(@NotBlank String id, @NotBlank String name, @NotBlank String category, @Min(1) int price, @NotBlank String image, @NotBlank String tagline) {}
    record SignupRequest(@NotBlank String name, @Email @NotBlank String email, @Size(min = 8) String password, @NotBlank String phone, @NotBlank String dateOfBirth) {}
    record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}
    record UserResponse(String name, String email, String phone, String dateOfBirth, String token) {}
    record BasketItem(@NotBlank String productId, @Min(1) int quantity) {}
    record Address(@NotBlank String name, @NotBlank String phone, @NotBlank String line, @NotBlank String city, @NotBlank String state, @NotBlank String pincode) {}
    record OrderRequest(@NotEmpty List<@Valid BasketItem> items, @Valid Address address, @NotBlank String paymentMethod) {}
    record OrderItemResponse(String productId, String name, String image, int quantity, int unitPrice) {}
    record OrderResponse(String id, String status, String paymentMethod, String createdAt, List<OrderItemResponse> items, int total) {}
}
