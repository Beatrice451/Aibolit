package org.beatrice.diploma_new_pharmacy.domain.product.mapper;


import org.beatrice.diploma_new_pharmacy.domain.product.dto.response.ReviewResponse;
import org.beatrice.diploma_new_pharmacy.domain.product.model.Review;
import org.beatrice.diploma_new_pharmacy.domain.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(source = "user", target = "username", qualifiedByName = "username")
    @Mapping(source = "id", target = "reviewId")
    ReviewResponse toDto(Review review);

    @Named("username")
    default String username(User user) {
        return user.getFirstName() + " " + user.getLastName();
    }
}
