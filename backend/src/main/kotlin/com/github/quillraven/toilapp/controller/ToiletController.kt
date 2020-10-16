package com.github.quillraven.toilapp.controller

import com.github.quillraven.toilapp.dto.ToiletResultDto
import com.github.quillraven.toilapp.model.Toilet
import com.github.quillraven.toilapp.service.ToiletService
import org.bson.types.ObjectId
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
import reactor.core.publisher.Flux
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties

fun <T : Any> KClass<out T>.getNonNullProperties(vararg exceptions: KProperty1<T, *>) =
    declaredMemberProperties
        .filter { !it.returnType.isMarkedNullable && !exceptions.contains(it) }
        .map { it.name }
        .toTypedArray()

@RestController
@RequestMapping("/api")
class ToiletController(
    @Autowired
    private val toiletService: ToiletService
) {
    @PostMapping("/toilets")
    fun createToilet(@RequestBody toilet: Toilet) = toiletService.create(toilet)

    @PutMapping("/toilets/{id}")
    fun updateToilet(@PathVariable id: String, @RequestBody toilet: Toilet) = toiletService.update(ObjectId(id), toilet)

    @GetMapping("/toilets")
    fun getNearbyToilets(
        @RequestParam(required = false) lon: Double?,
        @RequestParam(required = false) lat: Double?,
        @RequestParam(required = false) maxDistanceInMeters: Double?
    ): Flux<ToiletResultDto> {
        return when {
            lon == null || lat == null || maxDistanceInMeters == null -> {
                toiletService.getAll().map { ToiletResultDto(it, -1.0) }
            }
            else -> {
                toiletService.getNearbyToilets(lon, lat, maxDistanceInMeters)
            }
        }
    }

    @DeleteMapping("/toilets/{id}")
    fun deleteToilet(@PathVariable id: String) = toiletService.delete(ObjectId(id))
}
