package com.wah.ipr1.server.service

import com.wah.ipr1.server.model.Website
import org.springframework.stereotype.Service
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.util.*
import javax.xml.XMLConstants
import javax.xml.namespace.QName
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.SAXParserFactory
import javax.xml.stream.XMLInputFactory
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory
import kotlin.collections.ArrayList

/**
 * This class represents parse service
 */
@Service
class ParseService {

    private val xsdValidationSchema = """
        <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
            <xs:element name="websites">
                <xs:complexType>
                    <xs:sequence>
                        <xs:element name="website" maxOccurs="unbounded">
                            <xs:complexType>
                                <xs:sequence>
                                    <xs:element name="name" type="xs:string"/>
                                    <xs:element name="category" type="xs:string"/>
                                    <xs:element name="status" type="xs:string"/>
                                </xs:sequence>
                                <xs:attribute name="url" type="xs:anyURI" use="required"/>
                            </xs:complexType>
                        </xs:element>
                    </xs:sequence>
                </xs:complexType>
            </xs:element>
        </xs:schema>""".trimIndent()

    /**
     * Parses [data] xml using [type] parser and returns [List] of [Website]. Returns empty list if [data] is invalid
     * @param data xml to be parsed
     * @param type type of parser. "stax" for stax, "dom" for dom, "sax" for sax. defaults to sax
     * @return [List] of [Website] or empty list if [data] is invalid
     */
    fun parse(data: String, type: String): List<Website> {
        val factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
        val validator = factory.newSchema(StreamSource(xsdValidationSchema.byteInputStream())).newValidator()
        try {
            validator.validate(StreamSource(data.byteInputStream()))
        } catch (e: Exception) {
            return ArrayList()
        }
        return when (type.lowercase(Locale.getDefault())) {
            "stax" -> {
                parseStAX(data)
            }
            "dom" -> {
                parseDOM(data)
            }
            else -> {
                parseSAX(data)
            }
        }
    }

    private fun parseSAX(data: String): List<Website> {
        val saxParserFactory = SAXParserFactory.newInstance()
        val saxParser = saxParserFactory.newSAXParser()
        val parserHandler = SAXParserHandler()

        saxParser.parse(data.byteInputStream(), parserHandler)

        return parserHandler.getWebsites()
    }

    private fun parseStAX(data: String): List<Website> {
        val xmlInputFactory = XMLInputFactory.newInstance()
        val reader = xmlInputFactory.createXMLEventReader(data.byteInputStream())
        val websites = ArrayList<Website>()
        var website: Website? = null

        while (reader.hasNext()) {
            val event = reader.nextEvent()
            if (event.isStartElement) {
                val startElement = event.asStartElement()
                when (startElement.name.localPart) {
                    "website" -> {
                        website = Website().apply {
                            val url = startElement.getAttributeByName(QName("url"))
                            url?.let {
                                this.url = url.value
                            }
                        }
                    }
                    "name" -> {
                        website?.let {
                            it.name = reader.nextEvent().asCharacters().data
                        }
                    }
                    "category" -> {
                        website?.let {
                            it.category = reader.nextEvent().asCharacters().data
                        }
                    }
                    "status" -> {
                        website?.let {
                            it.status = reader.nextEvent().asCharacters().data
                        }
                    }
                }
            }
            if (event.isEndElement) {
                if (event.asEndElement().name.localPart == "website") {
                    website?.let {
                        websites.add(it)
                    }
                }
            }
        }
        return websites
    }

    private fun parseDOM(data: String): List<Website> {
        val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val document = builder.parse(data.byteInputStream())
        document.documentElement.normalize()
        val websites = ArrayList<Website>()
        val nodeList = document.getElementsByTagName("website")

        for (i in 0 until nodeList.length) {
            val website = Website()
            val attributes = nodeList.item(i).attributes
            website.url = attributes.item(0).nodeValue
            val childNodes = nodeList.item(i).childNodes
            for (j in 0 until childNodes.length) {
                when (childNodes.item(j).nodeName) {
                    "name" -> {
                        website.name = childNodes.item(j).textContent
                    }
                    "category" -> {
                        website.category = childNodes.item(j).textContent
                    }
                    "status" -> {
                        website.status = childNodes.item(j).textContent
                    }
                }
            }
            websites.add(website)
        }
        return websites
    }

    private class SAXParserHandler: DefaultHandler() {

        private val websites = ArrayList<Website>()
        private var website: Website? = null
        private var dataBuilder = StringBuilder()

        fun getWebsites(): ArrayList<Website> {
            return websites
        }

        override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
            when (qName) {
                "website" -> {
                    website = Website().apply {
                        attributes?.let {
                            this.url = it.getValue("url")
                        }
                    }
                }
                "name", "category", "status" -> {
                    dataBuilder = StringBuilder()
                }
            }
        }

        override fun endElement(uri: String?, localName: String?, qName: String?) {
            when (qName) {
                "website" -> {
                    website?.let {
                        websites.add(it)
                    }
                }
                "name" -> {
                    website?.let {
                        it.name = dataBuilder.toString()
                    }
                }
                "category" -> {
                    website?.let {
                        it.category = dataBuilder.toString()
                    }
                }
                "status" -> {
                    website?.let {
                        it.status = dataBuilder.toString()
                    }
                }
            }
        }

        override fun characters(ch: CharArray?, start: Int, length: Int) {
            ch?.let {
                dataBuilder.appendRange(it, start, start + length)
            }
        }

    }

}