package br.com.alura.screenmatch.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
@Entity
@Table(name = "series")
public class Serie {

    public Serie() {}
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String titulo;
    private Integer totalTemporadas;
    private Double avaliacao;
    @Enumerated(EnumType.STRING)
    private Categoria genero;
    private String atores;
    private String capa;
    private String sinopse;
    @Transient
    private List<Episodio> episodios = new ArrayList<>();

    public List<Episodio> getEpisodios() {
        return episodios;
    }

    public Long getIg() {
        return id;
    }

    public void setIg(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }
    public Integer getTotalTemporadas() {
        return totalTemporadas;
    }
    public Double getAvaliacao() {
        return avaliacao;
    }
    public String getAtores() {
        return atores;
    }
    public String getCapa() {
        return capa;
    }
    public String getSinopse() {
        return sinopse;
    }

    public Categoria getGenero() {
        return genero;
    }

    public Serie(DadosSerie dados) {
        this.titulo = dados.titulo();
        this.totalTemporadas = dados.totalTemporadas();
        this.avaliacao = OptionalDouble.of(Double.valueOf(dados.avaliacao())).orElse(0);
        this.genero = Categoria.fromSring(dados.genero().split(",")[0].trim());
        this.atores = dados.atores();
        this.capa = dados.capa();
        this.sinopse = dados.sinopse();
    }

    @Override
    public String toString() {
        return  "Genero=" + genero +
                ", titulo='" + titulo + '\'' +
                ", totalTemporadas=" + totalTemporadas +
                ", avaliacao=" + avaliacao +
                ", atores='" + atores + '\'' +
                ", capa='" + capa + '\'' +
                ", sinopse='" + sinopse + '\'';
    }
}
