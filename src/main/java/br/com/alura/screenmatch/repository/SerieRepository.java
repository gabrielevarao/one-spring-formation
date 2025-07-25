package br.com.alura.screenmatch.repository;

import br.com.alura.screenmatch.model.Categoria;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.model.Serie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SerieRepository extends JpaRepository<Serie, Long> {

    Optional<Serie> findByTituloContainingIgnoreCase (String nomeSerie);
    List<Serie> findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(
            String nomeAtor, Double avaliacao);
    List<Serie> findTop5ByOrderByAvaliacaoDesc();
    List<Serie> findByGenero(Categoria categoria);
    List<Serie> findAllByOrderByTituloAsc();

    @Query("""
    SELECT s from Serie s 
    WHERE s.totalTemporadas <= :totalTemporadas 
    AND s.avaliacao >= :avaliacao
    ORDER BY s.totalTemporadas
    """)
    List<Serie> seriesPorTemporadaEAValiacao(int totalTemporadas, double avaliacao);

    @Query("""
    SELECT e from Serie s 
    JOIN s.episodios e
    WHERE e.titulo ILIKE %:trechoEpisodio%
    ORDER BY s.titulo""")
    List<Episodio> episodiosPorTrecho(String trechoEpisodio);

    @Query("""
            SELECT e
            FROM Serie s
            JOIN s.episodios e
            WHERE s.titulo ILIKE %:serie%
            ORDER BY e.avaliacao DESC
            LIMIT 5""")
    List<Episodio> top5episodiosPorSerie(String serie);

    @Query("""
            SELECT e
            FROM Serie s
            JOIN s.episodios e
            WHERE s = :serie
            AND YEAR(e.dataLancamento) >= :ano""")
    List<Episodio> episodiosPorSerieEAno(Serie serie, int ano);

    @Query("""
            SELECT s FROM Serie s
            JOIN s.episodios e
            WHERE e.dataLancamento IS NOT NULL
            GROUP BY s
            ORDER BY MAX(e.dataLancamento) DESC
            LIMIT 5""")
    List<Serie> lancamentosMaisRecentes();


    @Query("""
            SELECT e FROM Serie s
            JOIN s.episodios e
            WHERE s.id = :id
            AND e.temporada = :numero""")
    List<Episodio> obterEpisodiosPorTemporada(Long id, Long numero);



}
