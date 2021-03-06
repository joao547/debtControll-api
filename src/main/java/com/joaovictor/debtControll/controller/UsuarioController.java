package com.joaovictor.debtControll.controller;

import com.joaovictor.debtControll.dto.AutenticacaoDTO;
import com.joaovictor.debtControll.dto.UsuarioDTO;
import com.joaovictor.debtControll.exceptions.ErroAutenticacao;
import com.joaovictor.debtControll.exceptions.RegraNegocioException;
import com.joaovictor.debtControll.model.entity.Usuario;
import com.joaovictor.debtControll.service.LancamentoService;
import com.joaovictor.debtControll.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService service;
    private final LancamentoService lancamentoService;

    @PostMapping
    public ResponseEntity salvar(@RequestBody UsuarioDTO dto){
        Usuario usuario = Usuario.builder()
                .nome(dto.getNome())
                .email(dto.getEmail())
                .senha(dto.getSenha())
                .build();

        try {
            Usuario salvarUsuario = service.salvarUsuario(usuario);
            return new ResponseEntity(salvarUsuario, HttpStatus.CREATED);
        }catch (RegraNegocioException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/autenticar")
    public ResponseEntity autenticar(@RequestBody AutenticacaoDTO dto){
        try {
            Usuario usuario = service.autenticar(dto.getEmail(), dto.getSenha());
            return ResponseEntity.ok(usuario);
        }catch (ErroAutenticacao e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("{id}/saldo")
    public ResponseEntity obterSaldo(@PathVariable("id") Long id){
        Optional<Usuario> usuario = service.obterPorId(id);

        if (!usuario.isPresent())
            return new ResponseEntity(HttpStatus.NOT_FOUND);

        BigDecimal saldo = lancamentoService.obterSaldoPorUsuario(id);
        return ResponseEntity.ok(saldo);
    }
}
