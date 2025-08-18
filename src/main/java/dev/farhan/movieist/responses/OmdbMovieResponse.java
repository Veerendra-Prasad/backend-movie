package dev.farhan.movieist.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.farhan.movieist.dto.RatingDto;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OmdbMovieResponse {
    @JsonProperty("imdbID")
    private String id;

    @JsonProperty("Title")
    private String title;

    @JsonProperty("Released")
    private String releaseDate;

    @JsonProperty("Genre")
    private String genres;

    @JsonProperty("Poster")
    private String poster;

    @JsonProperty("imdbRating")
    private String imdbRating;

    @JsonProperty("Plot")
    private String plot;

    @JsonProperty("Rated")
    private String rated;

    @JsonProperty("Director")
    private String director;

    @JsonProperty("Writer")
    private String writer;

    @JsonProperty("Actors")
    private String actors;

    @JsonProperty("Runtime")
    private String runtime;

    @JsonProperty("Language")
    private String language;

    @JsonProperty("BoxOffice")
    private String boxOffice;

    @JsonProperty("Type")
    private String type;

    @JsonProperty("Ratings")
    private List<RatingDto> rating;

    @Override
    public String toString() {
        return "OmdbMovieResponse{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", releaseDate='" + releaseDate + '\'' +
                ", genres='" + genres + '\'' +
                ", poster='" + poster + '\'' +
                ", imdbRating='" + imdbRating + '\'' +
                ", plot='" + plot + '\'' +
                ", rated='" + rated + '\'' +
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
}


