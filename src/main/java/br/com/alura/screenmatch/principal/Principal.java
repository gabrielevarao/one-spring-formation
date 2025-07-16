package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    Dotenv dotenv = Dotenv.configure().load();

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String OMDB_API_KEY = dotenv.get("OMDB_API_KEY");
    private Optional<Serie> serieBusca;

    @Autowired
    SerieRepository serieRepository;

    List<Serie> series;

    public Principal (SerieRepository serieRepository){
        this.serieRepository = serieRepository;
    }

    public void exibeMenu() {

        var opcao = -1;
        while (opcao != 0) {
            var menu = """
                    
                    ----------------------------------------------------------
                                        NOVA CONSULTA
                    ----------------------------------------------------------
                    ESCOLHA UMA OPÇÃO
                    
                    1 - Cadastrar uma nova série
                    2 - Buscar episódios de uma série cadastrada
                    3 - Listar séries cadastradas
                    4 - Buscar uma série cadastrada por título
                    5 - Buscar séries cadastradas filtrando por ator
                    6 - Buscar o TOP 5 das séries cadastradas
                    7 - Buscar séries cadastradas por categoria
                    8 - Buscar séries pela quantidade de temporadas e avaliação
                    9 - Buscar episódios de séries cadastradas por trecho
                    10 - Buscar o TOP 5 episódios de uma série
                    11 - Buscar episódios a partir de uma data
                    
                    0 - Sair""";

            System.out.println(menu);
            opcao = leitura.nextInt();
            leitura.nextLine();

            switch (opcao) {
                case 1:
                    buscarNovaSerie();
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
                    buscarSeriesPorCategoria();
                    break;
                case 8:
                    filtrarSeriesPorTemporadaEAvaliacao();
                    break;
                case 9:
                    buscarEpisodioPorTrecho();
                    break;
                case 10:
                    buscarTop5EpisodiosPorSerie();
                    break;
                case 11:
                    buscarEpisodiosDepoisDeUmaData();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;

                default:
                    System.out.println("Opção inválida");
            }
        }
    }

    private void buscarNovaSerie() {
        Serie dados = new Serie(getDadosSerie());
        serieRepository.save(dados);
        System.out.println("\n" + dados);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("\nDigite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + OMDB_API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        return dados;
    }

    private void buscarEpisodioPorSerie(){

        System.out.println("\nEstas são as séries buscadas: ");
        listarSeriesBuscadas();

        System.out.println("\nAgora, digite o nome da série para buscar seus episódios: ");
        String nomeSerie = leitura.nextLine();

        Optional<Serie> serie = serieRepository.findByTituloContainingIgnoreCase(nomeSerie);

        if (serie.isPresent()){

            var serieEncontrada = serie.get();
            List<DadosTemporada> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumo.obterDados(ENDERECO +
                                serieEncontrada.getTitulo().replace(" ", "+") +
                                "&season=" + i + OMDB_API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }

            temporadas.forEach(System.out::println);

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(), e)))
                    .collect((Collectors.toList()));

            serieEncontrada.setEpisodios(episodios);
            serieRepository.save(serieEncontrada);
        } else {
            System.out.println("Série não encontrada.");
        }
    }

    private void listarSeriesBuscadas(){

        series = serieRepository.findAll();
        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::print);
    }

    private void buscarSeriePorTitulo() {

        System.out.println("\nDigite o nome da série: ");
        String nomeSerie = leitura.nextLine();
        serieBusca = serieRepository.findByTituloContainingIgnoreCase(nomeSerie);

        if(serieBusca.isPresent()){
            System.out.println("\nDados da série: " + serieBusca.get());
        } else{
            System.out.println("Série não encontrada!");
        }
    }

    private void buscarSeriesPorAtor(){
        System.out.println("\nDigite o nome do ator ou da atriz: ");
        String nomeAtor = leitura.nextLine();

        System.out.println("\nDeseja ver as séries que tenham uma nota a partir de que valor? \nDigite um número de 0 (muito mal avaliada) a 10 (muito bem avaliada).");
        Double avaliacoes = leitura.nextDouble();

        List<Serie> seriesEncontradas = serieRepository.findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeAtor, avaliacoes);

        System.out.println("\nSéries com " + nomeAtor + ":");
        seriesEncontradas.forEach(s ->
                System.out.println("Nome: " + s.getTitulo() + " | Avaliação: " + s.getAvaliacao()));
    }

    private void buscarTop5Series(){
        List<Serie> top5Series = serieRepository.findTop5ByOrderByAvaliacaoDesc();
        System.out.println("");
        top5Series.forEach(serie ->
                System.out.println(">> " + serie.getTitulo() +
                        "| Avaliação: " + serie.getAvaliacao()));

    }

    private void buscarSeriesPorCategoria(){
        System.out.println("\nDeseja buscar séries de que categoria/gênero? ");
        var nomeGenero = leitura.nextLine();

        Categoria categoria = Categoria.fromPortugues(nomeGenero);

        List<Serie> seriesPorCategoria = serieRepository.findByGenero(categoria);

        System.out.println("Séries do gênero: " + nomeGenero);
        seriesPorCategoria.forEach(System.out::println);
    }

    public void filtrarSeriesPorTemporadaEAvaliacao(){
        System.out.println("\nDeseja buscar séries cadastradas com até quantas temporadas? ");
        var totalTemporadas = leitura.nextInt();
        leitura.nextLine();

        System.out.println("\nCom avaliação a partir de que valor (de 1 a 10)? ");
        var avaliacao = leitura.nextInt();
        leitura.nextLine();

        List<Serie> seriesPorNumeroDeTemporadas = serieRepository.seriesPorTemporadaEAValiacao(totalTemporadas, avaliacao);

        System.out.println("\nSÉRIES QUE ATENDEM AOS CRITÉRIOS SOLICITADOS\n");

        seriesPorNumeroDeTemporadas
                .forEach(s -> System.out.println(
                ">>>" + s.getTitulo() + "\nQuantidade de temporadas: " +
                        s.getTotalTemporadas() + " | Avaliação: " + s.getAvaliacao()));
    }

    private void buscarEpisodioPorTrecho(){
        System.out.println("\nDigite o nome do episódio para busca: ");
        String trechoEpisodio = leitura.nextLine();

        List<Episodio> episodiosEncontrados = serieRepository.episodiosPorTrecho(trechoEpisodio);

        System.out.println("\nEPISÓDIOS ENCONTRADOS\n");

        episodiosEncontrados.forEach(e -> System.out.printf(
                "Série: %s | Temporada: %d | Episódio %d - %s\n",
                e.getSerie().getTitulo(), e.getTemporada(),
                e.getNumeroEpisodio(), e.getTitulo()));
    }

    private void buscarTop5EpisodiosPorSerie(){
        buscarSeriePorTitulo();
        if(serieBusca.isPresent()){

            List<Episodio> episodiosEncontrados =
                    serieRepository.top5episodiosPorSerie(serieBusca.get().getTitulo());

            System.out.println("\nTOP 5 EPISÓDIOS\n");

            episodiosEncontrados.forEach(e -> System.out.printf(
                    "Série: %s | Temporada: %d | Episódio %d - %s| Avaliação: %f\n",
                    e.getSerie().getTitulo(), e.getTemporada(),
                    e.getNumeroEpisodio(), e.getTitulo(), e.getAvaliacao()));
        } else{
            System.out.println("Série não encontrada!");
        }
    }

    private void buscarEpisodiosDepoisDeUmaData(){
        buscarSeriePorTitulo();
        if(serieBusca.isPresent()){
            System.out.println("Quer buscar séries a partir de que ano? Digite a seguir.");
            int anoLancamento = leitura.nextInt();
            leitura.nextLine();

//            List<Episodio> episodiosAno = serieRepository
        } else{
            System.out.println("Série não encontrada!");
        }
    }

}
