package service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import model.Cliente;
import model.ClienteRepository;
import model.Endereco;
import model.EnderecoRepository;
import service.ClienteService;
import service.ViaCepService;

@Service
public class ClienteServiceImpl implements ClienteService {

	// Singleton: Injetar os componentes do Spring com @Autowired.
	
	@Autowired
	private ClienteRepository clienteRepository;
	@Autowired
	private EnderecoRepository enderecoRepository;
	@Autowired
	private ViaCepService viaCepService;
	
	// Strategy: Implementar os métodos definidos na interface.
	// Facade: Abstrair integrações com subsistemas, provendo uma interface simples.

	@Override
	public Iterable<Cliente> buscarTodos() {
		// Buscar todos os Clientes.
		return clienteRepository.findAll();
	}

	//@Override
	public Cliente buscarPorId(Long id) {
		// Buscar Cliente por ID.
		Optional<Cliente> cliente = clienteRepository.findById(id); // Opcional - pode ou não existir
		if(cliente.isPresent()) {
			return cliente.get();
		}else {
			throw new RuntimeException("O cliente id=" + id + "não existe");
		}
	}

	@Override
	public void inserir(Cliente cliente) {
		//verifica se o endereço do cliente já existe (pelo cep).
		String cep = cliente.getEndereco().getCep();
		Endereco endereco = enderecoRepository.findById(cep).orElseGet((()-> {
		// caso não exista, integrar com o ViaCEP e persistir o retorno.
		Endereco novoEndereco = viaCepService.consultarCep(cep);	
		enderecoRepository.save(novoEndereco);
		return novoEndereco;
		}));
		cliente.setEndereco(endereco);
		//Inserir Cliente, vinculando o Endereco (novo ou existente).
		clienteRepository.save(cliente);
	}

	@Override
	public void atualizar(Long id, Cliente cliente) {
		//Buscar Cliente por ID caso exista:
		Optional<Cliente> clienteBd = clienteRepository.findById(id); // Opcional - pode ou não existeir
		if(clienteBd.isPresent()) {
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


}