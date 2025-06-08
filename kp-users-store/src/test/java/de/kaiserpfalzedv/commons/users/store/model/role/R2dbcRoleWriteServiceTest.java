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


import de.kaiserpfalzedv.commons.api.events.EventBus;
import de.kaiserpfalzedv.commons.users.domain.model.role.KpRole;
import de.kaiserpfalzedv.commons.users.domain.model.role.RoleCantBeCreatedException;
import de.kaiserpfalzedv.commons.users.domain.model.role.RoleNotFoundException;
import de.kaiserpfalzedv.commons.users.domain.model.role.RoleToImpl;
import de.kaiserpfalzedv.commons.users.domain.model.role.events.RoleCreatedEvent;
import de.kaiserpfalzedv.commons.users.domain.model.role.events.RoleRemovedEvent;
import de.kaiserpfalzedv.commons.users.domain.model.role.events.RoleUpdateNameSpaceEvent;
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

/**
 * @author klenkes74 {@literal <rlichti@kaiserpfalz-edv.de>}
 * @since 2025-05-11
 */
@SuppressWarnings("LoggingSimilarMessage")
@ExtendWith(MockitoExtension.class)
@XSlf4j
public class R2dbcRoleWriteServiceTest {
  @InjectMocks
  private R2dbcRoleWriteService sut;
  
  @Mock
  private R2dbcRoleRepository repository;
  
  @Mock
  private EventBus bus;
  
  @Mock
  private RoleToImpl toJpa;
  
  
  @BeforeEach
  public void setUp() {
    reset(repository, bus, toJpa);
  }
  
  @AfterEach
  public void tearDown() {
    validateMockitoUsage(); // validate if the mocks are used as expected.
    verifyNoMoreInteractions(bus, toJpa, repository);
  }
  
  
  @Test
  void shouldCreateRoleWhenItDoesNotExistYet() {
    log.entry();
    
    when(toJpa.apply(DEFAULT_ROLE)).thenReturn(DEFAULT_JPA_ROLE);
    when(repository.save(DEFAULT_JPA_ROLE)).thenReturn(Mono.just(DEFAULT_JPA_ROLE));
    
    sut.create(DEFAULT_ROLE).block();
    
    verify(bus).post(any(RoleCreatedEvent.class));
    
    log.exit();
  }
  
  @Test
  void shouldThrowRoleCantBeCreatedExceptionWhenRoleAlreadyExists() {
    log.entry();

    when(toJpa.apply(DEFAULT_ROLE)).thenReturn(DEFAULT_JPA_ROLE);
    when(repository.save(DEFAULT_JPA_ROLE)).thenReturn(Mono.error(new OptimisticLockingFailureException("Test")));
    
    assertThrows(RoleCantBeCreatedException.class, () -> sut.create(DEFAULT_ROLE).block());
    
    verify(bus, never()).post(any(RoleCreatedEvent.class));
  }
  
  
  @Test
  void shouldUpdateNameSpaceWhenRoleExists() {
    log.entry();
    
    when(repository.findById(DEFAULT_ID)).thenReturn(Mono.just(DEFAULT_JPA_ROLE));
    when(repository.save(any(KpRole.class))).thenReturn(Mono.just(DEFAULT_JPA_ROLE.toBuilder().nameSpace("new-namespace").build()));
    
    sut.updateNameSpace(DEFAULT_ID, "new-namespace").block();
    
    verify(bus).post(any(RoleUpdateNameSpaceEvent.class));
    
    log.exit();
  }
  
  @Test
  void shouldThrowRoleNotFoundExceptionWhenUpdatingNameSpaceOfANonExistingRole() {
    log.entry();
    
    when(repository.findById(DEFAULT_ID)).thenReturn(Mono.empty());
    
    assertThrows(RoleNotFoundException.class, () -> sut.updateNameSpace(DEFAULT_ID, "new-namespace").block());
    
    verify(bus, never()).post(any(RoleUpdateNameSpaceEvent.class));
    
    log.exit();
  }
  
  
  @Test
  void shouldUpdateNameWhenRoleExists() {
    log.entry();
    
    when(repository.findById(DEFAULT_ID)).thenReturn(Mono.just(DEFAULT_JPA_ROLE));
    when(repository.save(any(KpRole.class))).thenReturn(Mono.just(DEFAULT_JPA_ROLE.toBuilder().name("new-name").build()));
    
    sut.updateName(DEFAULT_ID, "new-name").block();
    
    verify(bus).post(any(RoleUpdateNameSpaceEvent.class));
    
    log.exit();
  }
  
  @Test
  void shouldThrowRoleNotFoundExceptionWhenUpdatingNameOfANonExistingRole() {
    log.entry();
    
    when(repository.findById(DEFAULT_ID)).thenReturn(Mono.empty());
    
    assertThrows(RoleNotFoundException.class, () -> sut.updateName(DEFAULT_ID, "new-name").block());
    
    verify(bus, never()).post(any(RoleUpdateNameSpaceEvent.class));
    
    log.exit();
  }
  
  
  @Test
  void shouldRemoveRoleWhenRoleExists() {
    log.entry();
    
    when(repository.findById(DEFAULT_ID)).thenReturn(Mono.just(DEFAULT_JPA_ROLE));
    
    sut.remove(DEFAULT_ID).block();
    
    verify(repository).delete(DEFAULT_JPA_ROLE);
    verify(bus).post(any(RoleRemovedEvent.class));
    
    log.exit();
  }
  
  @Test
  void shouldDoNothingWhenRemovingANonExistingRole() {
    log.entry();
    
    when(repository.findById(DEFAULT_ID)).thenReturn(Mono.empty());
    
    sut.remove(DEFAULT_ID).block();
    
    verify(repository, never()).delete(any(KpRole.class));
    verify(bus, never()).post(any(RoleRemovedEvent.class));
    
    log.exit();
  }
  
  
  
  private static final UUID DEFAULT_ID = UUID.randomUUID();
  private static final OffsetDateTime CREATED_AT = OffsetDateTime.now();
  private static final KpRole DEFAULT_ROLE = KpRole.builder()
      .id(DEFAULT_ID)
      .nameSpace("namespace")
      .name("name")
      .created(CREATED_AT)
      .modified(CREATED_AT)
      .build();
  private static final KpRole DEFAULT_JPA_ROLE = KpRole.builder()
      .id(DEFAULT_ID)
      .nameSpace("namespace")
      .name("name")
      .created(CREATED_AT)
      .modified(CREATED_AT)
      .build();
}
