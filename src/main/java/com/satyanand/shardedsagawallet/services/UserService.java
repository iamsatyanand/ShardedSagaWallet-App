package com.satyanand.shardedsagawallet.services;

import com.satyanand.shardedsagawallet.entities.User;
import com.satyanand.shardedsagawallet.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public User createUser(User user){
        log.info("Creating user: {}", user.getEmail());
        User newUser = userRepository.save(user);
        log.info("User created with id: {} in database: shardwallet{}", newUser.getId(), (newUser.getId() % 2 + 1));
        return newUser;
    }
}
