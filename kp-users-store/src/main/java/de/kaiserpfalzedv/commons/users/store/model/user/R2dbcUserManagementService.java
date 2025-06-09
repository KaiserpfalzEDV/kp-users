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


import de.kaiserpfalzedv.commons.users.domain.model.user.*;
import de.kaiserpfalzedv.commons.users.domain.model.user.events.state.UserActivatedEvent;
import de.kaiserpfalzedv.commons.users.domain.model.user.events.state.UserCreatedEvent;
import de.kaiserpfalzedv.commons.users.domain.model.user.events.state.UserRemovedEvent;
import de.kaiserpfalzedv.commons.users.domain.services.UserManagementService;
import jakarta.validation.constraints.NotNull;
import lombok.ToString;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;


/**
 * Service for managing users in a JPA context.
 *
 * @author klenkes74 {@literal <rlichti@kaiserpfalz-edv.de>}
 * @since 2025-05-16
 */
@Service
@ToString(onlyExplicitlyIncluded = true)
@XSlf4j
public class R2dbcUserManagementService extends R2dbcAbstractManagementService implements UserManagementService {
  
  private final UserToKpUserDetailsImpl toImpl;

  public R2dbcUserManagementService(
      @NotNull final R2dbcUserRepository repository,
      @NotNull final ApplicationEventPublisher bus,
      @NotNull final UserToKpUserDetailsImpl toImpl,
      @Value("${spring.application.system:kp-users}") final String system
  ) {
    super(repository, bus, system);
    log.entry(repository, bus, system);
    
    this.toImpl = toImpl;
    
    log.exit();
  }
  
  
  @Override
  public Mono<KpUserDetails> create(@NotNull final User user) {
    log.entry(user);
    
    Mono<KpUserDetails> result = repository.save(toImpl.apply(user))
        .onErrorMap(IllegalArgumentException.class, e -> new UserCantBeCreatedException(user, e))
        .onErrorMap(OptimisticLockingFailureException.class, e -> new UserCantBeCreatedException(user, e))
        .switchIfEmpty(Mono.error(new UserCantBeCreatedException(user)))
        .map(u -> {
          log.info("User created successfully. id={}", u.getId());
          bus.publishEvent(UserCreatedEvent.builder().application(system).user(u).build());
          return u;
        });
    
    return log.exit(result);
  }
  
  
  @Override
  public Mono<KpUserDetails> delete(final UUID id) {
    log.entry(id);
    
    Mono<KpUserDetails> result = repository.findById(id)
        .switchIfEmpty(Mono.error(new UserNotFoundException(id)))
        .map(user -> user.delete(bus))
        .publishOn(Schedulers.boundedElastic())
        .mapNotNull(u -> saveUser(
            u,
            "User deleted",
            "User deleting error"
          ).block()
        )
    ;
    
    return log.exit(result);
  }
  
  
  @Override
  public Mono<KpUserDetails> undelete(final UUID id) {
    log.entry(id);
    
    Mono<KpUserDetails> result = repository.findById(id)
        .switchIfEmpty(Mono.error(new UserNotFoundException(id)))
        .map(user -> user.toBuilder().deleted(null).build())
        .publishOn(Schedulers.boundedElastic())
        .mapNotNull(u -> saveUser(
            u,
            UserActivatedEvent.builder().application(system).user(u).build(),
            "User undeleted",
            "User undeleting error")
            .block()
        );
    
    return log.exit(result);
  }
  
  
  @Override
  public Mono<Void> remove(final UUID id) {
    log.entry(id);
    
    Mono<Void> result = repository.deleteById(id);
    
    log.info("User removed successfully. id={}", id);
    bus.publishEvent(UserRemovedEvent.builder().application(system).id(id).build());

    return log.exit(result);
  }
}
