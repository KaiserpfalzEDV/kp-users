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
public class ReceiveUserModificationEventsConfigTest {
  @InjectMocks private ReceiveUserModificationEventsConfig sut;
  
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
  void shouldPostToBusWhenReceivingRoleAddedToUserEvent() {
    log.entry();

    // Given
    final var event = mock(RoleAddedToUserEvent.class);
    
    // When
    sut.addingRole().accept(event);
    
    // Then
    verify(bus).publishEvent(event);

    log.exit();
  }

  
  
  @Test
  void shouldPostToBusWhenReceivingRoleRemovedFromUserEvent() {
    log.entry();

    // Given
    final var event = mock(RoleRemovedFromUserEvent.class);
    
    // When
    sut.removeRole().accept(event);
    
    // Then
    verify(bus).publishEvent(event);

    log.exit();
  }

  
  
  @Test
  void shouldPostToBusWhenReceivingDiscordModificationEvent() {
    log.entry();

    // Given
    final var event = mock(UserDiscordModificationEvent.class);
    
    // When
    sut.modifyDiscord().accept(event);
    
    // Then
    verify(bus).publishEvent(event);

    log.exit();
  }

  
  @Test
  void shouldPostToBusWhenReceivingUserEmailModificationEvent() {
    log.entry();

    // Given
    final var event = mock(UserEmailModificationEvent.class);
    
    // When
    sut.modifyEmail().accept(event);
    
    // Then
    verify(bus).publishEvent(event);

    log.exit();
  }

  
  @Test
  void shouldPostToBusWhenReceivingUserNameModificationEvent() {
    log.entry();

    // Given
    final var event = mock(UserNameModificationEvent.class);
    
    // When
    sut.modifyName().accept(event);
    
    // Then
    verify(bus).publishEvent(event);

    log.exit();
  }

  
  @Test
  void shouldPostToBusWhenReceivingUserNamespaceModificationEvent() {
    log.entry();

    // Given
    final var event = mock(UserNamespaceModificationEvent.class);
    
    // When
    sut.modifyNamespace().accept(event);
    
    // Then
    verify(bus).publishEvent(event);

    log.exit();
  }

  
  @Test
  void shouldPostToBusWhenReceivingUserNamespaceAndNameModificationEvent() {
    log.entry();

    // Given
    final var event = mock(UserNamespaceAndNameModificationEvent.class);
    
    // When
    sut.modifyNamespaceAndName().accept(event);
    
    // Then
    verify(bus).publishEvent(event);

    log.exit();
  }

  
  @Test
  void shouldPostToBusWhenReceivingUserSubjectModificationEvent() {
    log.entry();

    // Given
    final var event = mock(UserSubjectModificationEvent.class);
    
    // When
    sut.modifySubject().accept(event);
    
    // Then
    verify(bus).publishEvent(event);

    log.exit();
  }

}
