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
import lombok.extern.slf4j.XSlf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.Mockito.*;


/**
 * @author klenkes74 {@literal <rlichti@kaiserpfalz-edv.de>}
 * @since 2025-05-18
 */
@ExtendWith(MockitoExtension.class)
@XSlf4j
public class ReceiveUserStateEventsConfigTest {
  @InjectMocks private ReceiveUserStateEventsConfig sut;
  
  @Mock private ApplicationEventPublisher bus;
  
  
  @BeforeEach
  public void setUp() {
    reset(bus);
  }
  
  @AfterEach
  public void tearDown() {
    validateMockitoUsage();
    verifyNoMoreInteractions(bus);
  }
  
  
  @Test
  void shouldPostToBusWhenReceivingUserActivatedEvent() {
    log.entry();
    
    // Given
    final var event = mock(UserActivatedEvent.class);
    
    // When
    sut.activateUser().accept(event);
    
    // Then
    verify(bus).publishEvent(event);
    
    log.exit();
  }
  
  @Test
  void shouldPostToBusWhenReceivingBanUserEvent() {
    log.entry();
    
    // Given
    final var event = mock(UserBannedEvent.class);
    
    // When
    sut.banUser().accept(event);
    
    // Then
    verify(bus).publishEvent(event);
    
    log.exit();
  }
  
  @Test
  void shouldPostToBusWhenReceivingCreateUserEvent() {
    log.entry();
    
    // Given
    final var event = mock(UserCreatedEvent.class);
    
    // When
    sut.createUser().accept(event);
    
    // Then
    verify(bus).publishEvent(event);
    
    log.exit();
  }
  
  @Test
  void shouldPostToBusWhenReceivingDeleteUserEvent() {
    log.entry();
    
    // Given
    final var event = mock(UserDeletedEvent.class);
    
    // When
    sut.deleteUser().accept(event);
    
    // Then
    verify(bus).publishEvent(event);
    
    log.exit();
  }
  
  @Test
  void shouldPostToBusWhenReceivingDetainUserEvent() {
    log.entry();
    
    // Given
    final var event = mock(UserDetainedEvent.class);
    
    // When
    sut.detainUser().accept(event);
    
    // Then
    verify(bus).publishEvent(event);
    
    log.exit();
  }
  
  @Test
  void shouldPostToBusWhenReceivingReleaseUserEvent() {
    log.entry();
    
    // Given
    final var event = mock(UserReleasedEvent.class);
    
    // When
    sut.releaseUser().accept(event);
    
    // Then
    verify(bus).publishEvent(event);
    
    log.exit();
  }
  
  @Test
  void shouldPostToBusWhenReceivingUserRemovedEvent() {
    log.entry();
    
    // Given
    final var event = mock(UserRemovedEvent.class);
    
    // When
    sut.removeUser().accept(event);
    
    // Then
    verify(bus).publishEvent(event);
    
    log.exit();
  }
}
