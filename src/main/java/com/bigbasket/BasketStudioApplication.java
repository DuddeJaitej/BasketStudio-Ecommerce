package com.bigbasket;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class BasketStudioApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(BasketStudioApplication.class, args);
    }

    @Bean
    CommandLineRunner seedProducts(ProductRepository repository) {
        return args -> {
            if (repository.count() > 0)
                return;
            repository.saveAll(List.of(
                    new ProductEntity("tomato", "Tomatoes", "Vegetables", 27, "wp9998853-tomato-4k-wallpapers.jpg",
                            "Vine-ripened and bright"),
                    new ProductEntity("onions", "Onions", "Vegetables", 35, "wp7253201-onions-wallpapers.jpg",
                            "Kitchen essential"),
                    new ProductEntity("garlic", "Garlic", "Vegetables", 40, "wp4309869-garlic-wallpapers.jpg",
                            "Aromatic cloves"),
                    new ProductEntity("chilli", "Green Chilli", "Vegetables", 50,
                            "wp9327759-green-chilli-wallpapers.jpg", "Fresh and fiery"),
                    new ProductEntity("spinach", "Baby Spinach", "Vegetables", 15,
                            "wp3145595-fresh-spinach-wallpapers.jpg", "Tender leafy greens"),
                    new ProductEntity("cabbage", "Cabbage", "Vegetables", 38, "wp4159429-cabbage-wallpapers.jpg",
                            "Crisp and crunchy"),
                    new ProductEntity("potato", "Potatoes", "Vegetables", 32, "wp2473648-potatoes-wallpapers.jpg",
                            "Everyday comfort"),
                    new ProductEntity("bitter-gourd", "Bitter Gourd", "Vegetables", 40,
                            "wp10276571-bitter-gourd-wallpapers.jpg", "Garden-fresh goodness"),
                    new ProductEntity("apple", "Royal Apples", "Fruits", 27, "apple.jpeg", "Sweet orchard crunch"),
                    new ProductEntity("orange", "Nagpur Oranges", "Fruits", 35, "orange.jpeg", "Citrus sunshine"),
                    new ProductEntity("banana", "Bananas", "Fruits", 40, "banana.jpeg", "Naturally energy-rich"),
                    new ProductEntity("guava", "Guavas", "Fruits", 50, "guava.jpeg", "Fragrant and juicy"),
                    new ProductEntity("dragon", "Dragon Fruit", "Fruits", 15, "dragon.jpeg", "A tropical favorite"),
                    new ProductEntity("pineapple", "Pineapple", "Fruits", 38, "pineapple.jpg", "Golden and tangy"),
                    new ProductEntity("avocado", "Avocado", "Fruits", 32, "avacado.jpg", "Silky and nourishing"),
                    new ProductEntity("strawberries", "Strawberries", "Fruits", 40, "strawberries.jpg",
                            "Ruby-red sweetness")));
        };
    }

    @Deprecated
    @RequestMapping("/api")
    static class StoreController {
        private final Map<String, User> users = new ConcurrentHashMap<>();
        private final Map<String, String> sessions = new ConcurrentHashMap<>();
        private final Map<String, Product> products = new ConcurrentHashMap<>();

        StoreController() {
            seed(
                    new Product("tomato", "Tomatoes", "Vegetables", 27, "wp9998853-tomato-4k-wallpapers.jpg",
                            "Vine-ripened and bright"),
                    new Product("onions", "Onions", "Vegetables", 35, "wp7253201-onions-wallpapers.jpg",
                            "Kitchen essential"),
                    new Product("garlic", "Garlic", "Vegetables", 40, "wp4309869-garlic-wallpapers.jpg",
                            "Aromatic cloves"),
                    new Product("chilli", "Green Chilli", "Vegetables", 50, "wp9327759-green-chilli-wallpapers.jpg",
                            "Fresh and fiery"),
                    new Product("spinach", "Baby Spinach", "Vegetables", 15, "wp3145595-fresh-spinach-wallpapers.jpg",
                            "Tender leafy greens"),
                    new Product("cabbage", "Cabbage", "Vegetables", 38, "wp4159429-cabbage-wallpapers.jpg",
                            "Crisp and crunchy"),
                    new Product("potato", "Potatoes", "Vegetables", 32, "wp2473648-potatoes-wallpapers.jpg",
                            "Everyday comfort"),
                    new Product("bitter-gourd", "Bitter Gourd", "Vegetables", 40,
                            "wp10276571-bitter-gourd-wallpapers.jpg", "Garden-fresh goodness"),
                    new Product("apple", "Royal Apples", "Fruits", 27, "apple.jpeg", "Sweet orchard crunch"),
                    new Product("orange", "Nagpur Oranges", "Fruits", 35, "orange.jpeg", "Citrus sunshine"),
                    new Product("banana", "Bananas", "Fruits", 40, "banana.jpeg", "Naturally energy-rich"),
                    new Product("guava", "Guavas", "Fruits", 50, "guava.jpeg", "Fragrant and juicy"),
                    new Product("dragon", "Dragon Fruit", "Fruits", 15, "dragon.jpeg", "A tropical favorite"),
                    new Product("pineapple", "Pineapple", "Fruits", 38, "pineapple.jpg", "Golden and tangy"),
                    new Product("avocado", "Avocado", "Fruits", 32, "avacado.jpg", "Silky and nourishing"),
                    new Product("strawberries", "Strawberries", "Fruits", 40, "strawberries.jpg",
                            "Ruby-red sweetness"));
        }

        private void seed(Product... initialProducts) {
            for (Product product : initialProducts)
                products.put(product.id(), product);
        }

        private final Map<String, Order> orders = new ConcurrentHashMap<>();

        @GetMapping("/products")
        List<Product> products(@RequestParam(required = false) String category) {
            return category == null || category.isBlank() ? products.values().stream().toList()
                    : products.values().stream().filter(p -> p.category().equalsIgnoreCase(category)).toList();
        }

        @GetMapping("/products/{id}")
        Product product(@PathVariable String id) {
            return productOr404(id);
        }

        @PostMapping("/products")
        Product createProduct(@Valid @RequestBody ProductRequest request) {
            String id = request.id().toLowerCase(Locale.ROOT);
            if (products.containsKey(id))
                throw new ResponseStatusException(HttpStatus.CONFLICT, "A product with this id already exists");
            Product product = new Product(id, request.name(), request.category(), request.price(), request.image(),
                    request.tagline());
            products.put(id, product);
            return product;
        }

        @PutMapping("/products/{id}")
        Product updateProduct(@PathVariable String id, @Valid @RequestBody ProductRequest request) {
            productOr404(id);
            Product updated = new Product(id, request.name(), request.category(), request.price(), request.image(),
                    request.tagline());
            products.put(id, updated);
            return updated;
        }

        @DeleteMapping("/products/{id}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        void deleteProduct(@PathVariable String id) {
            products.remove(id, productOr404(id));
        }

        @PostMapping("/auth/signup")
        UserView signup(@Valid @RequestBody SignupRequest request) {
            String email = request.email().toLowerCase(Locale.ROOT);
            if (users.containsKey(email))
                throw new ResponseStatusException(HttpStatus.CONFLICT, "An account already exists for this email");
            users.put(email,
                    new User(request.name(), email, hash(request.password()), request.phone(), request.dateOfBirth()));
            return loginUser(email, request.password());
        }

        @PostMapping("/auth/login")
        UserView login(@Valid @RequestBody LoginRequest request) {
            return loginUser(request.email().toLowerCase(Locale.ROOT), request.password());
        }

        @PostMapping("/orders")
        Order createOrder(@RequestHeader(value = "Authorization", required = false) String authorization,
                @Valid @RequestBody OrderRequest request) {
            String email = emailFor(authorization);
            if (request.items().isEmpty())
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your basket is empty");
            request.items().forEach(item -> productOr404(item.productId()));
            String id = "BB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
            Order order = new Order(id, email, request.items(), request.address(), request.paymentMethod(),
                    Instant.now().toString());
            orders.put(id, order);
            return order;
        }

        @GetMapping("/orders")
        List<Order> orderHistory(@RequestHeader(value = "Authorization", required = false) String authorization) {
            String email = emailFor(authorization);
            return orders.values().stream().filter(order -> order.email().equals(email))
                    .sorted(Comparator.comparing(Order::createdAt).reversed()).toList();
        }

        @GetMapping("/orders/{id}")
        Order order(@PathVariable String id,
                @RequestHeader(value = "Authorization", required = false) String authorization) {
            return ownedOrder(id, emailFor(authorization));
        }

        @PutMapping("/orders/{id}")
        Order updateOrder(@PathVariable String id,
                @RequestHeader(value = "Authorization", required = false) String authorization,
                @Valid @RequestBody OrderRequest request) {
            String email = emailFor(authorization);
            ownedOrder(id, email);
            if (request.items().isEmpty())
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your basket is empty");
            request.items().forEach(item -> productOr404(item.productId()));
            Order updated = new Order(id, email, request.items(), request.address(), request.paymentMethod(),
                    orders.get(id).createdAt());
            orders.put(id, updated);
            return updated;
        }

        @DeleteMapping("/orders/{id}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        void deleteOrder(@PathVariable String id,
                @RequestHeader(value = "Authorization", required = false) String authorization) {
            orders.remove(id, ownedOrder(id, emailFor(authorization)));
        }

        @GetMapping("/profile")
        UserView profile(@RequestHeader(value = "Authorization", required = false) String authorization) {
            String email = emailFor(authorization);
            User user = users.get(email);
            return new UserView(user.name(), user.email(), user.phone(), user.dateOfBirth(), sessions.entrySet()
                    .stream().filter(e -> e.getValue().equals(email)).map(Map.Entry::getKey).findFirst().orElse(""));
        }

        private UserView loginUser(String email, String password) {
            User user = users.get(email);
            if (user == null || !user.passwordHash().equals(hash(password)))
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email or password is incorrect");
            String token = UUID.randomUUID().toString();
            sessions.put(token, email);
            return new UserView(user.name(), user.email(), user.phone(), user.dateOfBirth(), token);
        }

        private Product productOr404(String id) {
            Product product = products.get(id);
            if (product == null)
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
            return product;
        }

        private Order ownedOrder(String id, String email) {
            Order order = orders.get(id);
            if (order == null || !order.email().equals(email))
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
            return order;
        }

        private String emailFor(String authorization) {
            if (authorization == null || !authorization.startsWith("Bearer "))
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please sign in first");
            String email = sessions.get(authorization.substring(7));
            if (email == null)
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Your session has expired");
            return email;
        }

        private static String hash(String value) {
            try {
                return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes()));
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    record Product(String id, String name, String category, int price, String image, String tagline) {
    }

    record ProductRequest(@NotBlank String id, @NotBlank String name, @NotBlank String category, @Min(1) int price,
            @NotBlank String image, @NotBlank String tagline) {
    }

    record SignupRequest(@NotBlank String name, @Email @NotBlank String email, @Size(min = 8) String password,
            @NotBlank String phone, @NotBlank String dateOfBirth) {
    }

    record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {
    }

    record User(String name, String email, String passwordHash, String phone, String dateOfBirth) {
    }

    record UserView(String name, String email, String phone, String dateOfBirth, String token) {
    }

    record BasketItem(@NotBlank String productId, @Min(1) int quantity) {
    }

    record Address(@NotBlank String name, @NotBlank String phone, @NotBlank String line, @NotBlank String city,
            @NotBlank String state, @NotBlank String pincode) {
    }

    record OrderRequest(@NotEmpty List<@Valid BasketItem> items, @Valid Address address,
            @NotBlank String paymentMethod) {
    }

    record Order(String id, String email, List<BasketItem> items, Address address, String paymentMethod,
            String createdAt) {
    }
}
