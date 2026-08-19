package one.digitalinnovation.gof.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import one.digitalinnovation.gof.controller.dto.CreateClienteRequest;
import one.digitalinnovation.gof.controller.dto.CreateEnderecoRequest;
import one.digitalinnovation.gof.exception.EnderecoManualIncompletoException;
import one.digitalinnovation.gof.model.Cliente;
import one.digitalinnovation.gof.model.ClienteRepository;
import one.digitalinnovation.gof.model.Endereco;
import one.digitalinnovation.gof.model.EnderecoRepository;
import one.digitalinnovation.gof.service.ViaCepService;

@ExtendWith(MockitoExtension.class)
class ClienteServiceImplTest {

	@Mock
	private ClienteRepository clienteRepository;
	@Mock
	private EnderecoRepository enderecoRepository;
	@Mock
	private ViaCepService viaCepService;

	private ClienteServiceImpl clienteService;

	@BeforeEach
	void setUp() {
		clienteService = new ClienteServiceImpl(clienteRepository, enderecoRepository, viaCepService);
	}

	@Test
	void deveCadastrarClienteComEnderecoEncontradoNoViaCep() {
		CreateClienteRequest request = criarRequestComCep("01001-000");
		Endereco enderecoViaCep = new Endereco();
		enderecoViaCep.setCep("01001-000");
		enderecoViaCep.setLogradouro("Praça da Sé");

		when(enderecoRepository.findById("01001-000")).thenReturn(Optional.empty());
		when(viaCepService.consultarCep("01001-000")).thenReturn(enderecoViaCep);
		when(enderecoRepository.save(enderecoViaCep)).thenReturn(enderecoViaCep);
		when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Cliente cliente = clienteService.inserir(request);

		assertEquals("Miguel", cliente.getNome());
		assertEquals("Praça da Sé", cliente.getEndereco().getLogradouro());
	}

	@Test
	void deveCadastrarEnderecoManualQuandoViaCepNaoRetornarEndereco() {
		CreateClienteRequest request = criarRequestComCep("00000-000");
		request.getEndereco().setLogradouro("Rua Manual");
		request.getEndereco().setBairro("Centro");
		request.getEndereco().setLocalidade("Recife");
		request.getEndereco().setUf("PE");

		when(enderecoRepository.findById("00000-000")).thenReturn(Optional.empty());
		when(viaCepService.consultarCep("00000-000")).thenReturn(null);
		when(enderecoRepository.save(any(Endereco.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Cliente cliente = clienteService.inserir(request);

		assertEquals("Rua Manual", cliente.getEndereco().getLogradouro());
		assertEquals("Recife", cliente.getEndereco().getLocalidade());
	}

	@Test
	void deveImpedirCadastroSemEnderecoManualQuandoCepNaoForEncontrado() {
		CreateClienteRequest request = criarRequestComCep("00000-000");
		Endereco enderecoInvalido = new Endereco();
		enderecoInvalido.setErro(true);

		when(enderecoRepository.findById("00000-000")).thenReturn(Optional.empty());
		when(viaCepService.consultarCep("00000-000")).thenReturn(enderecoInvalido);

		assertThrows(EnderecoManualIncompletoException.class, () -> clienteService.inserir(request));
	}

	private CreateClienteRequest criarRequestComCep(String cep) {
		CreateEnderecoRequest endereco = new CreateEnderecoRequest();
		endereco.setCep(cep);

		CreateClienteRequest request = new CreateClienteRequest();
		request.setNome("Miguel");
		request.setEndereco(endereco);
		return request;
	}
}
