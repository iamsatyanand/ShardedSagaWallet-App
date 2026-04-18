package com.satyanand.shardedsagawallet.repositories;

import com.satyanand.shardedsagawallet.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

}
