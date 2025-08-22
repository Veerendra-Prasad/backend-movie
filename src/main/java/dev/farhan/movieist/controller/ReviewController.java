package dev.farhan.movieist.controller;

import dev.farhan.movieist.dto.ReviewDto;
import dev.farhan.movieist.model.Review;
import dev.farhan.movieist.requests.ReviewRequest;
import dev.farhan.movieist.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService service;

    @PostMapping("/{movieId}/{userId}/create")
    public ResponseEntity<ReviewDto> createReview(@PathVariable String movieId,
                                               @RequestBody ReviewRequest request,
                                               @PathVariable ObjectId userId){
            Review review = service.createReview(movieId,request.getBody(),userId);
            ReviewDto dto = service.convertToDto(review);
            return ResponseEntity.ok(dto);
    }

    @PutMapping("/{reviewId}/update")
    public ResponseEntity<String> updateReview(@PathVariable ObjectId reviewId,
                                               @RequestBody ReviewRequest request){
        try{
            service.updateReview(reviewId, request.getBody());
            return ResponseEntity.ok( "Updated Review Successfully");
        } catch (RuntimeException e){
            return ResponseEntity.status(409).body(e.getMessage()); // change the status code appropriately
        }
    }

    @DeleteMapping("/{reviewId}/delete")
    public ResponseEntity<String> deleteReview(@PathVariable ObjectId reviewId){
        try{
            service.deleteReview(reviewId);
            return ResponseEntity.ok("Deleted Review Successfully");
        } catch (RuntimeException e){
            return ResponseEntity.status(404).body(e.getMessage()); // change the status code appropriately
        }
    }
}
