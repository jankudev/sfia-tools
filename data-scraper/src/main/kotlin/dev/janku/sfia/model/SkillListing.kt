package dev.janku.sfia.model

data class SkillListing(
  val name: String,
  val code: String,
  val description: String,
  val tags: List<String>,
  val url: String
)