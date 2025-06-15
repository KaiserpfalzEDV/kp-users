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

import de.kaiserpfalzedv.commons.users.domain.model.role.RoleToImpl;
import de.kaiserpfalzedv.commons.users.domain.model.user.KpUserDetails;
import de.kaiserpfalzedv.commons.users.domain.model.user.UserNotFoundException;
import de.kaiserpfalzedv.commons.users.domain.model.user.events.state.UserBannedEvent;
import de.kaiserpfalzedv.commons.users.domain.model.user.events.state.UserDetainedEvent;
import de.kaiserpfalzedv.commons.users.domain.model.user.events.state.UserReleasedEvent;
import lombok.extern.slf4j.XSlf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@XSlf4j
public class R2DbcUserRepositoryStateManagementServiceTest {
  @InjectMocks private R2dbcUserStateManagementService sut;
  @Mock private R2dbcUserRepository repository;
  @Mock private ApplicationEventPublisher bus;
  @Mock private RoleToImpl toJpa;
  
  
  @BeforeEach
  public void setUp() {
    reset(bus, repository, toJpa);
  }
  
  @AfterEach
  public void tearDown() {
    verifyNoMoreInteractions(bus, repository, toJpa);
    validateMockitoUsage();
  }

  
  @Test
  void shouldDetainUserWhenActive() {
    log.entry();
    
    when(repository.findById(DEFAULT_ID)).thenReturn(Mono.just(DEFAULT_JPA_USER));
    when(repository.save(any(KpUserDetails.class))).thenReturn(Mono.just(DEFAULT_JPA_USER.toBuilder().build()));
    
    sut.detain(DEFAULT_ID, 1).block();
    
    verify(bus).publishEvent(any(UserDetainedEvent.class));
    
    log.exit();
  }
  
  @Test
  void shouldThrowUserNotFoundExceptionWhenDetainingANonExistingUser() {
    log.entry();
    
    when(repository.findById(DEFAULT_ID)).thenReturn(Mono.empty());
    
    try {
      sut.detain(DEFAULT_ID, 1).block();
    } catch (Exception e) {
      log.error("Expected exception caught: {}", e.getMessage());
      
      assertInstanceOf(UserNotFoundException.class, e.getCause());
    }
    
    log.exit();
  }
  
  
  @Test
  void shouldBanUserWhenActive() {
    log.entry();
    
    when(repository.findById(DEFAULT_ID)).thenReturn(Mono.just(DEFAULT_JPA_USER));
    when(repository.save(any(KpUserDetails.class))).thenReturn(Mono.just(DEFAULT_JPA_USER.toBuilder().build()));
    
    sut.ban(DEFAULT_ID).block();
    
    verify(bus).publishEvent(any(UserBannedEvent.class));
    
    log.exit();
  }
  
  @Test
  void shouldThrowUserNotFoundExceptionWhenBanningANonExistingUser() {
    log.entry();
    
    when(repository.findById(DEFAULT_ID)).thenReturn(Mono.empty());

    try {
      sut.ban(DEFAULT_ID).block();
    } catch (Exception e) {
      log.error("Expected exception caught: {}", e.getMessage());
      
      assertInstanceOf(UserNotFoundException.class, e.getCause());
    }
    
    log.exit();
  }
  
  
  @Test
  void shouldReleaseUserWhenBanned() {
    log.entry();
    
    when(repository.findById(DEFAULT_ID)).thenReturn(Mono.just(DEFAULT_JPA_USER.toBuilder().bannedOn(CREATED_AT).build()));
    when(repository.save(any(KpUserDetails.class))).thenReturn(Mono.just(DEFAULT_JPA_USER));
    
    sut.release(DEFAULT_ID).block();
    
    verify(bus).publishEvent(any(UserReleasedEvent.class));
    
    log.exit();
  }
  
  @Test
  void shouldThrowUserNotFoundExceptionWhenReleasingANonExistingUser() {
    log.entry();
    
    when(repository.findById(DEFAULT_ID)).thenReturn(Mono.empty());
    
    try {
      sut.release(DEFAULT_ID).block();
    } catch (Exception e) {
      log.error("Expected exception caught: {}", e.getMessage());
      
      assertInstanceOf(UserNotFoundException.class, e.getCause());
    }
    
    log.exit();
  }
  
  
  private static final UUID DEFAULT_ID = UUID.randomUUID();
  private static final OffsetDateTime CREATED_AT = OffsetDateTime.now();
  private static final KpUserDetails DEFAULT_JPA_USER = KpUserDetails.builder()
      .id(DEFAULT_ID)
      
      .nameSpace("namespace")
      .name("name")
      
      .issuer("issuer")
      .subject(DEFAULT_ID.toString())
      
      .email("email@email.email")
      
      .created(CREATED_AT)
      .modified(CREATED_AT)
      
      .build();
}
