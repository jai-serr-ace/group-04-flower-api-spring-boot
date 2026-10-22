package com.example.group04flowerapispringboot.controllers

import com.example.group04flowerapispringboot.controller.QuoteController
import com.example.group04flowerapispringboot.model.Quote
import com.example.group04flowerapispringboot.repository.QuoteRepository
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(QuoteController::class)
class QuoteControllerTest : AuthenticatedControllerTest() {


    @MockitoBean
    lateinit var quotes: QuoteRepository

    private val base = "/api/v1/quotes"

    private val quote = Quote(
        quoteId = 1,
        quoteText = "the duck is red",
        author = "quackspeare"
    )

    private val quoteJson = """
        {"quoteText":"the duck is red","author":"quackspeare"}
    """.trimIndent()

    @Test
    fun `listQuotes returns the list`() {
        given(quotes.findAll()).willReturn(listOf(quote))

        mockMvc.perform(get(base))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].quoteId").value(1))
            .andExpect(jsonPath("$[0].quoteText").value(quote.quoteText))
            .andExpect(jsonPath("$[0].author").value("quackspeare"))
    }

    @Test
    fun `listQuotes returns an empty list when there are none`() {
        given(quotes.findAll()).willReturn(emptyList())

        mockMvc.perform(get(base))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    fun `createQuote returns 201 and the saved quote`() {
        given(quotes.create(quote.quoteText, "quackspeare")).willReturn(quote)

        mockMvc.perform(
            post(base)
                .contentType(MediaType.APPLICATION_JSON)
                .content(quoteJson)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.quoteId").value(1))
            .andExpect(jsonPath("$.author").value("quackspeare"))

        verify(quotes).create(quote.quoteText, "quackspeare")
    }

    @Test
    fun `createQuote works without an author`() {
        val anonymous = quote.copy(author = null)
        given(quotes.create(quote.quoteText, null)).willReturn(anonymous)

        mockMvc.perform(
            post(base)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"quoteText":"${quote.quoteText}"}""")
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.quoteText").value(quote.quoteText))

        verify(quotes).create(quote.quoteText, null)
    }

    @Test
    fun `createQuote returns 400 when quoteText is missing`() {
        mockMvc.perform(
            post(base)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"author":"Shakespeare"}""")
        )
            .andExpect(status().isBadRequest)

        verifyNoInteractions(quotes)
    }
}