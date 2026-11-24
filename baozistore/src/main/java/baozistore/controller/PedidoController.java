package baozistore.controller;

import baozistore.model.Cliente;
import baozistore.model.Produto;
import baozistore.model.Pedido;

import baozistore.repository.ClienteRepository;
import baozistore.repository.ProdutoRepository;
import baozistore.repository.PedidoRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;

    public PedidoController(
            PedidoRepository pedidoRepository,
            ClienteRepository clienteRepository,
            ProdutoRepository produtoRepository) {

        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.produtoRepository = produtoRepository;
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody PedidoRequest request) {

        if (request.clienteId() == null ||
            request.produtoId() == null ||
            request.quantidade() == null ||
            request.quantidade() <= 0) {

            return ResponseEntity.badRequest()
                    .body("Informe clienteId, produtoId e quantidade maior que zero.");
        }

        Cliente cliente = clienteRepository.findById(request.clienteId())
                .orElse(null);

        Produto produto = produtoRepository.findById(request.produtoId())
                .orElse(null);

        if (cliente == null || produto == null) {
            return ResponseEntity.badRequest()
                    .body("Cliente ou produto não encontrado.");
        }

        if (!Boolean.TRUE.equals(produto.getEstoque())) {
            return ResponseEntity.badRequest()
                    .body("Produto indisponível em estoque.");
        }

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setProduto(produto);
        pedido.setQuantidade(request.quantidade());

        Pedido salvo = pedidoRepository.save(pedido);

        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @GetMapping
    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pedido> buscarPorId(@PathVariable Long id) {
        return pedidoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {

        if (!pedidoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        pedidoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    public record PedidoRequest(
            Long clienteId,
            Long produtoId,
            Integer quantidade) {
    }
}