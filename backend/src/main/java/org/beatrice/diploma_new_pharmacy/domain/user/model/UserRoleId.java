package org.beatrice.diploma_new_pharmacy.domain.user.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Setter
@Getter
@EqualsAndHashCode
@Embeddable
public class UserRoleId implements Serializable {
    @Serial
    private static final long serialVersionUID = 1199177003586974073L;
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "role_id", nullable = false)
    private Integer roleId;


}