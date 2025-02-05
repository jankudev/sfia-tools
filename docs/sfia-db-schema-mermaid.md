classDiagram
direction BT

class Attribute {
   varchar(4) code PK
   varchar(64) name
   varchar(10) type FK
   varchar(1024) description
   text guidance_notes
}

class AttributeLevel {
   text description
   varchar(4) code PK FK
   int level PK FK
}

class AttributeType {
   varchar(10) name PK
   text description
}

class Level {
   int level PK
   varchar(32) name
   text description
}

class RelatedSkill {
   varchar(4) code_main PK FK
   varchar(4) code_related PK FK
}

class Skill {
   varchar(4) code PK
   varchar(64) name
   varchar(1024) description
   text guidance_notes
}

class SkillLevel {
   varchar(4) code PK FK
   int level PK FK
   text description
}

class SkillsProfile {
   varchar(4) code PK
   varchar(64) name
   varchar(1024) description
   varchar(4) skillsProfileFamilyCode FK
}

class SkillsProfileFamily {
   varchar(4) code PK
   varchar(64) name
}

class SkillsProfileJobTitle {
   varchar(4) skillsProfileCode PK FK
   varchar(64) name PK
}

class SkillsProfileSkill {
   varchar(4) skillsProfileCode PK FK
   varchar(4) skillCode PK FK
   boolean primary PK
}

Attribute --> AttributeType : "type -> name"
AttributeLevel --> Attribute : "code"
RelatedSkill --> Skill : "code_main -> code"
RelatedSkill --> Skill : "code_related -> code"
SkillLevel --> Skill : "code"
SkillsProfile --> SkillsProfileFamily : "skillsProfileFamilyCode -> code"
SkillsProfileJobTitle --> SkillsProfile : "skillsProfileCode -> code"
SkillsProfileSkill --> Skill : "skillCode -> code"
SkillsProfileSkill --> SkillsProfile : "skillsProfileCode -> code"