package dev.farhan.movieist.responses;

import dev.farhan.movieist.model.Movie;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.util.List;

@Getter
@Setter
public class LoginResponse {
    private String id;
    private String token;
    private long expiresIn;
    private String username;
    private List<String> likedMoviesIds;
    private String email;

    public LoginResponse() {

    }
}
