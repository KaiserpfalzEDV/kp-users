package de.kaiserpfalzedv.commons.users.store.model.user;


import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

/**
 * @author klenkes74 {@literal <rlichti@kaiserpfalz-edv.de>}
 * @since 2025-06-09
 */
@Table("USERS_ROLES")
@Builder(toBuilder = true)
@Getter
@ToString
public class KpUsersRoles {
  @Column("USER_ID")
  private UUID userId;
  @Column("ROLE_ID")
  private final UUID roleId;
}
