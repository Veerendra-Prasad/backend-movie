package dev.farhan.movieist.model;

import dev.farhan.movieist.dto.RatingDto;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "movies")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Movie {
    @Id
    private String id;
    private String title;
    private String releaseDate;
    private List<String> genres;
    private String poster;
    private Double imdbRating;
    private String plot;
    private String rated;

    private List<ObjectId> reviewIds;

    private String director;
    private String writer;
    private String actors;
    private String runtime;
    private String language;
    private String boxOffice;
    private String type;
    private List<RatingDto> rating;

    public Movie(String title, String releaseDate, List<String> genres, String poster, Double imdbRating, String plot, String rated, List<ObjectId> reviewIds, String director, String writer, String actors, String runtime, String language, String boxOffice, String type, List<RatingDto> rating) {
        this.title = title;
        this.releaseDate = releaseDate;
        this.genres = genres;
        this.poster = poster;
        this.imdbRating = imdbRating;
        this.plot = plot;
        this.rated = rated;
        this.reviewIds = reviewIds;
        this.director = director;
        this.writer = writer;
        this.actors = actors;
        this.runtime = runtime;
        this.language = language;
        this.boxOffice = boxOffice;
        this.type = type;
        this.rating = rating;
    }
}
