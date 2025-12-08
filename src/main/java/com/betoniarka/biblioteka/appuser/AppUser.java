package com.betoniarka.biblioteka.appuser;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Entity
@Table(name = "appuser")
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
    private String firstname;


    @Getter
    @Setter
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

    // @ManyToMany
    // @JoinTable(
    // name = "student_course",
    // joinColumns = @JoinColumn(name = "student_id", referencedColumnName = "ID"),
    // inverseJoinColumns = @JoinColumn(name = "course_id", referencedColumnName =
    // "ID"))
    // private Set<Student> studentSet = new HashSet<>();
    //
    // @OneToMany(mappedBy = "course")
    // private Set<Grade> gradeSet = new HashSet<>();

    public AppUser() {}

    public AppUser(AppRole role) {
        this.role = role;
    }

    /*********************************************************************************/

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
