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


import de.kaiserpfalzedv.commons.users.domain.model.user.events.state.*;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

/**
 * Configuration for receiving user state events.
 *
 * @author klenkes74 {@literal <rlichti@kaiserpfalz-edv.de>}
 * @since 2025-05-18
 */
@Configuration
@RequiredArgsConstructor(onConstructor_ = @__(@Autowired))
@ToString(onlyExplicitlyIncluded = true)
@XSlf4j
public class ReceiveUserStateEventsConfig {
  private final ApplicationEventPublisher bus;
  
  @Bean
  public Consumer<UserActivatedEvent> activateUser() {
    return event -> {
      log.entry(event);
      
      log.info("Received external event. event = {}", event);
      bus.publishEvent(event);
      
      log.exit();
    };
  }
  
  @Bean
  public Consumer<UserBannedEvent> banUser() {
    return event -> {
      log.entry(event);
      
      log.info("Received external event. event = {}", event);
      bus.publishEvent(event);
      
      log.exit();
    };
  }
  
  @Bean
  public Consumer<UserCreatedEvent> createUser() {
    return event -> {
      log.entry(event);
      
      log.info("Received external event. event = {}", event);
      bus.publishEvent(event);
      
      log.exit();
    };
  }
  
  @Bean
  public Consumer<UserDeletedEvent> deleteUser() {
    return event -> {
      log.entry(event);
      
      log.info("Received external event. event = {}", event);
      bus.publishEvent(event);
      
      log.exit();
    };
  }
  
  @Bean
  public Consumer<UserDetainedEvent> detainUser() {
    return event -> {
      log.entry(event);
      
      log.info("Received external event. event = {}", event);
      bus.publishEvent(event);
      
      log.exit();
    };
  }
  
  @Bean
  public Consumer<UserReleasedEvent> releaseUser() {
    return event -> {
      log.entry(event);
      
      log.info("Received external event. event = {}", event);
      bus.publishEvent(event);
      
      log.exit();
    };
  }
  
  @Bean
  public Consumer<UserRemovedEvent> removeUser() {
    return event -> {
      log.entry(event);
      
      log.info("Received external event. event = {}", event);
      bus.publishEvent(event);
      
      log.exit();
    };
  }
}
