package com.sw3.barber_microservice.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    /**
     * Define el manejador de recursos estáticos para la carpeta 'uploads'.
     *
     * @param registry El registro para añadir manejadores de recursos.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        
        // 1. Obtener la ruta absoluta de la carpeta 'uploads'
        // Es la misma lógica que usas en BarberController para guardar el archivo.
        Path uploadDir = Paths.get("uploads").toAbsolutePath();
        String uploadPath = uploadDir.toUri().toString();

        // 2. Registrar el manejador de recursos
        // Esto le dice a Spring que cualquier petición que empiece con /uploads/** // debe ser servida desde la ubicación física 'file:///path/to/tu/proyecto/uploads/'.
        
        // * addResourceHandler("/uploads/**") : Es el prefijo de URL que usará el navegador.
        // * addResourceLocations(uploadPath) : Es el directorio físico donde están los archivos.
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }

}
