package com.joaovictor.debtControll.controller;

import com.joaovictor.debtControll.dto.AtualizaStatusDTO;
import com.joaovictor.debtControll.dto.LancamentoDTO;
import com.joaovictor.debtControll.exceptions.RegraNegocioException;
import com.joaovictor.debtControll.model.entity.Lancamento;
import com.joaovictor.debtControll.model.entity.Usuario;
import com.joaovictor.debtControll.model.enums.StatusLancamento;
import com.joaovictor.debtControll.model.enums.TipoLancamento;
import com.joaovictor.debtControll.service.LancamentoService;
import com.joaovictor.debtControll.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/lancamentos")
@RequiredArgsConstructor
public class LancamentoController {

    private final LancamentoService service;

    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity buscar(
            @RequestParam(value = "descricao", required = false) String descricao,
            @RequestParam(value = "mes", required = false) Integer mes,
            @RequestParam(value = "ano", required = false) Integer ano,
            @RequestParam("usuario") Long idUsuario
            ){

        Lancamento lancamentoFiltro = new Lancamento();
        lancamentoFiltro.setDescricao(descricao);
        lancamentoFiltro.setMes(mes);
        lancamentoFiltro.setAno(ano);

        Optional<Usuario> usuario = usuarioService.obterPorId(idUsuario);
        if (!usuario.isPresent())
            return ResponseEntity.badRequest().body("Não foi possível realizar a consulta. Usuário não encontrado para o id informado.");
        else
            lancamentoFiltro.setUsuario(usuario.get());

        List<Lancamento> lancamentos = service.buscar(lancamentoFiltro);
        return ResponseEntity.ok(lancamentos);
    }

    @PostMapping
    public ResponseEntity salvar(@RequestBody LancamentoDTO dto){
        try {
            Lancamento entidade = converter(dto);
            entidade = service.salvar(entidade);
            return ResponseEntity.ok(entidade);
        }catch (RegraNegocioException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("{id}")
    public ResponseEntity atualizar(@PathVariable("id") Long id, @RequestBody LancamentoDTO dto){
        return service.obterPorId(id).map(entity -> {
            try {
                Lancamento lancamento = converter(dto);
                lancamento.setId(entity.getId());
                service.atualizar(lancamento);
                return ResponseEntity.ok().body(lancamento);
            }catch (RegraNegocioException e){
                return ResponseEntity.badRequest().body(e.getMessage());
            }catch (Exception e){
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
            }

        }).orElseGet(() -> new ResponseEntity("Lançamento não encontrado na base de Dados.", HttpStatus.BAD_REQUEST));
    }

    @PutMapping("{id}/atualiza-status")
    public ResponseEntity atualizarStatus(@PathVariable("id") Long id, @RequestBody AtualizaStatusDTO dto){
        return service.obterPorId(id).map(entity -> {
            try {
                StatusLancamento statusSelecionado = StatusLancamento.valueOf(dto.getStatus());
                if (statusSelecionado == null)
                    return ResponseEntity.badRequest().body("Não é possível atualizar o status do lançamento, envie um status válido");
                entity = service.atulizarStatus(entity,statusSelecionado);
                return ResponseEntity.ok(entity);
            }catch (RegraNegocioException e){
                return ResponseEntity.badRequest().body(e.getMessage());
            }catch (Exception e){
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
            }

        }).orElseGet(() -> new ResponseEntity("Lançamento não encontrado na base de Dados.", HttpStatus.BAD_REQUEST));
    }

    @DeleteMapping("{id}")
    public ResponseEntity deletar(@PathVariable("id") Long id){
        return service.obterPorId(id).map(entity -> {
            try {
                service.deletar(entity);
                return new ResponseEntity(HttpStatus.NO_CONTENT);
            }catch (RegraNegocioException e){
                return ResponseEntity.badRequest().body(e.getMessage());
            }catch (Exception e){
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
            }

        }).orElseGet(() -> new ResponseEntity("Lançamento não encontrado na base de Dados.", HttpStatus.BAD_REQUEST));
    }

    private Lancamento converter(LancamentoDTO dto){
        Lancamento lancamento = new Lancamento();
        lancamento.setId(dto.getId());
        lancamento.setDescricao(dto.getDescricao());
        lancamento.setMes(dto.getMes());
        lancamento.setAno(dto.getAno());
        lancamento.setValor(dto.getValor());

        Usuario usuario = usuarioService
                .obterPorId(dto.getUsuario())
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado para o id informado."));

        lancamento.setUsuario(usuario);
        if (dto.getTipo() != null)
            lancamento.setTipo(TipoLancamento.valueOf(dto.getTipo()));
        if (dto.getStatus() != null)
            lancamento.setStatus(StatusLancamento.valueOf(dto.getStatus()));
        return lancamento;
    }
}
