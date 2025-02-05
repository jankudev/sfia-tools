package dev.janku.sfia.model

/**
 * Scraped manually from the SFIA website as there is no clear structure.
 */
enum class AttributeType(
  val description: String,
  val attributeCodes: Set<String>
) {
  GENERIC(
    "Attributes indicate the level or responsibility.",
    setOf("AUTO", "INFL", "COMP"))
  ,
  KNOWLEDGE(
    "Attributes indicate the depth and breadth of understanding required to perform and influence work effectively",
    setOf("KNGE")
  ),
  BEHAVIORAL(
    "Business skills and behavioral factors describing the behaviors required to be effective at each level.",
    setOf("COLL", "COMM", "IMPM", "CRTY", "DECM", "DIGI", "LEAD", "LADV", "PLAN", "PROB", "ADAP", "SCPE")
  );

  companion object {
    fun fromCode(code: String): AttributeType {
      val normalizedCode = code.uppercase()
      return entries.find { it.attributeCodes.contains(normalizedCode) }
        ?: throw IllegalArgumentException("Unknown attribute code: $normalizedCode")
    }
  }
}