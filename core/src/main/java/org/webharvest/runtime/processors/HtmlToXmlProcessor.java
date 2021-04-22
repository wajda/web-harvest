/*  Copyright (c) 2006-2007, Vladimir Nikic
    All rights reserved.

    Redistribution and use of this software in source and binary forms,
    with or without modification, are permitted provided that the following
    conditions are met:

    * Redistributions of source code must retain the above
      copyright notice, this list of conditions and the
      following disclaimer.

    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the
      following disclaimer in the documentation and/or other
      materials provided with the distribution.

    * The name of Web-Harvest may not be used to endorse or promote
      products derived from this software without specific prior
      written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
    POSSIBILITY OF SUCH DAMAGE.

    You can contact Vladimir Nikic by sending e-mail to
    nikic_vladimir@yahoo.com. Please include the word "Web-Harvest" in the
    subject line.
*/
package org.webharvest.runtime.processors;

import org.apache.commons.lang.BooleanUtils;
import org.htmlcleaner.*;
import org.webharvest.definition.HtmlToXmlDef;
import org.webharvest.exception.ParserException;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.templaters.BaseTemplater;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.CommonUtil;

import java.io.IOException;

/**
 * HTML to XML processor.
 */
public class HtmlToXmlProcessor extends BaseProcessor<HtmlToXmlDef> {

    public HtmlToXmlProcessor(HtmlToXmlDef htmlToXmlDef) {
        super(htmlToXmlDef);
    }

    public Variable execute(Scraper scraper, ScraperContext context) throws InterruptedException {
        Variable body = getBodyTextContent(elementDef, scraper, context);

        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();

        final String advancedXmlEscape = BaseTemplater.evaluateToString(elementDef.getAdvancedXmlEscape(), null, scraper);
        if (advancedXmlEscape != null) {
            properties.setAdvancedXmlEscape(CommonUtil.isBooleanTrue(advancedXmlEscape));
        }

        final String cdataForScriptAndStyle = BaseTemplater.evaluateToString(elementDef.getUseCdataForScriptAndStyle(), null, scraper);
        if (cdataForScriptAndStyle != null) {
            properties.setUseCdataForScriptAndStyle(CommonUtil.isBooleanTrue(cdataForScriptAndStyle));
        }

        final String specialEntities = BaseTemplater.evaluateToString(elementDef.getTranslateSpecialEntities(), null, scraper);
        if (specialEntities != null) {
            properties.setTranslateSpecialEntities(CommonUtil.isBooleanTrue(specialEntities));
        }

        final String recognizeUnicodeChars = BaseTemplater.evaluateToString(elementDef.getRecognizeUnicodeChars(), null, scraper);
        if (recognizeUnicodeChars != null) {
            properties.setRecognizeUnicodeChars(CommonUtil.isBooleanTrue(recognizeUnicodeChars));
        }

        final String replaceNbspWithSp = BaseTemplater.evaluateToString(elementDef.getReplaceNbspWithSp(), null, scraper);

        final String omitUnknownTags = BaseTemplater.evaluateToString(elementDef.getOmitUnknownTags(), null, scraper);
        if (omitUnknownTags != null) {
            properties.setOmitUnknownTags(CommonUtil.isBooleanTrue(omitUnknownTags));
        }

        final String useEmptyElementTags = BaseTemplater.evaluateToString(elementDef.getUseEmptyElementTags(), null, scraper);
        if (useEmptyElementTags != null) {
            properties.setUseEmptyElementTags(CommonUtil.isBooleanTrue(useEmptyElementTags));
        }

        final String treatUnknownTagsAsContent = BaseTemplater.evaluateToString(elementDef.getTreatUnknownTagsAsContent(), null, scraper);
        if (treatUnknownTagsAsContent != null) {
            properties.setTreatUnknownTagsAsContent(CommonUtil.isBooleanTrue(treatUnknownTagsAsContent));
        }

        final String omitDeprecatedTags = BaseTemplater.evaluateToString(elementDef.getOmitDeprecatedTags(), null, scraper);
        if (omitDeprecatedTags != null) {
            properties.setOmitDeprecatedTags(CommonUtil.isBooleanTrue(omitDeprecatedTags));
        }

        final String treatDeprTagsAsContent = BaseTemplater.evaluateToString(elementDef.getTreatDeprecatedTagsAsContent(), null, scraper);
        if (treatDeprTagsAsContent != null) {
            properties.setTreatDeprecatedTagsAsContent(CommonUtil.isBooleanTrue(treatDeprTagsAsContent));
        }

        final String omitXmlDecl = BaseTemplater.evaluateToString(elementDef.getOmitXmlDecl(), null, scraper);
        if (omitXmlDecl != null) {
            properties.setOmitXmlDeclaration(CommonUtil.isBooleanTrue(omitXmlDecl));
        }

        final String omitComments = BaseTemplater.evaluateToString(elementDef.getOmitComments(), null, scraper);
        if (omitComments != null) {
            properties.setOmitComments(CommonUtil.isBooleanTrue(omitComments));
        }

        final String omitHtmlEnvelope = BaseTemplater.evaluateToString(elementDef.getOmitHtmlEnvelope(), null, scraper);
        if (omitHtmlEnvelope != null) {
            properties.setOmitHtmlEnvelope(CommonUtil.isBooleanTrue(omitHtmlEnvelope));
        }

        final String allowMultiWordAttributes = BaseTemplater.evaluateToString(elementDef.getAllowMultiWordAttributes(), null, scraper);
        if (allowMultiWordAttributes != null) {
            properties.setAllowMultiWordAttributes(CommonUtil.isBooleanTrue(allowMultiWordAttributes));
        }

        final String allowHtmlInsideAttributes = BaseTemplater.evaluateToString(elementDef.getAllowHtmlInsideAttributes(), null, scraper);
        if (allowHtmlInsideAttributes != null) {
            properties.setAllowHtmlInsideAttributes(CommonUtil.isBooleanTrue(allowHtmlInsideAttributes));
        }

        final String namespacesAware = BaseTemplater.evaluateToString(elementDef.getNamespacesAware(), null, scraper);
        if (namespacesAware != null) {
            properties.setNamespacesAware(CommonUtil.isBooleanTrue(namespacesAware));
        } else {
            properties.setNamespacesAware(false);
        }

        final String hyphenReplacement = BaseTemplater.evaluateToString(elementDef.getHyphenReplacement(), null, scraper);
        if (hyphenReplacement != null) {
            properties.setHyphenReplacementInComment(hyphenReplacement);
        }

        final String pruneTags = BaseTemplater.evaluateToString(elementDef.getPrunetags(), null, scraper);
        if (pruneTags != null) {
            properties.setPruneTags(pruneTags);
        }

        final String booleanAtts = BaseTemplater.evaluateToString(elementDef.getBooleanAtts(), null, scraper);
        if (booleanAtts != null) {
            properties.setBooleanAttributeValues(booleanAtts);
        }

        String outputType = BaseTemplater.evaluateToString(elementDef.getOutputType(), null, scraper);

        try {
            XmlSerializer xmlSerializer;
            if ("simple".equalsIgnoreCase(outputType)) {
                xmlSerializer = new SimpleXmlSerializer(properties);
            } else if ("pretty".equalsIgnoreCase(outputType)) {
                xmlSerializer = new PrettyXmlSerializer(properties);
            } else if ("browser-compact".equalsIgnoreCase(outputType)) {
                xmlSerializer = new BrowserCompactXmlSerializer(properties);
            } else {
                xmlSerializer = new CompactXmlSerializer(properties);
            }

            final String xmlAsString = xmlSerializer.getAsString(cleaner.clean(body.toString()));

            return new NodeVariable(
                    BooleanUtils.toBoolean(replaceNbspWithSp)
                            ? xmlAsString.replace('\u00A0', ' ')
                            : xmlAsString);

        } catch (IOException e) {
            throw new ParserException(e);
        }
    }

}