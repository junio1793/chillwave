package com.wrapper.mvc.controller;

import com.wrapper.mvc.services.TesteService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/api", consumes = {"application/json", "application/xml", "*/*"}, produces = {"application/json", "application/xml"})
public class TesteApiController {

    @Autowired
    private TesteService testeService;

    @Autowired
    private CacheManager cacheManager;

    @RequestMapping(value = "/jdbcgateway/{endpointConstante:[a-zA-Z0-9_-]+}/{endpointIdentify:[a-zA-Z0-9_-]+}", method = RequestMethod.GET)
    public ResponseEntity<?> get(@PathVariable(value = "endpointConstante", required = true) String constat,
                                 @PathVariable(value = "endpointIdentify", required = true) String endpointIdentify,
                                 @RequestParam(value = "size", required = true) Integer size,
                                 @RequestParam(value = "page", required = true) Integer page,
                                 @RequestParam(value = "sort", required = false) String sort,
                                 @RequestParam(value = "operation", required = false) String operation, HttpServletRequest httpServletRequest) {

        Object objResult = testeService.handleRequest(httpServletRequest);
        return ResponseEntity.ok(objResult);
    }

    @GetMapping
    @RequestMapping("/caches")
    public Map<String, Map<Object, Object>> listarCaches() {
        Map<String, Map<Object, Object>> resultado = new LinkedHashMap<>();

        for (String cacheName : cacheManager.getCacheNames()) {
            org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);

            if (cache instanceof CaffeineCache) {
                CaffeineCache caffeineCache = (CaffeineCache) cache;
                com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();

                Map<Object, Object> cacheEntries = new LinkedHashMap<>(nativeCache.asMap());
                resultado.put(cacheName, cacheEntries);
            } else {
                resultado.put(cacheName, Map.of("info", "Não é um cache do tipo Caffeine ou não suportado"));
            }
        }

        return resultado;
    }
}
