package br.com.magnasistemas.construcaocivil.controller;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import com.fasterxml.jackson.databind.JsonNode;

import br.com.magnasistemas.construcaocivil.dto.construtora.DadosAtualizarConstrutora;
import br.com.magnasistemas.construcaocivil.dto.construtora.DadosConstrutora;
import br.com.magnasistemas.construcaocivil.dto.construtora.DadosDetalhamentoConstrutora;
import br.com.magnasistemas.construcaocivil.exception.tratamento.CustomExceptionHandler;
import br.com.magnasistemas.construcaocivil.repository.ConstrutoraRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ConstrutoraControllerTest {
	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private ConstrutoraRepository construtoraRepository;

	void iniciarConstrutora() {
		DadosConstrutora dadosConstrutora = new DadosConstrutora("12345678901234", "Construtora Teste", "11912345678",
				"teste@hotmail.com");
		restTemplate.postForEntity("/construtora/cadastrar", dadosConstrutora, DadosDetalhamentoConstrutora.class);
	}

	@AfterEach
	void iniciar() {
		construtoraRepository.deleteAllAndResetSequence();
	}

	@Test
	@DisplayName("Deve retornar um created quando criado com sucesso")
	void criarConstrutora() {
		DadosConstrutora dadosConstrutora = new DadosConstrutora("12345678901234", "Construtora Teste", "11912345678",
				"teste@hotmail.com");
		ResponseEntity<DadosDetalhamentoConstrutora> response = restTemplate.postForEntity("/construtora/cadastrar",
				dadosConstrutora, DadosDetalhamentoConstrutora.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
	}

	@Test
	@DisplayName("Deve retornar erro quando o cnjp já existe")
	void criarConstrutoraComCnpjRepetido() {
		iniciarConstrutora();
		DadosConstrutora dadosConstrutora = new DadosConstrutora("12345678901234", "Construtora Teste", "11912345678",
				"teste@hotmail.com");
		ResponseEntity<	CustomExceptionHandler.ErroUnicidade[]> response = restTemplate.postForEntity("/construtora/cadastrar",
				dadosConstrutora, 	CustomExceptionHandler.ErroUnicidade[].class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}

	@Test
	@DisplayName("Deve retornar erro quando o telefone já existe")
	void criarConstrutoraComTelefoneRepetido() {
		iniciarConstrutora();
		DadosConstrutora dadosConstrutora = new DadosConstrutora("12345678901230", "Construtora Teste", "11912345678",
				"teste@hotmail.com");
		ResponseEntity<JsonNode> response = restTemplate.postForEntity("/construtora/cadastrar", dadosConstrutora,
				JsonNode.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}

	@Test
	@DisplayName("Deve retornar erro quando o email já existe")
	void criarConstrutoraComEmailRepetido() {
		iniciarConstrutora();
		DadosConstrutora dadosConstrutora = new DadosConstrutora("12345678901230", "Construtora Teste", "11912345670",
				"teste@hotmail.com");
		ResponseEntity<JsonNode> response = restTemplate.postForEntity("/construtora/cadastrar", dadosConstrutora,
				JsonNode.class);

		assertTrue(response.getStatusCode().is4xxClientError());
	}

	@ParameterizedTest
	@MethodSource("provideInvalidConstrutoraData")
	@DisplayName("Deve retornar erro quando algum campo é nulo")
	void criarConstrutoraComCamposNulos(String cpf, String nome, String telefone, String email) {
		DadosConstrutora dadosConstrutora = new DadosConstrutora(cpf, nome, telefone, email);
		ResponseEntity response = restTemplate.postForEntity("/construtora/cadastrar", dadosConstrutora, List.class);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}

	private static Stream<Object[]> provideInvalidConstrutoraData() {
		return Stream.of(new Object[] { null, "Construtora", "11987654321", "teste@hotmail.com" },
				new Object[] { " ", "Construtora", "11987654321", "teste@hotmail.com" },
				new Object[] { "1234567890123", null, "11987654321", "teste@hotmail.com" },
				new Object[] { "12345678901234", null, "11987654321", "teste@hotmail.com" },
				new Object[] { "12345678901234", " ", "11987654321", "teste@hotmail.com" },
				new Object[] { "12345678901234", "Construtora", null, "teste@hotmail.com" },
				new Object[] { "12345678901234", "Construtora", " ", "teste@hotmail.com" },
				new Object[] { "12345678901234", "Construtora", "1191234567", "teste@hotmail.com" },
				new Object[] { "12345678901234", "Construtora", "11987654321", "teste" },
				new Object[] { "12345678901234", "Construtora", "11987654321", " " });
	}

	@Test
	@DisplayName("Deve retornar codigo http 200 quando buscar uma construtora por id")
	void buscarConstrutoraPorId() {
		iniciarConstrutora();

		ResponseEntity<DadosDetalhamentoConstrutora> response = restTemplate.getForEntity("/construtora/buscar/1",
				DadosDetalhamentoConstrutora.class);

		assertTrue(response.getStatusCode().is2xxSuccessful());
	}

	@Test
	@DisplayName("Deve retornar um erro quando listar uma construtora por un id inexistente")
	void listarConstrutoraPorIdInvalido() {

		ResponseEntity<JsonNode> response = restTemplate.getForEntity("/construtora/buscar/1", JsonNode.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}

	@Test
	@DisplayName("Deve retornar codigo http 200 quando listar as construtoras")
	void listarConstrutoras() {
		iniciarConstrutora();

		ResponseEntity<DadosDetalhamentoConstrutora> response = restTemplate.getForEntity("/construtora/listar/todos",
				DadosDetalhamentoConstrutora.class);

		assertTrue(response.getStatusCode().is2xxSuccessful());

	}

	@Test
	@DisplayName("Deve retornar codigo http 200 quando listar as construtoras ativas")
	void listarConstrutorasAtivas() {
		iniciarConstrutora();

		ResponseEntity<DadosDetalhamentoConstrutora> response = restTemplate.getForEntity("/construtora/listar",
				DadosDetalhamentoConstrutora.class);

		assertTrue(response.getStatusCode().is2xxSuccessful());

	}

	@Test
	@DisplayName("deve devolver codigo http 200 quando atualizar uma construtora")
	void atualizarEConstrutora() {
		iniciarConstrutora();

		DadosAtualizarConstrutora dadosAtualizarConstrutora = new DadosAtualizarConstrutora(1L, "12345678901230",
				"Construtora atualizada", "11987654321", "testaAtualiza@gmail.com");
		ResponseEntity<DadosAtualizarConstrutora> response = restTemplate.exchange("/construtora/atualizar",
				HttpMethod.PUT, new HttpEntity<>(dadosAtualizarConstrutora), DadosAtualizarConstrutora.class);

		assertTrue(response.getStatusCode().is2xxSuccessful());
		assertEquals(HttpStatus.OK, response.getStatusCode());

	}

	@Test
	@DisplayName("deve devolver um erro quando atualizar com um id inválido")
	void atualizarConstrutoraInvalido() {

		ResponseEntity<JsonNode> response = restTemplate.getForEntity("/construtora/atualizar/1", JsonNode.class);

		assertTrue(response.getStatusCode().is4xxClientError());

	}

	@Test
	@DisplayName("deve retornar um 204 quando desativar com um id válido")
	void desativarConstrutora() {
		iniciarConstrutora();

		ResponseEntity<DadosDetalhamentoConstrutora> response = restTemplate.exchange("/construtora/desativar/1",
				HttpMethod.DELETE, null, DadosDetalhamentoConstrutora.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	@DisplayName("Deve retornar codigo http 200 quando ativar uma Construtora")
	void ativarConstrutora() {
		iniciarConstrutora();

		restTemplate.exchange("/construtora/desativar/1", HttpMethod.DELETE, null, DadosDetalhamentoConstrutora.class);

		ResponseEntity<DadosDetalhamentoConstrutora> response = restTemplate.exchange("/construtora/ativar/1",
				HttpMethod.PUT, null, DadosDetalhamentoConstrutora.class);

		assertTrue(response.getStatusCode().is2xxSuccessful());
		assertEquals(HttpStatus.OK, response.getStatusCode());

	}

	@Test
	@DisplayName("deve devolver um erro quando ativar uma construtora com um id inválido")
	void ativarConstrutoraIdInvalido() {

		ResponseEntity<JsonNode> response = restTemplate.exchange("/construtora/ativar/1", HttpMethod.PUT, null,
				JsonNode.class);

		assertTrue(response.getStatusCode().is4xxClientError());

	}

	@Test
	@DisplayName("deve retornar um 204 quando deletar com um id válido")
	void deletarConstrutora() {
		iniciarConstrutora();

		ResponseEntity<DadosDetalhamentoConstrutora> response = restTemplate.exchange("/construtora/deletar/1",
				HttpMethod.DELETE, null, DadosDetalhamentoConstrutora.class);
		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

	}

}
