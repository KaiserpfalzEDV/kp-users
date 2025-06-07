/*
 * Copyright (c) 2025. Roland T. Lichti, Kaiserpfalz EDV-Service.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.kaiserpfalzedv.commons.users.domain.model.role;


import lombok.*;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.annotation.Id;
import org.springframework.security.core.GrantedAuthority;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;


/**
 * The implementation of the role for the user management application.
 *
 * @author klenkes74 {@literal <rlichti@kaiserpfalz-edv.de>}
 * @since 2025-05-10
 */
@Jacksonized
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
@ToString
@EqualsAndHashCode(of = {"id"})
public class KpRole implements Role, GrantedAuthority {
  /** Internal role id */
  @Id
  @Builder.Default
  private UUID id = UUID.randomUUID();
  
  /** namespace where this role is valid */
  @Builder.Default
  private String nameSpace = "./.";
  
  /** name of the role */
  private String name;
  
  /** Role created at. */
  @Builder.Default
  private OffsetDateTime created = OffsetDateTime.now(Clock.systemUTC());
  
  /** Role last modified at */
  @Builder.Default
  private OffsetDateTime modified = OffsetDateTime.now(Clock.systemUTC());
  
  /** Role deleted at */
  private OffsetDateTime deleted;
}
