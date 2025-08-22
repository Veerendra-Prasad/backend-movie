package dev.farhan.movieist.service;


import dev.farhan.movieist.model.Movie;
import dev.farhan.movieist.model.User;
import dev.farhan.movieist.repository.MovieRepository;
import dev.farhan.movieist.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> allUsers() {
        List<User> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add);
        return users;
    }

    public void setLikedMovies(List<String> movies) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();

        User user = userRepository.findByUsername(name).orElseThrow(() -> new RuntimeException("User not found"));

        user.setLikedMoviesIds(movies);

        userRepository.save(user);
    }

}
