package com.example.group04flowerapispringboot.controller

import com.example.group04flowerapispringboot.model.Quote
import com.example.group04flowerapispringboot.repository.QuoteRepository
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

data class QuoteRequest(
    val quoteText: String,
    val author: String? = null
)

@RestController
@RequestMapping("/api/v1/quotes")
class QuoteController(private val quotes: QuoteRepository) {
    @GetMapping
    fun listQuotes(): List<Quote> = quotes.findAll()

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createQuote(@RequestBody request: QuoteRequest): Quote =
        quotes.create(request.quoteText, request.author)
}
