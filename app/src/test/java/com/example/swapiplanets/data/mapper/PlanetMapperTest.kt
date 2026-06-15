package com.example.swapiplanets.data.mapper

import com.example.swapiplanets.data.dto.PlanetDto
import org.junit.Assert.assertEquals
import org.junit.Test

class PlanetMapperTest {

    @Test
    fun toDomain_extractsIdFromUrl() {
        val dto = PlanetDto(
            name = "Alderaan",
            climate = "temperate",
            terrain = "grasslands",
            population = "2000000000",
            rotationPeriod = "24",
            orbitalPeriod = "364",
            diameter = "12500",
            url = "https://swapi.dev/api/planets/2/"
        )

        val mapped = dto.toDomain()

        assertEquals("2", mapped.id)
        assertEquals("Alderaan", mapped.name)
    }
}
