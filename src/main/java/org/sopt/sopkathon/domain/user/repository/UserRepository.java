package org.sopt.sopkathon.domain.user.repository;

import java.util.Optional;
import org.sopt.sopkathon.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByPhoneNumber(String phoneNumber);
}
