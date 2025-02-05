package dev.janku.sfia.model

data class Skill(
  val name: String,
  val code: String,
  val description: String,
  val guidanceNotes: String,
  val relatedSkills: List<String>,
  val levels: Map<Int, String>
)
