package cz.meind.database.entities;

import cz.meind.interfaces.Column;
import cz.meind.interfaces.Entity;

import java.sql.Timestamp;
import java.util.Date;

@Entity(tableName = "Customer")
public class Customer {

    @Column(name = "id", id = true)
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "address")
    private String address;

    @Column(name = "phone")
    private String phone;

    @Column(name = "registration_date")
    private Date registrationDate;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    @Override
    public String toString() {
        return "Customer{" + "registrationDate=" + registrationDate + ", phone='" + phone + '\'' + ", address='" + address + '\'' + ", email='" + email + '\'' + ", name='" + name + '\'' + ", id=" + id + '}';
    }
}
