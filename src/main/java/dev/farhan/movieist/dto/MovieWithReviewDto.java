package dev.farhan.movieist.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MovieWithReviewDto {
    private String title;
    private String releaseDate;
    private List<String> genres;
    private String poster;
    private Double imdbRating;
    private String plot;
    private String rated;

    private List<ReviewDto> reviews;

    private String director;

    private String writer;
    private String actors;
    private String runtime;
    private String language;
    private String boxOffice;
    private String type;
    private List<RatingDto> rating;
}
