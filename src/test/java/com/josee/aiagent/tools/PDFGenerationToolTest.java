package com.josee.aiagent.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PDFGenerationToolTest {

    @Test
    public void testGeneratePDF() {
        PDFGenerationTool tool = new PDFGenerationTool();
        String fileName = "工程师顾问AI应用.pdf";
        String content = "工程师顾问AI应用开发中......";
        String result = tool.generatePDF(fileName, content);
        assertNotNull(result);
    }
}
