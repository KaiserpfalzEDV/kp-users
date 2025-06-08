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


import de.kaiserpfalzedv.commons.users.domain.model.apikey.events.ApiKeyRevokedEvent;
import de.kaiserpfalzedv.commons.users.domain.model.user.*;
import de.kaiserpfalzedv.commons.users.domain.model.user.events.state.UserRemovedEvent;
import de.kaiserpfalzedv.commons.users.domain.services.UserManagementService;
import de.kaiserpfalzedv.commons.users.store.model.apikey.R2dbcApiKeyRepository;
import jakarta.validation.constraints.NotNull;
import lombok.ToString;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
  private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(1L);
  
  private final R2dbcApiKeyRepository r2dbcApiKeyRepository;
  private final UserToKpUserDetailsImpl toImpl;

  public R2dbcUserManagementService(
      @NotNull final R2dbcUserRepository repository,
      @NotNull final ApplicationEventPublisher bus,
      @NotNull final R2dbcApiKeyRepository r2dbcApiKeyRepository,
      @NotNull final UserToKpUserDetailsImpl toImpl,
      @Value("${spring.application.system:kp-users}") final String system
  ) {
    super(repository, bus, system);
    log.entry(repository, bus, r2dbcApiKeyRepository, system);
    
    this.r2dbcApiKeyRepository = r2dbcApiKeyRepository;
    this.toImpl = toImpl;
    
    log.exit();
  }
  
  
  @Override
  public Mono<KpUserDetails> create(@NotNull final User user) {
    log.entry(user);
    
    Mono<KpUserDetails> result = repository.save(toImpl.apply(user));
    result = result
        .switchIfEmpty(Mono.error(() -> new UserCantBeCreatedException(user)))
        .onErrorMap(IllegalArgumentException.class, e -> new UserCantBeCreatedException(user, e))
        .onErrorMap(OptimisticLockingFailureException.class, e -> new UserCantBeCreatedException(user, e))
        .doOnSuccess(u -> log.info("User created successfully. id={}", u.getId()))
        .doOnError(e -> log.error("Error creating user: {}", e.getMessage(), e));
    
    return log.exit(result);
  }
  
  
  @Override
  public Mono<KpUserDetails> delete(final UUID id) {
    log.entry(id);
    
    Mono<KpUserDetails> result = repository.findById(id);
    result = result
        .switchIfEmpty(Mono.error(() -> new UserNotFoundException(id)))
        .map(user -> user.toBuilder().deleted(OffsetDateTime.now(ZoneOffset.UTC)).build())
        .publishOn(Schedulers.boundedElastic())
        .doOnSuccess(u -> revokeAllApiKeysForUser(u).block(DEFAULT_TIMEOUT))
        .flatMap(u -> saveUser(u, "User deleted", "User deleting error"));
    
    return log.exit(result);
  }
  
  private Mono<Void> revokeAllApiKeysForUser(final User id) {
    log.entry(id);
    
    Mono<Void> result = r2dbcApiKeyRepository.deleteAll(r2dbcApiKeyRepository.findByUserId(id.getId()));
    result = result
        .doOnSuccess(k -> {
          log.info("Revoked all API keys for user. user={}", id);
          bus.publishEvent(ApiKeyRevokedEvent.builder().application(system).user(id).build());
        });
    
    return log.exit(result);
  }
  
  
  @Override
  public Mono<KpUserDetails> undelete(final UUID id) {
    log.entry(id);
    
    Mono<KpUserDetails> result = repository.findById(id);
    result = result
        .switchIfEmpty(Mono.error(() -> new UserNotFoundException(id)))
        .onErrorMap(UserNotFoundException.class, e -> e)
        .map(user -> user.toBuilder().deleted(null).build())
        .flatMap(u -> saveUser(u, "User undeleted", "User undeleting error"));
    
    return log.exit(result);
  }
  
  
  @Override
  public Mono<Void> remove(final UUID id) {
    log.entry(id);
    
    Mono<Void> result = repository.deleteById(id);
    result = result
        .doOnSuccess(v -> {
          bus.publishEvent(UserRemovedEvent.builder().application(system).id(id).build());
          log.info("User removed successfully. id={}", id);
        })
        .doOnError(e -> log.error("Error removing user: {}", e.getMessage(), e));

    return log.exit(result);
  }
}
