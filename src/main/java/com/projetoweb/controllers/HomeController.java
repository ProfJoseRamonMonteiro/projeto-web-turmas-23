package com.projetoweb.controllers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.projetoweb.models.ItemPedidoModel;
import com.projetoweb.models.ProdutoModel;
import com.projetoweb.repositories.ProdutoRepo;
import com.projetoweb.services.CategoriaService;
import com.projetoweb.services.ProdutoService;


@Controller
public class HomeController {

	private final ProdutoService produtoService;
	private final CategoriaService categoriaService;
	private final ProdutoRepo produtoRepo;

	List<ItemPedidoModel> itensPedido = new ArrayList<ItemPedidoModel>();

	public HomeController(ProdutoService produtoService, CategoriaService categoriaService, ProdutoRepo produtoRepo) {
		this.produtoService = produtoService;
		this.categoriaService = categoriaService;
		this.produtoRepo = produtoRepo;
	}

	@GetMapping(value = {"/", "/home"})
	public String index(Model model) {
		System.out.println("PegaUser4");
		model.addAttribute("produtos", produtoService.listarTodos());
		model.addAttribute("promocoes", produtoService.listarPromocoes());
		model.addAttribute("destaques", produtoService.listarDestaques());
		model.addAttribute("categorias", categoriaService.listarTodas());
		model.addAttribute("carrinho", itensPedido);
		return "/Home/home";
	}

	@GetMapping(value = {"/produto-detalhado/{idProduto}"})
	public String produtoDetalhado(@PathVariable("idProduto") Long idProduto, Model model) {
		System.out.println(idProduto);
		model.addAttribute("produto", produtoRepo.getReferenceById(idProduto));
		model.addAttribute("categorias", categoriaService.listarTodas());
		model.addAttribute("produtos", produtoRepo.findByCategoria(produtoRepo.getReferenceById(idProduto).getCategoria()));
		return "/Product/produto-detalhado";
	}

	@GetMapping("/imagem/{idProduto}")
	@ResponseBody
	public byte[] exibirImagem(Model model, @PathVariable("idProduto") Long idProduto) {
		ProdutoModel produto = produtoRepo.getReferenceById(idProduto);
		return produto.getImagem();

	}

	@RequestMapping(value = { "/removerProduto/{id}" }, method = RequestMethod.GET)
	public ModelAndView removerProduto(@PathVariable Integer id) {

		for (ItemPedidoModel ip : this.itensPedido) {
			if (ip.getProduto().getIdProduto().equals(id)) {

				this.itensPedido.remove(ip);

				break;
			}
		}

		ModelAndView modelAndView = new ModelAndView("redirect:/");

		return modelAndView;
	}

	@RequestMapping(value = { "/adicionarProdutoCarrinho" }, method = RequestMethod.POST)
	public ModelAndView adicionar(int quantidade, Long idProduto, Model model) {
		ItemPedidoModel itemPedido = new ItemPedidoModel();
		ProdutoModel produto = produtoRepo.getReferenceById(idProduto);
		Integer controle = 0;
		BigDecimal multDecimal;

		for (ItemPedidoModel ip : this.itensPedido) {
			if (ip.getProduto().getIdProduto().equals(produto.getIdProduto())) {
				ip.setQuantidade(ip.getQuantidade() + quantidade);
				multDecimal = ip.getProduto().getPreco().multiply(BigDecimal.valueOf(quantidade));
				ip.setPrecoUnitario(ip.getPrecoUnitario().add(multDecimal));
				controle = 1;
				break;
			}
		}

		if (controle == 0) {
			itemPedido.setProduto(produto);
			itemPedido.setQuantidade(quantidade);
			itemPedido.setPrecoUnitario(produto.getPreco().multiply(BigDecimal.valueOf(quantidade)));

			this.itensPedido.add(itemPedido);
		}

		ModelAndView modelAndView = new ModelAndView("redirect:/home");

		return modelAndView;
	}

}
