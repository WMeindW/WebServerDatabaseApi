package cz.meind.database.entities;

import cz.meind.interfaces.Column;
import cz.meind.interfaces.Entity;
import cz.meind.interfaces.ManyToMany;
import cz.meind.interfaces.ManyToOne;

import java.util.Date;

@Entity(tableName = "Payment")
public class Payment {

    @Column(name = "id", id = true)
    private Integer id;

    @Column(name = "order_id")
    private int orderId;

    @Column(name = "payment_date")
    private Date paymentDate;

    @Column(name = "amount")
    private float amount;

    @Column(name = "payment_type")
    private String paymentType;

    @ManyToOne
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

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
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
}
