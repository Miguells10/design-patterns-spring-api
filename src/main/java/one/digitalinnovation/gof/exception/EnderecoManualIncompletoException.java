package one.digitalinnovation.gof.exception;

public class EnderecoManualIncompletoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public EnderecoManualIncompletoException() {
		super("CEP não encontrado. Informe logradouro, bairro, localidade e UF para cadastrar o endereço manualmente.");
	}
}
