package dev.farhan.movieist.model;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
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
    private ObjectId userId;

    private String body;

    private LocalDateTime created;

    public Review(ObjectId userId, String body, LocalDateTime created) {
        this.userId = userId;
        this.body = body;
        this.created = created;
    }

}
