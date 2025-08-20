package dev.farhan.movieist.service;

import dev.farhan.movieist.dto.MovieWithReviewDto;
import dev.farhan.movieist.model.Movie;
import dev.farhan.movieist.repository.MovieRepository;
import dev.farhan.movieist.responses.OmdbMovieResponse;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.modelmapper.ModelMapper;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieService {
    private final MovieRepository repository;
    private final RestTemplate restTemplate;
    private final ModelMapper modelMapper;
    private final MongoTemplate mongoTemplate;

    @Value("${omdb.api.key}")
    private String omdbApiKey;

    public MovieWithReviewDto findMovieByTitleOrFetchFromOMDb(String title) {
        return getMovieByTitle(title);
    }

    public MovieWithReviewDto getMovieByTitle(String title) {
        Optional<Movie> movies = repository.findByTitleIgnoreCase(title);

        if (movies.isPresent()) {
            Movie movie = movies.get();
            return getMovieWithReviews(movie.getId());
        }

        Movie movie = getMovieByTitleFromOmdb(title);
        return convertMovieToMovieWithReviewDto(movie);
    }

    public MovieWithReviewDto findMovieById(String id) {
        Movie movie = getMovieById(id);

        return convertMovieToMovieWithReviewDto(movie);
    }

    public Movie getMovieById(String id) {
        Optional<Movie> optionalMovie = repository.findById(id);

        return optionalMovie.orElseGet(() -> getMovieByIdFromOmdb(id));

    }

    public Page<Movie> getMovies(int page, int size) {
        return repository.findAll(PageRequest.of(page, size));
    }

    public Movie getMovieByIdFromOmdb(String id) {
        String url = String.format("http://www.omdbapi.com/?i=%s&apikey=%s",
                id.replace(" ", "+"),
                omdbApiKey
        );

        ResponseEntity<OmdbMovieResponse> response =
                restTemplate.getForEntity(url, OmdbMovieResponse.class);

        if (response.getStatusCode() == HttpStatus.OK && Objects.requireNonNull(response.getBody()).getTitle() != null) {
            OmdbMovieResponse omdbData = response.getBody();

            return convertOmdbToMovie(omdbData);
        }

        throw new RuntimeException("Movie Not Found");
    }

    public Movie getMovieByTitleFromOmdb(String title) {
        String url = String.format("http://www.omdbapi.com/?t=%s&apikey=%s",
                title.replace(" ", "+"),
                omdbApiKey
        );

        ResponseEntity<OmdbMovieResponse> response =
                restTemplate.getForEntity(url, OmdbMovieResponse.class);

        if (response.getStatusCode() == HttpStatus.OK && Objects.requireNonNull(response.getBody()).getTitle() != null) {
            OmdbMovieResponse omdbData = response.getBody();

            return convertOmdbToMovie(omdbData);
        }

        throw new RuntimeException("Movie Not Found");
    }

    public Movie convertOmdbToMovie(OmdbMovieResponse omdbMovieResponse) {
        Movie movie = modelMapper.map(omdbMovieResponse, Movie.class);

        List<String> genresList = Arrays.stream(omdbMovieResponse.getGenres().split(","))
                .map(String::trim) // remove extra spaces
                .toList();
        movie.setGenres(genresList);
        movie.setReviewIds(Collections.emptyList());

        return movie;
    }

    public MovieWithReviewDto convertMovieToMovieWithReviewDto(Movie movie) {
        return getMovieWithReviews(movie.getId());
    }

    public MovieWithReviewDto convertOmdbToMovieWithReviewDto(OmdbMovieResponse omdbMovieResponse) {
        MovieWithReviewDto movieWithReviewDto = modelMapper.map(omdbMovieResponse, MovieWithReviewDto.class);

        movieWithReviewDto.setReviews(Collections.emptyList());

        return movieWithReviewDto;
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
}
