package com.exemplo.crud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import java.util.*;

@SpringBootApplication
public class PessoaApplication {
    public static void main(String[] args) {
        SpringApplication.run(PessoaApplication.class, args);
    }
}

@Entity
class Pessoa {
    @Id @GeneratedValue
    private Long id;

    @NotBlank
    private String nome;

    private String dataNascimento;

    @NotBlank
    @Column(unique = true)
    private String cpf;

    @OneToMany(mappedBy = "pessoa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Endereco> enderecos = new ArrayList<>();

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(String dataNascimento) { this.dataNascimento = dataNascimento; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public List<Endereco> getEnderecos() { return enderecos; }
    public void setEnderecos(List<Endereco> enderecos) {
        this.enderecos.clear();
        if (enderecos != null) {
            enderecos.forEach(e -> e.setPessoa(this));
            this.enderecos.addAll(enderecos);
        }
    }
}

@Entity
class Endereco {
    @Id @GeneratedValue
    private Long id;

    private String rua, numero, bairro, cidade, estado, cep;
    private boolean enderecoPrincipal;

    @ManyToOne
    @JoinColumn(name = "pessoa_id")
    private Pessoa pessoa;

    public Long getId() { return id; }
    public String getRua() { return rua; }
    public void setRua(String rua) { this.rua = rua; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }
    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }
    public boolean isEnderecoPrincipal() { return enderecoPrincipal; }
    public void setEnderecoPrincipal(boolean enderecoPrincipal) { this.enderecoPrincipal = enderecoPrincipal; }
    public Pessoa getPessoa() { return pessoa; }
    public void setPessoa(Pessoa pessoa) { this.pessoa = pessoa; }
}

interface PessoaRepository extends JpaRepository<Pessoa, Long> {
    boolean existsByCpf(String cpf);
}

interface EnderecoRepository extends JpaRepository<Endereco, Long> {}

@RestController
@RequestMapping("/pessoas")
@Validated
class PessoaController {
    private final PessoaRepository pessoaRepo;

    PessoaController(PessoaRepository pessoaRepo) {
        this.pessoaRepo = pessoaRepo;
    }

    @PostMapping
    public ResponseEntity<?> criarPessoa(@Valid @RequestBody Pessoa pessoa) {
        if (pessoaRepo.existsByCpf(pessoa.getCpf())) {
            return ResponseEntity.badRequest().body("CPF já existe");
        }
        return ResponseEntity.ok(pessoaRepo.save(pessoa));
    }

    @GetMapping
    public Page<Pessoa> listarPessoas(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "5") int size) {
        return pessoaRepo.findAll(PageRequest.of(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPessoa(@PathVariable Long id) {
        return pessoaRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarPessoa(@PathVariable Long id, @Valid @RequestBody Pessoa novaPessoa) {
        return pessoaRepo.findById(id).map(pessoa -> {
            pessoa.setNome(novaPessoa.getNome());
            pessoa.setCpf(novaPessoa.getCpf());
            pessoa.setDataNascimento(novaPessoa.getDataNascimento());
            pessoa.setEnderecos(novaPessoa.getEnderecos());
            return ResponseEntity.ok(pessoaRepo.save(pessoa));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarPessoa(@PathVariable Long id) {
        if (!pessoaRepo.existsById(id)) return ResponseEntity.notFound().build();
        pessoaRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
