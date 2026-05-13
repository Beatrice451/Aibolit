package org.beatrice.diploma_new_pharmacy.domain.product.repository;

import org.beatrice.diploma_new_pharmacy.domain.product.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
    Page<Review> findByProduct_Id(Integer productId, Pageable pageable);


    @Query("""
                SELECT AVG(r.rating)
                from Review r
                where r.product.id = :productId
            
            """)
    Double findAverageRatingByProductId(@Param("productId") Integer productId);

    Optional<Review> findByIdAndUser_Id(Integer id, Integer userId);

    boolean existsByUser_IdAndProduct_Id(Integer userId, Integer productId);

    int countByProduct_Id(Integer productId);
}
