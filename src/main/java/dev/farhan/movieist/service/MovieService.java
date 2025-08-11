package dev.farhan.movieist.service;

import dev.farhan.movieist.dto.MovieWithReviewDto;
import dev.farhan.movieist.model.Movie;
import dev.farhan.movieist.model.Review;
import dev.farhan.movieist.repository.MovieRepository;
import dev.farhan.movieist.repository.ReviewRepository;
import dev.farhan.movieist.responses.OmdbMovieResponse;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class MovieService {
    private final MovieRepository repository;
    private final MongoTemplate mongoTemplate;
    private final RestTemplate restTemplate; // For OMDb API call
    private final ModelMapper modelMapper;

    @Value("${omdb.api.key}")
    private String omdbApiKey;

    public MovieWithReviewDto findMovieByTitleOrFetchFromOMDb(String title) {
        Optional<Movie> optionalMovie = repository.findByTitleIgnoreCase(title);

        if (optionalMovie.isPresent()) {
            Movie movie = optionalMovie.get();
            return getMovieWithReviews(movie.getId());
        }

        // If movie not in DB, fetch from OMDb
        String omdbUrl = String.format(
                "http://www.omdbapi.com/?t=%s&apikey=%s",
                title.replace(" ", "+"),
                omdbApiKey
        );

        // Deserialize directly into OmdbMovieResponse
        ResponseEntity<OmdbMovieResponse> response =
                restTemplate.getForEntity(omdbUrl, OmdbMovieResponse.class);

        if (response.getStatusCode() == HttpStatus.OK && Objects.requireNonNull(response.getBody()).getTitle() != null) {
            System.out.println(response.getStatusCode());
            System.out.println(response.getBody().toString());
            OmdbMovieResponse omdbData = response.getBody();

//             Convert to DTO
            MovieWithReviewDto dto = convertOmdbToDto(omdbData);

//             Ensure reviews are empty
            dto.setReviews(List.of());

            return dto;
        }

        throw new RuntimeException("Movie not found in DB or OMDb");
    }


    public Page<Movie> getMovies(int page, int size) {
        return repository.findAll(PageRequest.of(page, size));
    }

    public MovieWithReviewDto findMovieById(String imdbId) {
            Movie movie = repository.findById(imdbId)
                    .orElseThrow(() -> new RuntimeException("movie not found"));
        return getMovieWithReviews(imdbId);
    }

    private MovieWithReviewDto getMovieWithReviews(String movieId) {
        MatchOperation matchMovie = Aggregation.match(Criteria.where("_id").is(movieId));

        LookupOperation lookupReviews = Aggregation.lookup("reviews", "reviewIds", "_id", "reviews");

        // Join each review with its user
        LookupOperation lookupUsers = Aggregation.lookup("users", "reviews.user_id", "_id", "reviewUsers");

        // Merge username into each review
        AggregationOperation addReviewUsernames = context -> new Document("$set", new Document("reviews",
                new Document("$map", new Document("input", "$reviews")
                        .append("as", "review")
                        .append("in", new Document()
                                .append("username", new Document("$arrayElemAt", Arrays.asList(
                                        "$reviewUsers.username",
                                        new Document("$indexOfArray", Arrays.asList("$reviewUsers._id", "$$review.user_id"))
                                )))
                                .append("body", "$$review.body")
                                .append("created", "$$review.created")
                        )
                )
        ));

        Aggregation aggregation = Aggregation.newAggregation(
                matchMovie,
                lookupReviews,
                lookupUsers,
                addReviewUsernames
        );

        AggregationResults<MovieWithReviewDto> results =
                mongoTemplate.aggregate(aggregation, "movies", MovieWithReviewDto.class);

        return results.getUniqueMappedResult();
    }

    public MovieWithReviewDto convertOmdbToDto(OmdbMovieResponse omdb) {
        MovieWithReviewDto dto = new MovieWithReviewDto();

        dto.setTitle(omdb.getTitle());
        dto.setReleaseDate(omdb.getReleaseDate());

        if (omdb.getGenres() != null && !omdb.getGenres().isEmpty()) {
            dto.setGenres(Arrays.asList(omdb.getGenres().split(",\\s*")));
        } else {
            dto.setGenres(Collections.emptyList());
        }

        dto.setPoster(omdb.getPoster());

        // Convert imdbRating String to Double, handle parse errors
        try {
            dto.setImdbRating(omdb.getImdbRating() != null ? Double.parseDouble(omdb.getImdbRating()) : null);
        } catch (NumberFormatException e) {
            dto.setImdbRating(null);
        }

        dto.setPlot(omdb.getPlot());
        dto.setRated(omdb.getRated());
        dto.setDirector(omdb.getDirector());
        dto.setWriter(omdb.getWriter());
        dto.setActors(omdb.getActors());
        dto.setRuntime(omdb.getRuntime());
        dto.setLanguage(omdb.getLanguage());
        dto.setBoxOffice(omdb.getBoxOffice());
        dto.setType(omdb.getType());
        dto.setRating(omdb.getRating()); // Assuming Rating class matches OMDb JSON

        dto.setReviews(Collections.emptyList()); // no reviews from OMDb API

        return dto;
    }


}
