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


import de.kaiserpfalzedv.commons.users.domain.model.user.KpUserDetails;
import de.kaiserpfalzedv.commons.users.domain.model.user.UserNotFoundException;
import de.kaiserpfalzedv.commons.users.domain.model.user.events.modification.*;
import de.kaiserpfalzedv.commons.users.domain.services.UserDataManagementService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;


/**
 * Service for managing user data in a JPA context.
 *
 * @author klenkes74 {@literal <rlichti@kaiserpfalz-edv.de>}
 * @since 2025-05-16
 */
@Service
@ToString(onlyExplicitlyIncluded = true)
@XSlf4j
public class R2dbcUserDataManagementService extends R2dbcAbstractManagementService implements UserDataManagementService {
  public R2dbcUserDataManagementService(
      @NotNull final R2dbcUserRepository repository,
      @NotNull final ApplicationEventPublisher bus,
      @Value("${spring.application.system:kp-users}") final String system
  ) {
    super(repository, bus, system);
    log.entry(repository, bus, system);
    
    log.exit();
  }
  
  @PreDestroy
  public void close() {
    log.entry(bus, system);
    log.exit();
  }
  
  
  @Override
  public Mono<KpUserDetails> updateSubject(@NotNull final UUID id, @NotNull final String issuer, @NotNull final String sub) throws UserNotFoundException {
    log.entry(id, issuer, sub);

    Mono<KpUserDetails> result = repository.findById(id);
    result = result
        .switchIfEmpty(Mono.error(new UserNotFoundException(id)))
        .map(u -> u.toBuilder().issuer(issuer).subject(sub).build())
        .flatMap(u -> saveUser(u, "User subject updated", "User subject updating error"));
        
    return log.exit(result);
  }

  
  @Override
  public Mono<KpUserDetails> updateNamespace(@NotNull final UUID id, @NotNull final String namespace) throws UserNotFoundException {
    log.entry(id, namespace);

    Mono<KpUserDetails> result = repository.findById(id);
    result = result
        .switchIfEmpty(Mono.error(new UserNotFoundException(id)))
        .map(u -> u.toBuilder().nameSpace(namespace).build())
        .flatMap(u -> saveUser(u, "User namespace updated", "User namespace updating error"));
    
    return log.exit(result);
  }
  
  
  @Override
  public Mono<KpUserDetails> updateName(@NotNull final UUID id, @NotNull final String name) throws UserNotFoundException {
    log.entry(id, name);
    
    Mono<KpUserDetails> result = repository.findById(id);
    result = result
        .switchIfEmpty(Mono.error(new UserNotFoundException(id)))
        .map(u -> u.toBuilder().name(name).build())
        .flatMap(u -> saveUser(u, "User name updated", "User name updating error"));
    
    return log.exit(result);
  }
  
  
  @Override
  public Mono<KpUserDetails> updateNamespaceAndName(@NotNull final UUID id, @NotNull final String namespace, @NotNull final String name) throws UserNotFoundException {
    log.entry(id, namespace, name);
    
    Mono<KpUserDetails> result = repository.findById(id);
    result = result
        .switchIfEmpty(Mono.error(new UserNotFoundException(id)))
        .map(u -> u.toBuilder().nameSpace(namespace).name(name).build())
        .flatMap(u -> saveUser(u, "User namespace and name updated", "User namespace and name updating error"));
    
    return log.exit(result);
  }
  
  
  @Override
  public Mono<KpUserDetails> updateEmail(@NotNull final UUID id, @NotNull final String email) throws UserNotFoundException {
    log.entry(id, email);
    
    Mono<KpUserDetails> result = repository.findById(id);
    result = result
        .switchIfEmpty(Mono.error(new UserNotFoundException(id)))
        .map(u -> u.toBuilder().email(email).build())
        .flatMap(u -> saveUser(u, "User email updated", "User email updating error"));
    
    return log.exit(result);
  }

  
  @Override
  public Mono<KpUserDetails> updateDiscord(@NotNull final UUID id, @NotNull final String discord) throws UserNotFoundException {
    log.entry(id, discord);
    
    Mono<KpUserDetails> result = repository.findById(id);
    result = result
        .switchIfEmpty(Mono.error(new UserNotFoundException(id)))
        .map(u -> u.toBuilder().discord(discord).build())
        .flatMap(u -> saveUser(u, "User Discord updated", "User Discord updating error"));

    return log.exit(result);
  }
}
