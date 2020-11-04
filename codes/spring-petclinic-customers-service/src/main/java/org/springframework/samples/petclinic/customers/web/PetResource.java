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
package org.springframework.samples.petclinic.customers.web;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.OwnerRepository;
import org.springframework.samples.petclinic.customers.model.Pet;
import org.springframework.samples.petclinic.customers.model.PetRepository;
import org.springframework.samples.petclinic.customers.model.PetType;
import org.springframework.samples.petclinic.visits.model.Visit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Maciej Szarlinski
 */
@RestController
class PetResource {

    private final RestTemplate restTemplate;

    private static final Logger log = LoggerFactory.getLogger(PetResource.class);

    private final PetRepository petRepository;

    private final OwnerRepository ownerRepository;

    public PetResource(
                       final RestTemplate restTemplate,
                       final PetRepository petRepository,
                       final OwnerRepository ownerRepository)
    {
        super();
        this.restTemplate = restTemplate;
        this.petRepository = petRepository;
        this.ownerRepository = ownerRepository;
    }

    @GetMapping("/petTypes")
    public List<PetType> getPetTypes() {
        return this.petRepository.findPetTypes();
    }

    @PostMapping("/owners/{ownerId}/pets")
    @ResponseStatus(HttpStatus.CREATED)
    public Pet processCreationForm(
        @RequestBody final PetRequest petRequest,
        @PathVariable("ownerId") final int ownerId) {

        final Pet pet = new Pet();
        final Optional<Owner> optionalOwner = this.ownerRepository.findById(ownerId);
        final Owner owner = optionalOwner.orElseThrow(() -> new ResourceNotFoundException("Owner "+ownerId+" not found"));
        owner.addPet(pet);

        return this.save(pet, petRequest);
    }

    @PutMapping("/owners/*/pets/{petId}")
    public Pet processUpdateForm(@RequestBody final PetRequest petRequest)
    {
        final int petId = petRequest.getId();
        final Pet pet = this.findPetById(petId);
        return this.save(pet, petRequest);
    }

    private Pet save(final Pet pet, final PetRequest petRequest)
    {

        pet.setName(petRequest.getName());
        pet.setBirthDate(petRequest.getBirthDate());

        this.petRepository.findPetTypeById(petRequest.getTypeId())
            .ifPresent(pet::setType);

        PetResource.log.info("Saving pet {}", pet);
        return this.petRepository.save(pet);
    }

    @GetMapping("owners/*/pets/{petId}")
    public PetDetails findPet(@PathVariable("petId") final int petId) {

        final List<Visit> visits = this.restTemplate.getForObject("http://visits-service/owners/*/pets/" + petId + "/visits", List.class);
        return new PetDetails(this.findPetById(petId), visits);
    }


    private Pet findPetById(final int petId) {
        final Optional<Pet> pet = this.petRepository.findById(petId);
        if (!pet.isPresent()) {
            throw new ResourceNotFoundException("Pet "+petId+" not found");
        }
        return pet.get();
    }

}
