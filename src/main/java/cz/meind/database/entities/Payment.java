package cz.meind.database.entities;

import cz.meind.interfaces.*;

import java.time.LocalDateTime;

@Entity(tableName = "Payment")
public class Payment {

    @Column(name = "id", id = true)
    private Integer id;

    @Column(name = "order_id")
    private int orderId;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "amount")
    private float amount;

    @Column(name = "payment_type")
    private String paymentType;

    @OneToMany
    @JoinColumn(name = "order_id")
    private Order order;

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    @Override
    public String toString() {
        return "Payment{" + "id=" + id + ", orderId=" + orderId + ", paymentDate=" + paymentDate + ", amount=" + amount + ", paymentType='" + paymentType + '\'' + ", order=" + order + '}';
    }
}
