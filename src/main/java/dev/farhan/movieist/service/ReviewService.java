package dev.farhan.movieist.service;

import com.mongodb.client.result.UpdateResult;
import dev.farhan.movieist.dto.MovieWithReviewDto;
import dev.farhan.movieist.dto.ReviewDto;
import dev.farhan.movieist.model.Movie;
import dev.farhan.movieist.model.Review;
import dev.farhan.movieist.model.User;
import dev.farhan.movieist.repository.MovieRepository;
import dev.farhan.movieist.repository.ReviewRepository;
import dev.farhan.movieist.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository repository;
    private final MongoTemplate mongoTemplate;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final MovieService movieService;

    public Review createReview(String movieId, String body, ObjectId userId) {
        Review review = repository.insert(new Review(userId, body, LocalDateTime.now(), LocalDateTime.now()));

        Optional<Movie> movie = movieRepository.findById(movieId);
        if (movie.isPresent()) {
            mongoTemplate.update(Movie.class)
                    .matching(Criteria.where("_id").is(movieId))
                    .apply(new Update().push("reviewIds").value(review.getId()))
                    .first();
        } else {
            Movie movieById = movieService.getMovieById(movieId);
            movieRepository.save(movieById);
            Query query = new Query(Criteria.where("_id").is(movieId));
            Update update = new Update().push("reviewIds", review.getId());
            mongoTemplate.updateFirst(query, update, Movie.class);
        }

        return review;
    }

    public void updateReview(ObjectId reviewId, String body) {
        Query query = new Query(Criteria.where("_id").is(reviewId));
        Update update = new Update().set("body", body).set("updated", LocalDateTime.now());

        UpdateResult result = mongoTemplate.updateFirst(query, update, Review.class);

        if (result.getMatchedCount() == 0) {
            throw new RuntimeException("Review Not Found");
        } else if (result.getModifiedCount() == 0) {
            throw new RuntimeException("No change required - Review up to date");
        }
    }

    public void deleteReview(ObjectId reviewId) {
        Optional<Review> review = repository.findById(reviewId);
        if (review.isPresent()) {
            repository.deleteById(reviewId); // delete the review by id
            Query q = Query.query(Criteria.where("reviewIds").is(reviewId));
            Update u = new Update().pull("reviewIds", reviewId);
            mongoTemplate.updateMulti(q, u, Movie.class);
        } else {
            throw new RuntimeException("Review Not Found");
        }
    }

    public ReviewDto convertToDto(Review review) {
        User user = userRepository.findById(review.getUserId()).get();
        return new ReviewDto(user.getUsername(), review.getBody(), review.getCreated(), review.getUpdated());

    }
}
