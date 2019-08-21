package greencity.service;

import greencity.entity.User;

import java.util.List;

public interface UserService {
    User save(User user);

    User update(User user);

    User findById(Long id);

    List<User> findAll();

    void deleteById(Long id);

    User findByEmail(String email);
}