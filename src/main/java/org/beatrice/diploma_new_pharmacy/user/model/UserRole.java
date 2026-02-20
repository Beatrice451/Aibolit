package org.beatrice.diploma_new_pharmacy.user.model;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "user_roles", schema = "pharmacy")
public class UserRole {
    @EmbeddedId
    private UserRoleId id = new UserRoleId();

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @MapsId("roleId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @CreationTimestamp
    @ColumnDefault("now()")
    @Column(name = "assigned_at")
    private Instant assignedAt;

    public UserRoleId getId() {
        return id;
    }

    public void setId(UserRoleId id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

        public void setUser(User user) {
        this.user = user;
        if (user != null && user.getId() != null) {
            if (this.id == null) {
                this.id = new UserRoleId();
            }
            this.id.setUserId(user.getId());
        }
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
        if (role != null && role.getId() != null) {
            if (this.id == null) {
                this.id = new UserRoleId();
            }
            this.id.setRoleId(role.getId());
        }
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(Instant assignedAt) {
        this.assignedAt = assignedAt;
    }

}