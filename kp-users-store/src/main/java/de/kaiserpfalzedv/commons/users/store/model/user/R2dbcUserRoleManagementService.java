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

package de.kaiserpfalzedv.commons.users.store.model.user;


import de.kaiserpfalzedv.commons.users.domain.model.role.Role;
import de.kaiserpfalzedv.commons.users.domain.model.role.RoleNotFoundException;
import de.kaiserpfalzedv.commons.users.domain.model.role.RoleToImpl;
import de.kaiserpfalzedv.commons.users.domain.model.user.KpUserDetails;
import de.kaiserpfalzedv.commons.users.domain.model.user.User;
import de.kaiserpfalzedv.commons.users.domain.model.user.UserNotFoundException;
import de.kaiserpfalzedv.commons.users.domain.services.UserRoleManagementService;
import de.kaiserpfalzedv.commons.users.store.model.role.R2dbcRoleRepository;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Objects;
import java.util.UUID;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;


/**
 * Service for managing user roles in a JPA context.
 *
 * @author klenkes74 {@literal <rlichti@kaiserpfalz-edv.de>}
 * @since 2025-05-16
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@XSlf4j
public class R2dbcUserRoleManagementService implements UserRoleManagementService, AutoCloseable {
  private final R2dbcRoleRepository roleRepository;
  private final R2dbcUserRepository userRepository;
  private final R2dbcEntityTemplate template;
  private final ApplicationEventPublisher bus;
  private final RoleToImpl roleToImpl;
  
  @Value("${spring.application.system:kp-users}")
  private final String system = "kp-users";
  
  @PreDestroy
  public void close() {
    log.entry(userRepository, roleRepository, roleToImpl, bus, system);
    log.exit();
  }
  
  
  @Override
  public Mono<User> addRole(final UUID id, final Role role) {
    log.entry(id, role);
    
    Mono<User> result = roleRepository.findById(role.getId())
        .switchIfEmpty(Mono.error(new RoleNotFoundException(role.getId())))
        .publishOn(Schedulers.boundedElastic())
        .mapNotNull(r -> userRepository.findById(id)
            .switchIfEmpty(Mono.error(new UserNotFoundException(id)))
            .map(u -> { ((KpUserDetails)u).addRole(r, bus); return u; })
            .publishOn(Schedulers.boundedElastic())
            .mapNotNull(
                u -> userRepository.save(((KpUserDetails)u)).block()
            ).block()
        );
    
    return log.exit(result);
  }
  
  @Override
  public Mono<User> removeRole(final UUID id, final Role role) {
    log.entry(id, role);
    
    Mono<User> result = roleRepository.findById(role.getId())
        .switchIfEmpty(Mono.error(new RoleNotFoundException(role.getId())))
        .filter(Objects::nonNull)
        .publishOn(Schedulers.boundedElastic())
        .mapNotNull(r -> userRepository.findById(id)
            .switchIfEmpty(Mono.defer(() -> Mono.error(() -> new UserNotFoundException(id))))
            .filter(Objects::nonNull)
            .map(u -> { ((KpUserDetails)u).removeRole(r, bus); return u; })
            .publishOn(Schedulers.boundedElastic())
            .mapNotNull(u -> userRepository.save(((KpUserDetails)u)).block())
            .block()
        );
        
    return log.exit(result);
  }
  
  @Override
  public Mono<Long> revokeRoleFromAllUsers(final Role role) {
    log.entry(role);
    
    return template
        .delete(KpUsersRoles.class)
        .matching(query(where("ROLE_ID").is(role.getId())))
        .all();
  }
}
