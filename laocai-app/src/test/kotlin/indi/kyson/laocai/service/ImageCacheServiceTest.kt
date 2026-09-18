package indi.kyson.laocai.service

import indi.kyson.laocai.bot.entity.GroupEntity
import indi.kyson.laocai.bot.entity.GroupMemberEntity
import indi.kyson.laocai.bot.enums.*
import indi.kyson.laocai.bot.event.GroupMessageEvent
import indi.kyson.laocai.bot.segment.IncomingImageSegment
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import java.nio.file.Files
import java.nio.file.Path

class ImageCacheServiceTest {
    @TempDir lateinit var directory: Path

    @Test
    fun availableBytesAreFrozenBeforeRenderingEvenIfCacheIsLaterRemoved() {
        val cache = ImageCacheService(directory)
        Files.write(directory.resolve("A"), byteArrayOf(1, 2))
        Files.write(directory.resolve("B"), byteArrayOf(3, 4))
        val available = cache.readAvailable(listOf("A", "missing", "B", "A"))
        Files.delete(directory.resolve("A"))
        assertEquals(setOf("A", "B"), available.keys)
        assertArrayEquals(byteArrayOf(1, 2), available["A"])
        assertArrayEquals(byteArrayOf(3, 4), available["B"])
        assertEquals(setOf("B"), cache.available(listOf("A", "B")))
    }

    @Test
    fun unresolvableUrisInvalidPathsAndDirectoriesAreSimplyUnavailable() {
        val cache = ImageCacheService(directory)
        Files.createDirectory(directory.resolve("folder"))
        val references = listOf("https://example.test/a", "file:///picture.png", "..", "../a", "a\\b", "a\u0000b", "folder", "")
        assertEquals(emptyMap<String, ByteArray>(), cache.readAvailable(references))
    }

    @Test
    fun ordinaryGroupImageCanBeCachedWithoutMention() {
        val source = directory.resolve("source.png")
        Files.write(source, byteArrayOf(1, 2, 3))
        val cache = ImageCacheService(directory.resolve("cache"))
        val image = IncomingImageSegment.Data("resource", source.toUri().toString(), 1, 1, "", ImageSubType.NORMAL).toSegment()
        val event = GroupMessageEvent(0, 9, 1, 1, 2, listOf(image), GroupEntity(1, "test", 2, 10),
            GroupMemberEntity(2, "", Sex.UNKNOWN, 1, "", "", 0, Role.MEMBER, 0, 0, null))
        assertEquals(listOf("resource"), cache.cache(event))
        assertArrayEquals(byteArrayOf(1, 2, 3), cache.readAvailable(listOf("resource"))["resource"])
    }

    @Test
    fun gifMessageIsRecognizedAndCanBeIgnored() {
        val source = directory.resolve("source.gif")
        Files.write(source, encodeImage("gif"))
        val cache = ImageCacheService(directory.resolve("cache"))
        val image = IncomingImageSegment.Data("resource", source.toUri().toString(), 2, 3, "", ImageSubType.NORMAL).toSegment()
        val event = GroupMessageEvent(0, 9, 1, 1, 2, listOf(image), GroupEntity(1, "test", 2, 10),
            GroupMemberEntity(2, "", Sex.UNKNOWN, 1, "", "", 0, Role.MEMBER, 0, 0, null))

        assertEquals(listOf("resource"), cache.cache(event))
        assertTrue(cache.containsGif(event))
    }

    @Test
    fun nonGifMessageIsNotIgnored() {
        val source = directory.resolve("source.png")
        Files.write(source, encodeImage("png"))
        val cache = ImageCacheService(directory.resolve("cache"))
        val image = IncomingImageSegment.Data("resource", source.toUri().toString(), 2, 3, "", ImageSubType.NORMAL).toSegment()
        val event = GroupMessageEvent(0, 9, 1, 1, 2, listOf(image), GroupEntity(1, "test", 2, 10),
            GroupMemberEntity(2, "", Sex.UNKNOWN, 1, "", "", 0, Role.MEMBER, 0, 0, null))

        assertEquals(listOf("resource"), cache.cache(event))
        assertFalse(cache.containsGif(event))
    }

    private fun encodeImage(format: String): ByteArray {
        val image = BufferedImage(2, 3, BufferedImage.TYPE_INT_RGB).apply {
            setRGB(0, 0, 0xff336699.toInt())
        }
        return ByteArrayOutputStream().use { output ->
            assertTrue(ImageIO.write(image, format, output))
            output.toByteArray()
        }
    }
}
