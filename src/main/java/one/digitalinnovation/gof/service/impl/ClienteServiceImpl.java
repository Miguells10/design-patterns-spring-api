package one.digitalinnovation.gof.service.impl;

import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import one.digitalinnovation.gof.controller.dto.CreateClienteRequest;
import one.digitalinnovation.gof.controller.dto.CreateEnderecoRequest;
import one.digitalinnovation.gof.exception.EnderecoManualIncompletoException;
import one.digitalinnovation.gof.model.Cliente;
import one.digitalinnovation.gof.model.ClienteRepository;
import one.digitalinnovation.gof.model.Endereco;
import one.digitalinnovation.gof.model.EnderecoRepository;
import one.digitalinnovation.gof.service.ClienteService;
import one.digitalinnovation.gof.service.ViaCepService;

/**
 * Implementação da <b>Strategy</b> {@link ClienteService}, a qual pode ser
 * injetada pelo Spring (via {@link Autowired}). Com isso, como essa classe é um
 * {@link Service}, ela será tratada como um <b>Singleton</b>.
 * 
 * @author falvojr
 */
@Service
public class ClienteServiceImpl implements ClienteService {

	private final ClienteRepository clienteRepository;
	private final EnderecoRepository enderecoRepository;
	private final ViaCepService viaCepService;

	public ClienteServiceImpl(ClienteRepository clienteRepository, EnderecoRepository enderecoRepository,
			ViaCepService viaCepService) {
		this.clienteRepository = clienteRepository;
		this.enderecoRepository = enderecoRepository;
		this.viaCepService = viaCepService;
	}
	
	// Strategy: Implementar os métodos definidos na interface.
	// Facade: Abstrair integrações com subsistemas, provendo uma interface simples.

	@Override
	public Iterable<Cliente> buscarTodos() {
		// Buscar todos os Clientes.
		return clienteRepository.findAll();
	}

	@Override
	public Cliente buscarPorId(Long id) {
		// Buscar Cliente por ID.
		Optional<Cliente> cliente = clienteRepository.findById(id);
		return cliente.get();
	}

	@Override
	public Cliente inserir(CreateClienteRequest request) {
		Cliente cliente = new Cliente();
		cliente.setNome(request.getNome());
		cliente.setEndereco(obterEndereco(request.getEndereco()));
		return clienteRepository.save(cliente);
	}

	@Override
	public void atualizar(Long id, Cliente cliente) {
		// Buscar Cliente por ID, caso exista:
		Optional<Cliente> clienteBd = clienteRepository.findById(id);
		if (clienteBd.isPresent()) {
			salvarClienteComCep(cliente);
		}
	}

	@Override
	public void deletar(Long id) {
		// Deletar Cliente por ID.
		clienteRepository.deleteById(id);
	}

	private void salvarClienteComCep(Cliente cliente) {
		// Verificar se o Endereco do Cliente já existe (pelo CEP).
		String cep = cliente.getEndereco().getCep();
		Endereco endereco = enderecoRepository.findById(cep).orElseGet(() -> {
			// Caso não exista, integrar com o ViaCEP e persistir o retorno.
			Endereco novoEndereco = viaCepService.consultarCep(cep);
			enderecoRepository.save(novoEndereco);
			return novoEndereco;
		});
		cliente.setEndereco(endereco);
		// Inserir Cliente, vinculando o Endereco (novo ou existente).
		clienteRepository.save(cliente);
	}

	private Endereco obterEndereco(CreateEnderecoRequest request) {
		Optional<Endereco> enderecoExistente = enderecoRepository.findById(request.getCep());
		if (enderecoExistente.isPresent()) {
			return enderecoExistente.get();
		}

		Endereco enderecoViaCep = consultarViaCep(request.getCep());
		if (enderecoViaCep != null && enderecoViaCep.getCep() != null
				&& !Boolean.TRUE.equals(enderecoViaCep.getErro())) {
			return enderecoRepository.save(enderecoViaCep);
		}

		if (enderecoManualEstaCompleto(request)) {
			return enderecoRepository.save(criarEnderecoManual(request));
		}

		throw new EnderecoManualIncompletoException();
	}

	private Endereco consultarViaCep(String cep) {
		try {
			return viaCepService.consultarCep(cep);
		} catch (RuntimeException exception) {
			return null;
		}
	}

	private boolean enderecoManualEstaCompleto(CreateEnderecoRequest request) {
		return Stream.of(request.getLogradouro(), request.getBairro(), request.getLocalidade(), request.getUf())
				.allMatch(valor -> valor != null && !valor.isBlank());
	}

	private Endereco criarEnderecoManual(CreateEnderecoRequest request) {
		Endereco endereco = new Endereco();
		endereco.setCep(request.getCep());
		endereco.setLogradouro(request.getLogradouro());
		endereco.setComplemento(request.getComplemento());
		endereco.setBairro(request.getBairro());
		endereco.setLocalidade(request.getLocalidade());
		endereco.setUf(request.getUf());
		return endereco;
	}

}
