package org.stanislavin.fabulous

import kotlinx.coroutines.experimental.newSingleThreadContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kotlin.experimental.coroutine.EnableCoroutine
import org.springframework.kotlin.experimental.coroutine.annotation.Coroutine
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * @author sslavin
 * @since 11/12/2017
 */
@SpringBootApplication
@RestController
@EnableCoroutine
class MyFabulousApplication(private val stasService: StasService,
                            private val properties: MessageProperties) {

    @RequestMapping("/eat")
    @Coroutine("stasCoroutineContext")
    suspend fun eat(): String {
        return try {
            stasService.eat()
            "${properties.good} FROM ${Thread.currentThread().name}\n"
        } catch (e: IAmFullException) {
            "${properties.bad} FROM ${Thread.currentThread().name}\n"
        }
    }

    @Bean
    fun stasCoroutineContext()
            = newSingleThreadContext("stas-context-thread")
}

@Service
class StasService(private val foodService: FoodService,
                  private val bellyRepository: BellyRepository) {

    fun eat() {
        val food = foodService.getFood()
        bellyRepository.save(food)
    }
}

@Configuration
class MessageProperties(@Value("\${messages.good:THANKS}") val good: String,
                        @Value("\${messages.bad:FULL}") val bad: String)

fun main(args: Array<String>) {
    SpringApplicationBuilder(MyFabulousApplication::class.java).run(*args)
}

data class Food(val ingredients: Any)
interface FoodService {
    fun getFood(): Food

}
interface BellyRepository {
    fun save(food: Food)

}
@Service
class FoodServiceImpl : FoodService {
    override fun getFood(): Food {
        return Food(Any())
    }

}

class IAmFullException : RuntimeException()

@Repository
class BellyRepositoryImpl : BellyRepository {
    private val FULL_THRESHOLD = 3

    private val foods = mutableListOf<Food>()
    override fun save(food: Food) {
        if (foods.size >= FULL_THRESHOLD) throw IAmFullException()
        foods.add(food)
    }

}
