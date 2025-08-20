package dev.farhan.movieist.model;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Document(collection = "users")
@Getter
@Setter
public class User implements UserDetails {
    @Id
    private ObjectId id;
    @Indexed(unique = true)
    @NotNull
    private String username;
    @Indexed(unique = true)
    @NotNull
    private String email;
    @NotNull
    private String password;

    private boolean enabled;
    @Field(name = "verification_code")
    private String verificationCode;

    @Field(name = "verification_expiration")
    @Indexed(expireAfter = "0s")
    private LocalDateTime verificationCodeExpiresAt;


    private List<ObjectId> likedMoviesIds = new ArrayList<>();

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public User() {
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
        return List.of();
    }

    @Override
    public boolean isAccountNonExpired(){
        return true;
    }

    @Override
    public boolean isAccountNonLocked(){
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired(){
        return true;
    }

    @Override
    public boolean isEnabled(){
        return enabled;
    }
}
