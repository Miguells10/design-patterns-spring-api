package one.digitalinnovation.gof.controller.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class CreateClienteRequest {
    @NotBlank
    private String nome;

    @NotNull
    @Valid
    private CreateEnderecoRequest endereco;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public CreateEnderecoRequest getEndereco() {
        return endereco;
    }

    public void setEndereco(CreateEnderecoRequest endereco) {
        this.endereco = endereco;
    }
}
