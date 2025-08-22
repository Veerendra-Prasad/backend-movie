package dev.farhan.movieist.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReviewDto {
    private String username;
    private String body;
    private LocalDateTime created;
    private LocalDateTime updated;

    public ReviewDto(String username, String body, LocalDateTime created, LocalDateTime updated) {
        this.username = username;
        this.body = body;
        this.created = created;
        this.updated = updated;
    }

    @Override
    public String toString() {
        return "ReviewDto{" +
                "username='" + username + '\'' +
                ", body='" + body + '\'' +
                ", created=" + created +
                ", updated=" + updated +
                '}';
    }
}
