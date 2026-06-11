package org.example.proyecto_baseee.model;

import jakarta.persistence.*;

@Entity
@Table(name = "workflows")
public class Workflow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nombre_proceso;
    private String descripcion;
    private String estado;
    private String responsable;
    private Long documentId;
    private String modulo;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNombre_proceso() { return nombre_proceso; }
    public void setNombre_proceso(String nombre_proceso) { this.nombre_proceso = nombre_proceso; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getResponsable() { return responsable; }
    public void setResponsable(String responsable) { this.responsable = responsable; }
    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }
    public String getModulo() { return modulo; }
    public void setModulo(String modulo) { this.modulo = modulo; }
}