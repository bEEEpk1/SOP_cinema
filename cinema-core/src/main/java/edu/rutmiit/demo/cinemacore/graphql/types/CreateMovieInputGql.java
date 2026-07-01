package edu.rutmiit.demo.cinemacore.graphql.types;

public record CreateMovieInputGql(String title, String description, Integer durationMinutes, String ageRating, String genre) {}
