package com.betoniarka.biblioteka.appuser;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);

  boolean existsByUsernameAndIdNot(String username, Long id);

  boolean existsByEmailAndIdNot(String email, Long id);

  Optional<AppUser> findByUsername(String username);
}
