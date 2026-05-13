package com.satyanand.shardedsagawallet.services;

import com.satyanand.shardedsagawallet.dtos.PaginatedResponse;
import com.satyanand.shardedsagawallet.entities.User;
import com.satyanand.shardedsagawallet.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

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

    public User getUserById(Long id){
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<User> getUsersByName(String name){
        return userRepository.findByNameContainingIgnoreCase(name);
    }

    public PaginatedResponse<User> getAllUsers(int page, int size){
        Pageable pageable = PageRequest.of(page, size);

        Page<User> userPage = userRepository.findAll(pageable);

        return PaginatedResponse.<User>builder()
                .content(userPage.getContent())
                .page(userPage.getNumber())
                .size(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .first(userPage.isFirst())
                .last(userPage.isLast())
                .hasNext(userPage.hasNext())
                .hasPrevious(userPage.hasPrevious())
                .build();
    }
}
