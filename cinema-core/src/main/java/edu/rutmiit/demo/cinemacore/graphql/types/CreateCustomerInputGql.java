package edu.rutmiit.demo.cinemacore.graphql.types;

public record CreateCustomerInputGql(String email, String phone, Boolean registered, String passwordHash) {}
