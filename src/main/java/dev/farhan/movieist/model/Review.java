package dev.farhan.movieist.model;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "reviews")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Review {
    @Id
    private ObjectId id;

    @Field(name = "user_id")
    @NotNull
    private ObjectId userId;

    private String body;

    private LocalDateTime created;

    private LocalDateTime updated;

    public Review(ObjectId userId, String body, LocalDateTime created, LocalDateTime updated) {
        this.userId = userId;
        this.body = body;
        this.created = created;
        this.updated = updated;
    }

}
