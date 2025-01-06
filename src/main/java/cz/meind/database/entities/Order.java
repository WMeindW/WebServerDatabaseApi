package cz.meind.database.entities;

import cz.meind.interfaces.Column;
import cz.meind.interfaces.Entity;
import cz.meind.interfaces.ManyToMany;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Set;

@Entity(tableName = "orders")
public class Order {
    @Column(name = "order_id", id = true)
    private int orderId;

    @Column(name ="order_date")
    private Timestamp orderDate;

    @Column(name = "total_amount")
    private BigDecimal orderTotal;

    @ManyToMany(joinTable = "user_orders", mappedBy = "order_id", targetColumn = "user_id")
    private Collection<User> users;

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public Timestamp getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Timestamp orderDate) {
        this.orderDate = orderDate;
    }

    public BigDecimal getOrderTotal() {
        return orderTotal;
    }

    public void setOrderTotal(BigDecimal orderTotal) {
        this.orderTotal = orderTotal;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Collection<User> getUsers() {
        return users;
    }

    public void setUsers(Collection<User> users) {
        this.users = users;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", orderDate=" + orderDate.toLocalDateTime() +
                ", orderTotal=" + orderTotal +
                '}';
    }
}
