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
import de.kaiserpfalzedv.commons.users.domain.model.role.RoleNotFoundException;
import de.kaiserpfalzedv.commons.users.domain.model.user.KpUserDetails;
import de.kaiserpfalzedv.commons.users.domain.model.user.UserCantBeCreatedException;
import de.kaiserpfalzedv.commons.users.domain.model.user.UserNotFoundException;
import de.kaiserpfalzedv.commons.users.domain.model.user.events.arbitration.UserPetitionedEvent;
import de.kaiserpfalzedv.commons.users.domain.model.user.events.modification.*;
import de.kaiserpfalzedv.commons.users.domain.model.user.events.state.*;
import de.kaiserpfalzedv.commons.users.store.model.user.R2dbcUserDataManagementService;
import de.kaiserpfalzedv.commons.users.store.model.user.R2dbcUserManagementService;
import de.kaiserpfalzedv.commons.users.store.model.user.R2dbcUserRoleManagementService;
import de.kaiserpfalzedv.commons.users.store.model.user.R2dbcUserStateManagementService;
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

import static org.mockito.Mockito.*;

/**
 * @author klenkes74 {@literal <rlichti@kaiserpfalz-edv.de>}
 * @since 2025-05-17
 */
@ExtendWith(MockitoExtension.class)
@XSlf4j
public class R2DbcUserRepositoryEventsHandlerTest {
  @InjectMocks private R2dbcUserEventsHandler sut;
  @Mock private R2dbcUserManagementService userManagement;
  @Mock private R2dbcUserDataManagementService userDataManagement;
  @Mock private R2dbcUserStateManagementService userStateManagement;
  @Mock private R2dbcUserRoleManagementService userRoleManagement;
  @Mock private ApplicationEventPublisher bus;
  
  private static final String LOCAL_SYSTEM = "kp-users";
  private static final String EXTERNAL_SYSTEM = "other-application";
  
  private static final UUID USER_ID = UUID.randomUUID();
  private static final String NAMESPACE = "namespace";
  private static final String NAME  = "name";
  private static final String ISSUER = "issuer";
  private static final String SUBJECT = "subject";
  private static final String EMAIL = "email@email.email";
  private static final String DISCORD = "discord";
  private static final OffsetDateTime NOW = OffsetDateTime.now();
  
  private static final long DETAINEMENT_DAYS = 30L;
  
  private static final UUID TEST_ROLE_ID = UUID.randomUUID();
  private static final String TEST_ROLE_NAME = "Test Role";
  private static final String TEST_ROLE_NAMESPACE = "Test Namespace";
  private static final OffsetDateTime TEST_ROLE_CREATED_TIME = NOW.minusDays(100);
  
  
  private KpUserDetails user;
  private KpRole role;

  @BeforeEach
  void setUp() {
    reset(userManagement, userDataManagement, userStateManagement, userRoleManagement, bus);
    
    user = KpUserDetails.builder()
          .id(USER_ID)
          
          .issuer(ISSUER)
          .subject(SUBJECT)
          
          .name(NAME)
          .nameSpace(NAMESPACE)
          
          .email(EMAIL)
          .discord(DISCORD)
          
          .created(NOW)
          .modified(NOW)
          
          .build();
      
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
    verifyNoMoreInteractions(userManagement, userDataManagement, userStateManagement, userRoleManagement, bus);
  }
  
  
  @Test
  void shouldHandleActivationEventWhenEventIsFromExternalSystem() {
    log.entry();
    
    // given
    UserActivatedEvent event = mock(UserActivatedEvent.class);
    when(event.getApplication()).thenReturn(EXTERNAL_SYSTEM);
    when(event.getUser()).thenReturn(user);
    when(userManagement.undelete(USER_ID)).thenReturn(Mono.just(user));
    
    // when
    sut.event(event);
    
    log.exit();
  }
  
  @Test
  void shouldHandleExceptionOnActivationEventWhenEventIsFromExternalSystem() {
    log.entry();
    
    // given
    UserActivatedEvent event = mock(UserActivatedEvent.class);
    when(event.getApplication()).thenReturn(EXTERNAL_SYSTEM);
    when(event.getUser()).thenReturn(user);
    when(userManagement.undelete(USER_ID)).thenReturn(Mono.error(new UserNotFoundException(USER_ID)));
    
    // when
    sut.event(event);
    
    // then
    verify(userManagement).undelete(USER_ID);
    verifyNoInteractions(userDataManagement);
    verifyNoInteractions(userStateManagement);
    verifyNoInteractions(userRoleManagement);
    verifyNoInteractions(bus);
    
    log.exit();
  }
  
  @Test
  void shouldIgnoreActivationEventWhenEventIsFromLocalSystem() {
    log.entry();
    
    // given
    UserActivatedEvent event = mock(UserActivatedEvent.class);
    when(event.getApplication()).thenReturn(LOCAL_SYSTEM);
    
    // when
    sut.event(event);
    
    // then
    verifyNoInteractions(userManagement);
    verifyNoInteractions(userDataManagement);
    verifyNoInteractions(userStateManagement);
    verifyNoInteractions(userRoleManagement);
    verifyNoInteractions(bus);
    
    log.exit();
  }
  
  
  @Test
  void shouldCreateUserWhenEventIsFromExternalSystem() {
    log.entry();
    
    // given
    UserCreatedEvent event = mock(UserCreatedEvent.class);
    when(event.getApplication()).thenReturn(EXTERNAL_SYSTEM);
    when(event.getUser()).thenReturn(user);
    when(userManagement.create(user)).thenReturn(Mono.just(user));
    
    // when
    sut.event(event);
    
    // then
    verifyNoInteractions(userDataManagement);
    verifyNoInteractions(userStateManagement);
    verifyNoInteractions(userRoleManagement);
    verifyNoInteractions(bus);
    
    log.exit();
  }
  
  @Test
  void shouldHandleExceptionOnCreateUserWhenEventIsFromExternalSystem() {
    log.entry();
    
    // given
    UserCreatedEvent event = mock(UserCreatedEvent.class);
    when(event.getApplication()).thenReturn(EXTERNAL_SYSTEM);
    when(event.getUser()).thenReturn(user);
    when(userManagement.create(user)).thenReturn(Mono.error(new UserCantBeCreatedException(ISSUER, SUBJECT, NAME, EMAIL)));
    
    // when
    sut.event(event);
    
    // then
    verifyNoInteractions(userDataManagement);
    verifyNoInteractions(userStateManagement);
    verifyNoInteractions(userRoleManagement);
    verifyNoInteractions(bus);
    
    log.exit();
  }
  
  @Test
  void shouldIgnoreCreateUserWhenEventIsFromLocalSystem() {
    log.entry();
    
    // given
    UserCreatedEvent event = mock(UserCreatedEvent.class);
    when(event.getApplication()).thenReturn(LOCAL_SYSTEM);
    
    // when
    sut.event(event);
    
    // then
    verifyNoInteractions(userManagement);
    verifyNoInteractions(userDataManagement);
    verifyNoInteractions(userStateManagement);
    verifyNoInteractions(userRoleManagement);
    verifyNoInteractions(bus);
    
    log.exit();
  }
  
  
  @Test
  void shouldDeleteUserWhenEventIsFromExternalSystem() {
    log.entry();
    
    // given
    UserDeletedEvent event = mock(UserDeletedEvent.class);
    when(event.getApplication()).thenReturn(EXTERNAL_SYSTEM);
    when(event.getUser()).thenReturn(user);
    when(userManagement.delete(USER_ID)).thenReturn(Mono.empty());
    
    // when
    sut.event(event);
    
    // then
    verifyNoInteractions(userDataManagement);
    verifyNoInteractions(userStateManagement);
    verifyNoInteractions(userRoleManagement);
    verifyNoInteractions(bus);
    
    log.exit();
  }

  @Test
  void shouldIgnoreDeleteUserWhenEventIsFromLocalSystem() {
    log.entry();
    
    // given
    UserDeletedEvent event = mock(UserDeletedEvent.class);
    when(event.getApplication()).thenReturn(LOCAL_SYSTEM);
    
    // when
    sut.event(event);
    
    // then
    verifyNoInteractions(userManagement);
    verifyNoInteractions(userDataManagement);
    verifyNoInteractions(userStateManagement);
    verifyNoInteractions(userRoleManagement);
    verifyNoInteractions(bus);
    
    log.exit();
  }
  
  
  @Test
  void shouldRemoveUserWhenEventIsFromExternalSystem() {
    log.entry();
    
    // given
    UserRemovedEvent event = mock(UserRemovedEvent.class);
    when(event.getApplication()).thenReturn(EXTERNAL_SYSTEM);
    when(event.getUser()).thenReturn(user);
    when(userManagement.remove(USER_ID)).thenReturn(Mono.empty());
    
    // when
    sut.event(event);
    
    // then
    verifyNoInteractions(userDataManagement);
    verifyNoInteractions(userStateManagement);
    verifyNoInteractions(userRoleManagement);
    verifyNoInteractions(bus);
    
    log.exit();
  }
  
  @Test
  void shouldIgnoreRemoveUserWhenEventIsFromLocalSystem() {
    log.entry();
    
    // given
    UserRemovedEvent event = mock(UserRemovedEvent.class);
    when(event.getApplication()).thenReturn(LOCAL_SYSTEM);
    
    // when
    sut.event(event);
    
    // then
    verifyNoInteractions(userManagement);
    verifyNoInteractions(userDataManagement);
    verifyNoInteractions(userStateManagement);
    verifyNoInteractions(userRoleManagement);
    verifyNoInteractions(bus);
    
    log.exit();
  }
  
  
  @Test
  void shouldBanUserWhenEventIsFromExternalSystem() {
    log.entry();
    
    // given
    UserBannedEvent event = mock(UserBannedEvent.class);
    when(event.getApplication()).thenReturn(EXTERNAL_SYSTEM);
    when(event.getUser()).thenReturn(user);
    when(userStateManagement.ban(USER_ID)).thenReturn(Mono.just(user));
    
    // when
    sut.event(event);
    
    // then
    verifyNoInteractions(userDataManagement);
    verifyNoInteractions(userManagement);
    verifyNoInteractions(userRoleManagement);
    verifyNoInteractions(bus);
    
    log.exit();
  }
  
  @Test
  void shouldHandleExceptionOnBanUserWhenEventIsFromExternalSystem() {
    log.entry();
    
    // given
    UserBannedEvent event = mock(UserBannedEvent.class);
    when(event.getApplication()).thenReturn(EXTERNAL_SYSTEM);
    when(event.getUser()).thenReturn(user);
    when(userStateManagement.ban(USER_ID)).thenReturn(Mono.error(new UserNotFoundException(USER_ID)));
    
    // when
    sut.event(event);
    
    log.exit();
  }
  
  @Test
  void shouldIgnoreBanUserWhenEventIsFromLocalSystem() {
    log.entry();
    
    // given
    UserBannedEvent event = mock(UserBannedEvent.class);
    when(event.getApplication()).thenReturn(LOCAL_SYSTEM);
    
    // when
    sut.event(event);
    
    // then
    verifyNoInteractions(userStateManagement);
    verifyNoInteractions(userDataManagement);
    verifyNoInteractions(userManagement);
    verifyNoInteractions(userRoleManagement);
    verifyNoInteractions(bus);
    
    log.exit();
  }
  
  
  @Test
  void shouldDetainUserWhenEventIsFromExternalSystem() {
    log.entry();
    
    // given
    UserDetainedEvent event = mock(UserDetainedEvent.class);
    when(event.getApplication()).thenReturn(EXTERNAL_SYSTEM);
    when(event.getUser()).thenReturn(user);
    when(event.getDays()).thenReturn(DETAINEMENT_DAYS);
    when(userStateManagement.detain(USER_ID, DETAINEMENT_DAYS)).thenReturn(Mono.just(user));
    
    // when
    sut.event(event);
    
    log.exit();
  }
  
  @Test
  void shouldHandleExceptionDetainingUserWhenEventIsFromExternalSystem() {
    log.entry();
    
    // given
    UserDetainedEvent event = mock(UserDetainedEvent.class);
    when(event.getApplication()).thenReturn(EXTERNAL_SYSTEM);
    when(event.getUser()).thenReturn(user);
    when(event.getDays()).thenReturn(DETAINEMENT_DAYS);
    when(userStateManagement.detain(USER_ID, DETAINEMENT_DAYS)).thenReturn(Mono.error(new UserNotFoundException(USER_ID)));
    
    // when
    sut.event(event);
    
    log.exit();
  }
  
  @Test
  void shouldIgnoreDetainUserWhenEventIsFromLocalSystem() {
    log.entry();
    
    // given
    UserDetainedEvent event = mock(UserDetainedEvent.class);
    when(event.getApplication()).thenReturn(LOCAL_SYSTEM);
    
    // when
    sut.event(event);
    
    // then
    verifyNoInteractions(userStateManagement);
    verifyNoInteractions(userDataManagement);
    verifyNoInteractions(userManagement);
    verifyNoInteractions(userRoleManagement);
    verifyNoInteractions(bus);
    
    log.exit();
  }
  
  
  @Test
  void shouldPetitionUserWhenEventIsFromExternalSystem() {
    log.entry();
    
    // given
    UserPetitionedEvent event = mock(UserPetitionedEvent.class);
    when(event.getApplication()).thenReturn(EXTERNAL_SYSTEM);
    
    // when
    sut.event(event);
    
    // then
    verifyNoInteractions(userStateManagement);
    verifyNoInteractions(userDataManagement);
    verifyNoInteractions(userManagement);
    verifyNoInteractions(userRoleManagement);
    verifyNoInteractions(bus);
    
    log.exit();
  }
  
  @Test
  void shouldIgnorePetitionUserWhenEventIsFromLocalSystem() {
    log.entry();
    
    // given
    UserPetitionedEvent event = mock(UserPetitionedEvent.class);
    when(event.getApplication()).thenReturn(LOCAL_SYSTEM);
    
    // when
    sut.event(event);
    
    // then
    verifyNoInteractions(userStateManagement);
    verifyNoInteractions(userDataManagement);
    verifyNoInteractions(userManagement);
    verifyNoInteractions(userRoleManagement);
    verifyNoInteractions(bus);
    
    log.exit();
  }
  
  
  @Test
  void shouldReleaseUserWhenEventIsFromExternalSystem() {
    log.entry();
    
    // given
    UserReleasedEvent event = mock(UserReleasedEvent.class);
    when(event.getApplication()).thenReturn(EXTERNAL_SYSTEM);
    when(event.getUser()).thenReturn(user);
    when(userStateManagement.release(USER_ID)).thenReturn(Mono.just(user));
    
    // when
    sut.event(event);
    
    log.exit();
  }
  
  @Test
  void shouldHandleExceptionReleasingUserWhenEventIsFromExternalSystem() {
    log.entry();
    
    // given
    UserReleasedEvent event = mock(UserReleasedEvent.class);
    when(event.getApplication()).thenReturn(EXTERNAL_SYSTEM);
    when(event.getUser()).thenReturn(user);
    when(userStateManagement.release(USER_ID)).thenReturn(Mono.error(new UserNotFoundException(USER_ID)));
    
    // when
    sut.event(event);
    
    log.exit();
  }
  
  @Test
  void shouldIgnoreReleasingUserWhenEventIsFromLocalSystem() {
    log.entry();
    
    // given
    UserReleasedEvent event = mock(UserReleasedEvent.class);
    when(event.getApplication()).thenReturn(LOCAL_SYSTEM);
    
    // when
    sut.event(event);
    
    // then
    verifyNoInteractions(userStateManagement);
    verifyNoInteractions(userDataManagement);
    verifyNoInteractions(userManagement);
    verifyNoInteractions(userRoleManagement);
    verifyNoInteractions(bus);
    
    log.exit();
  }
  
  
  @Test
  void shouldAddRoleToUserWhenEventIsFromExternalSystem() {
    log.entry();
    
    // given
    RoleAddedToUserEvent event = mock(RoleAddedToUserEvent.class);
    when(event.getApplication()).thenReturn(EXTERNAL_SYSTEM);
    when(event.getUser()).thenReturn(user);
    when(event.getRole()).thenReturn(role);
    
    when(userRoleManagement.addRole(user.getId(), role)).thenReturn(Mono.just(user));
    
    // when
    sut.event(event);
    
    // then
    verify(userRoleManagement).addRole(USER_ID, role);
    verifyNoInteractions(userDataManagement);
    verifyNoInteractions(userManagement);
    verifyNoInteractions(userStateManagement);
    verifyNoInteractions(bus);
    
    log.exit();
  }
  
  @Test
  void shouldHandleUserNotFoundExceptionAddingRoleToUserWhenEventIsFromExternalSystem() {
    log.entry();
    
    // given
    RoleAddedToUserEvent event = mock(RoleAddedToUserEvent.class);
    when(event.getApplication()).thenReturn(EXTERNAL_SYSTEM);
    when(event.getUser()).thenReturn(user);
    when(event.getRole()).thenReturn(role);
    when(userRoleManagement.addRole(USER_ID, role)).thenReturn(Mono.error(new UserNotFoundException(USER_ID)));
    
    // when
    sut.event(event);
    
    log.exit();
  }
  
  @Test
  void shouldHandleRoleNotFoundExceptionAddingRoleToUserWhenEventIsFromExternalSystem() {
    log.entry();
    
    // given
    RoleAddedToUserEvent event = mock(RoleAddedToUserEvent.class);
    when(event.getApplication()).thenReturn(EXTERNAL_SYSTEM);
    when(event.getUser()).thenReturn(user);
    when(event.getRole()).thenReturn(role);
    when(userRoleManagement.addRole(USER_ID, role)).thenReturn(Mono.error(new RoleNotFoundException(TEST_ROLE_ID)));
    
    // when
    sut.event(event);
    
    log.exit();
  }
  
  @Test
  void shouldIgnoreAddingRoleToUserWhenEventIsFromLocalSystem() {
    log.entry();
    
    // given
    RoleAddedToUserEvent event = mock(RoleAddedToUserEvent.class);
    when(event.getApplication()).thenReturn(LOCAL_SYSTEM);
    
    // when
    sut.event(event);
    
    log.exit();
  }
  
  
  @Test
  void shouldRemoveRoleFromUserWhenEventIsFromExternalSystem() {
    log.entry();
    
    // given
    RoleRemovedFromUserEvent event = mock(RoleRemovedFromUserEvent.class);
    when(event.getApplication()).thenReturn(EXTERNAL_SYSTEM);
    when(event.getUser()).thenReturn(user);
    when(event.getRole()).thenReturn(role);
    when(userRoleManagement.removeRole(USER_ID, role)).thenReturn(Mono.just(user));
    
    // when
    sut.event(event);
    
    log.exit();
  }
  
  @Test
  void shouldHandleUserNotFoundExceptionRemovingRoleFromUserWhenEventIsFromExternalSystem() {
    log.entry();
    
    // given
    RoleRemovedFromUserEvent event = mock(RoleRemovedFromUserEvent.class);
    when(event.getApplication()).thenReturn(EXTERNAL_SYSTEM);
    when(event.getUser()).thenReturn(user);
    when(event.getRole()).thenReturn(role);
    when(userRoleManagement.removeRole(USER_ID, role)).thenReturn(Mono.error(new UserNotFoundException(USER_ID)));
    
    // when
    sut.event(event);
    
    log.exit();
  }
  
  @Test
  void shouldHandleRoleNotFoundExeptionRemovingRoleFromUserWhenEventIsFromExternalSystem() {
    log.entry();
    
    // given
    RoleRemovedFromUserEvent event = mock(RoleRemovedFromUserEvent.class);
    when(event.getApplication()).thenReturn(EXTERNAL_SYSTEM);
    when(event.getUser()).thenReturn(user);
    when(event.getRole()).thenReturn(role);
    when(userRoleManagement.removeRole(USER_ID, role)).thenReturn(Mono.error(new RoleNotFoundException(TEST_ROLE_ID)));
    
    // when
    sut.event(event);
    
    log.exit();
  }
  
  @Test
  void shouldIgnoreRemovingRoleFromUserWhenEventIsFromLocalSystem() {
    log.entry();
    
    // given
    RoleRemovedFromUserEvent event = mock(RoleRemovedFromUserEvent.class);
    when(event.getApplication()).thenReturn(LOCAL_SYSTEM);
    
    // when
    sut.event(event);
    
    // then
    verifyNoInteractions(userRoleManagement);
    verifyNoInteractions(userDataManagement);
    verifyNoInteractions(userManagement);
    verifyNoInteractions(userStateManagement);
    verifyNoInteractions(bus);
    
    log.exit();
  }
  
  
  @Test
  void shouldUpdateSubjectWhenEventIsFromExternalSystem() {
    log.entry();
    
    // given
    UserSubjectModificationEvent event = mock(UserSubjectModificationEvent.class);
    when(event.getApplication()).thenReturn(EXTERNAL_SYSTEM);
    when(event.getUser()).thenReturn(user);
    
    when(userDataManagement.updateSubject(any(UUID.class), anyString(), anyString()))
        .thenReturn(Mono.just(user));
    
    // when
    sut.event(event);
    
    // then
    verify(userDataManagement).updateSubject(USER_ID, ISSUER, SUBJECT);
    verifyNoInteractions(userManagement);
    verifyNoInteractions(userStateManagement);
    verifyNoInteractions(userRoleManagement);
    verifyNoInteractions(bus);
    
    log.exit();
  }
  
  @Test
  void shouldHandleUserNotFoundExceptionUpdatingSubjectWhenEventIsFromExternalSystem() {
    log.entry();
    
    // given
    UserSubjectModificationEvent event = mock(UserSubjectModificationEvent.class);
    when(event.getApplication()).thenReturn(EXTERNAL_SYSTEM);
    when(event.getUser()).thenReturn(user);
    when(userDataManagement.updateSubject(any(UUID.class), anyString(), anyString()))
        .thenReturn(Mono.error(new UserNotFoundException(USER_ID)));
    
    // when
    sut.event(event);
    
    log.exit();
  }
  
  @Test
  void shouldIgnoreUpdatingSubjectWhenEventIsFromLocalSystem() {
    log.entry();
    
    // given
    UserSubjectModificationEvent event = mock(UserSubjectModificationEvent.class);
    when(event.getApplication()).thenReturn(LOCAL_SYSTEM);
    
    // when
    sut.event(event);
    
    log.exit();
  }
  
  
  @Test
  void shouldChangeNamespaceAndNameWhenEventIsFromExternalSystem() {
    log.entry();
    
    // given
    UserNamespaceAndNameModificationEvent event = mock(UserNamespaceAndNameModificationEvent.class);
    when(event.getApplication()).thenReturn(EXTERNAL_SYSTEM);
    when(event.getUser()).thenReturn(user);
    when(userDataManagement.updateNamespaceAndName(USER_ID, NAMESPACE, NAME)).thenReturn(Mono.just(user));
    
    // when
    sut.event(event);
    
    log.exit();
  }
  
  @Test
  void shouldHandleUserNotFoundUpdatingNamespaceAndNameWhenEventIsFromExternalSystem() {
    log.entry();
    
    // given
    UserNamespaceAndNameModificationEvent event = mock(UserNamespaceAndNameModificationEvent.class);
    when(event.getApplication()).thenReturn(EXTERNAL_SYSTEM);
    when(event.getUser()).thenReturn(user);
    when(userDataManagement.updateNamespaceAndName(USER_ID, NAMESPACE, NAME)).thenReturn(Mono.error(new UserNotFoundException(USER_ID)));
    
    // when
    sut.event(event);
    
    log.exit();
  }
  
  @Test
  void shouldIgnoreUpdatingNamespaceAndNameWhenEventIsFromLocalSystem() {
    log.entry();
    
    // given
    UserNamespaceAndNameModificationEvent event = mock(UserNamespaceAndNameModificationEvent.class);
    when(event.getApplication()).thenReturn(LOCAL_SYSTEM);
    
    // when
    sut.event(event);
    
    log.exit();
  }
  
  
  @Test
  void shouldUpdateNamespaceWhenEventIsFromExternalSystem() {
    log.entry();
    
    // given
    UserNamespaceModificationEvent event = mock(UserNamespaceModificationEvent.class);
    when(event.getApplication()).thenReturn(EXTERNAL_SYSTEM);
    when(event.getUser()).thenReturn(user);
    when(userDataManagement.updateNamespace(USER_ID, NAMESPACE)).thenReturn(Mono.just(user));
    
    // when
    sut.event(event);
    
    log.exit();
  }
  
  @Test
  void shouldHandleUserNotFoundUpdatingNamespaceWhenEventIsFromExternalSystem() {
    log.entry();
    
    // given
    UserNamespaceModificationEvent event = mock(UserNamespaceModificationEvent.class);
    when(event.getApplication()).thenReturn(EXTERNAL_SYSTEM);
    when(event.getUser()).thenReturn(user);
    when(userDataManagement.updateNamespace(USER_ID, NAMESPACE)).thenReturn(Mono.error(new UserNotFoundException(USER_ID)));
    
    // when
    sut.event(event);
    
    log.exit();
  }
  
  @Test
  void shouldIgnoreUpdatingNamespaceWhenEventIsFromLocalSystem() {
    log.entry();
    
    // given
    UserNamespaceModificationEvent event = mock(UserNamespaceModificationEvent.class);
    when(event.getApplication()).thenReturn(LOCAL_SYSTEM);
    
    // when
    sut.event(event);
    
    log.exit();
  }
  
  
  @Test
  void shouldUpdateNameWhenEventIsFromExternalSystem() {
    log.entry();
    
    // given
    UserNameModificationEvent event = mock(UserNameModificationEvent.class);
    when(event.getApplication()).thenReturn(EXTERNAL_SYSTEM);
    when(event.getUser()).thenReturn(user);
    when(userDataManagement.updateName(USER_ID, NAME)).thenReturn(Mono.just(user));
    
    // when
    sut.event(event);
    
    log.exit();
  }
  
  @Test
  void shouldHandleUserNotFoundUpdatingNameWhenEventIsFromExternalSystem() {
    log.entry();
    
    // given
    UserNameModificationEvent event = mock(UserNameModificationEvent.class);
    when(event.getApplication()).thenReturn(EXTERNAL_SYSTEM);
    when(event.getUser()).thenReturn(user);
    when(userDataManagement.updateName(USER_ID, NAME)).thenReturn(Mono.error(new UserNotFoundException(USER_ID)));
    
    // when
    sut.event(event);
    
    log.exit();
  }
  
  @Test
  void shouldIgnoreUpdatingNameWhenEventIsFromLocalSystem() {
    log.entry();
    
    // given
    UserNameModificationEvent event = mock(UserNameModificationEvent.class);
    when(event.getApplication()).thenReturn(LOCAL_SYSTEM);
    
    // when
    sut.event(event);
    
    log.exit();
  }
  
  
  @Test
  void shouldUpdateEmailWhenEventIsFromExternalSystem() {
    log.entry();
    
    // given
    UserEmailModificationEvent event = mock(UserEmailModificationEvent.class);
    when(event.getApplication()).thenReturn(EXTERNAL_SYSTEM);
    when(event.getUser()).thenReturn(user);
    when(userDataManagement.updateEmail(USER_ID, EMAIL)).thenReturn(Mono.just(user));
    
    // when
    sut.event(event);
    
    log.exit();
  }
  
  @Test
  void shouldHandleUserNotFoundUpdatingEmailWhenEventIsFromExternalSystem() {
    log.entry();
    
    // given
    UserEmailModificationEvent event = mock(UserEmailModificationEvent.class);
    when(event.getApplication()).thenReturn(EXTERNAL_SYSTEM);
    when(event.getUser()).thenReturn(user);
    when(userDataManagement.updateEmail(USER_ID, EMAIL)).thenReturn(Mono.error(new UserNotFoundException(USER_ID)));
    
    // when
    sut.event(event);
    
    log.exit();
  }
  
  @Test
  void shouldIgnoreUpdatingEmailWhenEventIsFromLocalSystem() {
    log.entry();
    
    // given
    UserEmailModificationEvent event = mock(UserEmailModificationEvent.class);
    when(event.getApplication()).thenReturn(LOCAL_SYSTEM);
    
    // when
    sut.event(event);
    
    log.exit();
  }
  
  
  @Test
  void shouldUpdateDiscordWhenEventIsFromExternalSystem() {
    log.entry();
    
    // given
    UserDiscordModificationEvent event = mock(UserDiscordModificationEvent.class);
    when(event.getApplication()).thenReturn(EXTERNAL_SYSTEM);
    when(event.getUser()).thenReturn(user);
    when(userDataManagement.updateDiscord(USER_ID, DISCORD)).thenReturn(Mono.just(user));
    
    // when
    sut.event(event);
    
    log.exit();
  }
  
  @Test
  void shouldHandleUserNotFoundUpdatingDiscordWhenEventIsFromExternalSystem() {
    log.entry();
    
    // given
    UserDiscordModificationEvent event = mock(UserDiscordModificationEvent.class);
    when(event.getApplication()).thenReturn(EXTERNAL_SYSTEM);
    when(event.getUser()).thenReturn(user);
    when(userDataManagement.updateDiscord(USER_ID, DISCORD)).thenReturn(Mono.error(new UserNotFoundException(USER_ID)));
    
    // when
    sut.event(event);
    
    log.exit();
  }
  
  @Test
  void shouldIgnoreUpdatingDiscordWhenEventIsFromLocalSystem() {
    log.entry();
    
    // given
    UserDiscordModificationEvent event = mock(UserDiscordModificationEvent.class);
    when(event.getApplication()).thenReturn(LOCAL_SYSTEM);
    
    // when
    sut.event(event);
    
    // then
    verifyNoInteractions(userDataManagement);
    verifyNoInteractions(userManagement);
    verifyNoInteractions(userStateManagement);
    verifyNoInteractions(userRoleManagement);
    verifyNoInteractions(bus);
    
    log.exit();
  }
}
