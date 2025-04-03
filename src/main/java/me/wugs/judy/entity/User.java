package me.wugs.judy.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import me.wugs.judy.enums.UserRole;

@Entity
@Table(name = "\"user\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, unique = true, length = 50)
  private String username;

  @Column(nullable = false, unique = true)
  private String email;

  @JsonIgnore
  @Column(nullable = false)
  private String password;

  @Column(nullable = false)
  private boolean enabled;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserRole role;

  @Column(
      nullable = false,
      updatable = false,
      columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
  private Instant createdAt;

  @Column(nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
  private Instant updatedAt;

  @PrePersist
  void prePersist() {
    if (this.role == null) {
      this.role = UserRole.USER;
    }
    // If createdAt already exists, it cannot be updated
    // We supply it for a newly created User
    if (this.createdAt == null) {
      this.createdAt = Instant.now();
    }
    // the PrePersist function takes precedence over manual DTO assignment
    this.updatedAt = Instant.now();
  }
}
