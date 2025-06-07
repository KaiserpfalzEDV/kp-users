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


import de.kaiserpfalzedv.commons.users.domain.model.user.events.arbitration.UserPetitionedEvent;
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
public class ReceiveUserArbitrationEventsConfigTest {
  
  @InjectMocks private ReceiveUserArbitrationEventsConfig sut;
  
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
  void shouldPostToBusWhenReceivingUserPetitioningEvent() {
    log.entry();
    
    // Given
    final var event = mock(UserPetitionedEvent.class);
    
    // When
    sut.petitionedUser().accept(event);
    
    // Then
    verify(bus).publishEvent(event);
    
    log.exit();
  }
}
