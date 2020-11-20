package com.github.quillraven.toilapp_dataloader

import com.github.quillraven.toilapp.configuration.MongoConfiguration
import com.github.quillraven.toilapp.model.db.Comment
import com.github.quillraven.toilapp.model.db.Rating
import com.github.quillraven.toilapp.model.db.Toilet
import com.github.quillraven.toilapp.model.db.User
import com.github.quillraven.toilapp.repository.CommentRepository
import com.github.quillraven.toilapp.repository.RatingRepository
import com.github.quillraven.toilapp.repository.ToiletRepository
import com.github.quillraven.toilapp.repository.UserRepository
import com.github.quillraven.toilapp.service.GridFsImageService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
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
import reactor.util.function.Tuples
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
    @Autowired private val toiletRepository: ToiletRepository,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val commentRepository: CommentRepository,
    @Autowired private val ratingRepository: RatingRepository,
    @Autowired private val imageService: GridFsImageService,
    @Autowired private val context: ConfigurableApplicationContext
) : CommandLineRunner {
    private val numComments = 20
    private val numToilets = 25
    private val numRatings = 50

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

    private fun createComments(users: List<User>) = Flux.range(0, numComments)
        .flatMap {
            LOG.debug("Creating comment $it")
            commentRepository.save(
                Comment(
                    id = ObjectId(),
                    text = commentTexts.random(),
                    userRef = users.random().id
                )
            )
        }
        .collectList()

    private fun createRatings(users: List<User>) = Flux.range(0, numRatings)
        .flatMap {
            LOG.debug("Creating rating $it")
            ratingRepository.save(
                Rating(
                    id = ObjectId(),
                    userRef = users.random().id,
                    value = Random.nextInt(0, 5)
                )
            )
        }
        .collectList()

    private fun createToilets(comments: List<Comment>, ratings: List<Rating>) = Flux.range(0, numToilets)
        .flatMap {
            val toilet = Toilet(
                id = ObjectId(),
                title = titles.random(),
                location = GeoJsonPoint(Random.nextDouble(10.0, 20.0), Random.nextDouble(45.0, 55.0)),
                description = descriptions.random()
            )

            // add comments
            val commentsToAdd = Random.nextInt(0, comments.size)
            for (i in 0 until commentsToAdd) {
                toilet.commentRefs.add(comments[i].id)
            }

            // add average rating and rating references
            val ratingsToAdd = Random.nextInt(0, ratings.size)
            for (i in 0 until ratingsToAdd) {
                toilet.ratingRefs.add(ratings[i].id)
                toilet.averageRating += ratings[i].value
            }
            if (toilet.ratingRefs.isNotEmpty()) {
                toilet.averageRating /= toilet.ratingRefs.size
            }

            LOG.debug("Creating toilet $it with $commentsToAdd comments and $ratingsToAdd ratings")

            toiletRepository.save(toilet)
        }
        // upload preview image
        .flatMap { toilet ->
            val imageName = "toilet${Random.nextInt(1, 10)}.jpg"
            imageService.store(
                Callable {
                    DataLoaderRunner::class.java.getResourceAsStream("/sample-images/$imageName")
                },
                "${toilet.title}-preview"
            ).zipWith(Mono.just(toilet))
        }
        // set preview image
        .flatMap {
            val imageId = it.t1
            val toilet = it.t2

            toiletRepository.save(toilet.copy(previewID = imageId))
        }
        .collectList()

    override fun run(vararg args: String?) {
        createUsers()
            .flatMap { users ->
                createComments(users).map { comments ->
                    Tuples.of(users, comments)
                }
            }
            .flatMap {
                val users = it.t1
                val comments = it.t2

                createRatings(users).map { ratings ->
                    Tuples.of(comments, ratings)
                }
            }
            .flatMap {
                val comments = it.t1
                val ratings = it.t2

                createToilets(comments, ratings)
            }
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
