package dev.farhan.movieist.service;

import dev.farhan.movieist.dto.MovieWithReviewDto;
import dev.farhan.movieist.model.Movie;
import dev.farhan.movieist.responses.OmdbMovieResponse;
import dev.farhan.movieist.responses.RecommendationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RecommendService {
    private final RestTemplate restTemplate;
    private final MovieService service;

    @Value("${recommend.api.key}")
    private String recommendationApi;

    public List<MovieWithReviewDto> getRecommend(List<String> stringList){
        List<String> movieList = Arrays.asList("Inception", "Titanic", "Interstellar");

        Map<String, Object> payload = new HashMap<>();
        payload.put("liked_movies", movieList);

        String pythonApiUrl = recommendationApi + "/recommend";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<RecommendationResponse> response = restTemplate.postForEntity(pythonApiUrl, requestEntity, RecommendationResponse.class);

        List<MovieWithReviewDto> movies = Objects.requireNonNull(response.getBody()).getRecommendations().stream().map(this::checkMovie).toList();

        return movies;
    }

    public MovieWithReviewDto checkMovie(String title){
        try{
            return service.getMovieByTitle(title);
        } catch (RuntimeException e) {
            return null;
        }
    }
}
