package com.betoniarka.biblioteka.notifications;

import com.betoniarka.biblioteka.appuser.AppUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Entity
@Table(name = "notifications")
public class Notification {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Getter
    @Setter
    @Column
    private String subject;

    @Getter
    @Setter
    @Column
    private String message;

    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "app_user_id")
    private AppUser appUser;

    public Notification() {
    }

    public Notification(String subject, String message) {
        this.subject = subject;
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return id == that.id && Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, message);
    }

}
