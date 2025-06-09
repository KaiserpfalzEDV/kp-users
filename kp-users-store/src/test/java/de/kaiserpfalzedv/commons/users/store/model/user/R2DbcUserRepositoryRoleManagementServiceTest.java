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
import de.kaiserpfalzedv.commons.users.domain.model.role.RoleNotFoundException;
import de.kaiserpfalzedv.commons.users.domain.model.role.RoleToImpl;
import de.kaiserpfalzedv.commons.users.domain.model.user.KpUserDetails;
import de.kaiserpfalzedv.commons.users.domain.model.user.UserNotFoundException;
import de.kaiserpfalzedv.commons.users.domain.model.user.events.modification.RoleAddedToUserEvent;
import de.kaiserpfalzedv.commons.users.domain.model.user.events.modification.RoleRemovedFromUserEvent;
import de.kaiserpfalzedv.commons.users.store.model.role.R2dbcRoleRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@XSlf4j
public class R2DbcUserRepositoryRoleManagementServiceTest {
  
  @InjectMocks private R2dbcUserRoleManagementService sut;
  @Mock private R2dbcUserRepository userRepository;
  @Mock private R2dbcRoleRepository roleRepository;
  @Mock private ApplicationEventPublisher bus;
  @Mock private RoleToImpl toRole;
  
  private static final UUID DEFAULT_ID = UUID.randomUUID();
  private static final OffsetDateTime CREATED_AT = OffsetDateTime.now();
  private static final UUID DEFAULT_ROLE_ID = UUID.randomUUID();

  private KpUserDetails user;
  private KpRole role;
  
  
  @BeforeEach
  public void setUp() {
    reset(bus, userRepository, roleRepository, toRole);
    
    user = KpUserDetails.builder()
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
  }
  
  @AfterEach
  public void tearDown() {
    validateMockitoUsage();
    verifyNoMoreInteractions(bus, userRepository, roleRepository, toRole);
  }
  
  
  @Test
  void shouldAddRoleToUserWhenUserExists() {
    log.entry();
    
    when(roleRepository.findById(DEFAULT_ROLE_ID)).thenReturn(Mono.just(role));
    when(userRepository.findById(DEFAULT_ID)).thenReturn(Mono.just(user));
    when(userRepository.save(any(KpUserDetails.class))).thenReturn(Mono.just(user));
    
    sut.addRole(DEFAULT_ID, role).block();
    
    verify(userRepository).save(user);
    verify(bus).publishEvent(any(RoleAddedToUserEvent.class));
    
    log.exit();
  }
  
  @Test
  void shouldDoNothingWhenUserHasRoleAlready() {
    log.entry();
    
    user.addRole(role, null);
    
    when(userRepository.findById(DEFAULT_ID)).thenReturn(Mono.just(user));
    when(roleRepository.findById(DEFAULT_ROLE_ID)).thenReturn(Mono.just(role));
    when(userRepository.save(any(KpUserDetails.class))).thenReturn(Mono.just(user));
    
    sut.addRole(DEFAULT_ID, role).block();
    
    verify(userRepository).save(user);
    verify(bus, never()).publishEvent(any(RoleAddedToUserEvent.class));
    
    log.exit();
  }
  
  @Test
  void shouldThrowRoleNotFoundExceptionWhenRoleDoesNotExistForAddRole() {
    log.entry();
    
    when(roleRepository.findById(DEFAULT_ROLE_ID)).thenReturn(Mono.error(new RoleNotFoundException(DEFAULT_ROLE_ID)));
    Exception expected = new RoleNotFoundException(DEFAULT_ROLE_ID);
    
    Mono<KpUserDetails> result = sut.addRole(DEFAULT_ID, role);

    checkException(result, expected);
    
    log.exit();
  }
  
  @Test
  void shouldThrowUserNotFoundExceptionWhenUserDoesNotExistForAddRole() {
    log.entry();
    
    when(userRepository.findById(DEFAULT_ID)).thenReturn(Mono.empty());
    when(roleRepository.findById(DEFAULT_ROLE_ID)).thenReturn(Mono.just(role));
    
    Exception expected = new UserNotFoundException(DEFAULT_ID);
    
    Mono<KpUserDetails> result = sut.addRole(DEFAULT_ID, role);
    
    checkException(result, expected);
    
    log.exit();
  }
  
  @Test
  void shouldRemoveRoleFromUserWhenUserWithRoleExists() throws UserNotFoundException, RoleNotFoundException {
    log.entry();
    
    user.addRole(role, bus);
    reset(bus);
    
    when(userRepository.findById(DEFAULT_ID)).thenReturn(Mono.just(user));
    when(roleRepository.findById(DEFAULT_ROLE_ID)).thenReturn(Mono.just(role));
    when(userRepository.save(any(KpUserDetails.class))).thenReturn(Mono.just(user));
    
    sut.removeRole(DEFAULT_ID, role).block();
    
    verify(userRepository).save(user);
    verify(bus).publishEvent(any(RoleRemovedFromUserEvent.class));
    
    log.exit();
  }
  
  @Test
  void shouldDoNothingWhenUserWithoutRoleExists() throws UserNotFoundException, RoleNotFoundException {
    log.entry();
    
    when(roleRepository.findById(DEFAULT_ROLE_ID)).thenReturn(Mono.just(role));
    when(userRepository.findById(DEFAULT_ID)).thenReturn(Mono.just(user));
    when(userRepository.save(any(KpUserDetails.class))).thenReturn(Mono.just(user));
    
    sut.removeRole(DEFAULT_ID, role).block();
    
    verify(userRepository).save(user);
    verify(bus, never()).publishEvent(any(RoleRemovedFromUserEvent.class));
    
    log.exit();
  }
  
  @Test
  void shouldThrowUserNotFoundExceptionWhenUserDoesNotExistForRemoveRole() {
    log.entry();
    
    when(roleRepository.findById(DEFAULT_ROLE_ID)).thenReturn(Mono.just(role));
    when(userRepository.findById(DEFAULT_ID)).thenReturn(Mono.empty());
    UserNotFoundException expected = new UserNotFoundException(DEFAULT_ID);
    
    Mono<KpUserDetails> result = sut.removeRole(DEFAULT_ID, role);
    
    checkException(result, expected);
    
    log.exit();
  }
  
  @Test
  void shouldThrowRoleNotFoundExceptionWhenRoleDoesNotExistForRemoveRole() {
    log.entry();
    
    when(roleRepository.findById(DEFAULT_ROLE_ID)).thenReturn(Mono.empty());
    Exception expected = new RoleNotFoundException(DEFAULT_ROLE_ID);
    
    Mono<KpUserDetails> result = sut.removeRole(DEFAULT_ID, role);
    
    
    checkException(result, expected);
    
    log.exit();
  }
  
  @Test
  void shouldThrowUnsupportedWhenRemovingRoleFromAllUsers() {
    log.entry();
    
    assertThrows(UnsupportedOperationException.class, () -> sut.revokeRoleFromAllUsers(role).block());
    
    log.exit();
  }

  
  private static void checkException(final Mono<KpUserDetails> result, final Exception expected) {
    try {
      result.block();
      
      fail("Expected exception to be thrown, but it was not.");
    } catch (Exception e) {
      log.info(e.getCause().getMessage(), e.getCause().getCause());
      
      assertInstanceOf(expected.getClass(), e.getCause());
    }
  }
  
}
