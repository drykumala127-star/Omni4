package com.omnifile.feature.toolkit.editor

import java.io.File
import java.io.StringReader
import java.io.StringWriter
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

/**
 * Model and utilities for XML editing and formatting.
 */
object XmlEditorModel {

    /**
     * Pretty prints unformatted XML content with 4-space indentation.
     */
    fun formatXml(xmlContent: String): String {
        return try {
            val transformer = TransformerFactory.newInstance().newTransformer().apply {
                setOutputProperty(OutputKeys.INDENT, "yes")
                setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")
                setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no")
            }
            val result = StreamResult(StringWriter())
            transformer.transform(StreamSource(StringReader(xmlContent)), result)
            result.writer.toString().trim()
        } catch (_: Exception) {
            xmlContent
        }
    }

    /**
     * Performs a fast tag validation check to ensure opening and closing tags match.
     */
    fun validateXmlSyntax(xmlContent: String): Pair<Boolean, String?> {
        return try {
            val factory = javax.xml.parsers.DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            builder.parse(org.xml.sax.InputSource(StringReader(xmlContent)))
            Pair(true, null)
        } catch (e: Exception) {
            Pair(false, e.message ?: "Format XML tidak valid")
        }
    }
}
