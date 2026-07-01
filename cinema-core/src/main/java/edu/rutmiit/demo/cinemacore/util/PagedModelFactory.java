package edu.rutmiit.demo.cinemacore.util;

import edu.rutmiit.demo.cinemacore.repository.base.PageSlice;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PagedModelFactory {

    public <T, D> PagedModel<EntityModel<D>> toPagedModel(PageSlice<T> slice,
                                                          RepresentationModelAssembler<T, EntityModel<D>> assembler) {
        List<EntityModel<D>> content = slice.content().stream().map(assembler::toModel).toList();
        PagedModel.PageMetadata metadata = new PagedModel.PageMetadata(slice.size(), slice.page(), slice.totalElements(), slice.totalPages());
        return PagedModel.of(content, metadata);
    }
}
