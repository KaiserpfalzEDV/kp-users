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

package de.kaiserpfalzedv.commons.users.messaging;


import de.kaiserpfalzedv.commons.users.domain.model.user.events.modification.*;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

/**
 * Configuration for receiving user modification events.
 *
 * @author klenkes74 {@literal <rlichti@kaiserpfalz-edv.de>}
 * @since 2025-05-18
 */
@Configuration
@RequiredArgsConstructor(onConstructor_ = @__(@Autowired))
@ToString(onlyExplicitlyIncluded = true)
@XSlf4j
public class ReceiveUserModificationEventsConfig {
  private final ApplicationEventPublisher bus;
  
  
  @Bean
  public Consumer<RoleAddedToUserEvent> addingRole() {
    return event -> {
      log.entry(event);
      
      log.info("Received external event. event={}", event);
      bus.publishEvent(event);
      
      log.exit();
    };
  }
  
  @Bean
  public Consumer<RoleRemovedFromUserEvent> removeRole() {
    return event -> {
      log.entry(event);
      
      log.info("Received external event. event={}", event);
      bus.publishEvent(event);
      
      log.exit();
    };
  }
  
  @Bean
  public Consumer<UserDiscordModificationEvent> modifyDiscord() {
    return event -> {
      log.entry(event);
      
      log.info("Received external event. event={}", event);
      bus.publishEvent(event);
      
      log.exit();
    };
  }
  
  @Bean
  public Consumer<UserEmailModificationEvent> modifyEmail() {
    return event -> {
      log.entry(event);
      
      log.info("Received external event. event={}", event);
      bus.publishEvent(event);
      
      log.exit();
    };
  }
  
  @Bean
  public Consumer<UserNameModificationEvent> modifyName() {
    return event -> {
      log.entry(event);
      
      log.info("Received external event. event={}", event);
      bus.publishEvent(event);
      
      log.exit();
    };
  }
  
  @Bean
  public Consumer<UserNamespaceModificationEvent> modifyNamespace() {
    return event -> {
      log.entry(event);
      
      log.info("Received external event. event={}", event);
      bus.publishEvent(event);
      
      log.exit();
    };
  }
  
  @Bean
  public Consumer<UserNamespaceAndNameModificationEvent> modifyNamespaceAndName() {
    return event -> {
      log.entry(event);
      
      log.info("Received external event. event={}", event);
      bus.publishEvent(event);
      
      log.exit();
    };
  }
  
  @Bean
  public Consumer<UserSubjectModificationEvent> modifySubject() {
    return event -> {
      log.entry(event);
      
      log.info("Received external event. event={}", event);
      bus.publishEvent(event);
      
      log.exit();
    };
  }
}
