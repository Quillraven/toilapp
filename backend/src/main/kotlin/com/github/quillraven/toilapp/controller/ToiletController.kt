package com.github.quillraven.toilapp.controller

import com.github.quillraven.toilapp.model.dto.CreateUpdateToiletDto
import com.github.quillraven.toilapp.service.ToiletService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/toilets")
class ToiletController(
    @Autowired private val toiletService: ToiletService
) {
    @PostMapping
    fun create(@RequestBody createUpdateToiletDto: CreateUpdateToiletDto) =
        toiletService.create(createUpdateToiletDto)

    @PutMapping
    fun update(@RequestBody createUpdateToiletDto: CreateUpdateToiletDto) =
        toiletService.update(createUpdateToiletDto)

    @GetMapping
    fun getNearbyToilets(
        @RequestParam(required = true) lon: Double,
        @RequestParam(required = true) lat: Double,
        @RequestParam(required = true) radiusInKm: Double,
        @RequestParam(required = true) maxToiletsToLoad: Long,
        @RequestParam(required = false) minDistanceInKm: Double,
        @RequestParam(required = false) toiletIdsToExclude: String
    ) = toiletService.getNearbyToilets(lon, lat, radiusInKm, maxToiletsToLoad, minDistanceInKm, toiletIdsToExclude)

    @GetMapping("/{id}")
    fun getToiletDetails(
        @PathVariable id: String,
        @RequestParam(required = false) lon: Double,
        @RequestParam(required = false) lat: Double
    ) = toiletService.getToiletDetails(id, lon, lat)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: String) = toiletService.delete(id)
}
