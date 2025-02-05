package dev.janku.sfia.apiservice.model

import jakarta.persistence.*
import org.hibernate.annotations.SQLJoinTableRestriction

@Entity
@Table(name = "SkillsProfileFamily")
data class SkillsProfileFamily(
  @Id
  val code: CodeId,

  @Column(nullable = false)
  val name: String
)

@Entity
@Table(name = "SkillsProfile")
data class SkillsProfile(
  @Id
  val code: CodeId,

  @Column(nullable = false)
  val name: String,

  @Column(nullable = false)
  val description: String,

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "skillsProfileFamilyCode", nullable = false)
  val family: SkillsProfileFamily,

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
    name = "SkillsProfileSkill",
    joinColumns = [JoinColumn(name = "skillsProfileCode")],
    inverseJoinColumns = [JoinColumn(name = "skillCode")]
  )
  @SQLJoinTableRestriction("main = 1")
  val primarySkills: Set<Skill> = HashSet(),

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
    name = "SkillsProfileSkill",
    joinColumns = [JoinColumn(name = "skillsProfileCode")],
    inverseJoinColumns = [JoinColumn(name = "skillCode")]
  )
  @SQLJoinTableRestriction("main = 0")
  val secondarySkills: Set<Skill> = HashSet()
)
