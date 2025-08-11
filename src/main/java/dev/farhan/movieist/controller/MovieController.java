package dev.farhan.movieist.controller;

import dev.farhan.movieist.dto.MovieWithReviewDto;
import dev.farhan.movieist.model.Movie;
import dev.farhan.movieist.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/movies")
public class MovieController {
    private final MovieService service;

    @GetMapping
    public ResponseEntity<Page<Movie>> getMovies(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(service.getMovies(page, size));
    }

    @GetMapping("/{imdbId}")
    public ResponseEntity<?> getSingleMovie(@PathVariable String imdbId) {
        try {
            return ResponseEntity.ok(service.findMovieById(imdbId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/title/{title}")
    public ResponseEntity<?> getMovieByTitle(@PathVariable String title) {
        try {
            MovieWithReviewDto movieDto = service.findMovieByTitleOrFetchFromOMDb(title);
            return ResponseEntity.ok(movieDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
