package cz.meind.database.entities;

import cz.meind.interfaces.Column;
import cz.meind.interfaces.Entity;
import cz.meind.interfaces.ManyToMany;

import java.util.Set;

@Entity(tableName = "orders")
public class Order {
    @Column(name = "order_id", unique = true)
    Long orderId;

    @ManyToMany(joinTable = "user_orders")
    Set<User> users;
}
