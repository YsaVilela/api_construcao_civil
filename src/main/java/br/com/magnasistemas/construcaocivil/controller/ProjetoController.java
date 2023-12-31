package br.com.magnasistemas.construcaocivil.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.magnasistemas.construcaocivil.dto.projeto.DadosAtualizarProjeto;
import br.com.magnasistemas.construcaocivil.dto.projeto.DadosDetalhamentoProjeto;
import br.com.magnasistemas.construcaocivil.dto.projeto.DadosProjeto;
import br.com.magnasistemas.construcaocivil.service.ProjetoService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid; 

@RestController
@RequestMapping ("projetos")
public class ProjetoController {
	
	@Autowired 
	private ProjetoService service;
	
		@PostMapping ("cadastrar")
		@Transactional
		public ResponseEntity<Optional<DadosDetalhamentoProjeto>> cadastrar (@RequestBody @Valid DadosProjeto dados) {
			return ResponseEntity.status(HttpStatus.CREATED).body(service.criarProjeto(dados));
		}
		
		@GetMapping ("/buscar/{id}")
		public ResponseEntity<Optional<DadosDetalhamentoProjeto>> buscar (@PathVariable Long id) {
			return ResponseEntity.ok(service.buscarPorId(id));
		}
		
		@GetMapping ("listar")
		public ResponseEntity<Page<DadosDetalhamentoProjeto>> listar (
				@PageableDefault(size = 10, sort = {"nome"}) Pageable paginacao) {
			return ResponseEntity.ok(service.listar(paginacao));
		}
		
		@PutMapping ("atualizar")
		@Transactional
		public ResponseEntity<DadosDetalhamentoProjeto> atualizar(@RequestBody @Valid DadosAtualizarProjeto dados){
			return ResponseEntity.ok(service.atualizar(dados)) ;
		}
		
		@DeleteMapping ("deletar/{id}")
		@Transactional
		public ResponseEntity<HttpStatus> deletar(@PathVariable Long id){
			service.deletar(id);
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		}
		

}
