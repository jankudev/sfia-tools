package dev.janku.sfia.model

data class Attribute(
  val code: String,
  val name: String,
  val description: String,
  val type: AttributeType,
  val guidanceNotes: String,
  val levels: Map<Int, String>
)