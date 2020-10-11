package com.github.quillraven.toilapp.repository

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.ReactiveMongoTransactionManager
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate

@Configuration
class MongoConfiguration {
    @Bean("reactiveGridFsTemplateForImages")
    fun reactiveGridFsTemplateForImages(
            reactiveMongoDbFactory: ReactiveMongoDatabaseFactory,
            mappingMongoConverter: MappingMongoConverter
    ) = ReactiveGridFsTemplate(reactiveMongoDbFactory, mappingMongoConverter, "images")

    @Bean
    fun reactiveMongoTransactionManager(
            reactiveMongoDbFactory: ReactiveMongoDatabaseFactory
    ) = ReactiveMongoTransactionManager(reactiveMongoDbFactory)
}