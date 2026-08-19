package one.digitalinnovation.gof.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import one.digitalinnovation.gof.exception.EnderecoManualIncompletoException;
import one.digitalinnovation.gof.model.Cliente;
import one.digitalinnovation.gof.service.ClienteService;

@WebMvcTest(ClienteRestController.class)
class ClienteRestControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ClienteService clienteService;

	@Test
	void deveCriarClienteComRequestValido() throws Exception {
		Cliente cliente = new Cliente();
		cliente.setId(1L);
		cliente.setNome("Miguel");
		when(clienteService.inserir(any())).thenReturn(cliente);

		mockMvc.perform(post("/clientes")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nome\":\"Miguel\",\"endereco\":{\"cep\":\"01001-000\"}}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.nome").value("Miguel"));
	}

	@Test
	void deveRetornarBadRequestQuandoNomeForVazio() throws Exception {
		mockMvc.perform(post("/clientes")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nome\":\"\",\"endereco\":{\"cep\":\"01001-000\"}}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros.nome").exists());
	}

	@Test
	void deveRetornarUnprocessableEntityQuandoEnderecoManualEstiverIncompleto() throws Exception {
		when(clienteService.inserir(any())).thenThrow(new EnderecoManualIncompletoException());

		mockMvc.perform(post("/clientes")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nome\":\"Miguel\",\"endereco\":{\"cep\":\"00000-000\"}}"))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.mensagem").exists());
	}
}
