package com.example.streams.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Order {

    @JsonProperty("orderId")
    private String orderId;

    @JsonProperty("customerId")
    private String customerId;

    @JsonProperty("product")
    private String product;

    @JsonProperty("quantity")
    private int quantity;

    @JsonProperty("price")
    private double price;

    @JsonProperty("source")
    private String source;

    public Order() {
    }

    public Order(String orderId, String customerId, String product, int quantity, double price, String source) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.product = product;
        this.quantity = quantity;
        this.price = price;
        this.source = source;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    @Override
    public String toString() {
        return "Order{orderId='" + orderId + "', customerId='" + customerId +
               "', product='" + product + "', qty=" + quantity +
               ", price=" + price + ", source='" + source + "'}";
    }
}
