package com.sportlink.ratingservice.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sportlink.ratingservice.dto.RatingRequest;
import com.sportlink.ratingservice.model.Rating;
import com.sportlink.ratingservice.model.UserReputation;
import com.sportlink.ratingservice.service.RatingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping("/ratings")
    public ResponseEntity<Rating> submitRating(@RequestBody RatingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ratingService.submitRating(request));
    }

    @GetMapping("/ratings/{ratingId}")
    public ResponseEntity<Rating> getRating(@PathVariable UUID ratingId) {
        return ResponseEntity.ok(ratingService.getRatingById(ratingId));
    }

    @GetMapping("/ratings")
    public ResponseEntity<List<Rating>> listRatings(
            @RequestParam(required = false) UUID reviewerId,
            @RequestParam(required = false) UUID revieweeId,
            @RequestParam(required = false) UUID activityId) {
        return ResponseEntity.ok(ratingService.getRatings(reviewerId, revieweeId, activityId));
    }

    @DeleteMapping("/ratings/{ratingId}")
    public ResponseEntity<Void> deleteRating(@PathVariable UUID ratingId) {
        ratingService.deleteRating(ratingId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reputations/{userId}")
    public ResponseEntity<UserReputation> getReputation(@PathVariable UUID userId) {
        return ResponseEntity.ok(ratingService.getReputation(userId));
    }

    @PostMapping("/reputations/{userId}/recompute")
    public ResponseEntity<UserReputation> recompute(@PathVariable UUID userId) {
        return ResponseEntity.ok(ratingService.recomputeReputation(userId));
    }
}