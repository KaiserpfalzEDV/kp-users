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

import de.kaiserpfalzedv.commons.users.domain.model.role.KpRole;
import de.kaiserpfalzedv.commons.users.domain.model.role.Role;
import de.kaiserpfalzedv.commons.users.domain.model.role.RoleNotFoundException;
import de.kaiserpfalzedv.commons.users.domain.model.role.RoleToImpl;
import de.kaiserpfalzedv.commons.users.domain.model.user.KpUserDetails;
import de.kaiserpfalzedv.commons.users.domain.model.user.UserNotFoundException;
import de.kaiserpfalzedv.commons.users.domain.model.user.events.modification.RoleAddedToUserEvent;
import de.kaiserpfalzedv.commons.users.domain.model.user.events.modification.RoleRemovedFromUserEvent;
import de.kaiserpfalzedv.commons.users.store.model.role.R2dbcRoleReadService;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@XSlf4j
public class R2dbcUserRoleManagementServiceTest {
  
  @InjectMocks
  private R2dbcUserRoleManagementService sut;
  
  @Mock
  private R2dbcRoleReadService jpaRoleReadService;
  
  @Mock
  private R2dbcUserRepository repository;
  
  @Mock
  private ApplicationEventPublisher bus;
  
  @Mock
  private RoleToImpl toJpa;
  
  private static final UUID DEFAULT_ID = UUID.randomUUID();
  private static final OffsetDateTime CREATED_AT = OffsetDateTime.now();
  private static final UUID DEFAULT_ROLE_ID = UUID.randomUUID();

  private KpUserDetails jpaUser;
  private Role role;
  private KpRole jpaRole;
  
  
  @BeforeEach
  public void setUp() {
    reset(bus, repository, toJpa);
    
    jpaUser = KpUserDetails.builder()
        .id(DEFAULT_ID)
        
        .nameSpace("namespace")
        .name("name")
        
        .issuer("issuer")
        .subject(DEFAULT_ID.toString())
        
        .email("email@email.email")
        
        .created(CREATED_AT)
        .modified(CREATED_AT)
        
        .build();
    
    role = KpRole.builder()
        .id(DEFAULT_ROLE_ID)
        
        .nameSpace("namespace")
        .name("role")
        
        .created(CREATED_AT)
        .modified(CREATED_AT)
        
        .build();
    
    jpaRole = KpRole.builder()
        .id(DEFAULT_ROLE_ID)
        
        .nameSpace("namespace")
        .name("role")
        
        .created(CREATED_AT)
        .modified(CREATED_AT)
        
        .build();
    
  }
  
  @AfterEach
  public void tearDown() {
    verifyNoMoreInteractions(bus, repository, toJpa);
    validateMockitoUsage();
  }
  
  
  @Test
  void shouldAddRoleToUserWhenUserExists() {
    log.entry();
    
    when(repository.findById(DEFAULT_ID)).thenReturn(Mono.just(jpaUser));
    when(jpaRoleReadService.retrieve(DEFAULT_ROLE_ID)).thenReturn(Mono.just(jpaRole));
    when(repository.save(any(KpUserDetails.class))).thenReturn(Mono.just(jpaUser));
    
    sut.addRole(DEFAULT_ID, role).block();
    
    verify(repository).save(jpaUser);
    verify(bus).publishEvent(any(RoleAddedToUserEvent.class));
    
    log.exit();
  }
  
  @Test
  void shouldDoNothingWhenUserHasRoleAlready() {
    log.entry();
    
    jpaUser.addRole(jpaRole, bus);
    reset(bus);
    
    when(repository.findById(DEFAULT_ID)).thenReturn(Mono.just(jpaUser));
    when(jpaRoleReadService.retrieve(DEFAULT_ROLE_ID)).thenReturn(Mono.just(jpaRole));
    
    sut.addRole(DEFAULT_ID, role).block();
    
    verify(repository).save(jpaUser);
    verify(bus, never()).publishEvent(any(RoleAddedToUserEvent.class));
    
    log.exit();
  }
  
  @Test
  void shouldThrowRoleNotFoundExceptionWhenRoleDoesNotExistForAddRole() {
    log.entry();
    
    when(repository.findById(DEFAULT_ID)).thenReturn(Mono.just(jpaUser));
    when(jpaRoleReadService.retrieve(DEFAULT_ROLE_ID)).thenReturn(Mono.empty());
    
    assertThrows(RoleNotFoundException.class, () -> sut.addRole(DEFAULT_ID, role).block());
    
    verify(repository).findById(DEFAULT_ID);
    verify(jpaRoleReadService).retrieve(DEFAULT_ROLE_ID);
    
    log.exit();
  }
  
  @Test
  void shouldThrowUserNotFoundExceptionWhenUserDoesNotExistForAddRole() {
    log.entry();
    
    when(repository.findById(DEFAULT_ID)).thenReturn(Mono.empty());
    
    assertThrows(UserNotFoundException.class, () -> sut.addRole(DEFAULT_ID, role).block());
    
    verify(repository).findById(DEFAULT_ID);
    
    log.exit();
  }
  
  
  @Test
  void shouldRemoveRoleFromUserWhenUserWithRoleExists() throws UserNotFoundException, RoleNotFoundException {
    log.entry();
    
    jpaUser.addRole(jpaRole, bus);
    reset(bus);
    
    when(repository.findById(DEFAULT_ID)).thenReturn(Mono.just(jpaUser));
    when(jpaRoleReadService.retrieve(DEFAULT_ROLE_ID)).thenReturn(Mono.just(jpaRole));
    when(repository.save(any(KpUserDetails.class))).thenReturn(Mono.just(jpaUser));
    
    sut.removeRole(DEFAULT_ID, role).block();
    
    verify(repository).save(jpaUser);
    verify(bus).publishEvent(any(RoleRemovedFromUserEvent.class));
    
    log.exit();
  }
  
  @Test
  void shouldDoNothingWhenUserWithoutRoleExists() throws UserNotFoundException, RoleNotFoundException {
    log.entry();
    
    when(repository.findById(DEFAULT_ID)).thenReturn(Mono.just(jpaUser));
    when(jpaRoleReadService.retrieve(DEFAULT_ROLE_ID)).thenReturn(Mono.just(jpaRole));
    
    sut.removeRole(DEFAULT_ID, role).block();
    
    verify(repository).save(jpaUser);
    verify(bus, never()).publishEvent(any(RoleRemovedFromUserEvent.class));
    
    log.exit();
  }
  
  @Test
  void shouldThrowUserNotFoundExceptionWhenUserDoesNotExistForRemoveRole() {
    log.entry();
    
    when(repository.findById(DEFAULT_ID)).thenReturn(Mono.empty());
    
    assertThrows(UserNotFoundException.class, () -> sut.removeRole(DEFAULT_ID, role).block());
    
    verify(repository).findById(DEFAULT_ID);
    
    log.exit();
  }
  
  @Test
  void shouldThrowRoleNotFoundExceptionWhenRoleDoesNotExistForRemoveRole() {
    log.entry();
    
    when(repository.findById(DEFAULT_ID)).thenReturn(Mono.just(jpaUser));
    when(jpaRoleReadService.retrieve(DEFAULT_ROLE_ID)).thenReturn(Mono.empty());
    
    assertThrows(RoleNotFoundException.class, () -> sut.removeRole(DEFAULT_ID, role).block());
    
    verify(repository).findById(DEFAULT_ID);
    verify(jpaRoleReadService).retrieve(DEFAULT_ROLE_ID);
    
    log.exit();
  }
  
  @Test
  void shouldThrowUnsupportedWhenRemovingRoleFromAllUsers() {
    log.entry();
    
    assertThrows(UnsupportedOperationException.class, () -> sut.revokeRoleFromAllUsers(role).block());
    
    log.exit();
  }
}
