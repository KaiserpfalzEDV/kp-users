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


import de.kaiserpfalzedv.commons.users.domain.model.role.KpRole;
import de.kaiserpfalzedv.commons.users.domain.model.role.RoleCantBeCreatedException;
import de.kaiserpfalzedv.commons.users.domain.model.role.events.RoleCreatedEvent;
import de.kaiserpfalzedv.commons.users.domain.model.role.events.RoleRemovedEvent;
import de.kaiserpfalzedv.commons.users.domain.model.role.events.RoleUpdateNameEvent;
import de.kaiserpfalzedv.commons.users.domain.model.role.events.RoleUpdateNameSpaceEvent;
import de.kaiserpfalzedv.commons.users.store.model.role.R2dbcRoleWriteService;
import de.kaiserpfalzedv.commons.users.store.model.user.R2dbcUserRoleManagementService;
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
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

/**
 * @author klenkes74 {@literal <rlichti@kaiserpfalz-edv.de>}
 * @since 2025-05-17
 */
@ExtendWith(MockitoExtension.class)
@XSlf4j
public class R2dbcRoleEventsHandlerTest {
  @InjectMocks
  private R2dbcRoleEventsHandler sut;

  @Mock
  private R2dbcRoleWriteService writeService;

  @Mock
  private R2dbcUserRoleManagementService userRoleManagement;

  @Mock
  private ApplicationEventPublisher bus;
  
  private static final String LOCAL_SYSTEM = "kp-users";
  private static final String EXTERNAL_SYSTEM = "other-application";
  
  
  private static final UUID TEST_ROLE_ID = UUID.randomUUID();
  private static final String TEST_ROLE_NAME = "Test Role";
  private static final String TEST_ROLE_NAMESPACE = "Test Namespace";
  private static final OffsetDateTime TEST_ROLE_CREATED_TIME = OffsetDateTime.now().minusDays(100);
  private KpRole role;

  @BeforeEach
  void setUp() {
      reset(writeService, userRoleManagement, bus);
      
      role = KpRole.builder()
          .id(TEST_ROLE_ID)
          
          .name(TEST_ROLE_NAME)
          .nameSpace(TEST_ROLE_NAMESPACE)
          
          .created(TEST_ROLE_CREATED_TIME)
          .modified(TEST_ROLE_CREATED_TIME)
          
          .build();
  }
  
  @AfterEach
  public void tearDown() {
    validateMockitoUsage();
    verifyNoMoreInteractions(writeService, userRoleManagement, bus);
  }
  
  
  @Test
  void shouldCreateRoleOnRoleCreatedEventWhenEventIsFromExternalSystem() {
    log.entry();
    
    // given
    RoleCreatedEvent event = mock(RoleCreatedEvent.class);
    when(event.getSystem()).thenReturn(EXTERNAL_SYSTEM);
    when(event.getRole()).thenReturn(role);
    when(writeService.create(any(KpRole.class))).thenReturn(Mono.just(role));
    
    // when
    sut.event(event);
    
  }
  
  @Test
  void shouldHandleExceptionWhenCreationFailsWhenEventIsFromExternalSystem() {
    log.entry();
    
    // given
    RoleCreatedEvent event = mock(RoleCreatedEvent.class);
    when(event.getSystem()).thenReturn(EXTERNAL_SYSTEM);
    when(event.getRole()).thenReturn(role);
    when(writeService.create(any(KpRole.class))).thenReturn(Mono.error(new RoleCantBeCreatedException(role, new IllegalStateException())));
    
    // when
    try {
      sut.event(event);
      fail("Expected exception to be thrown, but it was not.");
    } catch (Exception e) {
      log.debug("Caught exception. type={}, message={}", e.getCause().getClass().getSimpleName(), e.getCause().getMessage());
      
      assertInstanceOf(RoleCantBeCreatedException.class, e.getCause());
    }
    
    // then
    
    log.exit();
  }
  
  @Test
  void shouldIgnoreRoleOnRoleCreatedEventWhenEventIsFromLocalSystem() {
    log.entry();
    
    // given
    RoleCreatedEvent event = mock(RoleCreatedEvent.class);
    when(event.getSystem()).thenReturn(LOCAL_SYSTEM);
    
    // when
    sut.event(event);
    
    // then
    verify(writeService, never()).create(role);
  }
  
  
  @Test
  void shouldChangeNameSpaceOnRoleUpdateNameSpaceEventWhenEventIsFromExternalSystem() {
    log.entry();
    
    // given
    RoleUpdateNameSpaceEvent event = mock(RoleUpdateNameSpaceEvent.class);
    when(event.getSystem()).thenReturn(EXTERNAL_SYSTEM);
    when(event.getRole()).thenReturn(role);
    when(writeService.updateNameSpace(any(UUID.class), any(String.class))).thenReturn(Mono.just(role));
    
    // when
    sut.event(event);
    
    // then
    log.exit();
  }
  
  @Test
  void shouldHandleExceptionOnRoleUpdateNameSpaceEventWhenEventIsFromExternalSystem() {
    log.entry();
    
    // given
    RoleUpdateNameSpaceEvent event = mock(RoleUpdateNameSpaceEvent.class);
    when(event.getSystem()).thenReturn(EXTERNAL_SYSTEM);
    when(event.getRole()).thenReturn(role);
    when(writeService.updateNameSpace(any(UUID.class), any(String.class))).thenReturn(Mono.error(new RoleCantBeCreatedException(role, new IllegalStateException())));
    
    // when
    try {
      sut.event(event);
      fail("Expected exception to be thrown, but it was not.");
    } catch (Exception e) {
      log.debug("Caught exception. type={}, message={}", e.getCause().getClass().getSimpleName(), e.getCause().getMessage());
      
      assertInstanceOf(RoleCantBeCreatedException.class, e.getCause());
    }
    
    log.exit();
  }
  
  @Test
  void shouldIgnoreNameSpaceOnRoleUpdateNameSpaceEventWhenEventIsFromLocalSystem() {
    log.entry();
    
    // given
    RoleUpdateNameSpaceEvent event = mock(RoleUpdateNameSpaceEvent.class);
    when(event.getSystem()).thenReturn(LOCAL_SYSTEM);
    
    // when
    sut.event(event);
    
    // then
    verify(writeService, never()).updateNameSpace(role.getId(), role.getNameSpace());
    
    log.exit();
  }
  
  
  @Test
  void shouldChangeNameOnRoleUpdateNameEventWhenEventIsFromExternalSystem() {
    log.entry();
    
    // given
    RoleUpdateNameEvent event = mock(RoleUpdateNameEvent.class);
    when(event.getSystem()).thenReturn(EXTERNAL_SYSTEM);
    when(event.getRole()).thenReturn(role);
    when(writeService.updateName(any(UUID.class), any(String.class))).thenReturn(Mono.just(role));
    
    // when
    sut.event(event);
    
    
    // then
    log.exit();
  }
  
  @Test
  void shouldHandleExceptionOnRoleUpdateNameEventWhenEventIsFromExternalSystem() {
    log.entry();
    
    // given
    RoleUpdateNameEvent event = mock(RoleUpdateNameEvent.class);
    when(event.getSystem()).thenReturn(EXTERNAL_SYSTEM);
    when(event.getRole()).thenReturn(role);
    when(writeService.updateName(any(UUID.class), any(String.class))).thenReturn(Mono.error(new RoleCantBeCreatedException(role, new IllegalStateException())));
    
    // when
    try {
      sut.event(event);
      fail("Expected exception to be thrown, but it was not.");
    } catch (Exception e) {
      log.debug("Caught exception. type={}, message={}", e.getCause().getClass().getSimpleName(), e.getCause().getMessage());
      
      assertInstanceOf(RoleCantBeCreatedException.class, e.getCause());
    }
    
    log.exit();
  }
  
  @Test
  void shouldIgnoreNameOnRoleUpdateNameSpaceEventWhenEventIsFromLocalSystem() {
    log.entry();
    
    // given
    RoleUpdateNameEvent event = mock(RoleUpdateNameEvent.class);
    when(event.getSystem()).thenReturn(LOCAL_SYSTEM);
    
    // when
    sut.event(event);
    
    // then
    verify(writeService, never()).updateName(role.getId(), role.getName());
    
    log.exit();
  }
  
  
  @Test
  void shouldRemoveRoleOnRemoveEventWhenEventIsFromExternalSystem() {
    log.entry();
    
    // given
    RoleRemovedEvent event = mock(RoleRemovedEvent.class);
    when(event.getSystem()).thenReturn(EXTERNAL_SYSTEM);
    when(event.getRole()).thenReturn(role);
    when(userRoleManagement.revokeRoleFromAllUsers(any(KpRole.class))).thenReturn(Mono.just(3L));
    when(writeService.remove(any(UUID.class))).thenReturn(Mono.empty());
    
    // when
    sut.event(event);
    
    // then
    
    log.exit();
  }
  
  @Test
  void shouldIgnoreRoleOnRemoveEventWhenEventIsFromLocalSystem() {
    log.entry();
    
    // given
    RoleRemovedEvent event = mock(RoleRemovedEvent.class);
    when(event.getSystem()).thenReturn(LOCAL_SYSTEM);
    
    // when
    sut.event(event);
    
    // then
    verify(userRoleManagement, never()).revokeRoleFromAllUsers(role);
    verify(writeService, never()).remove(TEST_ROLE_ID);
    
    log.exit();
  }
}
