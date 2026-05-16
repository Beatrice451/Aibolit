package org.beatrice.diploma_new_pharmacy.domain.product.mapper;


import org.beatrice.diploma_new_pharmacy.domain.product.dto.response.ReviewListItemResponse;
import org.beatrice.diploma_new_pharmacy.domain.product.dto.response.UserReviewResponse;
import org.beatrice.diploma_new_pharmacy.domain.product.model.Product;
import org.beatrice.diploma_new_pharmacy.domain.product.model.Review;
import org.beatrice.diploma_new_pharmacy.domain.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(source = "user", target = "username", qualifiedByName = "username")
    @Mapping(source = "user", target = "userId", qualifiedByName = "userId")
    @Mapping(source = "id", target = "reviewId")
    ReviewListItemResponse toListItemDto(Review review);

    @Mapping(source = "product", target = "productName", qualifiedByName = "productName")
    @Mapping(source = "product", target = "productImageUrl", qualifiedByName = "productImageUrl")
    @Mapping(source = "product", target = "productId", qualifiedByName = "productId")
    @Mapping(source = "id", target = "reviewId")
    UserReviewResponse toUserReviewDto(Review review);

    @Named("username")
    default String username(User user) {
        return user.getFirstName() + " " + user.getLastName();
    }

    @Named("userId")
    default Integer userId(User user) {
        return user.getId();
    }

    @Named("productName")
    default String productName(Product product) {
        return product.getName();
    }

    @Named("productImageUrl")
    default String productImageUrl(Product product) {
        return product.getImageUrl();
    }

    @Named("productId")
    default Integer productId(Product product) {
        return product.getId();
    }
}
