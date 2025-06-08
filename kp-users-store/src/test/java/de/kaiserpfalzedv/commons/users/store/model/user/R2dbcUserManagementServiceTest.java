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

import de.kaiserpfalzedv.commons.api.events.EventBus;
import de.kaiserpfalzedv.commons.users.domain.model.user.KpUserDetails;
import de.kaiserpfalzedv.commons.users.domain.model.user.UserCantBeCreatedException;
import de.kaiserpfalzedv.commons.users.domain.model.user.UserNotFoundException;
import de.kaiserpfalzedv.commons.users.domain.model.user.UserToKpUserDetailsImpl;
import de.kaiserpfalzedv.commons.users.domain.model.user.events.state.UserActivatedEvent;
import de.kaiserpfalzedv.commons.users.domain.model.user.events.state.UserCreatedEvent;
import de.kaiserpfalzedv.commons.users.domain.model.user.events.state.UserDeletedEvent;
import de.kaiserpfalzedv.commons.users.domain.model.user.events.state.UserRemovedEvent;
import lombok.extern.slf4j.XSlf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@XSlf4j
public class R2dbcUserManagementServiceTest {
  
  @InjectMocks
  private R2dbcUserManagementService sut;
  
  @Mock
  private R2dbcUserRepository repository;
  
  @Mock
  private EventBus bus;
  
  @Mock
  private UserToKpUserDetailsImpl toJpa;
  
  
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
  void shouldCreateUserSuccessfullyWhenUserDoesNotExistAlready() {
    log.entry();
    
    when(toJpa.apply(DEFAULT_USER)).thenReturn(DEFAULT_JPA_USER);
    when(repository.save(DEFAULT_JPA_USER)).thenReturn(Mono.just(DEFAULT_JPA_USER));
    
    sut.create(DEFAULT_USER).block();
    
    verify(repository).save(DEFAULT_JPA_USER);
    verify(bus).post(any(UserCreatedEvent.class));
    
    log.exit();
  }
  
  @Test
  void shouldThrowExceptionWhenCreatingAnAlreadyExistingUser() {
    log.entry();
    
    when(toJpa.apply(DEFAULT_USER)).thenReturn(DEFAULT_JPA_USER);
    when(repository.save(DEFAULT_JPA_USER)).thenThrow(new OptimisticLockingFailureException("Test"));
    
    assertThrows(UserCantBeCreatedException.class, () -> sut.create(DEFAULT_USER).block());

    verify(bus, never()).post(any(UserCreatedEvent.class));
    
    log.exit();
  }
  
  
  @Test
  void shouldDeleteUserSuccessfullyWhenUserExists() {
    log.entry();
    
    when(repository.findById(DEFAULT_ID)).thenReturn(Mono.just(DEFAULT_JPA_USER));
    when(repository.save(DEFAULT_JPA_USER)).thenReturn(Mono.just(DEFAULT_JPA_USER));
    
    sut.delete(DEFAULT_ID).block();

    verify(bus, times(1)).post(any(UserDeletedEvent.class));
    
    
    log.exit();
  }
  
  @Test
  void shouldDeleteUserSuccessfullyWhenUserDoesNotExist() {
    log.entry();
    
    when(repository.findById(DEFAULT_ID)).thenReturn(Mono.empty());
    
    sut.delete(DEFAULT_ID).block();
    
    verify(bus, never()).post(any(UserDeletedEvent.class));
    
    log.exit();
  }
  
  
  @Test
  void shouldUndeleteUserSuccessfullyWhenUserExists() {
    log.entry();
    
    when(repository.findById(DEFAULT_ID)).thenReturn(Mono.just(DEFAULT_JPA_USER));
    when(repository.save(DEFAULT_JPA_USER)).thenReturn(Mono.just(DEFAULT_JPA_USER));
   
    sut.undelete(DEFAULT_ID).block();
    
    verify(bus, times(1)).post(any(UserActivatedEvent.class));
    
    log.exit();
  }
  
  @Test
  void shouldThrowExceptionWhenUndeletingUserThatDoesNotExist() {
    log.entry();
    
    when(repository.findById(DEFAULT_ID)).thenReturn(Mono.empty());
    
    assertThrows(UserNotFoundException.class, () -> sut.undelete(DEFAULT_ID).block());
    
    verify(bus, never()).post(any(UserActivatedEvent.class));
    
    log.exit();
  }
  
  
  @Test
  void shouldRemoveUserSuccessfullyWhenUserExists() {
    log.entry();
    
    when(repository.findById(DEFAULT_ID)).thenReturn(Mono.just(DEFAULT_JPA_USER));
    
    sut.remove(DEFAULT_ID).block();
    
    verify(repository).delete(DEFAULT_JPA_USER);
    verify(bus, times(1)).post(any(UserRemovedEvent.class));
    
    log.exit();
  }
  
  @Test
  void shouldQuietSilentlyWhenRemovingUserThatDoesNotExist() {
    log.entry();
    
    when(repository.findById(DEFAULT_ID)).thenReturn(Mono.empty());
    
    sut.remove(DEFAULT_ID).block();
    
    verify(repository, never()).delete(any(KpUserDetails.class));
    verify(bus, never()).post(any(UserRemovedEvent.class));
    
    log.exit();
  }
  
  
  private static final UUID DEFAULT_ID = UUID.randomUUID();
  private static final OffsetDateTime CREATED_AT = OffsetDateTime.now();
  private static final KpUserDetails DEFAULT_USER = KpUserDetails.builder()
      .id(DEFAULT_ID)
      
      .nameSpace("namespace")
      .name("name")
      
      .issuer("issuer")
      .subject(DEFAULT_ID.toString())
      
      .email("email@email.email")
      
      .created(CREATED_AT)
      .modified(CREATED_AT)
      
      .build();
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
