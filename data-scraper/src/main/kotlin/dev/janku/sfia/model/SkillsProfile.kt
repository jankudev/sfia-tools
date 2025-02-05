package dev.janku.sfia.model

data class SkillsProfile(
  val name: String,
  val description: String,
  val family: String,
  val jobTitles: List<String>,
  val primarySkills: List<String>,
  val secondarySkills: List<String>
)