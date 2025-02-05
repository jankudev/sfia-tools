package dev.janku.sfia

import dev.janku.sfia.model.*
import dev.janku.sfia.persistenceModel.Schema
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.sql.Connection

object DatabaseConfig {
  fun init() {

    val buildDir = System.getProperty("app.build.dir") ?: "build"
    val dbDir = "${buildDir}/db"
    val dbFile = File(dbDir, "sfia-sqlite.db")

    if (!dbFile.parentFile.exists() && !dbFile.parentFile.mkdirs()) {
      throw IllegalStateException("Failed to create database directory ${dbFile.absolutePath}")
    }

    Database.connect("jdbc:sqlite:${dbFile.absolutePath}", "org.sqlite.JDBC")
    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE

    val schemaTables = listOf(
      Schema.Skill, Schema.RelatedSkill, Schema.SkillLevel, Schema.AttributeType,
      Schema.Attribute, Schema.AttributeLevel, Schema.Level, Schema.SkillsProfileFamily, Schema.SkillsProfile,
      Schema.SkillsProfileJobTitle, Schema.SkillsProfileSkill
    )

    println("  initializing database schema")
    transaction {
      // delete all and recreate
      schemaTables.reversed().forEach(SchemaUtils::drop)
      schemaTables.forEach(SchemaUtils::create)
    }
  }
}

object SqlitePersistence {
  fun persistSkill(skill: Skill) {
    println("  persisting skill ${skill.code} -> ${skill.name}")

    transaction {
      Schema.Skill.insert {
        it[code] = skill.code
        it[name] = skill.name
        it[description] = skill.description
        it[guidanceNotes] = skill.guidanceNotes
      }

      skill.levels.forEach { skillLevel ->
        Schema.SkillLevel.insert {
          it[code] = skill.code
          it[level] = skillLevel.key
          it[description] = skillLevel.value
        }
      }
    }
  }

  fun persistRelatedSkills(skill: Skill) {
    println("  persisting related skills to ${skill.code} -> ${skill.name}")

    transaction {
      skill.relatedSkills.forEach { relatedSkill ->
        Schema.RelatedSkill.insert {
          it[codeMain] = skill.code
          it[codeRelated] = Schema.Skill
            .select(Schema.Skill.code)
            .where { Schema.Skill.name eq relatedSkill }
            .first()[Schema.Skill.code]
        }
      }
    }
  }

  fun persistAttributeTypes() {
    println("  persisting attribute types enumeration")

    transaction {
      AttributeType.entries.forEach { type ->
        Schema.AttributeType.insert {
          it[name] = type.name
          it[description] = type.description
        }
      }
    }
  }

  fun persistAttribute(attribute: Attribute) {
    println("  persisting attribute ${attribute.code} -> ${attribute.name}")

    transaction {
      Schema.Attribute.insert {
        it[code] = attribute.code
        it[name] = attribute.name
        it[type] = attribute.type.name
        it[description] = attribute.description
        it[guidanceNotes] = attribute.guidanceNotes
      }

      attribute.levels.forEach { attributeLevel ->
        Schema.AttributeLevel.insert {
          it[code] = attribute.code
          it[level] = attributeLevel.key
          it[description] = attributeLevel.value
        }
      }
    }
  }

  fun persistLevel(lvl: Level) {
    println("  persisting level ${lvl.level} - ${lvl.name}")

    transaction {
      Schema.Level.insert {
        it[level] = lvl.level
        it[name] = lvl.name
        it[description] = lvl.description
      }
    }
  }

  fun persistSkillsProfileFamily(families: List<String>) {
    println("  persisting skills profile families")

    transaction {
      families.forEach { family ->
        Schema.SkillsProfileFamily.insert {
          it[code] = stringToCode(family)
          it[name] = family
        }
      }
    }
  }

  fun persistSkillsProfile(skillsProfile: SkillsProfile) {
    println("  persisting skills profile ${skillsProfile.name}")

    val skillsProfileGeneratedCode = stringToCode(skillsProfile.name)

    transaction {
      Schema.SkillsProfile.insert {
        it[code] = skillsProfileGeneratedCode
        it[name] = skillsProfile.name
        it[description] = skillsProfile.description
        it[skillsProfileFamilyCode] = Schema.SkillsProfileFamily
          .select(Schema.SkillsProfileFamily.code)
          .where { Schema.SkillsProfileFamily.name eq skillsProfile.family }
          .first()[Schema.SkillsProfileFamily.code]
      }

      skillsProfile.jobTitles.forEach { jobTitle ->
        Schema.SkillsProfileJobTitle.insert {
          it[skillsProfileCode] = skillsProfileGeneratedCode
          it[name] = jobTitle
        }
      }

      skillsProfile.primarySkills.forEach { code ->
        Schema.SkillsProfileSkill.insert {
          it[skillsProfileCode] = skillsProfileGeneratedCode
          it[skillCode] = code
          it[main] = true
        }
      }

      skillsProfile.secondarySkills.forEach { code ->
        Schema.SkillsProfileSkill.insert {
          it[skillsProfileCode] = skillsProfileGeneratedCode
          it[skillCode] = code
          it[main] = false
        }
      }
    }
  }

  /**
   * Turn a string into a code (4 characters) following simple rules
   * - remove all non-alphanumeric characters
   * - remove all irrelevant words (and, or, the, role, family)
   * - take the first character of each letter, if the code is not 4 characters long then take more characters of the first word
   * - convert to upper-case
   * @param str the string to convert
   * @return the code
   */
  fun stringToCode(str: String, maxLength: Int = 4): String {
    val words = str.replace(REGEX_NON_ALPHANUMERIC, "")
      .split(" ")
      .filter(String::isNotBlank)
      .filter { it !in SET_OF_NON_CODE_WORDS }
      .take(maxLength)
    return words.mapIndexed { index, word ->
      when (index) {
        0 -> word.take(maxLength - words.size + 1)
        else -> word.first().toString()
      }
    }.joinToString("").padStart(maxLength, DEFAULT_CODE_PADDING_CHAR).uppercase()
  }

  val REGEX_NON_ALPHANUMERIC = Regex("[^A-Za-z0-9 ]")
  val SET_OF_NON_CODE_WORDS = setOf("and", "or", "the", "role", "family")
  val DEFAULT_CODE_PADDING_CHAR = '0'
}