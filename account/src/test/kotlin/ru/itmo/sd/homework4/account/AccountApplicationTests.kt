package ru.itmo.sd.homework4.account

import org.junit.jupiter.api.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.containers.FixedHostPortGenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers


class MyFixedHostPortGenericContainer(imageName: String) :
    FixedHostPortGenericContainer<MyFixedHostPortGenericContainer>(imageName)

@SpringBootTest
@Testcontainers
class AccountApplicationTests {
    private val exchange = "/exchange"
    private val account = "/account"
    private val info = "companiesInfo"

    private final val exchangeEndpoint = "http://localhost:2222"
    private val webExchangeClient = WebTestClient.bindToServer().baseUrl(exchangeEndpoint).build()
    private final val accountEndpoint = "http://localhost:3333"
    private val webAccountClient = WebTestClient.bindToServer().baseUrl(accountEndpoint).build()

    @Container
    private val exchangeApp = MyFixedHostPortGenericContainer("docker.io/library/exchange:0.0.1-SNAPSHOT")
        .withFixedExposedPort(2222, 2222)
        .withExposedPorts(2222)

    @BeforeEach
    fun setupEach() {
        webAccountClient.getFromAccount("/remove-all").exchange()
        webExchangeClient.getFromExchange("/remove-all").exchange()
        for (i in 1..10) {
            webAccountClient.getFromAccount("/new?username=user$i").exchange()
            webExchangeClient.getFromExchange("/new-company?name=company$i&shares=${i * 500}&cost=${i * 100}")
                .exchange()
        }
    }

    @Test
    fun buySharesOneUserOneCompany() {
        webAccountClient.run {
            getFromAccount("/deposit?username=user1&sum=1000").exchange()
            getFromAccount("/buy?username=user1&company=company1&amount=5").exchange()
            getFromAccount("?username=user1")
                .exchange()
                .expectBody().let {
                    it.jsonPath("\$.$info[0].companyName").isEqualTo("company1")
                    it.jsonPath("\$.$info[0].sharesAmount").isEqualTo("5")
                    it.jsonPath("\$.$info[0].cost").isEqualTo("500.0")
                }
        }
        webExchangeClient
            .getFromExchange("/company1")
            .exchange()
            .expectBody().let {
                it.jsonPath("\$.name").isEqualTo("company1")
                it.jsonPath("\$.sharesAmount").isEqualTo("495")
                it.jsonPath("\$.shareCost").isEqualTo("100.0")
            }
    }

    @Test
    fun buySharesMultipleUsersOneCompany() {
        webAccountClient.run {
            getFromAccount("/deposit?username=user1&sum=2000").exchange()
            getFromAccount("/deposit?username=user2&sum=2000").exchange()
            getFromAccount("/deposit?username=user3&sum=2000").exchange()
            getFromAccount("/buy?username=user1&company=company1&amount=5").exchange()
            getFromAccount("/buy?username=user2&company=company1&amount=2").exchange()
            getFromAccount("/buy?username=user3&company=company1&amount=3").exchange()
            getFromAccount("?username=user1")
                .exchange()
                .expectBody().let {
                    it.jsonPath("\$.$info[0].companyName").isEqualTo("company1")
                    it.jsonPath("\$.$info[0].sharesAmount").isEqualTo("5")
                    it.jsonPath("\$.$info[0].cost").isEqualTo("500.0")
                }
            getFromAccount("?username=user2")
                .exchange()
                .expectBody().let {

                    it.jsonPath("\$.$info[0].companyName").isEqualTo("company1")
                    it.jsonPath("\$.$info[0].sharesAmount").isEqualTo("2")
                    it.jsonPath("\$.$info[0].cost").isEqualTo("200.0")

                }
            getFromAccount("?username=user3")
                .exchange()
                .expectBody().let {
                    it.jsonPath("\$.$info[0].companyName").isEqualTo("company1")
                    it.jsonPath("\$.$info[0].sharesAmount").isEqualTo("3")
                    it.jsonPath("\$.$info[0].cost").isEqualTo("300.0")
                }
        }
        webExchangeClient.run {
            getFromExchange("/company1")
                .exchange()
                .expectBody().let {
                    it.jsonPath("\$.name").isEqualTo("company1")
                    it.jsonPath("\$.sharesAmount").isEqualTo("490")
                    it.jsonPath("\$.shareCost").isEqualTo("100.0")
                }
        }
    }

    @Test
    fun buySharesOneUserMultipleCompanies() {
        webAccountClient.run {
            getFromAccount("/deposit?username=user1&sum=2000").exchange()
            getFromAccount("/buy?username=user1&company=company1&amount=5").exchange()
            getFromAccount("/buy?username=user1&company=company2&amount=2").exchange()
            getFromAccount("/buy?username=user1&company=company3&amount=3").exchange()
            getFromAccount("?username=user1")
                .exchange()
                .expectBody().let {
                    it.jsonPath("\$.$info[0].companyName").isEqualTo("company1")
                    it.jsonPath("\$.$info[0].sharesAmount").isEqualTo("5")
                    it.jsonPath("\$.$info[0].cost").isEqualTo("500.0")

                    it.jsonPath("\$.$info[1].companyName").isEqualTo("company2")
                    it.jsonPath("\$.$info[1].sharesAmount").isEqualTo("2")
                    it.jsonPath("\$.$info[1].cost").isEqualTo("400.0")

                    it.jsonPath("\$.$info[2].companyName").isEqualTo("company3")
                    it.jsonPath("\$.$info[2].sharesAmount").isEqualTo("3")
                    it.jsonPath("\$.$info[2].cost").isEqualTo("900.0")
                }
        }
    }

    @Test
    fun buySharesAndThenSell() {
        webAccountClient.run {
            getFromAccount("/deposit?username=user1&sum=2000").exchange()
            getFromAccount("/buy?username=user1&company=company1&amount=5").exchange()
            getFromAccount("/buy?username=user1&company=company2&amount=2").exchange()
            getFromAccount("/buy?username=user1&company=company3&amount=3").exchange()
            getFromExchange("/company1")
                .exchange()
                .expectBody()
                .jsonPath("\$.sharesAmount")
                .isEqualTo("495")
            getFromAccount("/sell?username=user1&company=company1&amount=2").exchange()
            getFromAccount("?username=user1")
                .exchange()
                .expectBody().let {
                    it.jsonPath("\$.$info[0].companyName").isEqualTo("company1")
                    it.jsonPath("\$.$info[0].sharesAmount").isEqualTo("3")
                    it.jsonPath("\$.$info[0].cost").isEqualTo("300.0")

                    it.jsonPath("\$.$info[1].companyName").isEqualTo("company2")
                    it.jsonPath("\$.$info[1].sharesAmount").isEqualTo("2")
                    it.jsonPath("\$.$info[1].cost").isEqualTo("400.0")

                    it.jsonPath("\$.$info[2].companyName").isEqualTo("company3")
                    it.jsonPath("\$.$info[2].sharesAmount").isEqualTo("3")
                    it.jsonPath("\$.$info[2].cost").isEqualTo("900.0")
                }
            getFromExchange("/company1")
                .exchange()
                .expectBody()
                .jsonPath("\$.sharesAmount")
                .isEqualTo("497")
        }
    }

    @Test
    fun insufficientBalanceToBuyShares() {
        webAccountClient.run {
            getFromAccount("/deposit?username=user1&sum=2000").exchange()
            getFromAccount("/buy?username=user1&company=company1&amount=30")
                .exchange()
                .expectStatus()
                .is4xxClientError
        }
    }

    @Test
    fun insufficientBalanceToBuySharesAndThenMakePayment() {
        webAccountClient.run {
            getFromAccount("/deposit?username=user1&sum=2000").exchange()
            getFromAccount("/buy?username=user1&company=company1&amount=30")
                .exchange()
                .expectStatus()
                .is4xxClientError
            getFromAccount("/deposit?username=user1&sum=1500").exchange()
            getFromAccount("/buy?username=user1&company=company1&amount=30")
                .exchange()
                .expectStatus()
                .is2xxSuccessful
            getFromAccount("?username=user1")
                .exchange()
                .expectBody().let {
                    it.jsonPath("\$.$info[0].companyName").isEqualTo("company1")
                    it.jsonPath("\$.$info[0].sharesAmount").isEqualTo("30")
                    it.jsonPath("\$.$info[0].cost").isEqualTo("3000.0")
                }
        }
    }

    @Test
    fun insufficientSharesAmountAndThenAddShares() {
        webAccountClient.run {
            getFromAccount("/deposit?username=user1&sum=100000").exchange()
            getFromAccount("/buy?username=user1&company=company1&amount=600")
                .exchange()
                .expectStatus()
                .is4xxClientError
        }
        webExchangeClient.run {
            getFromExchange("/add-shares?company=company1&amount=200").exchange()
        }
        webAccountClient.run {
            getFromAccount("/buy?username=user1&company=company1&amount=600")
                .exchange()
                .expectStatus()
                .is2xxSuccessful
            getFromAccount("?username=user1")
                .exchange()
                .expectBody().let {
                    it.jsonPath("\$.$info[0].companyName").isEqualTo("company1")
                    it.jsonPath("\$.$info[0].sharesAmount").isEqualTo("600")
                    it.jsonPath("\$.$info[0].cost").isEqualTo("60000.0")
                }
        }
        webExchangeClient
            .getFromExchange("/company1")
            .exchange()
            .expectBody()
            .jsonPath("\$.sharesAmount")
            .isEqualTo("100")
    }

    @Test
    fun companyNotFound() {
        webExchangeClient
            .getFromExchange("/update-cost?company=company11&new-cost=200")
            .exchange()
            .expectStatus()
            .is4xxClientError
    }

    @Test
    fun companyAlreadyExists() {
        webExchangeClient
            .getFromExchange("/new-company?name=company1&shares=1000&cost=200")
            .exchange()
            .expectStatus()
            .is4xxClientError
    }

    @Test
    fun accountNotFound() {
        webAccountClient
            .getFromAccount("/deposit?username=user11&sum=1000")
            .exchange()
            .expectStatus()
            .is4xxClientError
    }

    @Test
    fun accountAlreadyExists() {
        webAccountClient
            .getFromAccount("/new?username=user5")
            .exchange()
            .expectStatus()
            .is4xxClientError
    }

    private fun WebTestClient.getFromAccount(request: String) =
        webAccountClient
            .get()
            .uri("$account$request")

    private fun WebTestClient.getFromExchange(request: String) =
        webExchangeClient
            .get()
            .uri("$exchange$request")
}
