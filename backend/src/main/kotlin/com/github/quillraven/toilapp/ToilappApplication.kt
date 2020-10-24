package com.github.quillraven.toilapp

import com.github.quillraven.toilapp.model.db.Toilet
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType
import org.springframework.data.mongodb.core.index.GeospatialIndex
import org.springframework.stereotype.Component


@SpringBootApplication
class ToilappApplication

fun main(args: Array<String>) {
    runApplication<ToilappApplication>(*args)
}

@Component
class GeoSpatialIndexCreatorBean(val template: ReactiveMongoTemplate) {
    @EventListener
    fun onApplicationEvent(event: ContextRefreshedEvent) {
        template.indexOps(Toilet::class.java)
            .ensureIndex(GeospatialIndex(Toilet::location.name).typed(GeoSpatialIndexType.GEO_2DSPHERE))
            .subscribe { println("GeoSpatial index for ${Toilet::location.name} created") }
    }
}
