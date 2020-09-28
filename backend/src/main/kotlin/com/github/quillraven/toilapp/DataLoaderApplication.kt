package com.github.quillraven.toilapp

import com.github.quillraven.toilapp.model.Comment
import com.github.quillraven.toilapp.model.Toilet
import com.github.quillraven.toilapp.model.User
import com.github.quillraven.toilapp.service.ImageService
import com.github.quillraven.toilapp.service.ToiletService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import org.springframework.stereotype.Component
import java.io.InputStream
import java.util.*
import java.util.concurrent.Callable
import kotlin.random.Random

val positions = listOf(
    "47.0 11.0"
);
val titles = listOf(
    "Beautiful Toilet", "Shit Heaven", "Plumber's Paradies", "Flipi's World"
);
val descriptions = listOf( "Very nice loo.")
val commentTexts = listOf( "It was a pleasure", "Pleasant shit. I'll come back for sure.");
val userNames = listOf( "namibian",
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
        "pannikin")

@SpringBootApplication
class DataLoaderApplication

@Component
class DataLoaderRunner(
        @Autowired val imgService: ImageService,
        @Autowired val toiletService: ToiletService
): CommandLineRunner {

    val numToilets = 5;

    override fun run(vararg args: String?) {
        for(num in 0 until numToilets) {
            val imgName = "toilet" +  Random.nextInt(1, 10) + ".jpg";
            val title = titles[Random.nextInt(0, titles.size)]
            val description = descriptions[Random.nextInt(0, descriptions.size)]
            val rating = Random.nextInt(1, 6)
            val numCommands = Random.nextInt(0, 4)
            val comments = mutableListOf<Comment>()
            val log = Random.nextDouble(10.0, 20.0)
            val lat = Random.nextDouble(45.0, 55.0)
            for(i in 0..numCommands) {
                val userName = userNames[Random.nextInt(0, userNames.size)]
                val email = userName + "@mail.com";
                val user = User(UUID.randomUUID().toString(), userName, email);
                val comment = Comment(UUID.randomUUID().toString(), user, Date(), commentTexts[Random.nextInt(0, commentTexts.size)])
                comments.add(comment)
            }

            val inStreamCallable: Callable<InputStream> = Callable<InputStream>{DataLoaderRunner::class.java.getResourceAsStream("/sample-images/" + imgName)}
            var objId = imgService.store(inStreamCallable, "my-name");
            objId.subscribe({oid -> run{
                println("imgOid = " + oid)
                val toilet = Toilet(UUID.randomUUID().toString(), title, GeoJsonPoint(log, lat), oid.toHexString(), rating,
                        false, false, description, comments.toTypedArray(), emptyArray());
                val result = toiletService.create(toilet)
                result.subscribe({res -> run{
                    println("create toilet" + res);
                }})
            }});
        }
    }
}

fun main(args: Array<String>) {
    runApplication<DataLoaderApplication>(*args)
}
