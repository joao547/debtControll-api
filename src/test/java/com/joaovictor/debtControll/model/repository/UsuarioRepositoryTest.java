package com.joaovictor.debtControll.model.repository;

import com.joaovictor.debtControll.model.entity.Usuario;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UsuarioRepositoryTest {

    @Autowired
    UsuarioRepository repository;

    @Autowired
    TestEntityManager entityManager;

    @Test
    public void deveVerificarAExistenciaDeUmEmail(){
        //cenário
        Usuario usuario = criarUsuario();
        entityManager.persist(usuario);

        //ação
        boolean result = repository.existsByEmail("usuario@email.com");

        //verificação
        Assertions.assertTrue(result,"Verifica a existência do email");
    }

    @Test
    public void deveRetornarFalsoQuandoNaoHouverUsuarioCadastradoComOEmail(){
        //cenário

        //ação
        boolean result = repository.existsByEmail("usuario@email.com");

        //verificação
        Assertions.assertFalse(result,"Verifica se não existe usuario com aquele email");
    }

    @Test
    public void devePersistirUmUsuarioNaBaseDeDados(){
        //cenário
        Usuario usuario = criarUsuario();

        //ação
        Usuario usuarioSalvo = repository.save(usuario);

        //verificação
        Assertions.assertNotNull(usuarioSalvo);
    }

    @Test
    public void deveBuscarUmUsuarioPorEmail(){
        //cenário
        Usuario usuario = criarUsuario();
        entityManager.persist(usuario);

        //ação
        Optional<Usuario> result = repository.findByEmail("usuario@email.com");

        //verificação
        Assertions.assertTrue(result.isPresent());
    }

    @Test
    public void deveRetornarVazioAoBuscarUmUsuarioPorEmailQuandoNaoExisteNaBase(){
        //cenário

        //ação
        Optional<Usuario> result = repository.findByEmail("usuario@email.com");

        //verificação
        Assertions.assertFalse(result.isPresent());
    }

    public static Usuario criarUsuario(){
        return Usuario
                .builder()
                .nome("usuario")
                .email("usuario@email.com")
                .senha("senha")
                .build();
    }
}