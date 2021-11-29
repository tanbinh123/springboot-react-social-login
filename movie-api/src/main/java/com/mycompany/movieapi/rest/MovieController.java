package com.mycompany.movieapi.rest;

import com.mycompany.movieapi.mapper.MovieMapper;
import com.mycompany.movieapi.model.Movie;
import com.mycompany.movieapi.rest.dto.CreateMovieRequest;
import com.mycompany.movieapi.rest.dto.MovieDto;
import com.mycompany.movieapi.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import static com.mycompany.movieapi.config.SwaggerConfig.BEARER_KEY_SECURITY_SCHEME;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;
    private final MovieMapper movieMapper;

    @Operation(security = { @SecurityRequirement(name = BEARER_KEY_SECURITY_SCHEME) })
    @GetMapping
    public List<MovieDto> getMovies(@RequestParam(value = "text", required = false) String text) {
        List<Movie> movies = (text == null) ? movieService.getMovies() : movieService.getMoviesContainingText(text);
        return movies.stream()
                .map(movieMapper::toMovieDto)
                .collect(Collectors.toList());
    }

    @Operation(security = { @SecurityRequirement(name = BEARER_KEY_SECURITY_SCHEME) })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public MovieDto createMovie(@Valid @RequestBody CreateMovieRequest createMovieRequest) {
        Movie movie = movieMapper.toMovie(createMovieRequest);
        return movieMapper.toMovieDto(movieService.saveMovie(movie));
    }

    @Operation(security = { @SecurityRequirement(name = BEARER_KEY_SECURITY_SCHEME) })
    @DeleteMapping("/{imdb}")
    public MovieDto deleteMovie(@PathVariable String imdb) {
        Movie movie = movieService.validateAndGetMovie(imdb);
        movieService.deleteMovie(movie);
        return movieMapper.toMovieDto(movie);
    }
}
