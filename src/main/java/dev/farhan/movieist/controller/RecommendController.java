package dev.farhan.movieist.controller;

import dev.farhan.movieist.dto.MovieWithReviewDto;
import dev.farhan.movieist.model.Movie;
import dev.farhan.movieist.service.RecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recommend")
@RequiredArgsConstructor
public class RecommendController {
    private final RecommendService service;

    @PostMapping("/")
    public ResponseEntity<List<MovieWithReviewDto>> recommendation(@RequestBody List<String> movieList ){
        if(!movieList.isEmpty()){
            return ResponseEntity.ok(service.getRecommend(movieList));
        }
        return ResponseEntity.ok(null);
    }
}
