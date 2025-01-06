package cz.meind.database.entities;

import cz.meind.interfaces.Column;
import cz.meind.interfaces.Entity;

@Entity(tableName = "user_types")
public class UserType {

    @Column(name = "user_type_id", id = true)
    private int userTypeId;

    @Column(name = "user_type_name")
    private String userTypeName;

    // Getters and setters
    public Integer getUserTypeId() {
        return userTypeId;
    }

    public void setUserTypeId(Integer userTypeId) {
        this.userTypeId = userTypeId;
    }

    public String getUserTypeName() {
        return userTypeName;
    }

    public void setUserTypeName(String userTypeName) {
        this.userTypeName = userTypeName;
    }

    @Override
    public String toString() {
        return "UserType{" + "userTypeId=" + userTypeId + ", userTypeName='" + userTypeName + '\'' + '}';
    }
}

