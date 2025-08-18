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
    private String id;
    private String title;
    private String releaseDate;
    private List<String> genres;
    private String poster;
    private Double imdbRating;
    private String plot;

    @Override
    public String toString() {
        return "MovieWithReviewDto{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", releaseDate='" + releaseDate + '\'' +
                ", genres=" + genres +
                ", poster='" + poster + '\'' +
                ", imdbRating=" + imdbRating +
                ", plot='" + plot + '\'' +
                ", rated='" + rated + '\'' +
                ", reviews=" + reviews +
                ", director='" + director + '\'' +
                ", writer='" + writer + '\'' +
                ", actors='" + actors + '\'' +
                ", runtime='" + runtime + '\'' +
                ", language='" + language + '\'' +
                ", boxOffice='" + boxOffice + '\'' +
                ", type='" + type + '\'' +
                ", rating=" + rating +
                '}';
    }

    private String rated;

    private List<ReviewDto> reviews;

    private String director;

    private String writer;
    private String actors;
    private String runtime;
    private String language;
    private String boxOffice;
    private String type;
    private List<?> rating;

}
