package com.sportapp.tiendasport.controller;

import com.sportapp.tiendasport.dto.MensajeResponse;
import com.sportapp.tiendasport.model.Producto;
import com.sportapp.tiendasport.repository.ProductoRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/producto")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoRepository productoRepository;

    // Pública: el catálogo lo puede ver cualquier invitado
    @GetMapping
    public List<Producto> listar() {
        return productoRepository.findAll();
    }

    // Administrador o Vendedor
    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody Producto producto) {
        producto.setIdProducto(null);
        if (producto.getImagenUrl() == null || producto.getImagenUrl().isBlank()) {
            producto.setImagenUrl("/imagenes/productos/default.png");
        }
        productoRepository.save(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(MensajeResponse.ok("Producto guardado exitosamente"));
    }

    // Administrador o Vendedor
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Integer id, @RequestBody Producto datos) {
        return productoRepository.findById(id).map(producto -> {
            producto.setNombre(datos.getNombre());
            producto.setDescripcion(datos.getDescripcion());
            producto.setTalla(datos.getTalla());
            producto.setColor(datos.getColor());
            producto.setGenero(datos.getGenero() != null ? datos.getGenero() : "Unisex");
            producto.setPrecioMayorista(datos.getPrecioMayorista());
            producto.setCostoProducto(datos.getCostoProducto());
            producto.setStock(datos.getStock());
            producto.setImagenUrl(datos.getImagenUrl());
            producto.setImagenesPorColor(datos.getImagenesPorColor());
            producto.setIdCategoria(datos.getIdCategoria());
            productoRepository.save(producto);
            return ResponseEntity.ok(MensajeResponse.ok("Producto actualizado correctamente"));
        }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(MensajeResponse.error("No se encontró el producto")));
    }

    // Solo Administrador
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        if (!productoRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MensajeResponse.error("No existe ese producto"));
        }
        productoRepository.deleteById(id);
        return ResponseEntity.ok(MensajeResponse.ok("Producto eliminado"));
    }
}
