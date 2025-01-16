package cz.meind.database.entities;

import cz.meind.interfaces.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Entity(tableName = "Orders")
public class Order {

    @Column(name = "id", id = true)
    private Integer id;

    @OneToMany
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @Column(name = "status")
    private String status;

    @Column(name = "total_price")
    private float totalPrice;

    @ManyToMany(joinTable = "Order_Product", mappedBy = "order_id", targetColumn = "product_id")
    private List<Product> products;

    @ManyToOne(tableName = "Payment")
    @JoinColumn(name = "order_id")
    private List<Payment> payment;

    public List<Payment> getPayments() {
        return payment;
    }

    public void setPayment(List<Payment> payment) {
        this.payment = payment;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public float getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(float totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id);
    }


    @Override
    public String toString() {
        return "[" + id + "] products: " + products + " " + totalPrice + payment + "\n";
    }
}

