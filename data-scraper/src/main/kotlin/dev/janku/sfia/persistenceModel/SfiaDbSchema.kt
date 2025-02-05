package dev.janku.sfia.persistenceModel

import org.jetbrains.exposed.sql.Table

object Schema {

  object Skill : Table() {
    val code = varchar("code", 4)
    val name = varchar("name", 64)
    val description = varchar("description", 1024)
    val guidanceNotes = text("guidance_notes")

    override val primaryKey = PrimaryKey(code)
  }

  object RelatedSkill : Table() {
    val codeMain = reference("code_main", Skill.code)
    val codeRelated = reference("code_related", Skill.code)

    override val primaryKey = PrimaryKey(codeMain, codeRelated)
  }

  object SkillLevel : Table() {
    val code = reference("code", Skill.code)
    val level = integer("level")
    val description = text("description")

    override val primaryKey = PrimaryKey(code, level)
  }

  object AttributeType : Table() {
    val name = varchar("name", 10)
    val description = text("description")

    override val primaryKey = PrimaryKey(name)
  }

  object Attribute : Table() {
    val code = varchar("code", 4)
    val name = varchar("name", 64)
    val type = reference("type", AttributeType.name)
    val description = varchar("description", 1024)
    val guidanceNotes = text("guidance_notes")

    override val primaryKey = PrimaryKey(code)
  }

  object AttributeLevel : Table() {
    val code = reference("code", Skill.code)
    val level = integer("level")
    val description = text("description")

    override val primaryKey = PrimaryKey(code, level)
  }

  object Level : Table() {
    val level = integer("level")
    val name = varchar("name", 32)
    val description = text("description")

    override val primaryKey = PrimaryKey(level)
  }

  object SkillsProfileFamily : Table() {
    val code = varchar("code", 4)
    val name = varchar("name", 64)

    override val primaryKey = PrimaryKey(code)
  }

  object SkillsProfile : Table() {
    val code = varchar("code", 4)
    val skillsProfileFamilyCode = reference("skillsProfileFamilyCode", SkillsProfileFamily.code)
    val name = varchar("name", 64)
    val description = varchar("description", 1024)

    override val primaryKey = PrimaryKey(code)
  }

  object SkillsProfileJobTitle: Table() {
    val skillsProfileCode = reference("skillsProfileCode", SkillsProfile.code)
    val name = varchar("name", 64)

    override val primaryKey = PrimaryKey(skillsProfileCode, name)
  }

  object SkillsProfileSkill: Table() {
    val skillsProfileCode = reference("skillsProfileCode", SkillsProfile.code)
    val skillCode = reference("skillCode", Skill.code)
    val main = bool("main")

    override val primaryKey = PrimaryKey(skillsProfileCode, skillCode, main)
  }
}