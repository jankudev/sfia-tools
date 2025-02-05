package dev.janku.sfia

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SqlitePersistenceTest {
  /* Generation of the primary key identifier (code) from name */
  @Test
  fun `string to code - empty should return 0000`() {
    assertEquals("0000", SqlitePersistence.stringToCode(""))
  }

  @Test
  fun `string to code - single word not long enough, should return prepended with 0s`() {
    assertEquals("000L", SqlitePersistence.stringToCode("L"))

  }

  @Test
  fun `string to code - special characters should be ignored`() {
    assertEquals("00LD", SqlitePersistence.stringToCode("L&D"))
    assertEquals("00LD", SqlitePersistence.stringToCode("L & D"))
  }

  @Test
  fun `string to code - non-relevant words should be ignored`() {
    assertEquals("00LD", SqlitePersistence.stringToCode("L and D role family"))
  }

  @Test
  fun `string to code - 4 relevant word string should create code of all first letters`() {
    assertEquals("SCAP", SqlitePersistence.stringToCode("scrum coach agile practitioner"))
  }

  @Test
  fun `string to code - 2 relevant words should create code from 3 first letters of first word and the 1st letter of second word`() {
    assertEquals("LEAD", SqlitePersistence.stringToCode("Learning and Development"))
  }

  @Test
  fun `string to code - different length of code`() {
    assertEquals("LEARDP", SqlitePersistence.stringToCode("Learning and Development Practitioner", 6))
  }

  @Test
  fun `string to code (regression) - problematic strings`() {
    assertEquals("LEAD", SqlitePersistence.stringToCode("Learning & development"))
  }
}