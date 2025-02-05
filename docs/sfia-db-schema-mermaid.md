classDiagram
direction BT
class Attribute {
   varchar(64) name
   varchar(10) type
   varchar(1024) description
   text guidance_notes
   varchar(4) code
}
class AttributeLevel {
   text description
   varchar(4) code
   int level
}
class AttributeType {
   text description
   varchar(10) name
}
class Level {
   varchar(32) name
   text description
   int level
}
class RelatedSkill {
   varchar(4) code_main
   varchar(4) code_related
}
class Skill {
   varchar(64) name
   varchar(1024) description
   text guidance_notes
   varchar(4) code
}
class SkillLevel {
   text description
   varchar(4) code
   int level
}
class SkillsProfile {
   varchar(4) skillsProfileFamilyCode
   varchar(64) name
   varchar(1024) description
   varchar(4) code
}
class SkillsProfileFamily {
   varchar(64) name
   varchar(4) code
}
class SkillsProfileJobTitle {
   varchar(4) skillsProfileCode
   varchar(64) name
}
class SkillsProfileSkill {
   varchar(4) skillsProfileCode
   varchar(4) skillCode
   boolean primary
}
class sqlite_master {
   text type
   text name
   text tbl_name
   int rootpage
   text sql
}

Attribute  -->  AttributeType : type:name
AttributeLevel  -->  Skill : code
RelatedSkill  -->  Skill : code_main:code
RelatedSkill  -->  Skill : code_related:code
SkillLevel  -->  Skill : code
SkillsProfile  -->  SkillsProfileFamily : skillsProfileFamilyCode:code
SkillsProfileJobTitle  -->  SkillsProfile : skillsProfileCode:code
SkillsProfileSkill  -->  Skill : skillCode:code
SkillsProfileSkill  -->  SkillsProfile : skillsProfileCode:code
