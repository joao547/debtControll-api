package com.joaovictor.debtControll.service;

import com.joaovictor.debtControll.exceptions.ErroAutenticacao;
import com.joaovictor.debtControll.exceptions.RegraNegocioException;
import com.joaovictor.debtControll.model.entity.Usuario;
import com.joaovictor.debtControll.model.repository.UsuarioRepository;
import com.joaovictor.debtControll.service.impl.UsuarioServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class UsuarioServiceTest {

    @SpyBean
    UsuarioServiceImpl service;

    @MockBean
    UsuarioRepository repository;

    @Test
    public void deveSalvarUmUsuario(){
        assertDoesNotThrow(() -> {
            //cenário
            Mockito.doNothing().when(service).validarEmail(Mockito.anyString());

            Usuario usuario = Usuario
                    .builder()
                    .id(1l)
                    .nome("usuario")
                    .email("usuario@email.com")
                    .senha("senha")
                    .build();

            Mockito.when(repository.save(Mockito.any(Usuario.class))).thenReturn(usuario);

            //ação
            Usuario usuarioSalvo = service.salvarUsuario(new Usuario());

            //verificação
            assertNotNull(usuarioSalvo);
            assertEquals(usuarioSalvo.getId(),1l);
            assertEquals(usuarioSalvo.getNome(),"usuario");
            assertEquals(usuarioSalvo.getEmail(),"usuario@email.com");
            assertEquals(usuarioSalvo.getSenha(),"senha");
        });
    }

    @Test
    public void naoDeveCadastrarUsuarioComEmailJaCadastrado(){
        assertThrows(RegraNegocioException.class, () -> {
            //cenário
            String email = "email@email.com";

            Usuario usuario = Usuario.builder().email(email).build();
            Mockito.doThrow(RegraNegocioException.class).when(service).validarEmail(email);

            //ação
            service.salvarUsuario(usuario);

            //verificação
            Mockito.verify(repository, Mockito.never()).save(usuario);
        });
    }

    @Test
    public void deveAutenticarUmUsuarioComSucesso(){
        assertDoesNotThrow(() -> {
            //cenário
            String email = "email@email.com";
            String senha = "senha";

            Usuario usuario = Usuario.builder().email(email).senha(senha).id(1l).build();
            Mockito.when(repository.findByEmail(email)).thenReturn(Optional.of(usuario));

            //ação
            Usuario result = service.autenticar(email, senha);

            //verificação
            Assertions.assertNotNull(result);
        });
    }

    @Test
    public void deveLancarErroQuandoNaoEncontrarUsuarioCadastradoComEmailInformado(){
        ErroAutenticacao exception = assertThrows(ErroAutenticacao.class, () -> {
            //cenário
            Mockito.when(repository.findByEmail(Mockito.anyString())).thenReturn(Optional.empty());

            //ação
            Usuario result = service.autenticar("email@email.com", "senha");
        });

        assertEquals(exception.getMessage(),"Usuário não encontrado para o email informado");
    }

    @Test
    public void deveLancarErroQuandoSenhaNaoBater(){
        ErroAutenticacao exception = assertThrows(ErroAutenticacao.class, () -> {
            //cenário
            String senha = "senha";

            Usuario usuario = Usuario.builder().email("email@email.com").senha(senha).build();
            Mockito.when(repository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(usuario));

            //ação
            service.autenticar("email@email.com", "123");
        });

        assertEquals(exception.getMessage(),"Senha inválida");
    }

    @Test
    public void deveValidarEmail(){
        assertDoesNotThrow(() -> {
            //cenário
            Mockito.when(repository.existsByEmail(Mockito.anyString())).thenReturn(false);

            //ação
            service.validarEmail("email@email.com");
        });
    }

    @Test
    public void deveLancarErroAoValidarEmailQuandoExistirEmailCadastrado(){
        assertThrows(RegraNegocioException.class, () -> {
            //cenário
            Mockito.when(repository.existsByEmail(Mockito.anyString())).thenReturn(true);

            //ação
            service.validarEmail("usuario@email.com");
        });
    }
}