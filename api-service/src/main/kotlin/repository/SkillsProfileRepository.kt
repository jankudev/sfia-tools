package dev.janku.sfia.apiservice.repository

import dev.janku.sfia.apiservice.model.CodeId
import dev.janku.sfia.apiservice.model.Skill
import dev.janku.sfia.apiservice.model.SkillsProfile
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource(collectionResourceRel = "skillsProfiles", path = "skillsProfiles")
interface SkillsProfileRepository : CrudRepository<SkillsProfile, CodeId>