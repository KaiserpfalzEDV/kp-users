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

package de.kaiserpfalzedv.commons.users.store.service;


import de.kaiserpfalzedv.commons.users.domain.model.role.events.*;
import de.kaiserpfalzedv.commons.users.store.model.role.R2dbcRoleWriteService;
import de.kaiserpfalzedv.commons.users.store.model.user.R2dbcUserRoleManagementService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.Duration;


/**
 * @author klenkes74 {@literal <rlichti@kaiserpfalz-edv.de>}
 * @since 2025-05-11
 */
@Service
@Scope("singleton")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@XSlf4j
public class R2dbcRoleEventsHandler implements RoleEventsHandler, AutoCloseable {
  public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(1L);
  
  private final R2dbcRoleWriteService writeService;
  private final R2dbcUserRoleManagementService userRoleManagement;
  
  @Value("${spring.application.system:kp-users}")
  private String system = "kp-users";
  
  
  @PostConstruct
  public void init() {
    log.entry(system);
    log.exit();
  }
  
  @Override
  @PreDestroy
  public void close() {
    log.entry(system);
    log.exit();
  }
  
  
  @Override
  @EventListener
  public void event(@NotNull final RoleCreatedEvent event) {
    log.entry(event);
    
    if (eventIsFromExternalSystem(event)) {
      writeService.create(event.getRole()).block(DEFAULT_TIMEOUT);
    }
    
    log.exit();
  }
  
  @Override
  @EventListener
  public void event(@NotNull final RoleUpdateNameSpaceEvent event) {
    log.entry(event);
    
    if (eventIsFromExternalSystem(event)) {
      writeService.updateNameSpace(event.getRole().getId(), event.getRole().getNameSpace()).block(DEFAULT_TIMEOUT);
    }
    
    log.exit();
  }
  
  
  @Override
  @EventListener
  public void event(@NotNull final RoleUpdateNameEvent event) {
    log.entry(event);
    
    if (eventIsFromExternalSystem(event)) {
      writeService.updateName(event.getRole().getId(), event.getRole().getName()).block(DEFAULT_TIMEOUT);
    }
    
    log.exit();
  }
  
  
  @Override
  @EventListener
  public void event(@NotNull final RoleRemovedEvent event) {
    log.entry(event);
    
    if (eventIsFromExternalSystem(event)) {
      Long count = userRoleManagement.revokeRoleFromAllUsers(event.getRole()).block();
      log.info("Removed Role from all users. count={}, role={}", count, event.getRole());
      writeService.remove(event.getRole().getId()).block(DEFAULT_TIMEOUT);
    }
    
    log.exit();
  }
  
  
  /**
   * Check if the event is from an external application.
   *
   * @param event The event to check.
   * @return True if the event is from an external application, false otherwise.
   */
  private boolean eventIsFromExternalSystem(final RoleBaseEvent event) {
    log.entry(event);
    
    boolean result;
    if (system.equals(event.getSystem())) {
      log.debug("System is the same. Ignoring event. event={}", event);
      result = false;
    } else {
      result = true;
    }
    
    return log.exit(result);
  }
}
