package com.github.quillraven.toilapp_dataloader

import com.github.quillraven.toilapp.configuration.MongoConfiguration
import com.github.quillraven.toilapp.model.db.Toilet
import com.github.quillraven.toilapp.model.db.User
import com.github.quillraven.toilapp.model.dto.CreateUpdateCommentDto
import com.github.quillraven.toilapp.model.dto.CreateUpdateRatingDto
import com.github.quillraven.toilapp.model.dto.CreateUpdateToiletDto
import com.github.quillraven.toilapp.model.dto.ToiletDto
import com.github.quillraven.toilapp.model.dto.UserDto
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
    @Autowired private val context: ConfigurableApplicationContext
) : CommandLineRunner {

    val numToilets = 1

    override fun run(vararg args: String?) {
        createUsers()
            .flatMap { createToilets().zipWith(Mono.just(it)) }
            .flatMap { createComments(it.t1, it.t2).zipWith(Mono.just(it)) }
            .flatMap { createRatings(it.t2.t1, it.t2.t2) }
            .subscribe {
                println("$numToilets toilets created on thread ${Thread.currentThread()}")
                GlobalScope.launch {
                    println("Shutdown on thread ${Thread.currentThread()}")
                    exitProcess(SpringApplication.exit(context))
                }
            }
    }

    private fun createUsers(): Mono<List<UserDto>> {
        val userMonoList = mutableListOf<Mono<UserDto>>()
        //create users
        val devUser = User(ObjectId("000000000000012343456789"), "devuser", "devuser@mail.com")
        userMonoList.add(userService.create(devUser))
        for (usrName in userNames) {
            val userName = userNames[Random.nextInt(0, userNames.size)]
            val email = "$userName@mail.com"
            val user = User(ObjectId(), userName, email)
            userMonoList.add(userService.create(user))
        }
        return Flux.fromIterable(userMonoList).flatMap { it }.collectList()
    }

    private fun createToilets(): Mono<List<ToiletDto>> {
        val toiletMonoList = mutableListOf<Mono<ToiletDto>>()
        for (num in 0 until numToilets) {
            toiletMonoList.add(createToilet())
        }
        return Flux.fromIterable(toiletMonoList).flatMap { it }.collectList()
    }

    private fun createToilet(): Mono<ToiletDto> {
        val imgName = "toilet" + Random.nextInt(1, 10) + ".jpg"
        val title = titles.random()
        val description = descriptions.random()
        val log = Random.nextDouble(10.0, 20.0)
        val lat = Random.nextDouble(45.0, 55.0)

        val inStreamCallable = Callable {
            DataLoaderRunner::class.java.getResourceAsStream("/sample-images/$imgName")
        }

        return toiletService
            .create(
                CreateUpdateToiletDto(
                    id = "",
                    title = title,
                    location = GeoJsonPoint(log, lat),
                    disabled = false,
                    toiletCrewApproved = false,
                    description = description
                )
            )
            .zipWith(imgService.store(inStreamCallable, "my-name"))
            .flatMap {
                val imageId = it.t2
                val toiletDto = it.t1
                toiletService.linkImage(imageId.toHexString(), toiletDto.id)
            }
    }

    private fun createComments(toilets: List<ToiletDto>, users: List<UserDto>): Mono<List<Toilet>> {
        val toiletMonoList = mutableListOf<Mono<Toilet>>()
        for (num in 0 until 100) {
            toiletMonoList.add(
                toiletService.addComment(
                    ObjectId(users.random().id),
                    CreateUpdateCommentDto(
                        toiletId = toilets.random().id,
                        text = commentTexts.random()
                    )
                )
            )
        }
        return Flux.fromIterable(toiletMonoList).flatMap { it }.collectList()
    }

    private fun createRatings(toilets: List<ToiletDto>, users: List<UserDto>): Mono<List<Toilet>> {
        val toiletMonoList = mutableListOf<Mono<Toilet>>()
        for (num in 0 until 30) {
            toiletMonoList.add(
                toiletService.addRating(
                    ObjectId(users.random().id),
                    CreateUpdateRatingDto(
                        toiletId = toilets.random().id,
                        value = Random.nextInt(0, 6).toDouble()
                    )
                )
            )
        }
        return Flux.fromIterable(toiletMonoList).flatMap { it }.collectList()
    }
}

fun main(args: Array<String>) {
    runApplication<DataLoaderApplication>(*args)
}
