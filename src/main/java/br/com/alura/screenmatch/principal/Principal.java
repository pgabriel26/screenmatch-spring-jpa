package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=6585022c";
    private List<DadosSerie> dadosSeries = new ArrayList<>();
    private SerieRepository repositorio;
    private List<Serie> series = new ArrayList<>();
    private Optional<Serie> serie;

    public Principal(SerieRepository repositorio) {
        this.repositorio = repositorio;
    }

    public void exibeMenu() {
        var opcao = -1;
        while (opcao != 0) {
            var menu = """
                    \n1 - Buscar séries
                    2 - Buscar episódios
                    3 - Listar series buscadas
                    4 - Buscar Serie por Titulo
                    5 - Buscar series por ator
                    6 - top 5 series
                    7 - buscar por categoria
                    8 - busca por numero de temporados + avaliacao
                    9 - busca por trecho de episodio
                    10 - top 5 episodios
                                    
                    0 - Sair                       
                    """;

            System.out.println(menu);
            opcao = leitura.nextInt();
            leitura.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscarSeriesPorAtor();
                    break;
                case 6:
                    buscarTop5Series();
                    break;
                case 7:
                    buscarPorCategoria();
                    break;
                case 8:
                    buscaPorNumeroDeTemporadasEAvaliacao();
                    break;
                case 9:
                    buscaEpisodioPorNome();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                case 10:
                    buscaTop5Episodios();
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }

    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
        Serie serie = new Serie(dados);
        repositorio.save(serie);
//        dadosSeries.add(dados);
        System.out.println(dados);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        return dados;
    }

    private void buscarEpisodioPorSerie() {
        System.out.println("Escolha uma serie pelo nome: ");
        var nomeSerie = leitura.nextLine();
        listarSeriesBuscadas();
        Optional<Serie> serie = series.stream()
                .filter(s -> s.getTitulo().toLowerCase().contains(nomeSerie.toLowerCase()))
                .findFirst();

        if (serie.isPresent()) {
            var serieEncontrada = serie.get();
            List<DadosTemporada> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumo.obterDados(ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }
            temporadas.forEach(System.out::println);

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());
            serieEncontrada.setEpisodios(episodios);
            repositorio.save(serieEncontrada);
        } else {
            System.out.println("Serie nao encontrada");
        }
    }

    private void listarSeriesBuscadas() {
        series = repositorio.findAll();

        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }

    private void buscarSeriePorTitulo() {
        System.out.println("Escolha uma serie pelo nome: ");
        var nomeSerie = leitura.nextLine();
        serie = repositorio.findByTituloContainingIgnoreCase(nomeSerie);

        if (serie.isPresent()) {
            System.out.println(serie);
        } else {
            System.out.println("serie nao encontrada");
        }
    }

    private void buscarSeriesPorAtor() {
        System.out.println("Qual do nome da pessoa que atua?");
        var nomeAtor = leitura.nextLine();
        System.out.println("qual a avaliacao?");
        var nota = leitura.nextDouble();
        List<Serie> seriesEncontradas = repositorio.findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeAtor, nota);
        if (!seriesEncontradas.isEmpty()) {
            seriesEncontradas.forEach(s ->
                    System.out.println(s.getTitulo() + " nota da serie: " + s.getAvaliacao()));
        } else {
            System.out.println("Atriz ou ator nao encontrado");
        }
    }

    private void buscarTop5Series() {
        List<Serie> topSeries = repositorio.findTop5ByOrderByAvaliacaoDesc();
        topSeries.forEach(serie ->
                System.out.println(serie.getTitulo() + " avaliacao = " + serie.getAvaliacao())
        );
    }

    private void buscarPorCategoria() {
        System.out.println("qual genero/categoria vc deseja buscar?");
        var texto = leitura.nextLine();
        Categoria categoria = Categoria.fromPortugues(texto);
        List<Serie> seriesPorCategoria = repositorio.findByGenero(categoria);
        System.out.println("Series do genero " + categoria + " encontradas ");
        seriesPorCategoria.forEach(System.out::println);
    }

    private void buscaPorNumeroDeTemporadasEAvaliacao() {
        System.out.println("Qual o numero maximo de temporadas voce deseja?");
        var numeroTemporadas = leitura.nextInt();
        System.out.println("qual a avaliacao?");
        var nota = leitura.nextDouble();
        List<Serie> seriesEncontradas = repositorio.seriesPorTemporadaEAvaliacao(numeroTemporadas, nota);
        if (!seriesEncontradas.isEmpty()) {
            seriesEncontradas.forEach(s ->
                    System.out.println("A serie " + s.getTitulo() + " tem " + s.getTotalTemporadas() + " temporadas.  Nota da serie: " + s.getAvaliacao()));
        } else {
            System.out.println("serie nao encontrada");
        }
    }

    private void buscaEpisodioPorNome() {
        System.out.println("Digite um trecho do episodio que deseja buscar:");
        var trecho = leitura.nextLine();
        List<Episodio> episodiosEncontrados = repositorio.episodioPorTrecho(trecho);
        if (!episodiosEncontrados.isEmpty()) {
            episodiosEncontrados.forEach(episodio ->
                    System.out.println(episodio.getTitulo() + " nota da episodio: " + episodio.getAvaliacao()));
        } else {
            System.out.println("Atriz ou ator nao encontrado");
        }
    }

    private void buscaTop5Episodios() {
        buscarSeriePorTitulo();
        if (serie.isPresent()) {
            Serie serieBuscada = serie.get();
            List<Episodio> listaEpisodios = repositorio.top5Episodios(serieBuscada);
            listaEpisodios.forEach(System.out::println);
            System.out.println("fim");
        }
    }


}