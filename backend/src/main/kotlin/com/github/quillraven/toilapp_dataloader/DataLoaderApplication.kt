package com.github.quillraven.toilapp_dataloader

import com.github.quillraven.toilapp.configuration.MongoConfiguration
import com.github.quillraven.toilapp.model.db.Toilet
import com.github.quillraven.toilapp.model.db.User
import com.github.quillraven.toilapp.model.dto.CommentDto
import com.github.quillraven.toilapp.model.dto.ToiletDto
import com.github.quillraven.toilapp.model.dto.UserDto
import com.github.quillraven.toilapp.service.CommentService
import com.github.quillraven.toilapp.service.ImageService
import com.github.quillraven.toilapp.service.ToiletService
import com.github.quillraven.toilapp.service.UserService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.concurrent.Callable
import kotlin.random.Random
import kotlin.system.exitProcess

val titles = listOf(
    "Beautiful Toilet",
    "Shit Heaven",
    "Plumber's Paradies",
    "Flipi's World",
    "Diarrhea nightmares",
    "Decent public toilet",
    "When you gotta you gott go"
)
val descriptions = listOf(
    "Very nice loo.",
    "No toilet paper :( :(",
    "Only on layer toilet paper :(",
    "Very nice pooping experience.",
    "One of the most beautiful toilets in the neighborhood.",
    "Best crap ever!"
)
val commentTexts = listOf("It was a pleasure", "Pleasant shit. I'll come back for sure.")
val userNames = listOf(
    "user1",
    "namibian",
    "specialist",
    "homes",
    "imaginary",
    "weathertop",
    "banana",
    "plink",
    "manrope,",
    "rapid",
    "moonraker",
    "wholesale",
    "rundown",
    "movement",
    "partridge",
    "obsessed",
    "pannikin"
)

@EnableReactiveMongoRepositories(basePackages = ["com.github.quillraven.toilapp.repository"])
@SpringBootApplication(
    scanBasePackages = [
        "com.github.quillraven.toilapp.model",
        "com.github.quillraven.toilapp.repository",
        "com.github.quillraven.toilapp.service",
        "com.github.quillraven.toilapp_dataloader"
    ],
    scanBasePackageClasses = [MongoConfiguration::class]
)
class DataLoaderApplication

@Component
class DataLoaderRunner(
    @Autowired private val imgService: ImageService,
    @Autowired private val toiletService: ToiletService,
    @Autowired private val userService: UserService,
    @Autowired private val commentService: CommentService,
    @Autowired private val context: ConfigurableApplicationContext
) : CommandLineRunner {

    val numToilets = 20

    override fun run(vararg args: String?) {
        createUsers()
            .flatMap { createComments(it) }
            .flatMap { createToilets(it) }
            .subscribe {
                println("${it.size} toilets created on thread ${Thread.currentThread()}")
                GlobalScope.launch {
                    println("Shutdown on thread ${Thread.currentThread()}")
                    exitProcess(SpringApplication.exit(context))
                }
            }
    }

    private fun createUsers(): Mono<List<UserDto>> {
        val userMonoList = mutableListOf<Mono<UserDto>>()
        //create users
        for (usrName in userNames) {
            val userName = userNames[Random.nextInt(0, userNames.size)]
            val email = "$userName@mail.com"
            val user = User(ObjectId(), userName, email)
            userMonoList.add(userService.create(user))
        }
        return Flux.fromIterable(userMonoList).flatMap { it }.collectList()
    }

    private fun createComments(users: List<UserDto>): Mono<List<CommentDto>> {
        val commentMonoList = mutableListOf<Mono<CommentDto>>()
        for (i in 0..5) {
            val user = users.random()
            commentMonoList.add(commentService.create(ObjectId(user.id), commentTexts.random()))
        }
        return Flux.fromIterable(commentMonoList).flatMap { it }.collectList()
    }

    private fun createToilets(
        comments: List<CommentDto>
    ): Mono<List<ToiletDto>> {
        val toiletMonoList = mutableListOf<Mono<ToiletDto>>()
        for (num in 0 until numToilets) {
            toiletMonoList.add(createToilet(comments))
        }
        return Flux.fromIterable(toiletMonoList).flatMap { it }.collectList()
    }

    private fun createToilet(
        comments: List<CommentDto>
    ): Mono<ToiletDto> {
        val imgName = "toilet" + Random.nextInt(1, 10) + ".jpg"
        val title = titles.random()
        val description = descriptions.random()
        val rating = Random.nextDouble(1.0, 6.0)
        val log = Random.nextDouble(10.0, 20.0)
        val lat = Random.nextDouble(45.0, 55.0)

        val inStreamCallable = Callable {
            DataLoaderRunner::class.java.getResourceAsStream("/sample-images/$imgName")
        }
        val objId = imgService.store(inStreamCallable, "my-name")
        return objId.flatMap { oid ->
            println("imgOid = $oid")
            val toilet = Toilet(
                ObjectId(),
                title,
                GeoJsonPoint(log, lat),
                oid,
                rating,
                disabled = false,
                toiletCrewApproved = false,
                description = description,
                commentRefs = comments.subList(Random.nextInt(4), comments.size).map { ObjectId(it.id) }
                    .toMutableList(),
                imageRefs = mutableListOf()
            )
            toiletService.create(toilet)
        }
    }
}

fun main(args: Array<String>) {
    runApplication<DataLoaderApplication>(*args)
}
