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
import de.kaiserpfalzedv.commons.users.domain.model.user.UserNotFoundException;
import de.kaiserpfalzedv.commons.users.domain.services.UserRoleManagementService;
import de.kaiserpfalzedv.commons.users.store.model.role.R2dbcRoleRepository;
import jakarta.annotation.PreDestroy;
import lombok.ToString;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.UUID;


/**
 * Service for managing user roles in a JPA context.
 *
 * @author klenkes74 {@literal <rlichti@kaiserpfalz-edv.de>}
 * @since 2025-05-16
 */
@Service
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@XSlf4j
public class R2dbcUserRoleManagementService extends R2dbcAbstractManagementService implements UserRoleManagementService, AutoCloseable {
  private final R2dbcRoleRepository roleRepository;
  private final RoleToImpl roleToImpl;
  
  public R2dbcUserRoleManagementService(
      @Autowired final R2dbcUserRepository repository,
      @Autowired final R2dbcRoleRepository roleRepository,
      @Autowired final RoleToImpl roleToImpl,
      @Autowired final ApplicationEventPublisher bus,
      @Value("${spring.application.system:kp-users}") final String system
  ) {
    super(repository, bus, system);
    log.entry(repository, roleRepository, roleToImpl, bus, system);
    
    this.roleRepository = roleRepository;
    this.roleToImpl = roleToImpl;
    
    log.exit();
  }
  
  @PreDestroy
  public void close() {
    log.entry(repository, roleRepository, roleToImpl, bus, system);
    log.exit();
  }
  
  
  @Override
  public Mono<KpUserDetails> addRole(final UUID id, final Role role) {
    log.entry(id, role);
    
    Mono<KpUserDetails> result = roleRepository.findById(role.getId())
        .switchIfEmpty(roleRepository.save(roleToImpl.apply(role)))
        .switchIfEmpty(Mono.defer(() -> Mono.error(() -> new RoleNotFoundException(role.getId()))))
        .filter(Objects::nonNull)
        .flatMap(r -> repository.findById(id)
            .switchIfEmpty(Mono.defer(() -> Mono.error(() -> new UserNotFoundException(id))))
            .filter(Objects::nonNull)
            .map(u -> { u.addRole(r, bus); return u; })
            .flatMap(u -> saveUser(u, "Role added to user", "Error adding role to user"))
        );
    
    return log.exit(result);
  }
  
  @Override
  public Mono<KpUserDetails> removeRole(final UUID id, final Role role) throws UserNotFoundException, RoleNotFoundException {
    log.entry(id, role);
    
    Mono<KpUserDetails> result = roleRepository.findById(role.getId())
        .switchIfEmpty(roleRepository.save(roleToImpl.apply(role)))
        .switchIfEmpty(Mono.defer(() -> Mono.error(() -> new RoleNotFoundException(role.getId()))))
        .filter(Objects::nonNull)
        .flatMap(r -> repository.findById(id)
            .switchIfEmpty(Mono.defer(() -> Mono.error(() -> new UserNotFoundException(id))))
            .filter(Objects::nonNull)
            .map(u -> { u.removeRole(r, bus); return u; })
            .flatMap(u -> saveUser(u, "Role removed from user", "Error removing role from user"))
        );
        
    return log.exit(result);
  }
  
  @Override
  public Mono<KpUserDetails> revokeRoleFromAllUsers(final Role role) {
    log.entry(role);
    
    // TODO 2025-05-16 klenkes74 Implement the role removal.
    throw log.throwing(new UnsupportedOperationException("Revoke role from all users is not implemented yet!"));
  }
}
