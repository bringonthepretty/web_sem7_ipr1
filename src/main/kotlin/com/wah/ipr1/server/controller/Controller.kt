package com.wah.ipr1.server.controller

import com.wah.ipr1.server.model.Website
import com.wah.ipr1.server.service.ParseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.hateoas.CollectionModel
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/parse")
class Controller(@Autowired private val parseService: ParseService) {

    @PostMapping
    fun parse(@RequestBody data: String, @RequestParam(name = "type", defaultValue = "sax") type: String):
            ResponseEntity<CollectionModel<Website>> {
        return ResponseEntity.ok(CollectionModel.of(parseService.parse(data, type)))
    }
}