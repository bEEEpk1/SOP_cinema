package edu.rutmiit.demo.cinemacore.graphql.types;

import edu.rutmiit.demo.cinemaapicontract.enums.HallType;

public record CreateHallInputGql(String name, HallType hallType, Integer capacity) {}
