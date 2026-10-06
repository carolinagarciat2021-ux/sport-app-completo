package com.sportapp.tiendasport.controller;

import com.sportapp.tiendasport.dto.MensajeResponse;
import com.sportapp.tiendasport.model.Categoria;
import com.sportapp.tiendasport.repository.CategoriaRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaRepository categoriaRepository;

    // Pública: los invitados también deben poder ver las categorías del catálogo
    @GetMapping
    public List<Categoria> listar() {
        return categoriaRepository.findAll();
    }

    // Solo Administrador (regla definida en SecurityConfig)
    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody Categoria categoria) {
        categoria.setIdCategoria(null); // por si acaso viene un id desde el cliente, lo ignoramos
        categoriaRepository.save(categoria);
        return ResponseEntity.status(HttpStatus.CREATED).body(MensajeResponse.ok("Categoría guardada exitosamente"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Integer id, @RequestBody Categoria datos) {
        return categoriaRepository.findById(id).map(categoria -> {
            categoria.setNombre(datos.getNombre());
            categoria.setDescripcion(datos.getDescripcion());
            categoria.setTipoCategoria(datos.getTipoCategoria());
            categoriaRepository.save(categoria);
            return ResponseEntity.ok(MensajeResponse.ok("Categoría actualizada correctamente"));
        }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(MensajeResponse.error("No se encontró la categoría")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        if (!categoriaRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MensajeResponse.error("No existe esa categoría"));
        }
        categoriaRepository.deleteById(id);
        return ResponseEntity.ok(MensajeResponse.ok("Categoría eliminada"));
    }
}
