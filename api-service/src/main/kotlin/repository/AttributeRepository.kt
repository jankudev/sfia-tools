package dev.janku.sfia.apiservice.repository

import dev.janku.sfia.apiservice.model.Attribute
import dev.janku.sfia.apiservice.model.CodeId
import dev.janku.sfia.apiservice.model.Skill
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource(collectionResourceRel = "attributes", path = "attributes")
interface AttributeRepository : CrudRepository<Attribute, CodeId>