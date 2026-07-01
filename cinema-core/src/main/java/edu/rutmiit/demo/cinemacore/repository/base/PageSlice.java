package edu.rutmiit.demo.cinemacore.repository.base;

import java.util.List;

public record PageSlice<T>(List<T> content, int page, int size, long totalElements) {

    public int totalPages() {
        return size <= 0 ? 1 : (int) Math.ceil((double) totalElements / size);
    }

    public boolean last() {
        return page >= Math.max(0, totalPages() - 1);
    }
}
