package com.joaovictor.debtControll.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AutenticacaoDTO {
    private String email;
    private String senha;
}
