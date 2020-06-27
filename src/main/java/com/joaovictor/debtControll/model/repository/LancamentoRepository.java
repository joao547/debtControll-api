package com.joaovictor.debtControll.model.repository;

import com.joaovictor.debtControll.model.entity.Lancamento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {
}
