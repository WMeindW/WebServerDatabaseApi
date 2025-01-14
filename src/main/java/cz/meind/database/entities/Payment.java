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
    private int cvv;

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

    public int getCvv() {
        return cvv;
    }

    public void setCvv(int cvv) {
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
