package com.exemplo.demo;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import javax.persistence.*;
import java.util.*;


public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}

class Pessoa {
    
    public Long id;
    public String nome;
    public String dataNascimento;

    
    public String cpf;

    @OneToMany(mappedBy = "pessoa", cascade = CascadeType.ALL)
    public List<Endereco> enderecos = new ArrayList<>();
}


class Endereco {
    
    public Long id;
    public String rua, numero, bairro, cidade, estado, cep;

    public Pessoa pessoa;
}

interface PessoaRepository extends JpaRepository<Pessoa, Long> {}
interface EnderecoRepository extends JpaRepository<Endereco, Long> {}


class PessoaController {
    @Autowired PessoaRepository repo;

    
    List<Pessoa> listar() { return repo.findAll(); }

    
    Pessoa criar(@RequestBody Pessoa p) { return repo.save(p); }
}


class EnderecoController {
    @Autowired EnderecoRepository repo;
    @Autowired PessoaRepository pessoaRepo;

    
    Endereco criar(@PathVariable Long pessoaId, @RequestBody Endereco e) {
        Pessoa p = pessoaRepo.findById(pessoaId).orElseThrow();
        e.pessoa = p;
        return repo.save(e);
    }

   
    List<Endereco> listar() { return repo.findAll(); }
}
