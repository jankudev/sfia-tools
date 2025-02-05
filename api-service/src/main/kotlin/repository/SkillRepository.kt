package dev.janku.sfia.apiservice.repository

import dev.janku.sfia.apiservice.model.CodeId
import dev.janku.sfia.apiservice.model.Skill
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource(collectionResourceRel = "skills", path = "skills")
interface SkillRepository : CrudRepository<Skill, CodeId>