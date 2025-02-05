package dev.janku.sfia.apiservice.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.persistence.*
import java.io.Serializable

@Embeddable
data class SkillLevelId(
  val code: CodeId,
  val level: Int
) : Serializable

@Entity
@Table(name = "SkillLevel")
data class SkillLevel(
  @EmbeddedId
  val id: SkillLevelId,

  @Column(nullable = false, insertable = false, updatable = false)
  val level: Int,

  @Column(nullable = false)
  val description: String
)

@Entity
@Table(name = "Skill")
@JsonIgnoreProperties("relatedSkills") // WARN: avoid infinite recursion in serialization !!!
data class Skill(
  @Id
  val code: CodeId,

  @Column(nullable = false)
  val name: String,

  @Column(nullable = false)
  val description: String,

  @Column(name = "guidance_notes", nullable = false)
  val guidanceNotes: MarkdownString,

  @ElementCollection
  @CollectionTable(
    name = "SkillLevel",
    joinColumns = [JoinColumn(name = "code")]
  )
  @MapKeyColumn(name = "level")
  @Column(name = "description")
  val levels: Map<LevelId, String> = emptyMap(),

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
    name = "RelatedSkill",
    joinColumns = [JoinColumn(name = "code_main")],
    inverseJoinColumns = [JoinColumn(name = "code_related")]
  )
  val relatedSkills: Set<Skill> = HashSet()
) {
  /** Override equals to compare by 'code' as PK */
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Skill) return false
    return code == other.code
  }

  /** Override hashCode to compare by 'code' as PK */
  override fun hashCode(): Int = code.hashCode()
}