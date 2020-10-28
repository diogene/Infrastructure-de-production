/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.visits.web;

import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.visits.model.Visit;
import org.springframework.samples.petclinic.visits.model.VisitRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Maciej Szarlinski
 */
@RestController
class VisitResource {

    private final VisitRepository visitRepository;

    private static final Logger log = LoggerFactory.getLogger(VisitResource.class);

    public VisitResource(final VisitRepository visitRepository)
    {
        super();
        this.visitRepository = visitRepository;
    }

    @PostMapping("owners/*/pets/{petId}/visits")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void create(
        @Valid @RequestBody final Visit visit,
        @PathVariable("petId") final int petId) {

        visit.setPetId(petId);
        VisitResource.log.info("Saving visit {}", visit);
        this.visitRepository.save(visit);
    }

    @GetMapping("owners/*/pets/{petId}/visits")
    List<Visit> visits(@PathVariable("petId") final int petId) {
        return this.visitRepository.findByPetId(petId);
    }

    @GetMapping("pets/visits")
    Visits visitsMultiGet(@RequestParam("petId") final List<Integer> petIds) {
        final List<Visit> byPetIdIn = this.visitRepository.findByPetIdIn(petIds);
        return new Visits(byPetIdIn);
    }

    static class Visits {
        private final List<Visit> items;

        public Visits(final List<Visit> items)
        {
            super();
            this.items = items;
        }

    }
}
