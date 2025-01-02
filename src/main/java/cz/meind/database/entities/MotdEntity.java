package cz.meind.database.entities;

import cz.meind.interfaces.Column;
import cz.meind.interfaces.Entity;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity(tableName = "motd")
public class MotdEntity {

    @Column(name = "id", unique = true)
    private Long id;

    @Column(name = "content")
    private String content;

    @Column(name = "date")
    private LocalDateTime date;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public MotdEntity() {

    }

    public MotdEntity(String content, LocalDateTime date) {
        this.content = content;
        this.date = date;
    }

    @Override
    public String toString() {
        return "MotdEntity{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", date=" + date +
                '}';
    }
}

