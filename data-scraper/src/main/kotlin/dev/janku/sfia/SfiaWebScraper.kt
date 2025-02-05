package dev.janku.sfia

import dev.janku.sfia.model.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode

private object SfiaWebUrls {
  /** Root URL of the SFIA framework web - version 9 */
  const val ROOT = "https://sfia-online.org/en/"
  const val ROOT_SFIA_9 = "${ROOT}/sfia-9"
}

/** Scrape the SFIA skills basic listing for further processing from the web */
private object ScrapeSkillListing {
  /* urls */
  const val SKILL_LISTING = "${SfiaWebUrls.ROOT_SFIA_9}/all-skills-a-z"

  /* content selector templates */
  const val LISTING_SELECTOR = "#content-core > div.table-responsive > table > tbody > tr"
  const val LISTING_FIELD_SELECTOR = "td:nth-child(%d)"

  private fun elementToSkillListing(element: Element) : SkillListing {
    val name = element.select(LISTING_FIELD_SELECTOR.format(1)).text()
    val url = element.select(LISTING_FIELD_SELECTOR.format(1) + " > a").attr("href")

    val code = element.select(LISTING_FIELD_SELECTOR.format(2)).text()
    val description = element.select(LISTING_FIELD_SELECTOR.format(3)).text()
    val tags = element.select(LISTING_FIELD_SELECTOR.format(4)).text().split(", ")

    return SkillListing(name, code, description, tags, url)
  }

  fun scrape() : List<SkillListing> {
    val page = Jsoup.connect(SKILL_LISTING).get()
    return page.select(LISTING_SELECTOR).map(::elementToSkillListing)
  }
}

private object ScrapeSkillDetails {
  /* content selector templates */
  const val GUIDANCE_SELECTOR = "div.guidance-body > div"
  const val RELATED_SKILLS_SELECTOR = "#related-sfia-skills > div > span"
  const val LEVEL = "div.skill_level:not(.collapsed)"
  const val LEVEL_HEADER = "h3 > span"
  const val LEVEL_DESC = "div.level_text > div"

  private fun scrapeGuidance(page: Document): String {
    return JSoupElementToMarkdownConverter.convert(page.select(GUIDANCE_SELECTOR).first())
  }

  private fun scrapeRelatedSkills(page: Document): List<String> {
    return page.select(RELATED_SKILLS_SELECTOR).map { it.text() }
  }

  private fun scrapeLevels(page: Document): Map<Int, String> {
    return page.select(LEVEL).map { level ->
      level.select(LEVEL_HEADER).text().replace("Level ", "").toInt() to
      JSoupElementToMarkdownConverter.convert(level.select(LEVEL_DESC).first())
    }.toMap()
  }

  fun scrape(skillListing: SkillListing): Skill {
    println("  scraping details - ${skillListing.code} -> ${skillListing.name}")

    val page = Jsoup.connect(skillListing.url).get()
    return Skill(
      name = skillListing.name,
      code = skillListing.code,
      description = skillListing.description,
      guidanceNotes = scrapeGuidance(page),
      relatedSkills = scrapeRelatedSkills(page),
      levels = scrapeLevels(page)
    )
  }
}

/** Scrape the listing of all generic and behavioral attributes */
private object ScrapeAttributeListing {
  /* urls */
  const val ATTRIBUTES_LISTING = "${SfiaWebUrls.ROOT_SFIA_9}/responsibilities/generic-attributes-business-skills-behaviours"

  /* content templates */
  const val ATTRIBUTE_LISTING_SELECTOR = "article.tileItem"
  const val ATTRIBUTE_NAME_SELECTOR = "h2"
  const val ATTRIBUTE_URL_SELECTOR = "h2 > a"
  const val ATTRIBUTE_DESC_SELECTOR = "div.tileBody > span"

  private fun elementToAttributeListing(element: Element): AttributeListing {
    return AttributeListing(
      name = element.select(ATTRIBUTE_NAME_SELECTOR).text(),
      description = element.select(ATTRIBUTE_DESC_SELECTOR).text(),
      url = element.select(ATTRIBUTE_URL_SELECTOR).attr("href")
    )
  }

  fun scrape(): List<AttributeListing> {
    val page = Jsoup.connect(ATTRIBUTES_LISTING).get()
    return page.select(ATTRIBUTE_LISTING_SELECTOR).map(::elementToAttributeListing)
  }
}

/** Scraping the attribute full detail */
private object ScrapeAttributeDetails {
  /* content selectors */
  const val ATTRIBUTE_HEADER = "article#content > header > h1"
  const val GUIDANCE_NOTES_SELECTOR = "div.guidance-notes > div.guidance-body"
  const val ATTRIBUTE_LEVELS = "article#content > div#content-core > h3, article#content > div#content-core > p, article#content > div#content-core > ul"

  fun scrapeGuidance(page: Document): String {
    return JSoupElementToMarkdownConverter.convert(page.select(GUIDANCE_NOTES_SELECTOR).first())
  }

  fun scrapeLevels(page: Document): Map<Int, String> {
    return page.select(ATTRIBUTE_LEVELS).chunked(2).map { levelPair ->
      levelPair[0].text().replace(Regex("Level "), "").substring(0, 1).toInt() to
      JSoupElementToMarkdownConverter.convert(levelPair[1])
    }.toMap()
  }

  fun scrape(attributeListing: AttributeListing): Attribute {
    println("  scraping details - ${attributeListing.name}")

    val page = Jsoup.connect(attributeListing.url).get()
    val code = page.select(ATTRIBUTE_HEADER).text().replace(attributeListing.name, "").trim()
    val guidanceNotes = scrapeGuidance(page)
    val levels = scrapeLevels(page)

    return Attribute(
      code = code,
      name = attributeListing.name,
      description = attributeListing.description,
      type = AttributeType.fromCode(code),
      guidanceNotes =  guidanceNotes,
      levels = levels
    )
  }
}

/** The generic levels enumeration from SFIA */
private object ScrapeLevels {
  /* urls */
  const val LEVEL_LISTING = "${SfiaWebUrls.ROOT_SFIA_9}/responsibilities"

  /* content selectors */
  const val LEVEL_TILE = "article.tileItem"
  const val LEVEL_HEADER = "h2.tileHeadline"
  const val LEVEL_DESC = "div.tileBody > span.description"

  private fun scrapeLevelDetails(element: Element): Level {
    val headerRegex = Regex("Level (\\d)\\s*-\\s*(.+)")
    val header = headerRegex.matchEntire(element.select(LEVEL_HEADER).text().trim())!!
    val description = element.select(LEVEL_DESC).text()

    return Level(
      level = header.groups.get(1)!!.value.toInt(),
      name = header.groups.get(2)!!.value,
      description = description
    )
  }

  fun scrape(): List<Level> {
    val page = Jsoup.connect(LEVEL_LISTING).get()
    return page.select(LEVEL_TILE)
      .take(7)  // limit to the first 7 tiles as there are only 7 levels (and the listing uses same tiles for other content)
      .map(::scrapeLevelDetails)
  }
}

/** Scrate the SFIA illustrative skill profiles */
private object ScrapeSkillsProfiles {
  /* urls */
  const val PROFILE_LISTING = "${SfiaWebUrls.ROOT}/tools-and-resources/standard-industry-skills-profiles/sfia-9-skills-for-role-families-job-titles"

  /* content selectors */
  const val PROFILE_FAMILY_TABLES = "div#content-core > section#section-text > div#parent-fieldname-text > table"
  const val PROFILE_FAMILY_HEADER = "tbody > tr:nth-child(1) > td.role-family"
  const val PROFILE_TRIPLETS = "tbody > tr:not(:first-child)"
  const val PROFILE_HEADER_TITLE = "td.role-title > h3"
  const val PROFILE_HEADER_DESC = "td.role-purpose > p"
  const val PROFILE_JOB_TITLES = "td:nth-child(1) > ul > li"
  const val PROFILE_PRIMARY_SKILLS = "td:nth-child(2) > ul > li > a"
  const val PROFILE_SECONDARY_SKILLS = "td:nth-child(3) > ul > li > a"

  private fun extractCodeFromFullSkillName(fullName: String): String {
    return fullName.trim().takeLast(4)
  }

  private fun scrapeSkillsProfile(familyName: String, header: Element, content: Element): SkillsProfile {
    val name = header.select(PROFILE_HEADER_TITLE).text()
    val description = header.select(PROFILE_HEADER_DESC).text()

    val jobTitles = content.select(PROFILE_JOB_TITLES).map { it.text() }
    val primarySkills = content.select(PROFILE_PRIMARY_SKILLS).map { it.text() }.map(::extractCodeFromFullSkillName)
    val secondarySkills = content.select(PROFILE_SECONDARY_SKILLS).map { it.text() }.map(::extractCodeFromFullSkillName)

    return SkillsProfile(
      name = name,
      description = description,
      family = familyName,
      jobTitles = jobTitles,
      primarySkills = primarySkills,
      secondarySkills = secondarySkills
    )
  }

  private fun scrapeSkillsProfileFamily(element: Element): List<SkillsProfile> {
    val familyName = element.select(PROFILE_FAMILY_HEADER).text()
      .replace(Regex("\\s*role family\\s*"), "").trim()
    return element.select(PROFILE_TRIPLETS).chunked(3).map {
      (header, _, content) -> scrapeSkillsProfile(familyName, header, content)
    }
  }

  fun scrape(): List<SkillsProfile> {
    val page = Jsoup.connect(PROFILE_LISTING).get()
    return page.select(PROFILE_FAMILY_TABLES).flatMap(::scrapeSkillsProfileFamily)
  }
}

/** Simple conversion of HTML from SFIA web content to Markdown (for larger structured texts) */
private object JSoupElementToMarkdownConverter {
  fun convert(element: Element?): String = buildString {
    // guard
    if (element == null) return ""

    element.childNodes().forEach { node ->
      when (node) {
        is TextNode -> append(node.text())
        is Element -> when (node.tagName()) {
          "p" -> {
            append(convert(node))
            append("\n\n")
          }
          "ul", "ol" -> {
            append("\n")
            node.children().forEachIndexed { index, li ->
              val prefix = if (node.tagName() == "ul") "* " else "${index + 1}. "
              append("$prefix${convert(li)}\n")
            }
            append("\n")
          }
          "strong", "b" -> append("**${convert(node)}**")
          "em", "i" -> append("*${convert(node)}*")
          "br" -> append("\n")
          else -> append(convert(node))
        }
      }
    }
  }.replace(Regex("\n+"), "\n").trim()
}

/** Main object for the SFIA web scraping process */
object SfiaWebScraper {
  fun scrapeSkills(): List<Skill> {
    val skillListings = ScrapeSkillListing.scrape()
    return skillListings.map { skillListing -> ScrapeSkillDetails.scrape(skillListing) }
  }

  fun scrapeAttributes(): List<Attribute> {
    val attributeListings = ScrapeAttributeListing.scrape()
    return attributeListings.map { attributeListing -> ScrapeAttributeDetails.scrape(attributeListing) }
  }

  fun scrapeLevels(): List<Level> {
    return ScrapeLevels.scrape()
  }

  fun scrapeSkillsProfiles(): List<SkillsProfile> {
    return ScrapeSkillsProfiles.scrape()
  }
}

fun main(args: Array<String> = emptyArray()) {

  println("Scraping SFIA skills from the web...")
  val skills = SfiaWebScraper.scrapeSkills()
  println("Scraped ${skills.size} skills")

  println()

  println("Scraping SFIA generic attributes from the web...")
  val attrs = SfiaWebScraper.scrapeAttributes()
  println("Scraped ${attrs.size} attributes")

  println()

  println("Scraping SFIA levels from the web...")
  val levels = SfiaWebScraper.scrapeLevels()
  println("Scraped ${levels.size} levels")

  println()

  println("Scraping SFIA illustrative skills profiles from the web...")
  val skillsProfiles = SfiaWebScraper.scrapeSkillsProfiles()
  val skillsProfilesFamilies = skillsProfiles.map { it.family }.distinct()
  println("Scraped ${skillsProfiles.size} skill profiles")

  println()

  println("Persisting scraped data to the database...")
  DatabaseConfig.init()
  skills.forEach(SqlitePersistence::persistSkill)
  skills.forEach(SqlitePersistence::persistRelatedSkills)
  SqlitePersistence.persistAttributeTypes()
  attrs.forEach(SqlitePersistence::persistAttribute)
  levels.forEach(SqlitePersistence::persistLevel)
  SqlitePersistence.persistSkillsProfileFamily(skillsProfilesFamilies)
  skillsProfiles.forEach(SqlitePersistence::persistSkillsProfile)

}