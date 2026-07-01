package edu.rutmiit.demo.cinemacore.graphql.util;

import edu.rutmiit.demo.cinemacore.graphql.types.PageInfoGql;
import edu.rutmiit.demo.cinemacore.repository.base.PageSlice;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;

import java.util.List;

public final class GqlSupport {
    private GqlSupport() {
    }

    public static Long id(String value) {
        return value == null ? null : Long.parseLong(value);
    }

    public static int page(Integer value) {
        return value != null ? value : 0;
    }

    public static int size(Integer value) {
        return value != null ? value : 20;
    }

    public static <E, D> List<D> content(PageSlice<E> slice, RepresentationModelAssembler<E, EntityModel<D>> assembler) {
        return slice.content().stream()
                .map(assembler::toModel)
                .map(EntityModel::getContent)
                .toList();
    }

    public static <E> PageInfoGql pageInfo(PageSlice<E> slice) {
        return new PageInfoGql(slice.page(), slice.size(), slice.totalPages(), slice.last());
    }
}
