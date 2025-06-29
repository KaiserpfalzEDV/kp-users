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
import de.kaiserpfalzedv.commons.users.domain.model.user.User;
import de.kaiserpfalzedv.commons.users.domain.model.user.UserNotFoundException;
import de.kaiserpfalzedv.commons.users.domain.services.UserStateManagementService;
import jakarta.validation.constraints.NotNull;
import lombok.ToString;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;


/**
 * Handles user state changes and updates the user repository accordingly.
 *
 * @author klenkes74 {@literal <rlichti@kaiserpfalz-edv.de>}
 * @since 2025-05-16
 */
@Service
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@XSlf4j
public class R2dbcUserStateManagementService extends R2dbcAbstractManagementService implements UserStateManagementService {
  
  public R2dbcUserStateManagementService(
      @NotNull final R2dbcUserRepository repository,
      @NotNull final ApplicationEventPublisher bus,
      @Value("${spring.application.system:kp-users}") final String system
  ) {
    super(repository, bus, system);
    log.entry(repository, bus, system);
    log.exit();
  }
  
  
  @SuppressWarnings("removal")
  @Override
  public Mono<User> activate(final UUID id) {
    log.entry(id);
    
    Mono<User> result = repository.findById(id)
        .switchIfEmpty(Mono.error(new UserNotFoundException(id)))
        .map(u -> u.undelete(bus))
        .flatMap(u -> saveUser(((KpUserDetails)u), "User undeleted", "User undeleting error"));
    
    return log.exit(result);
    
  }
  
  @Override
  public Mono<User> detain(final UUID id, final long days) {
    log.entry(id, days);
    
    Mono<User> result = repository.findById(id)
        .switchIfEmpty(Mono.error(new UserNotFoundException(id)))
        .map(u -> u.detain(bus, days))
        .flatMap(u -> saveUser(((KpUserDetails)u), "User detained", "User detaining error"));
    
    return log.exit(result);
  }
  
  @Override
  public Mono<User> ban(final UUID id) {
    log.entry(id);
    
    Mono<User> result = repository.findById(id)
        .switchIfEmpty(Mono.error(new UserNotFoundException(id)))
        .map(u -> u.ban(bus))
        .flatMap(u -> saveUser(((KpUserDetails)u), "User banned", "User banning error"));
    
    return log.exit(result);
  }
  
  @Override
  public Mono<User> release(final UUID id) {
    log.entry(id);
    
    Mono<User> result = repository.findById(id)
        .switchIfEmpty(Mono.error(new UserNotFoundException(id)))
        .map(u -> u.release(bus))
        .flatMap(u -> saveUser(((KpUserDetails)u), "User released", "User releasing error"));
    
    return log.exit(result);
  }
  
  
}
