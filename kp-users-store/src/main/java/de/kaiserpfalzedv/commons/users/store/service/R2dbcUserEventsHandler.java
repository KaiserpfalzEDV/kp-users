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


import de.kaiserpfalzedv.commons.users.domain.model.role.RoleNotFoundException;
import de.kaiserpfalzedv.commons.users.domain.model.user.UserCantBeCreatedException;
import de.kaiserpfalzedv.commons.users.domain.model.user.UserNotFoundException;
import de.kaiserpfalzedv.commons.users.domain.model.user.events.UserBaseEvent;
import de.kaiserpfalzedv.commons.users.domain.model.user.events.UserEventsHandler;
import de.kaiserpfalzedv.commons.users.domain.model.user.events.activity.UserLoginEvent;
import de.kaiserpfalzedv.commons.users.domain.model.user.events.activity.UserLogoutEvent;
import de.kaiserpfalzedv.commons.users.domain.model.user.events.arbitration.UserPetitionedEvent;
import de.kaiserpfalzedv.commons.users.domain.model.user.events.modification.*;
import de.kaiserpfalzedv.commons.users.domain.model.user.events.state.*;
import de.kaiserpfalzedv.commons.users.store.model.user.R2dbcUserDataManagementService;
import de.kaiserpfalzedv.commons.users.store.model.user.R2dbcUserManagementService;
import de.kaiserpfalzedv.commons.users.store.model.user.R2dbcUserRoleManagementService;
import de.kaiserpfalzedv.commons.users.store.model.user.R2dbcUserStateManagementService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.Duration;


/**
 * Handles user events and updates the user repository accordingly.
 *
 * @author klenkes74 {@literal <rlichti@kaiserpfalz-edv.de>}
 * @since 2025-04-19
 */
@SuppressWarnings("LoggingSimilarMessage")
@Service
@Scope("singleton")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@XSlf4j
public class R2dbcUserEventsHandler implements UserEventsHandler, AutoCloseable {
  private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(1L);
  
  private final R2dbcUserManagementService service;
  private final R2dbcUserDataManagementService dataService;
  private final R2dbcUserRoleManagementService roleService;
  private final R2dbcUserStateManagementService stateService;
  
  
  @Value("${spring.application.application:kp-users}")
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
  public void event(final UserActivatedEvent event) {
    log.entry(event);
    
    if (eventIsFromExternalSystem(event)) {
      try {
        service.undelete(event.getUser().getId()).block(DEFAULT_TIMEOUT);
      } catch (Exception e) {
        if (e.getCause() instanceof UserNotFoundException) {
          log.warn("User not found for this id. id={}", event.getUser().getId());
        } else {
          throw e;
        }
      }
    }
    
    log.exit();
  }
  
  @Override
  @EventListener
  public void event(final UserCreatedEvent event) {
    log.entry(event);
    
    if (eventIsFromExternalSystem(event)) {
      try {
        service.create(event.getUser()).block(DEFAULT_TIMEOUT);
      } catch (Exception e) {
        if (e.getCause() instanceof UserCantBeCreatedException) {
          log.warn("{}", e.getCause().getMessage());
        } else {
          throw e;
        }
      }
    }
    
    log.exit();
  }
  
  @Override
  @EventListener
  public void event(final UserDeletedEvent event) {
    log.entry(event);
    
    if (eventIsFromExternalSystem(event)) {
      try {
        service.delete(event.getUser().getId()).block(DEFAULT_TIMEOUT);
      } catch (Exception e) {
        if (e.getCause() instanceof UserNotFoundException) {
          log.warn("User not found for this id. id={}", event.getUser().getId());
        } else {
          throw e;
        }
      }
    }
    
    log.exit();
  }
  
  @Override
  @EventListener
  public void event(final UserRemovedEvent event) {
    log.entry(event);
    
    if (eventIsFromExternalSystem(event)) {
      service.remove(event.getUser().getId()).block(DEFAULT_TIMEOUT);
    }
    
    log.exit();
  }
  
  @Override
  @EventListener
  public void event(final UserBannedEvent event) {
    log.entry(event);
    
    if (eventIsFromExternalSystem(event)) {
      try {
        stateService.ban(event.getUser().getId()).block(DEFAULT_TIMEOUT);
      } catch (Exception e) {
        if (e.getCause() instanceof UserNotFoundException) {
          log.warn("User not found for this id. id={}", event.getUser().getId());
        } else {
          // This is a general error, so we log it as an error.
          log.warn(e.getMessage(), e);
        }
      }
    }
    
    log.exit();
  }
  
  @Override
  @EventListener
  public void event(final UserDetainedEvent event) {
    log.entry(event);
    
    if (eventIsFromExternalSystem(event)) {
      try {
        stateService.detain(event.getUser().getId(), event.getDays()).block(DEFAULT_TIMEOUT);
      } catch (Exception e) {
        if (e.getCause() instanceof UserNotFoundException) {
          log.warn("User not found for this id. id={}", event.getUser().getId());
        } else {
          throw e;
        }
      }
    }
    log.exit();
  }
  
  @Override
  @EventListener
  public void event(final UserPetitionedEvent event) {
    log.entry(event);
    
    if (eventIsFromExternalSystem(event)) {
      log.info("User petitioned event not yet implemented!");
    }
    
    log.exit();
  }
  
  @Override
  @EventListener
  public void event(final UserReleasedEvent event) {
    log.entry(event);
    
    if (eventIsFromExternalSystem(event)) {
      try {
        // Release the user from detention.
        stateService.release(event.getUser().getId()).block(DEFAULT_TIMEOUT);
      } catch (Exception e) {
        if (e.getCause() instanceof UserNotFoundException) {
          log.warn("User not found for this id. id={}", event.getUser().getId());
        } else {
          throw e;
        }
      }
    }
    
    log.exit();
  }
  
  @Override
  @EventListener
  public void event(final UserLoginEvent event) {
    log.entry(event);
    
    if (eventIsFromExternalSystem(event)) {
      // TODO 2025-05-10 klenkes74 Implement a user log database.
      log.info("User logged in. user={}", event.getUser());
    }
    
    log.exit();
  }
  
  @Override
  @EventListener
  public void event(final UserLogoutEvent event) {
    log.entry(event);
    
    if (eventIsFromExternalSystem(event)) {
      // TODO 2025-05-10 klenkes74 Implement a user log database.
      log.info("User logged out. user={}", event.getUser());
    }
    
    log.exit();
  }
  
  @Override
  @EventListener
  public void event(final RoleAddedToUserEvent event) {
    log.entry(event);
    
    if (eventIsFromExternalSystem(event)) {
      try {
        // Add the role to the user.
        roleService.addRole(event.getUser().getId(), event.getRole()).block(DEFAULT_TIMEOUT);
      } catch (Exception e) {
        if (e.getCause() instanceof UserNotFoundException) {
          log.warn("User not found for this id. id={}", event.getUser().getId());
        } else if (e.getCause() instanceof RoleNotFoundException) {
          log.warn("Role not found for this id. id={}", event.getRole().getId());
        } else {
          throw e;
        }
      }
    }
  }
  
  @Override
  @EventListener
  public void event(final RoleRemovedFromUserEvent event) {
    log.entry(event);
    
    if (eventIsFromExternalSystem(event)) {
      try {
        // Remove the role from the user.
        roleService.removeRole(event.getUser().getId(), event.getRole()).block(DEFAULT_TIMEOUT);
      } catch (Exception e) {
        if (e.getCause() instanceof UserNotFoundException) {
          log.warn("User not found for this id. id={}", event.getUser().getId());
        } else if (e.getCause() instanceof RoleNotFoundException) {
          log.warn("Role not found for this id. id={}", event.getRole().getId());
        } else {
          throw e;
        }
      }
    }
  }
  
  
  @Override
  @EventListener
  public void event(final UserSubjectModificationEvent event) {
    log.entry(event);
    
    log.info("Modifying user subject. user={}", event.getUser());
    
    if (eventIsFromExternalSystem(event)) {
      try {
        dataService.updateSubject(event.getUser().getId(), event.getUser().getIssuer(), event.getUser().getSubject()).block(DEFAULT_TIMEOUT);
      } catch (Exception e) {
        if (e.getCause() instanceof UserNotFoundException) {
          log.warn("User not found for this id. id={}", event.getUser().getId());
        } else {
          throw e;
        }
      }
    }
    
    log.exit();
  }
  
  @Override
  @EventListener
  public void event(final UserNamespaceAndNameModificationEvent event) {
    log.entry(event);
    
    if (eventIsFromExternalSystem(event)) {
      try {
        dataService.updateNamespaceAndName(event.getUser().getId(), event.getUser().getNameSpace(), event.getUser().getName()).block(DEFAULT_TIMEOUT);
      } catch (Exception e) {
        if (e.getCause() instanceof UserNotFoundException) {
          log.warn("User not found for this id. id={}", event.getUser().getId());
        } else {
          throw e;
        }
      }
    }
    
    log.exit();
  }
  
  @Override
  @EventListener
  public void event(final UserNamespaceModificationEvent event) {
    log.entry(event);
    
    if (eventIsFromExternalSystem(event)) {
      try {
        dataService.updateNamespace(event.getUser().getId(), event.getUser().getNameSpace()).block(DEFAULT_TIMEOUT);
      } catch (Exception e) {
        if (e.getCause() instanceof UserNotFoundException) {
          log.warn("User not found for this id. id={}", event.getUser().getId());
        } else {
          throw e;
        }
      }
    }
    
    log.exit();
  }
  
  @Override
  @EventListener
  public void event(final UserNameModificationEvent event) {
    log.entry(event);
    
    if (eventIsFromExternalSystem(event)) {
      try {
        dataService.updateName(event.getUser().getId(), event.getUser().getName()).block(DEFAULT_TIMEOUT);
      } catch (Exception e) {
        if (e.getCause() instanceof UserNotFoundException) {
          log.warn("User not found for this id. id={}", event.getUser().getId());
        } else {
          throw e;
        }
      }
    }
    
    log.exit();
  }
  
  @Override
  @EventListener
  public void event(final UserEmailModificationEvent event) {
    log.entry(event);
    
    if (eventIsFromExternalSystem(event)) {
      try {
        dataService.updateEmail(event.getUser().getId(), event.getUser().getEmail()).block(DEFAULT_TIMEOUT);
      } catch (Exception e) {
        if (e.getCause() instanceof UserNotFoundException) {
          log.warn("User not found for this id. id={}", event.getUser().getId());
        } else {
          throw e;
        }
      }
    }
    
    log.exit();
  }
  
  @Override
  @EventListener
  public void event(final UserDiscordModificationEvent event) {
    log.entry(event);
    
    if (eventIsFromExternalSystem(event)) {
      try {
        dataService.updateDiscord(event.getUser().getId(), event.getUser().getDiscord()).block(DEFAULT_TIMEOUT);
      } catch (Exception e) {
        if (e.getCause() instanceof UserNotFoundException) {
          log.warn("User not found for this id. id={}", event.getUser().getId());
        } else {
          throw e;
        }
      }
    }
    
    log.exit();
  }
  
  
  /**
   * Check if the event is from an external application.
   *
   * @param event The event to check.
   * @return True if the event is from an external application, false otherwise.
   */
  private boolean eventIsFromExternalSystem(final UserBaseEvent event) {
    log.entry(event);
    
    boolean result;
    if (system.equals(event.getApplication())) {
      log.debug("System is the same. Ignoring event. event={}", event);
      result = false;
    } else {
      result = true;
    }
    
    return log.exit(result);
  }
}
