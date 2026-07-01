package edu.rutmiit.demo.cinemacore.graphql.types;

public record UpdateMovieInputGql(String title, String description, Integer durationMinutes, String ageRating, String genre, Boolean active) {}
