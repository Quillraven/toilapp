package com.github.quillraven.toilapp.controller

import com.github.quillraven.toilapp.model.Toilet
import com.github.quillraven.toilapp.service.ToiletService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
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
    fun updateToilet(@PathVariable id: String, @RequestBody toilet: Toilet) = toiletService.update(id, toilet)

    @GetMapping("/toilets")
    fun getNearbyToilets(
        @RequestParam(required = false) x: Double?,
        @RequestParam(required = false) y: Double?,
        @RequestParam(required = false) maxDistanceInMeters: Double?
    ): Flux<Toilet> {
        return when {
            x == null || y == null || maxDistanceInMeters == null -> {
                toiletService.getAll()
            }
            else -> {
                toiletService.getNearbyToilets(x, y, maxDistanceInMeters)
            }
        }
    }

    @DeleteMapping("/toilets/{id}")
    fun deleteToilet(@PathVariable id: String) = toiletService.delete(id)
}
