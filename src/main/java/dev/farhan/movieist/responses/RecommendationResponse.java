package dev.farhan.movieist.responses;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RecommendationResponse {
    private List<String> recommendations;
}
