package dev.janku.sfia.apiservice.model

import jakarta.persistence.*
import java.io.Serializable

@Entity
@Table(name = "AttributeType")
data class AttributeType(
  @Id
  val name: String,

  @Column(nullable = false)
  val description: String
)

@Embeddable
data class AttributeLevelId(
  val code : CodeId,
  val level: Int
) : Serializable

@Entity
@Table(name = "AttributeLevel")
data class AttributeLevel(
  @EmbeddedId
  val id: AttributeLevelId,

  @Column(nullable = false, insertable = false, updatable = false)
  val level: Int,

  @Column(nullable = false)
  val description: String
)

@Entity
@Table(name = "Attribute")
data class Attribute(
  @Id
  val code: CodeId,

  @Column(nullable = false)
  val name: String,

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "type", nullable = false)
  val type: AttributeType,

  @Column(nullable = false)
  val description: String,

  @Column(name="guidance_notes", nullable = false)
  val guidanceNotes: MarkdownString,

  @ElementCollection
  @CollectionTable(
    name = "AttributeLevel",
    joinColumns = [JoinColumn(name = "code")]
  )
  @MapKeyColumn(name = "level")
  @Column(name = "description")
  val levels: Map<LevelId, String> = mutableMapOf()
)
