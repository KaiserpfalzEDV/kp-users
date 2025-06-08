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


import de.kaiserpfalzedv.commons.users.domain.model.role.KpRole;
import lombok.extern.slf4j.XSlf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author klenkes74 {@literal <rlichti@kaiserpfalz-edv.de>}
 * @since 2025-05-11
 */
@SuppressWarnings("LoggingSimilarMessage")
@ExtendWith(MockitoExtension.class)
@XSlf4j
public class R2dbcRoleReadServiceTest {
  @InjectMocks private R2dbcRoleReadService sut;
  
  @Mock private R2dbcRoleRepository r2dbcRoleRepository;
  
  
  @BeforeEach
  public void setUp() {
    reset(r2dbcRoleRepository);
  }
  
  @AfterEach
  public void tearDown() {
    validateMockitoUsage(); // validate if the mocks are used as expected.
    verifyNoMoreInteractions(r2dbcRoleRepository);
  }
  
  
  @Test
  void shouldFindRoleByIdWhenRoleExists() {
    log.entry();
    
    when(r2dbcRoleRepository.findById(DEFAULT_ROLE.getId())).thenReturn(Mono.just(DEFAULT_ROLE));
    
    KpRole result = sut.retrieve(DEFAULT_ROLE.getId()).block();
    log.debug("result. role={}", result);
    
    assertNotNull(result);
    
    log.exit();
  }
  
  @Test
  void shouldNotFindRoleByIdWhenRoleWithIdDoesNotExist() {
    log.entry();
    
    when(r2dbcRoleRepository.findById(DEFAULT_ROLE.getId())).thenReturn(Mono.empty());
    
    KpRole result = sut.retrieve(DEFAULT_ROLE.getId()).block();
    log.debug("result. role={}", result);
    
    assertNull(result);
    
    log.exit();
  }
  
  @Test
  void shouldReturnListOfRolesWhenRolesExist() {
    log.entry();
    
    when(r2dbcRoleRepository.findAll()).thenReturn(Flux.just(DEFAULT_ROLE, DEFAULT_ROLE));
    
    List<KpRole> result = sut.retrieveAll().collectList().block();
    log.debug("result. roles={}", result);
    
    assertNotNull(result);
    assertEquals(1, result.size());
    
    log.exit();
  }
  
  @Test
  void shouldReturnListOfRolesInNamespaceWhenRolesInNameSpaceExist() {
    log.entry();
    
    when(r2dbcRoleRepository.findByNameSpace(DEFAULT_ROLE.getNameSpace())).thenReturn(Flux.just(DEFAULT_ROLE, DEFAULT_ROLE));
    
    List<KpRole> result = sut.retrieveAllFromNamespace(DEFAULT_ROLE.getNameSpace()).collectList().block();
    log.debug("result. roles={}", result);
    
    assertNotNull(result);
    assertEquals(2, result.size());
    
    log.exit();
  }
  
  @Test
  void shouldReturnFluxOfRolesByNameWhenRolesWithNameExist() {
    log.entry();
    
    when(r2dbcRoleRepository.findByName(DEFAULT_ROLE.getName())).thenReturn(Flux.just(DEFAULT_ROLE, DEFAULT_ROLE));
    
    List<KpRole> result = sut.retrieveByName(DEFAULT_ROLE.getName()).collectList().block();
    log.debug("result. roles={}", result);
    
    assertNotNull(result);
    assertEquals(2, result.size());
    
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
}
