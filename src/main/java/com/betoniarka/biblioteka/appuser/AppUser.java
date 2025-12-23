package com.betoniarka.biblioteka.appuser;

import com.betoniarka.biblioteka.book.Book;
import com.betoniarka.biblioteka.borrowedbook.BorrowedBook;
import com.betoniarka.biblioteka.queueentry.QueueEntry;
import com.betoniarka.biblioteka.review.Review;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "app_user")
public class AppUser {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Getter
    @Setter
    @Column(unique = true)
    @NotNull(message = "username is required")
    private String username;

    @Setter
    @Column
    @NotNull(message = "password is required")
    private String password;

    @Getter
    @Setter
    @Column
    private String firstname;

    @Getter
    @Setter
    @Column
    private String lastname;

    @Getter
    @Setter
    @Column(unique = true)
    @NotNull(message = "email is required")
    private String email;

    @Getter
    @Setter
    @Column
    @Enumerated(EnumType.STRING)
    private AppRole role;

    @OneToMany(mappedBy = "appUser")
    private List<BorrowedBook> borrowedBooks = new ArrayList<>();

    @OneToMany(mappedBy = "appUser")
    private List<QueueEntry> queuedBooks = new ArrayList<>();

    @OneToMany(mappedBy = "appUser")
    private List<Review> reviews = new ArrayList<>();

    public AppUser() {}

    public AppUser(AppRole role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        AppUser appUser = (AppUser) o;
        return id == appUser.id &&
                Objects.equals(email, appUser.email) &&
                Objects.equals(firstname, appUser.firstname) &&
                Objects.equals(lastname, appUser.lastname) &&
                Objects.equals(role, appUser.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, firstname, lastname, role);
    }

}
