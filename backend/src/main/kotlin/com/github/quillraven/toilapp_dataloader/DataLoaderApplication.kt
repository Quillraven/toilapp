package com.github.quillraven.toilapp_dataloader

import com.github.quillraven.toilapp.configuration.MongoConfiguration
import com.github.quillraven.toilapp.model.db.Comment
import com.github.quillraven.toilapp.model.db.ImageMetadata
import com.github.quillraven.toilapp.model.db.Rating
import com.github.quillraven.toilapp.model.db.Toilet
import com.github.quillraven.toilapp.model.db.User
import com.github.quillraven.toilapp.repository.CommentRepository
import com.github.quillraven.toilapp.repository.RatingRepository
import com.github.quillraven.toilapp.repository.ToiletRepository
import com.github.quillraven.toilapp.repository.UserRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.util.function.Tuples
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
    @Autowired private val userRepository: UserRepository,
    @Autowired private val toiletRepository: ToiletRepository,
    @Qualifier("reactiveGridFsTemplateForImages")
    @Autowired private val gridFsTemplate: ReactiveGridFsTemplate,
    @Autowired private val commentRepository: CommentRepository,
    @Autowired private val ratingRepository: RatingRepository,
    @Autowired private val context: ConfigurableApplicationContext
) : CommandLineRunner {
    private val numToilets = 20
    private val numComments = numToilets * 10
    private val numRatings = numToilets * 100

    private fun createUsers() = Flux.range(0, userNames.size + 1)
        .flatMap {
            if (it < userNames.size) {
                LOG.debug("Creating user $it")
                userRepository.save(
                    User(
                        id = ObjectId(),
                        name = userNames[it],
                        email = "${userNames[it]}@mail.com"
                    )
                )
            } else {
                LOG.debug("Creating dev-user")
                userRepository.save(
                    User(
                        id = ObjectId("000000000000012343456789"),
                        name = "devuser",
                        email = "devuser@mail.com"
                    )
                )
            }
        }
        .collectList()

    private fun createToilets() = Flux.range(0, numToilets)
        .flatMap {
            LOG.debug("Creating toilet $it")

            toiletRepository.save(
                Toilet(
                    id = ObjectId(),
                    title = titles.random(),
                    location = GeoJsonPoint(Random.nextDouble(10.0, 20.0), Random.nextDouble(45.0, 55.0)),
                    description = descriptions.random()
                )
            )
        }
        // upload preview image
        .flatMap { toilet ->
            val imageName = "toilet${Random.nextInt(1, 10)}.jpg"

            gridFsTemplate.store(
                DataBufferUtils.readInputStream(
                    { DataLoaderRunner::class.java.getResourceAsStream("/sample-images/$imageName") },
                    DefaultDataBufferFactory(),
                    DefaultDataBufferFactory.DEFAULT_INITIAL_CAPACITY
                ),
                imageName,
                MediaType.IMAGE_JPEG_VALUE,
                ImageMetadata(
                    toiletId = toilet.id,
                    preview = true
                )
            ).map { toilet }
        }
        .collectList()

    private fun createComments(users: List<User>, toilets: MutableList<Toilet>) = Flux.range(0, numComments)
        .flatMap {
            LOG.debug("Creating comment $it")
            commentRepository.save(
                Comment(
                    id = ObjectId(),
                    toiletId = toilets.random().id,
                    text = commentTexts.random(),
                    userRef = users.random().id
                )
            )
        }

    private fun createRatings(users: List<User>, toilets: MutableList<Toilet>) = Flux.range(0, numRatings)
        .flatMap {
            LOG.debug("Creating rating $it")
            ratingRepository.save(
                Rating(
                    id = ObjectId(),
                    toiletId = toilets.random().id,
                    userRef = users.random().id,
                    value = Random.nextInt(1, 6)
                )
            )
        }

    override fun run(vararg args: String?) {
        createUsers()
            .flatMap { users ->
                createToilets().map { toilets ->
                    Tuples.of(toilets, users)
                }
            }
            .flatMapMany {
                val toilets = it.t1
                val users = it.t2

                Flux.merge(
                    createComments(users, toilets),
                    createRatings(users, toilets)
                )
            }
            .collectList()
            // close application when all toilets are processed
            .subscribe {
                GlobalScope.launch {
                    exitProcess(SpringApplication.exit(context))
                }
            }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(DataLoaderRunner::class.java)
    }
}

fun main(args: Array<String>) {
    runApplication<DataLoaderApplication>(*args)
}
