package homepage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import base.BaseTests;
import pages.CarrinhoPage;
import pages.CheckoutPage;
import pages.LoginPage;
import pages.ModalProdutoPage;
import pages.PedidoPage;
import pages.ProdutoPage;
import util.Funcoes;

public class HomePageTests extends BaseTests {

	// TESTAR PRDUTOS PÁGINA INICIAL (CONTAGEM E UI)
	@Test
	public void testContarProdutos_oitoProdutosDiferentes() {
		carregarPaginainicial();
		assertThat(homePage.contarProdutos(), is(8));

	}

	@Test
	public void testValidarCarrinhoZerado_ZeroItensNoCarrinho() {

		int produtosNoCarrinho = homePage.obterQuantidadeProdutosNoCarrinho();
		System.out.println(produtosNoCarrinho);
		assertThat(produtosNoCarrinho, is(0));
	}

	ProdutoPage produtoPage;
	String nomeProduto_ProdutoPage;

	@Test
	public void testValidarDetalheDoProduto_DescricaoEValoresIguais() {
		int indice = 0;
		String nomeProduto_HomePage = homePage.obterNomeProduto(indice);
		String precoProduto_HomePage = homePage.obterPrecoProduto(indice);

		System.out.println(nomeProduto_HomePage);
		System.out.println(precoProduto_HomePage);

		produtoPage = homePage.clicarProduto(indice);

		nomeProduto_ProdutoPage = produtoPage.obterNomeProduto();
		String precoProduto_ProdutoPage = produtoPage.obterPrecoProduto();

		System.out.println(nomeProduto_ProdutoPage);
		System.out.println(precoProduto_ProdutoPage);

		assertThat(nomeProduto_HomePage.toUpperCase(), is(nomeProduto_ProdutoPage.toUpperCase()));
		assertThat(precoProduto_HomePage, is(precoProduto_ProdutoPage));
	}

	// TESTAR LOGIN
	LoginPage loginPage;

	@Test
	public void testLoginComSucesso_UsuarioLogado() {
		// Clicar no botão Sign In na Home page
		loginPage = homePage.clicarBotaoSignIn();

		// Preencher usuário e senha
		loginPage.preencherEmail("maurilio.cardoso@teste.com");
		loginPage.preencherPassword("Pre#2021");

		// Clicar no botão Sign In para logar
		loginPage.clicarBotaoSignIn();

		// Validar se o usuário está logado de fato
		assertThat(homePage.estaLogado("Maurilio Cardoso"), is(true));

		carregarPaginainicial();
	}

	@ParameterizedTest
	@CsvFileSource(resources = "/massaTeste_Login.csv", numLinesToSkip = 1, delimiter = ';')
	public void testLogin_UsuarioLogadoComDadosValidos(String nomeTeste, String email, String password,
			String nomeUsuario, String resultado) {
		// Clicar no botão Sign In na Home page
		loginPage = homePage.clicarBotaoSignIn();

		// Preencher usuário e senha
		loginPage.preencherEmail(email);
		loginPage.preencherPassword(password);

		// Clicar no botão Sign In para logar
		loginPage.clicarBotaoSignIn();

		boolean esperado_loginOk;
		if (resultado.equals("positivo"))
			esperado_loginOk = true;
		else
			esperado_loginOk = false;

		// Validar se o usuário está logado de fato
		assertThat(homePage.estaLogado(nomeUsuario), is(esperado_loginOk));

		capturarTela(nomeTeste, resultado);

		if (esperado_loginOk)
			homePage.clicarBotaoSignOut();

		carregarPaginainicial();
	}

	// TESTAR INCLUIR PRODUTOS NO CARRINHO COM USUARIO LOGADO
	ModalProdutoPage modalProdutoPage;

	@Test
	public void testIncluirProdutoNoCarrinho_ProdutoIncluidoComSucesso() {

		String tamanhoProduto = "M";
		String corProduto = "Black";
		int quantidadeProduto = 2;

		// --Pré-condição
		// usuário logado
		if (!homePage.estaLogado("Maurilio Cardoso")) {
			testLoginComSucesso_UsuarioLogado();
		}

		// --Teste
		// Selecionar produto
		testValidarDetalheDoProduto_DescricaoEValoresIguais();

		// Selecionar tamanho
		List<String> listaOpcoes = produtoPage.obterOpcoesSelecionadas();
		System.out.println(listaOpcoes.get(0));
		System.out.println("Tamanho da Lista: " + listaOpcoes.size());

		produtoPage.selecionarOpcaoDropDown(tamanhoProduto);

		listaOpcoes = produtoPage.obterOpcoesSelecionadas();
		System.out.println(listaOpcoes.get(0));
		System.out.println("Tamanho da Lista: " + listaOpcoes.size());

		// Selecionar cor
		produtoPage.selecionarCorPreta();

		// Selecionar quantidade
		produtoPage.alterarQuantidade(quantidadeProduto);

		// Adicionar no carrinho
		modalProdutoPage = produtoPage.clicarBotaoAddToCart();

		// Validações
		assertTrue(modalProdutoPage.obterMensagemProdutoAdicionado()
				.endsWith("Product successfully added to your shopping cart"));

		System.out.println();

		assertThat(modalProdutoPage.obterDescricaoProduto().toUpperCase(), is(nomeProduto_ProdutoPage.toUpperCase()));

		// String precoProdutoString = (modalProdutoPage.obterPrecoProduto());
		// precoProdutoString = precoProdutoString.replace("$", "");
		// Double precoProduto = Double.parseDouble(precoProdutoString);
		Double precoProduto = Double.parseDouble(modalProdutoPage.obterPrecoProduto().replace("$", ""));

		assertThat(modalProdutoPage.obterTamanhoProduto(), is(tamanhoProduto));
		assertThat(modalProdutoPage.obterCorProduto(), is(corProduto));
		assertThat(modalProdutoPage.obterQuantidadeProduto(), is(Integer.toString(quantidadeProduto)));

		String subtotalString = (modalProdutoPage.obterSubtotal());
		subtotalString = subtotalString.replace("$", "");
		Double subtotal = Double.parseDouble(subtotalString);

		Double subtotalCalculado = quantidadeProduto * precoProduto;

		assertThat(subtotal, is(subtotalCalculado));

	}

	// Valores esperados
	String esperado_nomeProduto = "Hummingbird printed t-shirt";
	Double esperado_precoProduto = 19.12;
	String esperado_tamanhoProduto = "M";
	String esperado_corProduto = "Black";
	int esperado_input_quantidadeProduto = 2;
	Double esperado_subtotalProduto = esperado_precoProduto * esperado_input_quantidadeProduto;

	int esperado_numeroItensTotal = esperado_input_quantidadeProduto;
	Double esperado_subtotalTotal = esperado_subtotalProduto;
	Double esperado_shippingTotal = 7.00;
	Double esperado_totalTaxExclTotal = esperado_subtotalTotal + esperado_shippingTotal;
	Double esperado_totalTaxIncTotal = esperado_totalTaxExclTotal;
	Double esperado_taxesTotal = 0.00;

	String esperado_nomeCliente = "Maurilio Cardoso";

	// VALIDAR PAGINAS DE PRODUTOS NO CARRINHO E CHECKTOUT
	CarrinhoPage carrinhoPage;

	@Test
	public void testIrParaCarrinho_InformacoesPersistidas() {
		// --Pré-condições
		// Produto incluído na tela ModalProdutoPage
		testIncluirProdutoNoCarrinho_ProdutoIncluidoComSucesso();

		carrinhoPage = modalProdutoPage.clicarBotaoProceedToCheckout();

		// Teste

		// Validar todos elementos da tela
		System.out.println("*** TELA DO CARRINHO ***");

		System.out.println(carrinhoPage.obter_nomeProduto());
		System.out.println(Funcoes.removeCifraoDevolveDouble(carrinhoPage.obter_precoProduto()));
		System.out.println(carrinhoPage.obter_tamanhoProduto());
		System.out.println(carrinhoPage.obter_corProduto());
		System.out.println(carrinhoPage.obter_input_quantidadeProduto());
		System.out.println(Funcoes.removeCifraoDevolveDouble(carrinhoPage.obter_subtotalProduto()));

		System.out.println("*** ITENS DE TOTAIS ***");

		System.out.println(Funcoes.removeTextoItemsDevolveInt(carrinhoPage.obter_numeroItensTotal()));
		System.out.println(Funcoes.removeCifraoDevolveDouble(carrinhoPage.obter_subtotalTotal()));
		System.out.println(Funcoes.removeCifraoDevolveDouble(carrinhoPage.obter_shippingTotal()));
		System.out.println(Funcoes.removeCifraoDevolveDouble(carrinhoPage.obter_totalTaxExclTotal()));
		System.out.println(Funcoes.removeCifraoDevolveDouble(carrinhoPage.obter_totalTaxIncTotal()));
		System.out.println(Funcoes.removeCifraoDevolveDouble(carrinhoPage.obter_taxesTotal()));

		// Asserções Hamcrest
		assertThat(carrinhoPage.obter_nomeProduto(), is(esperado_nomeProduto));
		assertThat(Funcoes.removeCifraoDevolveDouble(carrinhoPage.obter_precoProduto()), is(esperado_precoProduto));
		assertThat(carrinhoPage.obter_tamanhoProduto(), is(esperado_tamanhoProduto));
		assertThat(carrinhoPage.obter_corProduto(), is(esperado_corProduto));
		assertThat(Integer.parseInt(carrinhoPage.obter_input_quantidadeProduto()),
				is(esperado_input_quantidadeProduto));
		assertThat(Funcoes.removeCifraoDevolveDouble(carrinhoPage.obter_subtotalProduto()),
				is(esperado_subtotalProduto));

		assertThat(Funcoes.removeTextoItemsDevolveInt(carrinhoPage.obter_numeroItensTotal()),
				is(esperado_numeroItensTotal));
		assertThat(Funcoes.removeCifraoDevolveDouble(carrinhoPage.obter_subtotalTotal()), is(esperado_subtotalTotal));
		assertThat(Funcoes.removeCifraoDevolveDouble(carrinhoPage.obter_shippingTotal()), is(esperado_shippingTotal));
		assertThat(Funcoes.removeCifraoDevolveDouble(carrinhoPage.obter_totalTaxExclTotal()),
				is(esperado_totalTaxExclTotal));
		assertThat(Funcoes.removeCifraoDevolveDouble(carrinhoPage.obter_totalTaxIncTotal()),
				is(esperado_totalTaxIncTotal));
		assertThat(Funcoes.removeCifraoDevolveDouble(carrinhoPage.obter_taxesTotal()), is(esperado_taxesTotal));

		// Asserções JUnit
		/*
		 * assertEquals(esperado_nomeProduto, carrinhoPage.obter_nomeProduto());
		 * assertEquals(esperado_precoProduto,Funcoes.removeCifraoDevolveDouble(
		 * carrinhoPage.obter_precoProduto())); assertEquals(esperado_tamanhoProduto,
		 * carrinhoPage.obter_tamanhoProduto()); assertEquals(esperado_corProduto,
		 * carrinhoPage.obter_corProduto());
		 * assertEquals(esperado_input_quantidadeProduto,
		 * Integer.parseInt(carrinhoPage.obter_input_quantidadeProduto()));
		 * assertEquals(esperado_subtotalProduto,
		 * Funcoes.removeCifraoDevolveDouble(carrinhoPage.obter_subtotalProduto()));
		 * 
		 * assertEquals(esperado_numeroItensTotal,
		 * Funcoes.removeTextoItemsDevolveInt(carrinhoPage.obter_numeroItensTotal()));
		 * assertEquals(esperado_subtotalTotal,
		 * Funcoes.removeCifraoDevolveDouble(carrinhoPage.obter_subtotalTotal()));
		 * assertEquals(esperado_shippingTotal,
		 * Funcoes.removeCifraoDevolveDouble(carrinhoPage.obter_shippingTotal()));
		 * assertEquals(esperado_totalTaxExclTotal,
		 * Funcoes.removeCifraoDevolveDouble(carrinhoPage.obter_totalTaxExclTotal()));
		 * assertEquals(esperado_totalTaxIncTotal,
		 * Funcoes.removeCifraoDevolveDouble(carrinhoPage.obter_totalTaxIncTotal()));
		 * assertEquals(esperado_taxesTotal,
		 * Funcoes.removeCifraoDevolveDouble(carrinhoPage.obter_taxesTotal()));
		 */

	}

	CheckoutPage checkoutPage;

	@Test
	public void testIrParaCheckout_FreteMeioPagamentoEnderecoListadosOk() {
		// --Pré-condições
		// Produto disponível no carrinho de compras
		testIrParaCarrinho_InformacoesPersistidas();

		// Teste
		// Clicar no botão
		checkoutPage = carrinhoPage.clicarBotaoProceedToCheckOut();

		// Preencher informações

		// Validar informações na tela
		assertThat(Funcoes.removeCifraoDevolveDouble(checkoutPage.obter_totalTaxIncTotal()),
				is(esperado_totalTaxIncTotal));
		// assertThat(checkoutPage.obter_nomeCliente(), is(esperado_nomeCliente));
		assertTrue(checkoutPage.obter_nomeCliente().startsWith(esperado_nomeCliente));

		checkoutPage.clicarBotaoContinueAddress();

		String encontrado_shippingValor = checkoutPage.obter_shippingValor();
		encontrado_shippingValor = Funcoes.removeTexto(encontrado_shippingValor, " tax excl.");
		Double encontrado_shippingValor_Double = Funcoes.removeCifraoDevolveDouble(encontrado_shippingValor);

		assertThat(encontrado_shippingValor_Double, is(esperado_shippingTotal));

		checkoutPage.clicarBotaoContinueShipping();

		// Selecionar a opção "Pay by Check"
		checkoutPage.selecionarRadioPayByCheck();

		// Validar valor do cheque (amount)
		String encontrado_amountPayByCheck = checkoutPage.obter_amountPayByCheck();
		encontrado_amountPayByCheck = Funcoes.removeTexto(encontrado_amountPayByCheck, " (tax incl.)");
		Double encontrado_amountPayByCheck_Double = Funcoes.removeCifraoDevolveDouble(encontrado_amountPayByCheck);

		assertThat(encontrado_amountPayByCheck_Double, is(esperado_totalTaxIncTotal));
		// Clicar na opção "I agree"
		checkoutPage.selecionarCheckboxIAgree();

		assertTrue(checkoutPage.estaSelecionadoCheckboxIAgree());
	}

	@Test
	public void testFinalizarPedido_pedidoFinalizadoComSucesso() {
		// Pré-condições
		// Checkout completamente concluído
		testIrParaCheckout_FreteMeioPagamentoEnderecoListadosOk();

		// Teste
		// Clicar no botão para confirmar pedido
		PedidoPage pedidoPage = checkoutPage.clicarBotaoConfirmaPedido();

		// Validar valores da tela
		// assertThat(pedidoPage.obter_textoPedidoConfirmado().toUpperCase(), is("YOUR
		// ORDER IS CONFIRMED"));
		assertTrue(pedidoPage.obter_textoPedidoConfirmado().endsWith("YOUR ORDER IS CONFIRMED"));

		assertThat(pedidoPage.obter_email(), is("maurilio.cardoso@teste.com"));

		assertThat(pedidoPage.obter_totalProdutos(), is(esperado_subtotalProduto));

		assertThat(pedidoPage.obter_totalTaxIncl(), is(esperado_totalTaxIncTotal));

		assertThat(pedidoPage.obter_metodoPagamento(), is("check"));

	}

}