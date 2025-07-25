package dev.farhan.movieist.movies;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MovieService {

    @Autowired
    private MovieRepository repository;

    @Autowired
    private ReviewRepository reviewRepository;

    public List<Movie> findAllMovies() {
        return repository.findAll();
    }

    public Optional<Movie> findMovieByImdbId(String imdbId) {
        Optional<Movie> optionalMovie = repository.findMovieByImdbId(imdbId);

        optionalMovie.ifPresent(movie -> {
            if (movie.getReviewIds() != null && !movie.getReviewIds().isEmpty()) {
                List<Review> populatedReviews = reviewRepository.findAllById(movie.getReviewIds());
                movie.setReviews(populatedReviews);
            }
        });
        return optionalMovie;
    }
}
