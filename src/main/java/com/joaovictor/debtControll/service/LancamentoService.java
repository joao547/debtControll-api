package com.joaovictor.debtControll.service;

import com.joaovictor.debtControll.model.entity.Lancamento;
import com.joaovictor.debtControll.model.enums.StatusLancamento;

import java.util.List;

public interface LancamentoService {

    Lancamento salvar(Lancamento lancamento);

    Lancamento atualizar(Lancamento lancamento);

    void deletar(Lancamento lancamento);

    List<Lancamento> buscar(Lancamento lancamentoFiltro);

    void atulizarStatus(Lancamento lancamento, StatusLancamento status);

    void validar(Lancamento lancamento);
}
