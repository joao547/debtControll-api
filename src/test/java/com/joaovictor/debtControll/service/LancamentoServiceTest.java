package com.joaovictor.debtControll.service;

import com.joaovictor.debtControll.exceptions.RegraNegocioException;
import com.joaovictor.debtControll.model.entity.Lancamento;
import com.joaovictor.debtControll.model.entity.Usuario;
import com.joaovictor.debtControll.model.enums.StatusLancamento;
import com.joaovictor.debtControll.model.enums.TipoLancamento;
import com.joaovictor.debtControll.model.repository.LancamentoRepository;
import com.joaovictor.debtControll.model.repository.LancamentoRepositoryTest;
import com.joaovictor.debtControll.service.impl.LancamentoServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Example;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class LancamentoServiceTest {

    @SpyBean
    LancamentoServiceImpl service;

    @MockBean
    LancamentoRepository repository;

    @Test
    public void deveSalvarUmLancamento(){
        //cenário
        Lancamento lancamentoASalvar = LancamentoRepositoryTest.criarLancamento();
        Mockito.doNothing().when(service).validar(lancamentoASalvar);

        Lancamento lancamentoSalvo = LancamentoRepositoryTest.criarLancamento();
        lancamentoSalvo.setId(1l);
        Mockito.when(repository.save(lancamentoASalvar)).thenReturn(lancamentoSalvo);

        //ação
        Lancamento lancamento = service.salvar(lancamentoASalvar);

        //verificação
        assertEquals(lancamentoSalvo.getId(), lancamento.getId());
        assertEquals(StatusLancamento.PENDENTE, lancamento.getStatus());
    }

    @Test
    public void naoDeveSalvarUmLancamentoQuandoHouverErroDeValidacao(){
        //cenário
        Lancamento lancamentoASalvar = LancamentoRepositoryTest.criarLancamento();
        Mockito.doThrow(RegraNegocioException.class).when(service).validar(lancamentoASalvar);

        //ação e verificação
        assertThrows(RegraNegocioException.class,() -> service.salvar(lancamentoASalvar));
        Mockito.verify(repository, Mockito.never()).save(lancamentoASalvar);
    }

    @Test
    public void deveAtualizarUmLancamento(){
        //cenário
        Lancamento lancamentoSalvo = LancamentoRepositoryTest.criarLancamento();
        lancamentoSalvo.setId(1l);
        Mockito.doNothing().when(service).validar(lancamentoSalvo);

        Mockito.when(repository.save(lancamentoSalvo)).thenReturn(lancamentoSalvo);

        //ação
        lancamentoSalvo.setStatus(StatusLancamento.EFETIVADO);
        Lancamento lancamentoAtualizado = service.atualizar(lancamentoSalvo);

        //verificação
        Mockito.verify(repository, Mockito.times(1)).save(lancamentoSalvo);
        assertEquals(StatusLancamento.EFETIVADO, lancamentoAtualizado.getStatus());
    }

    @Test
    public void deveLancarErroAoTentarAtualizarLancamentoQueAindaNaoFoiSalvo(){
        //cenário
        Lancamento lancamentoASalvar = LancamentoRepositoryTest.criarLancamento();

        //ação e verificação
        assertThrows(NullPointerException.class,() -> service.atualizar(lancamentoASalvar));
        Mockito.verify(repository, Mockito.never()).save(lancamentoASalvar);
    }

    @Test
    public void deveDeletarUmLancamento(){
        //cenário
        Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
        lancamento.setId(1l);

        //ação
        service.deletar(lancamento);

        //verificação
        Mockito.verify(repository).delete(lancamento);
    }

    @Test
    public void deveLancarErroAoTentarDeletarLancamentoQueAindaNaoFoiSalvo(){
        //cenário
        Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();

        //ação
        assertThrows(NullPointerException.class,() -> service.deletar(lancamento));

        //verificação
        Mockito.verify(repository, Mockito.never()).delete(lancamento);
    }

    @Test
    public void deveFiltrarLancamento(){
        //cenário
        Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
        lancamento.setId(1l);

        List<Lancamento> lista = Arrays.asList(lancamento);
        Mockito.when(repository.findAll(Mockito.any(Example.class))).thenReturn(lista);

        //ação
        List<Lancamento> resultado = service.buscar(lancamento);
        assertEquals(lista, resultado);
    }

    @Test
    public void deveAtualizarOStatusDeUmLancamento(){
        //cenário
        Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
        lancamento.setId(1l);

        StatusLancamento novoStatus = StatusLancamento.EFETIVADO;
        Mockito.doReturn(lancamento).when(service).atualizar(lancamento);

        //ação
        service.atulizarStatus(lancamento,novoStatus);

        //verificação
        assertEquals(novoStatus, lancamento.getStatus());
        Mockito.verify(service).atualizar(lancamento);
    }

    @Test
    public void deveObterUmLancamentoPorId(){
        //cenário
        Long id = 1l;

        Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
        lancamento.setId(id);

        Mockito.when(repository.findById(id)).thenReturn(Optional.of(lancamento));

        //ação
        Optional<Lancamento> resultado = service.obterPorId(id);

        //verificação
        assertTrue(resultado.isPresent());
    }

    @Test
    public void deveRetornarVazioQuandoOLancamentoNaoExiste(){
        //cenário
        Long id = 1l;
        Mockito.when(repository.findById(id)).thenReturn(Optional.empty());

        //ação
        Optional<Lancamento> resultado = service.obterPorId(id);

        //verificação
        assertFalse(resultado.isPresent());
    }

    @Test
    public void deveLancarErrosAoValidarUmLancamento(){
        //cenário com descrição vazia
        Lancamento lancamento = new Lancamento();
        RegraNegocioException exception = criarExcecaoNaValidacaoDeUmLancamento(lancamento);
        assertEquals(exception.getMessage(),"Informe uma Descrição válida.");

        //cenário com mes vazio
        lancamento.setDescricao("teste");
        exception = criarExcecaoNaValidacaoDeUmLancamento(lancamento);
        assertEquals(exception.getMessage(),"Informe um Mês válido.");

        //cenário com ano vazio
        lancamento.setMes(6);
        exception = criarExcecaoNaValidacaoDeUmLancamento(lancamento);
        assertEquals(exception.getMessage(),"Informe um Ano válido.");


        //cenário com usuario vazio
        lancamento.setAno(2020);
        exception = criarExcecaoNaValidacaoDeUmLancamento(lancamento);
        assertEquals(exception.getMessage(),"Informe um Usuário.");


        //cenário com valor vazio
        lancamento.setUsuario(new Usuario());
        lancamento.getUsuario().setId(1l);
        exception = criarExcecaoNaValidacaoDeUmLancamento(lancamento);
        assertEquals(exception.getMessage(),"Informe um Valor válido.");


        //cenário com tipo vazio
        lancamento.setValor(BigDecimal.valueOf(200));
        exception = criarExcecaoNaValidacaoDeUmLancamento(lancamento);
        assertEquals(exception.getMessage(),"Informe um tipo de Lançamento.");
    }

    private RegraNegocioException criarExcecaoNaValidacaoDeUmLancamento(Lancamento lancamento) {
        return assertThrows(RegraNegocioException.class, () -> {
            service.validar(lancamento);
        });
    }
}