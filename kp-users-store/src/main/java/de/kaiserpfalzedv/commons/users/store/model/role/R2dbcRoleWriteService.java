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

package de.kaiserpfalzedv.commons.users.store.model.role;


import de.kaiserpfalzedv.commons.users.domain.model.role.*;
import de.kaiserpfalzedv.commons.users.domain.model.role.events.RoleCreatedEvent;
import de.kaiserpfalzedv.commons.users.domain.model.role.events.RoleRemovedEvent;
import de.kaiserpfalzedv.commons.users.domain.model.role.events.RoleUpdateNameEvent;
import de.kaiserpfalzedv.commons.users.domain.model.role.events.RoleUpdateNameSpaceEvent;
import de.kaiserpfalzedv.commons.users.domain.services.RoleWriteService;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;


/**
 * @author klenkes74 {@literal <rlichti@kaiserpfalz-edv.de>}
 * @since 2025-05-17
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@ToString(onlyExplicitlyIncluded = true)
@XSlf4j
public class R2dbcRoleWriteService implements RoleWriteService {
  private final R2dbcRoleRepository repository;
  private final ApplicationEventPublisher bus;
  private final RoleToImpl toImpl;
  
  
  @Value("${spring.application.system:kp-users}")
  private String system = "kp-users";
  
  
  @PostConstruct
  public void init() {
    log.entry(bus, system);
    log.exit();
  }
  
  @PreDestroy
  public void close() {
    log.entry(bus, system);
    log.exit();
  }
  
  
  @Timed
  @Counted
  @Override
  public Mono<KpRole> create(@NotNull final Role role) {
    log.entry(role);
    
    Mono<KpRole> result = repository.save(toImpl.apply(role))
        .onErrorMap(DuplicateKeyException.class, e -> new RoleCantBeCreatedException(role, e))
        .doOnSuccess(r -> {
          log.info("Created role. role={}", r);
          bus.publishEvent(RoleCreatedEvent.builder().system(system).role(r).build());
        });
    
    return log.exit(result);
  }
  
  
  @Timed
  @Counted
  @Override
  public Mono<KpRole> updateNameSpace(@NotNull final UUID id, @NotNull final String namespace) {
    log.entry(id, namespace);
    
    Mono<KpRole> result = repository.findById(id)
        .switchIfEmpty(Mono.error(new RoleNotFoundException(id)))
        .map(role -> role.toBuilder().nameSpace(namespace).build())
        .flatMap(repository::save)
        .doOnSuccess(role -> {
          log.info("Updated role. nameSpace='{}', id={}", role.getNameSpace(), role.getId());
          bus.publishEvent(RoleUpdateNameSpaceEvent.builder().system(system).role(role).build());
        })
        .doOnError(
            OptimisticLockingFailureException.class,
            e -> log.error("Optimistic locking failure while updating role nameSpace. id={}, namespace={}", id, namespace, e)
        );
    
    return log.exit(result);
  }
  
  @Timed
  @Counted
  @Override
  public Mono<KpRole> updateName(@NotNull final UUID id, @NotNull final String name) {
    log.entry(id, name);
    
    Mono<KpRole> result = repository.findById(id)
        .switchIfEmpty(Mono.error(new RoleNotFoundException(id)))
        .map(role -> role.toBuilder().name(name).build())
        .flatMap(repository::save)
        .doOnSuccess(role -> {
          log.info("Updated role. name='{}', id={}", role.getName(), role.getId());
          bus.publishEvent(RoleUpdateNameEvent.builder().system(system).role(role).build());
        })
        .doOnError(
            OptimisticLockingFailureException.class,
            e -> log.error("Optimistic locking failure while updating role name. id={}, name={}", id, name, e)
        );
    
    return log.exit(result);
  }
  
  @Timed
  @Counted
  @Override
  public Mono<Void> remove(@NotNull final UUID id) {
    log.entry(id);
    
    Mono<Void> result = repository.deleteById(id)
        .doOnSuccess(role -> {
          log.info("Removed role. id={}", id);
          bus.publishEvent(RoleRemovedEvent.builder().system(system).id(id).build());
        })
        .doOnError(r -> log.error("Error while removing role. id={}", id));
    
    return log.exit(result);
  }
}
