package cz.meind.database.entities;

import cz.meind.interfaces.*;

import java.time.LocalDateTime;

@Entity(tableName = "Payment")
public class Payment {

    @Column(name = "id", id = true)
    private Integer id;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "amount")
    private float amount;

    @Column(name = "holder")
    private String holder;

    @Column(name = "cvv")
    private String cvv;

    @Column(name = "card_number")
    private String cardNumber;

    @Column(name = "expire")
    private String expire;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getHolder() {
        return holder;
    }

    public void setHolder(String holder) {
        this.holder = holder;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getExpire() {
        return expire;
    }

    public void setExpire(String expire) {
        this.expire = expire;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Payment(String expire, String cardNumber, String cvv, String holder, float amount) {
        this.expire = expire;
        this.cardNumber = cardNumber;
        this.cvv = cvv;
        this.holder = holder;
        this.amount = amount;
        this.paymentDate = LocalDateTime.now();
    }

    @OneToMany
    @JoinColumn(name = "order_id")
    private Order order;

    @Override
    public String toString() {
        return "Payment{" +
                "expire='" + expire + '\'' +
                ", cardNumber='" + cardNumber + '\'' +
                ", holder='" + holder + '\'' +
                ", paymentDate=" + paymentDate +
                ", amount=" + amount +
                ", id=" + id +
                '}';
    }
}
